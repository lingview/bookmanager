<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="pageTitle" value="搜索"/>
<c:set var="navActive" value="books"/>
<%@ include file="../common/head.jspf" %>

<form class="search-bar" action="${ctx}/books/search" method="get" id="searchForm">
    <a class="search-back" href="${ctx}/books" aria-label="返回书架">←</a>
    <input class="input search-input" type="text" name="q" id="searchInput"
           placeholder="搜索书名或作者" maxlength="50" value="<c:out value='${q}'/>" autofocus>
    <button type="submit" class="btn">搜索</button>
    <button type="button" class="btn ghost" id="searchFilterToggle">筛选</button>
</form>

<c:set var="hasAdvanced" value="${not empty category_id or not empty tag or not empty shelf}"/>
<form class="shelf-filters search-open ${hasAdvanced ? 'advanced-open' : ''}" id="searchFilters"
      action="${ctx}/books/search" method="get" style="margin-top: 8px;">
    <input type="hidden" name="q" value="<c:out value='${q}'/>">
    <div class="filter-row advanced-row">
        <div class="filter-group filter-group-full">
            <span class="filter-label">状态</span>
            <label class="seg"><input type="radio" name="shelf" value=""
                   ${empty shelf ? 'checked' : ''}>全部</label>
            <label class="seg"><input type="radio" name="shelf" value="want"
                   ${shelf == 'want' ? 'checked' : ''}>想读</label>
            <label class="seg"><input type="radio" name="shelf" value="reading"
                   ${shelf == 'reading' ? 'checked' : ''}>在读</label>
            <label class="seg"><input type="radio" name="shelf" value="finished"
                   ${shelf == 'finished' ? 'checked' : ''}>读过</label>
        </div>
        <select class="select" name="category_id">
            <option value="">全部分类</option>
            <c:forEach var="cat" items="${categories}">
                <option value="${cat.id}" ${cat.id == category_id ? 'selected' : ''}>
                    <c:out value="${cat.name}"/>
                </option>
            </c:forEach>
        </select>
        <select class="select" name="tag">
            <option value="">全部标签</option>
            <c:forEach var="tagItem" items="${tags}">
                <option value="<c:out value='${tagItem.name}'/>" ${tagItem.name == tag ? 'selected' : ''}>
                    <c:out value="${tagItem.name}"/>
                </option>
            </c:forEach>
        </select>
        <select class="select" name="sort" onchange="this.form.submit()">
            <option value="updated" ${sort == 'updated' ? 'selected' : ''}>最近添加</option>
            <option value="recent_read" ${sort == 'recent_read' ? 'selected' : ''}>最近在读</option>
            <option value="title" ${sort == 'title' ? 'selected' : ''}>书名</option>
            <option value="chapters" ${sort == 'chapters' ? 'selected' : ''}>章节数</option>
            <option value="words" ${sort == 'words' ? 'selected' : ''}>字数</option>
        </select>
        <button type="submit" class="btn ghost">应用筛选</button>
        <c:url var="fulltextUrl" value="/books/fulltext">
            <c:param name="q" value="${q}"/>
        </c:url>
        <a class="btn ghost" href="${fulltextUrl}">搜正文</a>
    </div>
</form>

<c:choose>
    <c:when test="${not filtered}">
        <p class="empty">输入关键词，或使用筛选查找图书</p>
    </c:when>
    <c:when test="${empty pageResult.data}">
        <p class="empty">没有匹配的图书</p>
    </c:when>
    <c:otherwise>
        <p class="muted" style="margin: 4px 0 10px;">找到 ${pageResult.total} 本</p>
        <div class="search-results">
            <c:forEach var="b" items="${pageResult.data}">
                <a class="search-item" href="${ctx}/books/${b.id}">
                    <span class="search-cover">
                        <c:choose>
                            <c:when test="${not empty b.cover_url}">
                                <img src="<c:out value='${b.cover_url}'/>" alt="">
                            </c:when>
                            <c:otherwise>无封面</c:otherwise>
                        </c:choose>
                    </span>
                    <span class="search-info">
                        <span class="search-title"><c:out value="${b.title}"/></span>
                        <span class="search-meta">
                            <c:out value="${b.author}"/>
                            <c:if test="${not empty b.author and not empty b.category_name}"> · </c:if>
                            <c:out value="${b.category_name}"/>
                            · ${b.chapter_count} 章
                            <c:if test="${not empty b.my_rating}"> · ★${b.my_rating}</c:if>
                        </span>
                        <c:if test="${not empty b.progress_chapter_id}">
                            <span class="search-progress">
                                读到 <c:out value="${b.progress_chapter_title}"/>
                            </span>
                        </c:if>
                    </span>
                    <c:choose>
                        <c:when test="${not empty b.progress_chapter_id}">
                            <span class="chip" style="align-self: center;">继续阅读</span>
                        </c:when>
                        <c:when test="${b.chapter_count > 0}">
                            <span class="chip" style="align-self: center;">开始阅读</span>
                        </c:when>
                    </c:choose>
                </a>
            </c:forEach>
        </div>
    </c:otherwise>
</c:choose>

<c:if test="${pageResult.total_pages > 1}">
    <div class="pagination">
        <c:forEach begin="1" end="${pageResult.total_pages}" var="p">
            <c:choose>
                <c:when test="${p == pageResult.page}">
                    <span class="page-current">${p}</span>
                </c:when>
                <c:otherwise>
                    <c:url var="pageUrl" value="/books/search">
                        <c:param name="page" value="${p}"/>
                        <c:param name="q" value="${q}"/>
                        <c:param name="category_id" value="${category_id}"/>
                        <c:param name="tag" value="${tag}"/>
                        <c:param name="sort" value="${sort}"/>
                        <c:param name="shelf" value="${shelf}"/>
                    </c:url>
                    <a class="page-link" href="${pageUrl}">${p}</a>
                </c:otherwise>
            </c:choose>
        </c:forEach>
    </div>
</c:if>

<script>
    (function () {
        var filters = document.getElementById('searchFilters');
        var toggle = document.getElementById('searchFilterToggle');
        var input = document.getElementById('searchInput');
        if (filters && toggle) {
            toggle.addEventListener('click', function () {
                filters.classList.toggle('advanced-open');
            });
        }
        if (input && !input.value) {
            setTimeout(function () {
                input.focus();
            }, 120);
        }
    })();
</script>

<%@ include file="../common/foot.jspf" %>

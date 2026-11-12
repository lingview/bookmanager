<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="pageTitle" value="图书"/>
<c:set var="navActive" value="books"/>
<c:set var="hasFilter"
       value="${not empty keyword or not empty category_id or not empty tag or not empty shelf}"/>
<%@ include file="../common/head.jspf" %>

<div class="shelf-head">
    <h2 class="zone-title">我的书架</h2>
    <div class="shelf-tools">
        <button type="button" class="icon-btn" id="searchToggle" title="搜索" aria-label="搜索">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor"
                 stroke-width="2" stroke-linecap="round">
                <circle cx="11" cy="11" r="7"></circle>
                <path d="M20 20l-3.5-3.5"></path>
            </svg>
        </button>
        <button type="button" class="icon-btn" id="filterToggle" title="筛选" aria-label="筛选">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor"
                 stroke-width="2" stroke-linecap="round">
                <path d="M4 6h16M7 12h10M10 18h4"></path>
            </svg>
            <c:if test="${hasFilter}"><span class="badge-dot"></span></c:if>
        </button>
        <c:if test="${canManage}">
            <a class="btn small" href="${ctx}/books/new">新增图书</a>
        </c:if>
    </div>
</div>

<c:if test="${hasFilter}">
    <div class="active-filters">
        <span>筛选：</span>
        <c:if test="${shelf == 'want'}"><span class="filter-chip">想读</span></c:if>
        <c:if test="${shelf == 'reading'}"><span class="filter-chip">在读</span></c:if>
        <c:if test="${shelf == 'finished'}"><span class="filter-chip">读过</span></c:if>
        <c:forEach var="cat" items="${categories}">
            <c:if test="${cat.id == category_id}">
                <span class="filter-chip"><c:out value="${cat.name}"/></span>
            </c:if>
        </c:forEach>
        <c:if test="${not empty tag}">
            <span class="filter-chip"><c:out value="${tag}"/></span>
        </c:if>
        <c:choose>
            <c:when test="${sort == 'recent_read'}"><span class="filter-chip">最近在读</span></c:when>
            <c:when test="${sort == 'title'}"><span class="filter-chip">按书名</span></c:when>
            <c:when test="${sort == 'chapters'}"><span class="filter-chip">按章节数</span></c:when>
            <c:when test="${sort == 'words'}"><span class="filter-chip">按字数</span></c:when>
        </c:choose>
        <a class="active-clear" href="${ctx}/books">清除</a>
    </div>
</c:if>

<form class="shelf-filters" id="shelfFilters" action="${ctx}/books" method="get">
    <div class="filter-row search-row">
        <input class="input" type="text" name="keyword" placeholder="搜索书名或作者"
               value="<c:out value='${keyword}'/>">
        <button type="submit" class="btn">搜索</button>
        <a class="btn ghost" href="${ctx}/books/fulltext">搜正文</a>
    </div>

    <div class="filter-popover" id="filterPopover">
        <div class="filter-group">
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
        <div class="filter-grid">
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
            <select class="select" name="sort">
                <option value="updated" ${sort == 'updated' ? 'selected' : ''}>最近添加</option>
                <option value="recent_read" ${sort == 'recent_read' ? 'selected' : ''}>最近在读</option>
                <option value="title" ${sort == 'title' ? 'selected' : ''}>书名</option>
                <option value="chapters" ${sort == 'chapters' ? 'selected' : ''}>章节数</option>
                <option value="words" ${sort == 'words' ? 'selected' : ''}>字数</option>
            </select>
        </div>
        <div class="filter-actions">
            <a class="btn ghost" href="${ctx}/books">重置</a>
            <button type="submit" class="btn">应用</button>
        </div>
    </div>
    <div class="filter-mask" id="filterMask"></div>
</form>

<c:choose>
    <c:when test="${empty pageResult.data}">
        <p class="empty">
            <c:choose>
                <c:when test="${hasFilter}">没有匹配的图书</c:when>
                <c:otherwise>还没有图书</c:otherwise>
            </c:choose>
        </p>
    </c:when>
    <c:otherwise>
        <div class="shelf">
            <c:forEach var="b" items="${pageResult.data}">
                <div class="shelf-slot">
                    <a class="book-cover" href="${ctx}/books/${b.id}">
                        <span class="thumb ${empty b.cover_url ? 'thumb-empty' : ''}">
                            <c:choose>
                                <c:when test="${not empty b.cover_url}">
                                    <img src="<c:out value='${b.cover_url}'/>" alt="">
                                </c:when>
                                <c:otherwise>无封面</c:otherwise>
                            </c:choose>
                        </span>
                    </a>
                    <span class="shelf-plank"></span>
                    <div class="book-label">
                        <a class="book-title" href="${ctx}/books/${b.id}"
                           title="<c:out value='${b.title}'/>"><c:out value="${b.title}"/></a>
                        <div class="book-meta">
                            <c:out value="${b.author}"/>
                            <c:if test="${not empty b.author and not empty b.category_name}"> · </c:if>
                            <c:out value="${b.category_name}"/>
                            · ${b.chapter_count} 章
                            <c:if test="${not empty b.my_rating}"> · ★${b.my_rating}</c:if>
                        </div>
                        <div class="book-actions">
                            <c:choose>
                                <c:when test="${not empty b.progress_chapter_id}">
                                    <a class="chip" href="${ctx}/books/${b.id}/read?chapterId=${b.progress_chapter_id}"
                                       title="继续阅读：<c:out value='${b.progress_chapter_title}'/>">继续阅读</a>
                                </c:when>
                                <c:when test="${b.chapter_count > 0}">
                                    <a class="chip" href="${ctx}/books/${b.id}/read">开始阅读</a>
                                </c:when>
                            </c:choose>
                        </div>
                    </div>
                </div>
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
                    <c:url var="pageUrl" value="/books">
                        <c:param name="page" value="${p}"/>
                        <c:param name="keyword" value="${keyword}"/>
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
        var form = document.getElementById('shelfFilters');
        var searchToggle = document.getElementById('searchToggle');
        var filterToggle = document.getElementById('filterToggle');
        var mask = document.getElementById('filterMask');
        if (!form || !searchToggle || !filterToggle) return;

        function closeAll() {
            form.classList.remove('search-open', 'filter-open');
        }

        searchToggle.addEventListener('click', function () {
            if (window.matchMedia('(max-width: 768px)').matches) {
                window.location.href = '${ctx}/books/search';
                return;
            }
            form.classList.toggle('search-open');
            if (form.classList.contains('search-open')) {
                form.classList.remove('filter-open');
                var input = form.querySelector('input[name="keyword"]');
                if (input) input.focus();
            }
        });

        filterToggle.addEventListener('click', function () {
            form.classList.toggle('filter-open');
            if (form.classList.contains('filter-open')) {
                form.classList.remove('search-open');
            }
        });

        if (mask) mask.addEventListener('click', closeAll);

        document.addEventListener('click', function (e) {
            if (!form.classList.contains('search-open') && !form.classList.contains('filter-open')) return;
            if (form.contains(e.target) || searchToggle.contains(e.target) || filterToggle.contains(e.target)) return;
            closeAll();
        });
    })();
</script>

<%@ include file="../common/foot.jspf" %>

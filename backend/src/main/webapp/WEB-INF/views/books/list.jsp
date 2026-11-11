<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="pageTitle" value="图书"/>
<c:set var="navActive" value="books"/>
<%@ include file="../common/head.jspf" %>

<form class="toolbar" action="${ctx}/books" method="get">
    <input class="input" type="text" name="keyword" placeholder="搜索书名或作者"
           value="<c:out value='${keyword}'/>">
    <select class="select" name="category_id">
        <option value="">全部分类</option>
        <c:forEach var="cat" items="${categories}">
            <option value="${cat.id}" ${cat.id == category_id ? 'selected' : ''}>
                <c:out value="${cat.name}"/>
            </option>
        </c:forEach>
    </select>
    <button type="submit" class="btn">搜索</button>
    <c:if test="${not empty keyword or not empty category_id}">
        <a class="btn ghost" href="${ctx}/books">重置</a>
    </c:if>
    <span class="grow"></span>
    <c:if test="${canManage}">
        <a class="btn" href="${ctx}/books/new">新增图书</a>
    </c:if>
</form>

<c:choose>
    <c:when test="${empty pageResult.data}">
        <p class="empty">
            <c:choose>
                <c:when test="${not empty keyword or not empty category_id}">没有匹配的图书</c:when>
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
                    </c:url>
                    <a class="page-link" href="${pageUrl}">${p}</a>
                </c:otherwise>
            </c:choose>
        </c:forEach>
    </div>
</c:if>

<%@ include file="../common/foot.jspf" %>
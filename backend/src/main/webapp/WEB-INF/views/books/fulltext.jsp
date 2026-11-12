<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="pageTitle" value="全文搜索"/>
<c:set var="navActive" value="books"/>
<%@ include file="../common/head.jspf" %>

<form class="toolbar fulltext-bar" action="${ctx}/books/fulltext" method="get">
    <input class="input" type="text" name="q" placeholder="搜索所有书籍的章节正文" maxlength="50"
           value="<c:out value='${q}'/>">
    <button type="submit" class="btn">搜索</button>
    <a class="btn ghost" href="${ctx}/books">返回书架</a>
</form>

<c:url var="searchUrl" value="/books/search">
    <c:if test="${not empty q}">
        <c:param name="q" value="${q}"/>
    </c:if>
</c:url>
<div class="search-scope">
    <a class="scope-tab" href="${searchUrl}">书名/作者</a>
    <a class="scope-tab active" href="${ctx}/books/fulltext">全站正文</a>
</div>

<c:choose>
    <c:when test="${empty q}">
        <p class="empty">输入关键词，搜索所有书籍的章节标题和正文</p>
    </c:when>
    <c:when test="${empty results}">
        <p class="empty">没有找到匹配内容</p>
    </c:when>
    <c:otherwise>
        <div class="fulltext-results">
            <c:forEach var="hit" items="${results}">
                <div class="fulltext-hit">
                    <a class="fulltext-title"
                       href="${ctx}/books/${hit.book_id}/read?chapterId=${hit.chapter_id}">
                        <c:out value="${hit.book_title}"/> · <c:out value="${hit.chapter_title}"/>
                    </a>
                    <p class="fulltext-snippet"><c:out value="${hit.snippet}"/></p>
                </div>
            </c:forEach>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="../common/foot.jspf" %>

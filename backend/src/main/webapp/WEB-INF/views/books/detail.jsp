<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="pageTitle" value="${book.title}"/>
<c:set var="navActive" value="books"/>
<%@ include file="../common/head.jspf" %>

<div class="reader">
    <div class="card">
        <div class="cover detail-cover">
            <c:choose>
                <c:when test="${not empty book.cover_url}">
                    <img src="<c:out value='${book.cover_url}'/>" alt="">
                </c:when>
                <c:otherwise>无封面</c:otherwise>
            </c:choose>
        </div>
    </div>

    <div class="stack">
        <div class="card">
            <h1 class="chapter-title"><c:out value="${book.title}"/></h1>
            <div class="muted">
                <c:out value="${book.author}"/>
                <c:if test="${not empty book.author and not empty book.category_name}"> · </c:if>
                <c:out value="${book.category_name}"/>
                · ${book.chapter_count} 章
                · 更新于 ${book.update_time}
            </div>

            <c:if test="${not empty book.description}">
                <div class="text-block"><c:out value="${book.description}"/></div>
            </c:if>

            <div class="actions-row">
                <c:choose>
                    <c:when test="${not empty book.progress_chapter_id}">
                        <a class="btn" href="${ctx}/books/${book.id}/read?chapterId=${book.progress_chapter_id}">
                            继续阅读 · <c:out value="${book.progress_chapter_title}"/>
                        </a>
                    </c:when>
                    <c:when test="${not empty toc}">
                        <a class="btn" href="${ctx}/books/${book.id}/read">开始阅读</a>
                    </c:when>
                </c:choose>

                <c:if test="${canManage}">
                    <a class="btn ghost" href="${ctx}/books/${book.id}/edit">编辑</a>
                    <a class="btn ghost" href="${ctx}/books/${book.id}/chapters">管理章节</a>
                    <form action="${ctx}/books/${book.id}/delete" method="post" class="inline"
                          onsubmit="return confirm('确定删除这本书吗？');">
                        <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                        <button type="submit" class="btn danger">删除</button>
                    </form>
                </c:if>
            </div>
        </div>

        <div class="card">
            <div class="section-title">目录</div>
            <c:choose>
                <c:when test="${empty toc}">
                    <p class="muted">还没有章节</p>
                </c:when>
                <c:otherwise>
                    <div class="toc toc-card" style="max-height: 420px;">
                        <c:forEach var="chapter" items="${toc}">
                            <a href="${ctx}/books/${book.id}/read?chapterId=${chapter.id}">
                                <c:out value="${chapter.title}"/>
                            </a>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<%@ include file="../common/foot.jspf" %>
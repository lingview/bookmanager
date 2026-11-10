<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="pageTitle" value="${empty chapterId ? '新增章节' : '编辑章节'}"/>
<c:set var="navActive" value="books"/>
<%@ include file="../common/head.jspf" %>

<div class="card">
    <div class="section-title">${empty chapterId ? '新增章节' : '编辑章节'}</div>
    <p class="muted" style="margin-bottom: 16px;">所属图书：<c:out value="${book.title}"/></p>

    <c:if test="${not empty error}">
        <p class="form-error"><c:out value="${error}"/></p>
    </c:if>

    <form action="${ctx}/books/${bookId}/chapters${empty chapterId ? '' : '/'.concat(chapterId).concat('/edit')}"
          method="post">
        <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">

        <div class="form-row">
            <label class="form-label" for="title">章节标题</label>
            <input class="input" type="text" id="title" name="title" maxlength="255" required
                   value="<c:out value='${chapterForm.title}'/>">
            <c:if test="${not empty errors.title}">
                <p class="field-error"><c:out value="${errors.title}"/></p>
            </c:if>
        </div>

        <div class="form-row">
            <label class="form-label" for="content">章节正文</label>
            <textarea class="textarea" id="content" name="content" rows="22" required
                      style="line-height: 1.8;"><c:out value="${chapterForm.content}"/></textarea>
            <c:if test="${not empty errors.content}">
                <p class="field-error"><c:out value="${errors.content}"/></p>
            </c:if>
        </div>

        <div class="form-actions">
            <button type="submit" class="btn">${empty chapterId ? '创建' : '保存'}</button>
            <a class="btn ghost" href="${ctx}/books/${bookId}/chapters">取消</a>
        </div>
    </form>
</div>

<%@ include file="../common/foot.jspf" %>
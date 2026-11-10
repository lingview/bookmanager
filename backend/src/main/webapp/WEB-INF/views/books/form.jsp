<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="pageTitle" value="${empty bookId ? '新增图书' : '编辑图书'}"/>
<c:set var="navActive" value="books"/>
<%@ include file="../common/head.jspf" %>

<div class="card">
    <div class="section-title">${empty bookId ? '新增图书' : '编辑图书'}</div>

    <c:if test="${not empty error}">
        <p class="form-error"><c:out value="${error}"/></p>
    </c:if>

    <form action="${ctx}/books${empty bookId ? '' : '/'.concat(bookId).concat('/edit')}"
          method="post" enctype="multipart/form-data">
        <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
        <input type="hidden" name="cover_url" value="<c:out value='${bookForm.cover_url}'/>">

        <div class="form-row">
            <label class="form-label" for="title">书名</label>
            <input class="input" type="text" id="title" name="title" maxlength="255" required
                   value="<c:out value='${bookForm.title}'/>">
            <c:if test="${not empty errors.title}">
                <p class="field-error"><c:out value="${errors.title}"/></p>
            </c:if>
        </div>

        <div class="form-row">
            <label class="form-label" for="author">作者</label>
            <input class="input" type="text" id="author" name="author" maxlength="255"
                   value="<c:out value='${bookForm.author}'/>">
            <c:if test="${not empty errors.author}">
                <p class="field-error"><c:out value="${errors.author}"/></p>
            </c:if>
        </div>

        <div class="form-row">
            <label class="form-label" for="category_id">分类</label>
            <select class="select" id="category_id" name="category_id">
                <option value="">未分类</option>
                <c:forEach var="cat" items="${categories}">
                    <option value="${cat.id}" ${cat.id == bookForm.category_id ? 'selected' : ''}>
                        <c:out value="${cat.name}"/>
                    </option>
                </c:forEach>
            </select>
        </div>

        <div class="form-row">
            <label class="form-label" for="description">简介</label>
            <textarea class="textarea" id="description" name="description" rows="6" maxlength="2000"><c:out value="${bookForm.description}"/></textarea>
            <c:if test="${not empty errors.description}">
                <p class="field-error"><c:out value="${errors.description}"/></p>
            </c:if>
        </div>

        <div class="form-row">
            <label class="form-label" for="cover">封面</label>
            <c:if test="${not empty coverUrl}">
                <div style="margin-bottom: 10px;">
                    <img class="cover-preview" src="<c:out value='${coverUrl}'/>" alt="当前封面">
                </div>
            </c:if>
            <input class="input" type="file" id="cover" name="cover"
                   accept=".jpg,.jpeg,.png,.gif,.webp,.bmp">
            <p class="form-hint">支持 jpg/png/gif/webp/bmp，最大 5MB；不选择则保留现有封面</p>
        </div>

        <div class="form-actions">
            <button type="submit" class="btn">${empty bookId ? '创建' : '保存'}</button>
            <a class="btn ghost" href="${ctx}/books${empty bookId ? '' : '/'.concat(bookId)}">取消</a>
        </div>
    </form>
</div>

<%@ include file="../common/foot.jspf" %>
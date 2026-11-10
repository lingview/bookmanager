<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="pageTitle" value="分类管理"/>
<c:set var="navActive" value="categories"/>
<%@ include file="../common/head.jspf" %>

<div class="card" style="margin-bottom: 20px;">
    <div class="section-title">新增分类</div>
    <form class="toolbar" style="margin: 0;" action="${ctx}/books/categories" method="post">
        <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
        <input class="input" type="text" name="name" placeholder="分类名称" maxlength="100" required
               value="<c:out value='${categoryForm.name}'/>">
        <input class="input" type="number" name="sort_order" placeholder="排序" style="width: 110px;"
               value="${categoryForm.sort_order}">
        <button type="submit" class="btn">添加</button>
    </form>
    <c:if test="${not empty errors.name}">
        <p class="field-error"><c:out value="${errors.name}"/></p>
    </c:if>
    <p class="form-hint">删除分类前请先移除该分类下的图书，否则会被拒绝</p>
</div>

<c:choose>
    <c:when test="${empty categories}">
        <p class="empty">还没有分类</p>
    </c:when>
    <c:otherwise>
        <table class="table">
            <thead>
            <tr>
                <th style="width: 80px;">ID</th>
                <th>分类名称</th>
                <th style="width: 100px;">排序</th>
                <th style="width: 120px;">操作</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="cat" items="${categories}">
                <tr>
                    <td>${cat.id}</td>
                    <td><c:out value="${cat.name}"/></td>
                    <td>${cat.sort_order}</td>
                    <td class="actions">
                        <form action="${ctx}/books/categories/${cat.id}/delete" method="post"
                              class="inline" onsubmit="return confirm('确定删除该分类吗？');">
                            <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                            <button type="submit" class="btn small danger">删除</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </c:otherwise>
</c:choose>

<%@ include file="../common/foot.jspf" %>
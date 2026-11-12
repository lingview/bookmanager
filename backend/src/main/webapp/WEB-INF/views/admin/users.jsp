<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="pageTitle" value="用户管理"/>
<c:set var="navActive" value="users"/>
<%@ include file="../common/head.jspf" %>

<p class="muted" style="margin-bottom: 16px;">为账号分配角色。拥有 ADMIN 角色可管理用户；图书管理需要 book:management 权限（ADMIN 默认拥有）。</p>

<c:choose>
    <c:when test="${empty pageResult.data}">
        <p class="empty">还没有用户</p>
    </c:when>
    <c:otherwise>
        <div class="table-scroll">
        <table class="table">
            <thead>
            <tr>
                <th style="width: 60px;">ID</th>
                <th>用户名</th>
                <th>邮箱</th>
                <th>手机</th>
                <th style="min-width: 260px;">角色</th>
                <th style="width: 160px;">注册时间</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="u" items="${pageResult.data}">
                <tr>
                    <td>${u.id}</td>
                    <td><c:out value="${u.username}"/></td>
                    <td><c:out value="${u.email}"/></td>
                    <td><c:out value="${u.phone}"/></td>
                    <td>
                        <form class="toolbar" style="margin: 0; gap: 10px;"
                              action="${ctx}/admin/users/${u.id}/roles" method="post">
                            <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                            <c:forEach var="role" items="${roles}">
                                <label class="choice">
                                    <input type="checkbox" name="roleIds" value="${role.id}"
                                           ${u.role_ids.contains(role.id) ? 'checked' : ''}>
                                    <c:out value="${role.name}"/>
                                </label>
                            </c:forEach>
                            <button type="submit" class="btn small ghost">保存</button>
                        </form>
                    </td>
                    <td class="muted">${u.create_time}</td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
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
                    <a class="page-link" href="${ctx}/admin/users?page=${p}">${p}</a>
                </c:otherwise>
            </c:choose>
        </c:forEach>
    </div>
</c:if>

<%@ include file="../common/foot.jspf" %>

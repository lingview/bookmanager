<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="pageTitle" value="用户管理"/>
<c:set var="navActive" value="users"/>
<%@ include file="../common/head.jspf" %>

<p class="muted" style="margin-bottom: 16px;">为账号分配角色。角色权限决定可访问的功能；拥有 ADMIN 角色的账号默认拥有全部权限。</p>

<c:if test="${canManageUsers}">
<div class="card" style="margin-bottom: 18px;">
    <div class="section-title">注册设置</div>
    <form class="toolbar" action="${ctx}/admin/users/register-settings" method="post" style="margin: 0;">
        <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
        <label class="choice">
            <input type="checkbox" name="enableRegister" value="1" ${enableRegister == 1 ? 'checked' : ''}>
            允许新用户注册
        </label>
        <select class="select" name="registerRoleId" style="min-width: 200px;">
            <option value="">不分配默认角色</option>
            <c:forEach var="role" items="${roles}">
                <option value="${role.id}" ${registerRoleId == role.id ? 'selected' : ''}>
                    <c:out value="${role.name}"/>
                </option>
            </c:forEach>
        </select>
        <button type="submit" class="btn small">保存注册设置</button>
    </form>
</div>
</c:if>

<c:if test="${showRolesMenu}">
    <div class="card" style="margin-bottom: 18px;">
        <div class="zone-head" style="margin-bottom: 12px;">
            <div class="section-title" style="margin: 0;">角色管理</div>
            <c:if test="${canManageRoles}">
                <a class="btn small ghost" href="${ctx}/admin/users?editRole=0">新建角色</a>
            </c:if>
        </div>
        <div class="table-scroll">
            <table class="table">
                <thead>
                <tr>
                    <th>名称</th>
                    <th>编码</th>
                    <th style="width: 90px;">权限数</th>
                    <th style="width: 90px;">用户数</th>
                    <th style="width: 80px;">状态</th>
                    <c:if test="${canManageRoles}">
                        <th style="width: 140px;">操作</th>
                    </c:if>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="r" items="${rolesWithStats}">
                    <tr>
                        <td><c:out value="${r.name}"/></td>
                        <td class="muted"><c:out value="${r.code}"/></td>
                        <td>${r.permission_count}</td>
                        <td>${r.user_count}</td>
                        <td>${r.status == 1 ? '启用' : '停用'}</td>
                        <c:if test="${canManageRoles}">
                            <td>
                                <div class="toolbar" style="margin: 0; gap: 8px;">
                                    <a class="btn small ghost" href="${ctx}/admin/users?editRole=${r.id}">编辑</a>
                                    <c:if test="${r.code != 'ADMIN' and r.user_count == 0}">
                                        <form action="${ctx}/admin/users/roles/${r.id}/delete" method="post"
                                              onsubmit="return confirm('确定删除该角色吗？');">
                                            <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                                            <button type="submit" class="btn small danger">删除</button>
                                        </form>
                                    </c:if>
                                </div>
                            </td>
                        </c:if>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>

        <c:if test="${not empty editRoleId}">
            <div class="role-editor">
                <div class="section-title">
                    ${editRoleId > 0 ? '编辑角色' : '新建角色'}
                </div>
                <form action="${ctx}/admin/users/roles/save" method="post">
                    <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                    <c:if test="${not empty editRole}">
                        <input type="hidden" name="id" value="${editRole.id}">
                    </c:if>
                    <div class="toolbar" style="margin-bottom: 10px;">
                        <input class="input" type="text" name="name" placeholder="角色名称" maxlength="100" required
                               value="<c:out value='${editRole.name}'/>" style="width: 200px;">
                        <input class="input" type="text" name="code" placeholder="角色编码（创建后不可改）" maxlength="50"
                               value="<c:out value='${editRole.code}'/>" style="width: 220px;"
                               ${editRoleId > 0 ? 'readonly' : 'required'}>
                        <select class="select" name="status" style="width: 100px;">
                            <option value="1" ${empty editRole or editRole.status != 0 ? 'selected' : ''}>启用</option>
                            <option value="0" ${not empty editRole and editRole.status == 0 ? 'selected' : ''}>停用</option>
                        </select>
                    </div>
                    <input class="input" type="text" name="description" placeholder="角色描述（可选）" maxlength="255"
                           value="<c:out value='${editRole.description}'/>" style="width: 100%; margin-bottom: 12px;">

                    <c:choose>
                        <c:when test="${not empty editRole and editRole.code == 'ADMIN'}">
                            <p class="muted">管理员角色固定拥有全部权限，权限不可修改。</p>
                        </c:when>
                        <c:otherwise>
                            <div class="role-perm-groups">
                                <c:forEach var="entry" items="${permissionGroups}">
                                    <div class="role-perm-group">
                                        <div class="role-perm-title">${moduleLabels[entry.key]}</div>
                                        <div class="role-perm-items">
                                            <c:forEach var="p" items="${entry.value}">
                                                <label class="choice">
                                                    <input type="checkbox" name="permissionIds" value="${p.id}"
                                                           ${editPermissionIds.contains(p.id) ? 'checked' : ''}>
                                                    <c:out value="${p.name}"/>
                                                </label>
                                            </c:forEach>
                                        </div>
                                    </div>
                                </c:forEach>
                            </div>
                        </c:otherwise>
                    </c:choose>

                    <div class="toolbar" style="margin: 14px 0 0; justify-content: flex-end;">
                        <a class="btn ghost" href="${ctx}/admin/users">取消</a>
                        <button type="submit" class="btn">保存角色</button>
                    </div>
                </form>
            </div>
        </c:if>
    </div>
</c:if>

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
                <c:if test="${canManageUsers}">
                    <th style="width: 80px;">操作</th>
                </c:if>
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
                        <c:choose>
                            <c:when test="${canManageUsers}">
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
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="role" items="${u.roles}">
                                    <span class="filter-chip"><c:out value="${role.name}"/></span>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td class="muted">${u.create_time}</td>
                    <c:if test="${canManageUsers}">
                        <td>
                            <c:if test="${u.username != currentUsername}">
                                <form action="${ctx}/admin/users/${u.id}/delete" method="post"
                                      onsubmit="return confirm('确定删除该用户吗？其阅读数据会一并删除。');">
                                    <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                                    <button type="submit" class="btn small danger">删除</button>
                                </form>
                            </c:if>
                        </td>
                    </c:if>
                </tr>
            </c:forEach>
            </tbody>
        </table>
        </div>
    </c:otherwise>
</c:choose>

<c:if test="${pageResult.total_pages > 1}">
    <%@ include file="../common/pagination.jspf" %>
</c:if>

<%@ include file="../common/foot.jspf" %>

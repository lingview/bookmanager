<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="pageTitle" value="用户管理"/>
<c:set var="navActive" value="users"/>
<%@ include file="../common/head.jspf" %>

<p class="muted" style="margin-bottom: 16px;">为账号分配角色。角色权限决定可访问的功能；拥有 ADMIN 角色的账号默认拥有全部权限。</p>

<div class="sub-tabs">
    <a class="sub-tab ${activeTab == 'users' ? 'active' : ''}" href="${ctx}/admin/users?tab=users">用户</a>
    <c:if test="${showRolesMenu}">
        <a class="sub-tab ${activeTab == 'roles' ? 'active' : ''}" href="${ctx}/admin/users?tab=roles">角色</a>
    </c:if>
</div>

<c:if test="${canManageUsers and activeTab == 'users'}">
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

<c:if test="${showRolesMenu and activeTab == 'roles'}">
    <div class="card" style="margin-bottom: 18px;">
        <div class="zone-head" style="margin-bottom: 12px;">
            <div class="section-title" style="margin: 0;">角色管理</div>
            <c:if test="${canManageRoles}">
                <button type="button" class="btn small ghost" id="roleNewBtn">新建角色</button>
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
                                    <button type="button" class="btn small ghost role-edit"
                                            data-id="${r.id}"
                                            data-name="<c:out value='${r.name}'/>"
                                            data-code="<c:out value='${r.code}'/>"
                                            data-desc="<c:out value='${r.description}'/>"
                                            data-status="${r.status}"
                                            data-perms="${r.permission_ids}">编辑</button>
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


        <div class="admin-modal-mask" id="roleModal" hidden>
            <div class="admin-modal">
                <div class="zone-head" style="margin-bottom: 12px;">
                    <div class="section-title" id="roleModalTitle" style="margin: 0;">新建角色</div>
                    <button type="button" class="sheet-close" data-close="roleModal">×</button>
                </div>
                <form id="roleForm" action="${ctx}/admin/users/roles/save" method="post">
                    <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                    <input type="hidden" name="id" id="roleId">
                    <div class="toolbar" style="margin: 0 0 10px;">
                        <input class="input" type="text" id="roleName" name="name" placeholder="角色名称"
                               maxlength="100" required style="width: 200px;">
                        <input class="input" type="text" id="roleCode" name="code" placeholder="角色编码（创建后不可改）"
                               maxlength="50" required style="width: 220px;">
                        <select class="select" id="roleStatus" name="status" style="width: 100px;">
                            <option value="1">启用</option>
                            <option value="0">停用</option>
                        </select>
                    </div>
                    <input class="input" type="text" id="roleDesc" name="description"
                           placeholder="角色描述（可选）" maxlength="255" style="width: 100%; margin-bottom: 12px;">
                    <p class="muted" id="roleAdminNote" hidden>管理员角色固定拥有全部权限，权限不可修改。</p>
                    <div class="role-perm-groups" id="rolePermGroups">
                        <c:forEach var="entry" items="${permissionGroups}">
                            <div class="role-perm-group">
                                <div class="role-perm-title">${moduleLabels[entry.key]}</div>
                                <div class="role-perm-items">
                                    <c:forEach var="p" items="${entry.value}">
                                        <label class="choice">
                                            <input type="checkbox" name="permissionIds" value="${p.id}"
                                                   class="role-perm-check">
                                            <c:out value="${p.name}"/>
                                        </label>
                                    </c:forEach>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                    <div class="toolbar" style="margin: 14px 0 0; justify-content: flex-end;">
                        <button type="button" class="btn ghost" data-close="roleModal">取消</button>
                        <button type="submit" class="btn">保存角色</button>
                    </div>
                </form>
            </div>
        </div>

    </div>
</c:if>

<c:if test="${activeTab == 'users'}">
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
                            <c:when test="${empty u.roles}">
                                <span class="muted">未分配</span>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="role" items="${u.roles}">
                                    <span class="filter-chip"><c:out value="${role.name}"/></span>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                        <c:if test="${canManageUsers}">
                            <button type="button" class="btn small ghost user-role-btn" style="margin-left: 8px;"
                                    data-user-id="${u.id}"
                                    data-username="<c:out value='${u.username}'/>"
                                    data-role-ids="<c:forEach var='rid' items='${u.role_ids}' varStatus='s'>${rid}${s.last ? '' : ','}</c:forEach>">分配角色</button>
                        </c:if>
                    </td>
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

<div class="admin-modal-mask" id="userRoleModal" hidden>
    <div class="admin-modal" style="width: min(420px, 100%);">
        <div class="zone-head" style="margin-bottom: 12px;">
            <div class="section-title" style="margin: 0;">分配角色 · <span id="userRoleName"></span></div>
            <button type="button" class="sheet-close" data-close="userRoleModal">×</button>
        </div>
        <form id="userRoleForm" method="post">
            <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
            <div class="user-role-list">
                <c:forEach var="role" items="${roles}">
                    <label class="choice">
                        <input type="checkbox" name="roleIds" value="${role.id}" class="user-role-check">
                        <c:out value="${role.name}"/>
                    </label>
                </c:forEach>
            </div>
            <div class="toolbar" style="margin: 16px 0 0; justify-content: flex-end;">
                <button type="button" class="btn ghost" data-close="userRoleModal">取消</button>
                <button type="submit" class="btn">保存</button>
            </div>
        </form>
    </div>
</div>
</c:if>

<script>
    (function () {
        var ctx = '${ctx}';
        var roleModal = document.getElementById('roleModal');
        var roleNewBtn = document.getElementById('roleNewBtn');

        function openModal(id) {
            var mask = document.getElementById(id);
            if (mask) mask.hidden = false;
        }

        function closeModal(id) {
            var mask = document.getElementById(id);
            if (mask) mask.hidden = true;
        }

        document.querySelectorAll('[data-close]').forEach(function (btn) {
            btn.addEventListener('click', function () {
                closeModal(btn.getAttribute('data-close'));
            });
        });
        document.querySelectorAll('.admin-modal-mask').forEach(function (mask) {
            mask.addEventListener('click', function (e) {
                if (e.target === mask) mask.hidden = true;
            });
        });
        document.addEventListener('keydown', function (e) {
            if (e.key !== 'Escape') return;
            document.querySelectorAll('.admin-modal-mask').forEach(function (mask) {
                mask.hidden = true;
            });
        });

        function openRole(data) {
            if (!roleModal) return;
            var form = document.getElementById('roleForm');
            form.reset();
            document.getElementById('roleId').value = data ? data.id : '';
            document.getElementById('roleModalTitle').textContent = data ? '编辑角色' : '新建角色';
            document.getElementById('roleName').value = data ? (data.name || '') : '';
            var code = document.getElementById('roleCode');
            code.value = data ? (data.code || '') : '';
            code.readOnly = !!data;
            code.required = !data;
            document.getElementById('roleDesc').value = data ? (data.desc || '') : '';
            document.getElementById('roleStatus').value = data ? String(data.status) : '1';
            var admin = data && data.code === 'ADMIN';
            document.getElementById('roleAdminNote').hidden = !admin;
            document.getElementById('rolePermGroups').hidden = !!admin;
            var perms = data && data.perms ? String(data.perms).split(',') : [];
            roleModal.querySelectorAll('.role-perm-check').forEach(function (box) {
                box.checked = perms.indexOf(box.value) >= 0;
                box.disabled = !!admin;
            });
            openModal('roleModal');
        }

        if (roleNewBtn) {
            roleNewBtn.addEventListener('click', function () { openRole(null); });
        }
        document.querySelectorAll('.role-edit').forEach(function (btn) {
            btn.addEventListener('click', function () {
                openRole({
                    id: btn.getAttribute('data-id'),
                    name: btn.getAttribute('data-name'),
                    code: btn.getAttribute('data-code'),
                    desc: btn.getAttribute('data-desc'),
                    status: btn.getAttribute('data-status'),
                    perms: btn.getAttribute('data-perms')
                });
            });
        });

        document.querySelectorAll('.user-role-btn').forEach(function (btn) {
            btn.addEventListener('click', function () {
                var userId = btn.getAttribute('data-user-id');
                document.getElementById('userRoleName').textContent = btn.getAttribute('data-username') || '';
                var form = document.getElementById('userRoleForm');
                form.action = ctx + '/admin/users/' + userId + '/roles';
                var ids = (btn.getAttribute('data-role-ids') || '').split(',').filter(Boolean);
                document.querySelectorAll('#userRoleModal .user-role-check').forEach(function (box) {
                    box.checked = ids.indexOf(box.value) >= 0;
                });
                openModal('userRoleModal');
            });
        });
    })();
</script>

<%@ include file="../common/foot.jspf" %>

<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="pageTitle" value="章节管理"/>
<c:set var="navActive" value="books"/>
<%@ include file="../common/head.jspf" %>

<div class="toolbar">
    <a class="btn" href="${ctx}/books/${book.id}/chapters/new">新增章节</a>
    <a class="btn ghost" href="${ctx}/books/${book.id}">返回详情</a>
    <span class="grow"></span>
    <span class="muted">拖拽行可调整章节顺序</span>
</div>

<div class="card" style="margin-bottom: 20px;">
    <div class="section-title">批量导入</div>
    <form class="toolbar" style="margin: 0;"
          action="${ctx}/books/${book.id}/chapters/import" method="post" enctype="multipart/form-data">
        <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
        <input class="input" type="file" name="file" accept=".txt" required>
        <button type="submit" class="btn">导入</button>
        <span class="form-hint" style="margin: 0;">
            单文件按“第X章 / 第X回 / Chapter N”标题自动拆分；无标题则整篇作为一章；支持 UTF-8 与 GBK
        </span>
    </form>
</div>

<c:choose>
    <c:when test="${empty chapters}">
        <p class="empty">还没有章节，可以手动新增或批量导入</p>
    </c:when>
    <c:otherwise>
        <table class="table" id="chapterTable">
            <thead>
            <tr>
                <th style="width: 40px;"></th>
                <th style="width: 80px;">排序</th>
                <th>章节标题</th>
                <th style="width: 200px;">操作</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="ch" items="${chapters}">
                <tr class="drag-row" draggable="true" data-id="${ch.id}">
                    <td class="drag-handle">⠿</td>
                    <td>${ch.sort_order}</td>
                    <td><c:out value="${ch.title}"/></td>
                    <td class="actions">
                        <a class="btn small ghost" href="${ctx}/books/${book.id}/read?chapterId=${ch.id}">预览</a>
                        <a class="btn small ghost" href="${ctx}/books/${book.id}/chapters/${ch.id}/edit">编辑</a>
                        <form action="${ctx}/books/${book.id}/chapters/${ch.id}/delete" method="post"
                              class="inline" onsubmit="return confirm('确定删除该章节吗？');">
                            <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                            <button type="submit" class="btn small danger">删除</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>

        <input type="hidden" id="csrfTokenHolder" value="<c:out value='${csrfToken}'/>">
        <input type="hidden" id="orderMeta" data-url="${ctx}/books/${book.id}/chapters/reorder">
        <script>
            (function () {
                var table = document.getElementById('chapterTable');
                var meta = document.getElementById('orderMeta');
                var tokenHolder = document.getElementById('csrfTokenHolder');
                if (!table || !meta || !tokenHolder) return;

                var tbody = table.querySelector('tbody');
                var url = meta.getAttribute('data-url');
                var token = tokenHolder.value;
                var dragging = null;

                function rowOf(target) {
                    return target && target.closest ? target.closest('tr.drag-row') : null;
                }

                tbody.addEventListener('dragstart', function (e) {
                    var row = rowOf(e.target);
                    if (!row) return;
                    dragging = row;
                    row.classList.add('dragging');
                    e.dataTransfer.effectAllowed = 'move';
                    e.dataTransfer.setData('text/plain', row.getAttribute('data-id'));
                });

                tbody.addEventListener('dragend', function () {
                    if (dragging) dragging.classList.remove('dragging');
                    dragging = null;
                });

                tbody.addEventListener('dragover', function (e) {
                    if (!dragging) return;
                    e.preventDefault();
                    e.dataTransfer.dropEffect = 'move';
                    var row = rowOf(e.target);
                    if (!row || row === dragging) return;
                    var rect = row.getBoundingClientRect();
                    var after = (e.clientY - rect.top) > rect.height / 2;
                    tbody.insertBefore(dragging, after ? row.nextSibling : row);
                });

                tbody.addEventListener('drop', function (e) {
                    if (!dragging) return;
                    e.preventDefault();
                    persist();
                });

                function persist() {
                    var ids = [];
                    tbody.querySelectorAll('tr.drag-row').forEach(function (row) {
                        ids.push(parseInt(row.getAttribute('data-id'), 10));
                    });
                    fetch(url, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json', 'X-CSRF-Token': token },
                        credentials: 'same-origin',
                        body: JSON.stringify({ ids: ids })
                    }).then(function (r) { return r.json(); }).then(function (res) {
                        if (res.code !== 200) {
                            alert(res.message || '排序保存失败');
                            location.reload();
                        }
                    }).catch(function () {
                        alert('排序保存失败');
                        location.reload();
                    });
                }
            })();
        </script>
    </c:otherwise>
</c:choose>

<%@ include file="../common/foot.jspf" %>
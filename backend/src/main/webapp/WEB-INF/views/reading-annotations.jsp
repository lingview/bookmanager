<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="pageTitle" value="笔记与书签"/>
<c:set var="navActive" value="reading"/>
<%@ include file="common/head.jspf" %>

<div class="zone-head">
    <h2 class="zone-title">笔记与书签</h2>
    <a class="zone-link" href="${ctx}/reading">返回阅读</a>
</div>

<section class="zone">
    <div class="zone-head">
        <h2 class="zone-title">划线笔记 · ${fn:length(notes)} 条</h2>
    </div>
    <c:choose>
        <c:when test="${empty notes}">
            <p class="empty">还没有笔记，阅读时选中正文即可添加</p>
        </c:when>
        <c:otherwise>
            <div class="annotation-list" id="noteList">
                <c:forEach var="n" items="${notes}">
                    <div class="annotation-item" data-id="${n.id}" data-book="${n.book_id}">
                        <div class="annotation-head">
                            <a href="${ctx}/books/${n.book_id}/read?chapterId=${n.chapter_id}&amp;note=${n.id}">
                                <c:out value="${n.book_title}"/> · <c:out value="${n.chapter_title}"/>
                            </a>
                            <span class="annotation-time"><c:out value="${n.create_time}"/></span>
                            <button type="button" class="annotation-del" data-kind="note" title="删除笔记">×</button>
                        </div>
                        <c:if test="${not empty n.quote_text}">
                            <p class="annotation-quote">“<c:out value="${n.quote_text}"/>”</p>
                        </c:if>
                        <c:if test="${not empty n.note_text}">
                            <p class="annotation-text"><c:out value="${n.note_text}"/></p>
                        </c:if>
                    </div>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</section>

<section class="zone">
    <div class="zone-head">
        <h2 class="zone-title">书签 · ${fn:length(bookmarks)} 条</h2>
    </div>
    <c:choose>
        <c:when test="${empty bookmarks}">
            <p class="empty">还没有书签，阅读时点击侧栏「书签」即可添加</p>
        </c:when>
        <c:otherwise>
            <div class="annotation-list" id="bookmarkList">
                <c:forEach var="bm" items="${bookmarks}">
                    <div class="annotation-item" data-id="${bm.id}" data-book="${bm.book_id}">
                        <div class="annotation-head">
                            <a href="${ctx}/books/${bm.book_id}/read?chapterId=${bm.chapter_id}&amp;scroll=${bm.scroll_percent}">
                                <c:out value="${bm.book_title}"/> · <c:out value="${bm.chapter_title}"/>
                            </a>
                            <span class="annotation-time">读至 ${bm.scroll_percent}%</span>
                            <button type="button" class="annotation-del" data-kind="bookmark" title="删除书签">×</button>
                        </div>
                        <c:if test="${not empty bm.note}">
                            <p class="annotation-text"><c:out value="${bm.note}"/></p>
                        </c:if>
                    </div>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</section>

<input type="hidden" id="csrfTokenHolder" value="<c:out value='${csrfToken}'/>">
<script>
    (function () {
        var token = document.getElementById('csrfTokenHolder').value;
        var ctx = '${ctx}';
        document.querySelectorAll('.annotation-del').forEach(function (btn) {
            btn.addEventListener('click', function () {
                if (!confirm('确定删除这条记录吗？')) return;
                var item = btn.closest('.annotation-item');
                var bookId = item.getAttribute('data-book');
                var id = item.getAttribute('data-id');
                var kind = btn.getAttribute('data-kind');
                var url = kind === 'note'
                    ? ctx + '/books/' + bookId + '/notes/' + id + '/delete'
                    : ctx + '/books/' + bookId + '/bookmarks/' + id + '/delete';
                var params = new URLSearchParams();
                params.append('csrfToken', token);
                btn.disabled = true;
                fetch(url, {
                    method: 'POST',
                    credentials: 'same-origin',
                    headers: { 'X-CSRF-Token': token },
                    body: params
                }).then(function (r) { return r.json(); }).then(function (res) {
                    if (res.code === 200) {
                        item.parentNode.removeChild(item);
                    } else {
                        alert(res.message || '删除失败');
                        btn.disabled = false;
                    }
                }).catch(function () {
                    alert('删除失败');
                    btn.disabled = false;
                });
            });
        });
    })();
</script>

<%@ include file="common/foot.jspf" %>

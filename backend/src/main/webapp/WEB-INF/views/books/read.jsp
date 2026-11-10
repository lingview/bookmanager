<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="pageTitle" value="${book.title}"/>
<c:set var="navActive" value="books"/>
<%@ include file="../common/head.jspf" %>

<div class="reader">
    <aside class="toc">
        <div class="toc-title">目录</div>
        <c:forEach var="item" items="${toc}">
            <a class="${item.id == chapter.id ? 'current' : ''}"
               href="${ctx}/books/${book.id}/read?chapterId=${item.id}">
                <c:out value="${item.title}"/>
            </a>
        </c:forEach>
    </aside>

    <div>
        <c:choose>
            <c:when test="${noChapter}">
                <div class="card empty">这本书还没有章节</div>
            </c:when>
            <c:otherwise>
                <article class="chapter">
                    <h1 class="chapter-title"><c:out value="${chapter.title}"/></h1>
                    <div class="chapter-content"><c:out value="${chapter.content}"/></div>
                </article>

                <div class="chapter-nav">
                    <c:choose>
                        <c:when test="${not empty prevChapter}">
                            <a class="btn ghost" href="${ctx}/books/${book.id}/read?chapterId=${prevChapter.id}">← 上一章</a>
                        </c:when>
                        <c:otherwise><span></span></c:otherwise>
                    </c:choose>
                    <a class="btn ghost" href="${ctx}/books/${book.id}">返回详情</a>
                    <c:choose>
                        <c:when test="${not empty nextChapter}">
                            <a class="btn ghost" href="${ctx}/books/${book.id}/read?chapterId=${nextChapter.id}">下一章 →</a>
                        </c:when>
                        <c:otherwise><span></span></c:otherwise>
                    </c:choose>
                </div>

                <p class="muted" style="margin-top: 16px;">阅读位置：<span id="progressLabel">0%</span></p>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<c:if test="${not noChapter}">
    <input type="hidden" id="csrfTokenHolder" value="<c:out value='${csrfToken}'/>">
    <input type="hidden" id="progressMeta" data-book-id="${book.id}" data-chapter-id="${chapter.id}"
           data-ctx="<c:out value='${ctx}'/>" data-restore="${restorePercent}">
    <script>
        (function () {
            var meta = document.getElementById('progressMeta');
            var tokenHolder = document.getElementById('csrfTokenHolder');
            if (!meta || !tokenHolder) return;

            var bookId = meta.getAttribute('data-book-id');
            var chapterId = meta.getAttribute('data-chapter-id');
            var ctx = meta.getAttribute('data-ctx') || '';
            var restore = parseFloat(meta.getAttribute('data-restore')) || 0;
            var token = tokenHolder.value;
            var url = ctx + '/books/' + bookId + '/progress';
            var label = document.getElementById('progressLabel');

            function scrollable() {
                return document.documentElement.scrollHeight - window.innerHeight;
            }

            function currentPercent() {
                var max = scrollable();
                if (max <= 0) return 0;
                var percent = (window.scrollY || document.documentElement.scrollTop) / max * 100;
                return Math.max(0, Math.min(100, percent));
            }

            if (restore > 0) {
                var max = scrollable();
                if (max > 0) {
                    window.scrollTo(0, max * restore / 100);
                }
            }

            var lastSent = -1;
            var timer = null;

            function report(force) {
                var percent = Math.round(currentPercent() * 100) / 100;
                if (label) label.textContent = percent.toFixed(1) + '%';
                if (!force && lastSent >= 0 && Math.abs(percent - lastSent) < 0.5) return;
                lastSent = percent;

                var params = new URLSearchParams();
                params.append('csrfToken', token);
                params.append('chapter_id', chapterId);
                params.append('scroll_percent', percent);

                if (force && navigator.sendBeacon) {
                    navigator.sendBeacon(url, params);
                    return;
                }
                fetch(url, { method: 'POST', body: params, credentials: 'same-origin', keepalive: true });
            }

            window.addEventListener('scroll', function () {
                if (timer) return;
                timer = setTimeout(function () {
                    timer = null;
                    report(false);
                }, 800);
            }, { passive: true });

            window.addEventListener('pagehide', function () { report(true); });
            document.addEventListener('visibilitychange', function () {
                if (document.visibilityState === 'hidden') report(true);
            });

            report(false);
        })();
    </script>
</c:if>

<%@ include file="../common/foot.jspf" %>
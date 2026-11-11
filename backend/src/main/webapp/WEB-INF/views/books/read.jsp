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
                    <div class="chapter-content" id="chapterContent"><c:out value="${chapter.content}"/></div>
                </article>

                <p class="muted" style="margin-top: 16px;">阅读位置：<span id="progressLabel">0%</span></p>
            </c:otherwise>
        </c:choose>
    </div>

    <c:if test="${not noChapter}">
        <aside class="reader-rail">
            <div class="rail-group">
                <button type="button" class="rail-btn" id="fontMinus" title="缩小字号">
                    <span class="rail-icon">A−</span>
                    <span class="rail-text">缩小</span>
                </button>
                <button type="button" class="rail-btn" id="fontPlus" title="放大字号">
                    <span class="rail-icon">A+</span>
                    <span class="rail-text">放大</span>
                </button>
                <button type="button" class="rail-btn" id="fontReset" title="恢复默认字号">
                    <span class="rail-icon">A</span>
                    <span class="rail-text">默认</span>
                </button>
            </div>
            <c:if test="${not empty prevChapter or not empty nextChapter}">
                <div class="rail-group">
                    <c:if test="${not empty prevChapter}">
                        <a class="rail-btn" title="上一章"
                           href="${ctx}/books/${book.id}/read?chapterId=${prevChapter.id}">
                            <span class="rail-icon">↑</span>
                            <span class="rail-text">上一章</span>
                        </a>
                    </c:if>
                    <c:if test="${not empty nextChapter}">
                        <a class="rail-btn" title="下一章"
                           href="${ctx}/books/${book.id}/read?chapterId=${nextChapter.id}">
                            <span class="rail-icon">↓</span>
                            <span class="rail-text">下一章</span>
                        </a>
                    </c:if>
                </div>
            </c:if>
            <div class="rail-group">
                <a class="rail-btn" title="返回首页" href="${ctx}/">
                    <span class="rail-icon">⌂</span>
                    <span class="rail-text">首页</span>
                </a>
                <a class="rail-btn" title="返回详情" href="${ctx}/books/${book.id}">
                    <span class="rail-icon">☰</span>
                    <span class="rail-text">详情</span>
                </a>
            </div>
        </aside>
    </c:if>
</div>

<c:if test="${not noChapter}">
    <input type="hidden" id="csrfTokenHolder" value="<c:out value='${csrfToken}'/>">
    <input type="hidden" id="progressMeta" data-book-id="${book.id}" data-chapter-id="${chapter.id}"
           data-ctx="<c:out value='${ctx}'/>" data-restore="${restorePercent}">
    <script>
        (function () {
            var content = document.getElementById('chapterContent');
            if (!content) return;

            var STORAGE_KEY = 'reader-font-size';
            var SIZES = [13, 14, 15, 16, 18, 20, 22];
            var DEFAULT_INDEX = 2;
            var index = DEFAULT_INDEX;

            try {
                var saved = parseInt(localStorage.getItem(STORAGE_KEY), 10);
                if (!isNaN(saved) && saved >= 0 && saved < SIZES.length) {
                    index = saved;
                }
            } catch (e) {
            }

            var minus = document.getElementById('fontMinus');
            var plus = document.getElementById('fontPlus');
            var reset = document.getElementById('fontReset');

            function apply() {
                content.style.fontSize = SIZES[index] + 'px';
                minus.disabled = index === 0;
                plus.disabled = index === SIZES.length - 1;
                try {
                    localStorage.setItem(STORAGE_KEY, String(index));
                } catch (e) {
                }
            }

            minus.addEventListener('click', function () {
                if (index > 0) {
                    index--;
                    apply();
                }
            });

            plus.addEventListener('click', function () {
                if (index < SIZES.length - 1) {
                    index++;
                    apply();
                }
            });

            reset.addEventListener('click', function () {
                index = DEFAULT_INDEX;
                apply();
            });

            apply();
        })();
    </script>
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
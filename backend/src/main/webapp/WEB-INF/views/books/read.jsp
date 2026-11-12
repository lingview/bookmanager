<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="pageTitle" value="${book.title}"/>
<c:set var="navActive" value="books"/>
<%@ include file="../common/head.jspf" %>

<style>
    @media (max-width: 768px) {
        .page { padding: 0; }
    }
</style>

<div class="reader">
    <aside class="toc" id="tocDrawer">
        <div class="toc-mobile-head">
            <span>阅读导航</span>
            <button type="button" class="toc-close" id="tocClose" title="关闭">×</button>
        </div>
        <div class="toc-tabs" id="tocTabs">
            <button type="button" class="toc-tab active" data-panel="tocPanelChapters">目录</button>
            <button type="button" class="toc-tab" data-panel="tocPanelBookmarks">书签</button>
            <button type="button" class="toc-tab" data-panel="tocPanelNotes">笔记</button>
        </div>

        <div class="toc-panel active" id="tocPanelChapters">
            <div class="toc-title">目录</div>
            <form class="toc-search" id="tocSearchForm">
                <input class="input" type="text" id="tocSearchInput" placeholder="书内搜索，回车查找" maxlength="50">
            </form>
            <div class="toc-results" id="tocSearchResults" hidden></div>
            <c:forEach var="item" items="${toc}">
                <a class="${item.id == chapter.id ? 'current' : ''}"
                   href="${ctx}/books/${book.id}/read?chapterId=${item.id}">
                    <c:out value="${item.title}"/>
                </a>
            </c:forEach>
        </div>

        <div class="toc-panel" id="tocPanelBookmarks">
            <div class="toc-section-title">书签</div>
            <c:choose>
                <c:when test="${empty bookmarks}">
                    <p class="muted" style="padding: 6px 8px;">还没有书签，点击底部「书签」可在当前位置添加</p>
                </c:when>
                <c:otherwise>
                    <div class="toc-bookmarks">
                        <c:forEach var="bm" items="${bookmarks}">
                            <div class="toc-bookmark">
                                <a href="${ctx}/books/${book.id}/read?chapterId=${bm.chapter_id}&scroll=${bm.scroll_percent}">
                                    <c:out value="${bm.chapter_title}"/> · ${bm.scroll_percent}%
                                </a>
                                <button type="button" class="bookmark-del" data-id="${bm.id}" title="删除书签">×</button>
                            </div>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

        <div class="toc-panel" id="tocPanelNotes">
            <div class="toc-section-title">
                笔记
                <a class="note-export" href="${ctx}/books/${book.id}/notes/export">导出</a>
            </div>
            <c:choose>
                <c:when test="${empty notes}">
                    <p class="muted" style="padding: 6px 8px;">选中正文文字可添加划线笔记</p>
                </c:when>
                <c:otherwise>
                    <div class="toc-notes">
                        <c:forEach var="n" items="${notes}">
                            <div class="toc-note">
                                <a href="${ctx}/books/${book.id}/read?chapterId=${n.chapter_id}">
                                    <c:if test="${not empty n.quote_text}">
                                        <span class="note-quote">“<c:out value="${n.quote_text}"/>”</span>
                                    </c:if>
                                    <c:if test="${not empty n.note_text}">
                                        <span class="note-text"><c:out value="${n.note_text}"/></span>
                                    </c:if>
                                    <span class="note-chapter"><c:out value="${n.chapter_title}"/></span>
                                </a>
                                <button type="button" class="note-del" data-id="${n.id}" title="删除笔记">×</button>
                            </div>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
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
            <div class="rail-group">
                <button type="button" class="rail-btn" id="themeToggle" title="切换纸张样式（日间生效）">
                    <span class="rail-icon">◐</span>
                    <span class="rail-text" id="themeLabel">纸张</span>
                </button>
                <button type="button" class="rail-btn" id="lineToggle" title="调整行距">
                    <span class="rail-icon">≡</span>
                    <span class="rail-text" id="lineLabel">行距</span>
                </button>
                <button type="button" class="rail-btn" id="widthToggle" title="调整页宽">
                    <span class="rail-icon">↔</span>
                    <span class="rail-text" id="widthLabel">宽</span>
                </button>
            </div>
            <div class="rail-group">
                <button type="button" class="rail-btn" id="addBookmark" title="在当前位置加书签">
                    <span class="rail-icon">☆</span>
                    <span class="rail-text">书签</span>
                </button>
                <button type="button" class="rail-btn" id="toggleScroll" title="自动滚动">
                    <span class="rail-icon">⇊</span>
                    <span class="rail-text">滚动</span>
                </button>
                <button type="button" class="rail-btn" id="toggleTts" title="朗读本章">
                    <span class="rail-icon">♪</span>
                    <span class="rail-text">朗读</span>
                </button>
            </div>
            <c:if test="${not empty prevChapter or not empty nextChapter}">
                <div class="rail-group">
                    <c:if test="${not empty prevChapter}">
                        <a class="rail-btn" title="上一章（←）"
                           href="${ctx}/books/${book.id}/read?chapterId=${prevChapter.id}">
                            <span class="rail-icon">↑</span>
                            <span class="rail-text">上一章</span>
                        </a>
                    </c:if>
                    <c:if test="${not empty nextChapter}">
                        <a class="rail-btn" title="下一章（→）"
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
                <a class="rail-btn" title="返回详情（Esc）" href="${ctx}/books/${book.id}">
                    <span class="rail-icon">☰</span>
                    <span class="rail-text">详情</span>
                </a>
            </div>
        </aside>
    </c:if>
</div>

<c:if test="${not noChapter}">
    <div class="note-pop" id="notePop" hidden>
        <button type="button" id="notePopBtn">划线笔记</button>
    </div>
    <div class="note-mask" id="noteMask" hidden>
        <div class="note-dialog">
            <div class="note-dialog-title">添加笔记</div>
            <blockquote class="note-quote-preview" id="noteQuotePreview"></blockquote>
            <textarea class="textarea" id="noteText" rows="4" maxlength="2000"
                      placeholder="写点想法（可选）"></textarea>
            <div class="note-dialog-actions">
                <button type="button" class="btn ghost" id="noteCancel">取消</button>
                <button type="button" class="btn" id="noteSave">保存</button>
            </div>
        </div>
    </div>
    <div class="reader-mobile-mask" id="readerMobileMask"></div>

    <div class="reader-mobile-top">
        <a class="mobile-back" href="${ctx}/books/${book.id}" aria-label="返回详情">←</a>
        <span class="mobile-title">
            <c:out value="${book.title}"/> · <c:out value="${chapter.title}"/>
        </span>
    </div>

    <div class="reader-mobile-bottom">
        <c:if test="${not empty prevChapter}">
            <a class="mobile-btn" href="${ctx}/books/${book.id}/read?chapterId=${prevChapter.id}">
                <span class="mobile-icon">↑</span><span>上一章</span>
            </a>
        </c:if>
        <button type="button" class="mobile-btn" id="mobileTocBtn">
            <span class="mobile-icon">☰</span><span>目录</span>
        </button>
        <button type="button" class="mobile-btn" id="mobileBookmarkBtn">
            <span class="mobile-icon">☆</span><span>书签</span>
        </button>
        <button type="button" class="mobile-btn" id="mobileSettingsBtn">
            <span class="mobile-icon">⚙</span><span>设置</span>
        </button>
        <c:if test="${not empty nextChapter}">
            <a class="mobile-btn" href="${ctx}/books/${book.id}/read?chapterId=${nextChapter.id}">
                <span class="mobile-icon">↓</span><span>下一章</span>
            </a>
        </c:if>
    </div>

    <div class="reader-settings-sheet" id="readerSettings">
        <div class="sheet-head">
            <span>阅读设置</span>
            <button type="button" class="sheet-close" id="settingsClose">×</button>
        </div>
        <div class="sheet-row">
            <span class="sheet-label">字号</span>
            <button type="button" class="sheet-btn" id="mFontMinus">A−</button>
            <span class="sheet-value" id="mFontValue"></span>
            <button type="button" class="sheet-btn" id="mFontPlus">A+</button>
            <button type="button" class="sheet-btn" id="mFontReset">默认</button>
        </div>
        <div class="sheet-row">
            <span class="sheet-label">纸张</span>
            <button type="button" class="sheet-btn" id="mThemeLight">纸张</button>
            <button type="button" class="sheet-btn" id="mThemeSepia">护眼</button>
            <span class="sheet-hint">夜间请用顶部全局开关</span>
        </div>
        <div class="sheet-row">
            <span class="sheet-label">行距</span>
            <button type="button" class="sheet-btn" id="mLine1">紧凑</button>
            <button type="button" class="sheet-btn" id="mLine2">标准</button>
            <button type="button" class="sheet-btn" id="mLine3">宽松</button>
        </div>
        <div class="sheet-row">
            <span class="sheet-label">页宽</span>
            <button type="button" class="sheet-btn" id="mWidthWide">宽</button>
            <button type="button" class="sheet-btn" id="mWidthNarrow">窄</button>
        </div>
        <div class="sheet-row">
            <span class="sheet-label">自动滚动</span>
            <button type="button" class="sheet-btn" id="mScrollBtn">开启</button>
            <span class="sheet-label">朗读</span>
            <button type="button" class="sheet-btn" id="mTtsBtn">开启</button>
        </div>
    </div>

    <input type="hidden" id="csrfTokenHolder" value="<c:out value='${csrfToken}'/>">
    <input type="hidden" id="progressMeta" data-book-id="${book.id}" data-chapter-id="${chapter.id}"
           data-ctx="<c:out value='${ctx}'/>" data-restore="${restorePercent}">
    <c:if test="${not empty prevChapter}">
        <input type="hidden" id="prevChapterUrl" value="${ctx}/books/${book.id}/read?chapterId=${prevChapter.id}">
    </c:if>
    <c:if test="${not empty nextChapter}">
        <input type="hidden" id="nextChapterUrl" value="${ctx}/books/${book.id}/read?chapterId=${nextChapter.id}">
    </c:if>
    <input type="hidden" id="detailUrl" value="${ctx}/books/${book.id}">
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
            var mobileFontValue = document.getElementById('mFontValue');

            function apply() {
                content.style.fontSize = SIZES[index] + 'px';
                minus.disabled = index === 0;
                plus.disabled = index === SIZES.length - 1;
                if (mobileFontValue) {
                    mobileFontValue.textContent = SIZES[index];
                }
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

            var mFontMinus = document.getElementById('mFontMinus');
            var mFontPlus = document.getElementById('mFontPlus');
            var mFontReset = document.getElementById('mFontReset');
            if (mFontMinus) mFontMinus.addEventListener('click', function () { minus.click(); });
            if (mFontPlus) mFontPlus.addEventListener('click', function () { plus.click(); });
            if (mFontReset) mFontReset.addEventListener('click', function () { reset.click(); });

            apply();
        })();
    </script>
    <script>
        (function () {
            var content = document.getElementById('chapterContent');
            if (!content) return;

            var THEMES = ['light', 'sepia'];
            var THEME_LABELS = ['纸张', '护眼'];
            var LINE_HEIGHTS = ['1.9', '1.6', '2.2'];
            var LINE_LABELS = ['行距', '紧凑', '宽松'];

            var themeLabel = document.getElementById('themeLabel');
            var lineLabel = document.getElementById('lineLabel');
            var widthLabel = document.getElementById('widthLabel');

            var mThemeLight = document.getElementById('mThemeLight');
            var mThemeSepia = document.getElementById('mThemeSepia');
            var mLine1 = document.getElementById('mLine1');
            var mLine2 = document.getElementById('mLine2');
            var mLine3 = document.getElementById('mLine3');
            var mWidthWide = document.getElementById('mWidthWide');
            var mWidthNarrow = document.getElementById('mWidthNarrow');

            function paintSheet() {
                if (mThemeLight) mThemeLight.classList.toggle('on', themeIndex === 0);
                if (mThemeSepia) mThemeSepia.classList.toggle('on', themeIndex === 1);
                if (mLine1) mLine1.classList.toggle('on', lineIndex === 1);
                if (mLine2) mLine2.classList.toggle('on', lineIndex === 0);
                if (mLine3) mLine3.classList.toggle('on', lineIndex === 2);
                if (mWidthWide) mWidthWide.classList.toggle('on', widthIndex === 0);
                if (mWidthNarrow) mWidthNarrow.classList.toggle('on', widthIndex === 1);
            }

            function load(key, size) {
                try {
                    var value = parseInt(localStorage.getItem(key), 10);
                    if (!isNaN(value) && value >= 0 && value < size) {
                        return value;
                    }
                } catch (e) {
                }
                return 0;
            }

            function save(key, value) {
                try {
                    localStorage.setItem(key, String(value));
                } catch (e) {
                }
            }

            var themeIndex = load('reader-theme', THEMES.length);
            var lineIndex = load('reader-line-height', LINE_HEIGHTS.length);
            var widthIndex = load('reader-width', 2);

            function isDarkMode() {
                return !!(window.AppTheme && window.AppTheme.isDark());
            }

            function applyTheme() {
                var dark = isDarkMode();
                var className = dark ? 'reader-theme-dark' : 'reader-theme-' + THEMES[themeIndex];
                document.body.classList.remove('reader-theme-light', 'reader-theme-sepia', 'reader-theme-dark');
                document.documentElement.classList.remove('reader-theme-light', 'reader-theme-sepia', 'reader-theme-dark');
                document.body.classList.add(className);
                document.documentElement.classList.add(className);
                themeLabel.textContent = THEME_LABELS[themeIndex];
                save('reader-theme', themeIndex);
                paintSheet();
            }

            document.addEventListener('app-theme-change', applyTheme);

            function applyLine() {
                content.style.lineHeight = LINE_HEIGHTS[lineIndex];
                lineLabel.textContent = LINE_LABELS[lineIndex];
                save('reader-line-height', lineIndex);
                paintSheet();
            }

            function applyWidth() {
                if (widthIndex === 0) {
                    content.style.maxWidth = '';
                    content.style.margin = '';
                } else {
                    content.style.maxWidth = '680px';
                    content.style.margin = '0 auto';
                }
                widthLabel.textContent = widthIndex === 0 ? '宽' : '窄';
                save('reader-width', widthIndex);
                paintSheet();
            }

            document.getElementById('themeToggle').addEventListener('click', function () {
                themeIndex = (themeIndex + 1) % THEMES.length;
                applyTheme();
            });

            document.getElementById('lineToggle').addEventListener('click', function () {
                lineIndex = (lineIndex + 1) % LINE_HEIGHTS.length;
                applyLine();
            });

            document.getElementById('widthToggle').addEventListener('click', function () {
                widthIndex = (widthIndex + 1) % 2;
                applyWidth();
            });

            if (mThemeLight) mThemeLight.addEventListener('click', function () {
                themeIndex = 0;
                applyTheme();
            });
            if (mThemeSepia) mThemeSepia.addEventListener('click', function () {
                themeIndex = 1;
                applyTheme();
            });
            if (mLine1) mLine1.addEventListener('click', function () {
                lineIndex = 1;
                applyLine();
            });
            if (mLine2) mLine2.addEventListener('click', function () {
                lineIndex = 0;
                applyLine();
            });
            if (mLine3) mLine3.addEventListener('click', function () {
                lineIndex = 2;
                applyLine();
            });
            if (mWidthWide) mWidthWide.addEventListener('click', function () {
                widthIndex = 0;
                applyWidth();
            });
            if (mWidthNarrow) mWidthNarrow.addEventListener('click', function () {
                widthIndex = 1;
                applyWidth();
            });

            applyTheme();
            applyLine();
            applyWidth();
        })();
    </script>
    <script>
        (function () {
            var prev = document.getElementById('prevChapterUrl');
            var next = document.getElementById('nextChapterUrl');
            var detail = document.getElementById('detailUrl');

            document.addEventListener('keydown', function (e) {
                if (e.altKey || e.ctrlKey || e.metaKey || e.shiftKey) return;
                var target = e.target;
                if (target && (target.tagName === 'INPUT' || target.tagName === 'TEXTAREA' || target.isContentEditable)) {
                    return;
                }
                if (e.key === 'ArrowLeft' && prev) {
                    window.location.href = prev.value;
                } else if (e.key === 'ArrowRight' && next) {
                    window.location.href = next.value;
                } else if (e.key === 'Escape' && detail) {
                    window.location.href = detail.value;
                }
            });
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
                fetch(url, { method: 'POST', body: params, credentials: 'same-origin', keepalive: true })
                    .catch(function () {
                    });
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
    <script>
        (function () {
            var meta = document.getElementById('progressMeta');
            var tokenHolder = document.getElementById('csrfTokenHolder');
            if (!meta || !tokenHolder) return;

            var bookId = meta.getAttribute('data-book-id');
            var ctx = meta.getAttribute('data-ctx') || '';
            var token = tokenHolder.value;
            var url = ctx + '/books/' + bookId + '/reading-time';
            var HEARTBEAT_MS = 15000;
            var IDLE_LIMIT_MS = 90000;

            var lastActivity = Date.now();
            function markActive() {
                lastActivity = Date.now();
            }

            ['scroll', 'mousemove', 'keydown', 'click', 'touchstart'].forEach(function (name) {
                window.addEventListener(name, markActive, { passive: true });
            });

            var lastTick = Date.now();
            setInterval(function () {
                var now = Date.now();
                if (document.visibilityState !== 'visible' || now - lastActivity > IDLE_LIMIT_MS) {
                    lastTick = now;
                    return;
                }
                var seconds = Math.min(Math.round((now - lastTick) / 1000), 120);
                lastTick = now;
                if (seconds <= 0) return;

                var params = new URLSearchParams();
                params.append('csrfToken', token);
                params.append('seconds', String(seconds));
                fetch(url, { method: 'POST', body: params, credentials: 'same-origin', keepalive: true })
                    .catch(function () {
                    });
            }, HEARTBEAT_MS);
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
            var token = tokenHolder.value;
            var content = document.getElementById('chapterContent');

            function currentPercent() {
                var max = document.documentElement.scrollHeight - window.innerHeight;
                if (max <= 0) return 0;
                var percent = (window.scrollY || document.documentElement.scrollTop) / max * 100;
                return Math.max(0, Math.min(100, Math.round(percent * 100) / 100));
            }

            var addBtn = document.getElementById('addBookmark');
            addBtn.addEventListener('click', function () {
                var params = new URLSearchParams();
                params.append('csrfToken', token);
                params.append('chapter_id', chapterId);
                params.append('scroll_percent', String(currentPercent()));
                addBtn.disabled = true;
                fetch(ctx + '/books/' + bookId + '/bookmarks', {
                    method: 'POST',
                    credentials: 'same-origin',
                    headers: { 'X-CSRF-Token': token },
                    body: params
                }).then(function (r) { return r.json(); }).then(function (res) {
                    if (res.code !== 200) {
                        alert(res.message || '添加失败');
                        return;
                    }
                    window.location.reload();
                }).catch(function () {
                    alert('添加失败');
                }).then(function () {
                    addBtn.disabled = false;
                });
            });

            Array.prototype.slice.call(document.querySelectorAll('.bookmark-del')).forEach(function (btn) {
                btn.addEventListener('click', function () {
                    var params = new URLSearchParams();
                    params.append('csrfToken', token);
                    fetch(ctx + '/books/' + bookId + '/bookmarks/' + btn.getAttribute('data-id') + '/delete', {
                        method: 'POST',
                        credentials: 'same-origin',
                        headers: { 'X-CSRF-Token': token },
                        body: params
                    }).then(function (r) { return r.json(); }).then(function (res) {
                        if (res.code === 200) {
                            window.location.reload();
                        } else {
                            alert(res.message || '删除失败');
                        }
                    });
                });
            });

            var searchForm = document.getElementById('tocSearchForm');
            var searchInput = document.getElementById('tocSearchInput');
            var searchResults = document.getElementById('tocSearchResults');
            searchForm.addEventListener('submit', function (e) {
                e.preventDefault();
                var q = (searchInput.value || '').trim();
                if (!q) {
                    searchResults.hidden = true;
                    searchResults.innerHTML = '';
                    return;
                }
                fetch(ctx + '/books/' + bookId + '/search?q=' + encodeURIComponent(q), {
                    credentials: 'same-origin'
                }).then(function (r) { return r.json(); }).then(function (res) {
                    var items = (res && res.data) || [];
                    searchResults.innerHTML = '';
                    searchResults.hidden = false;
                    if (!items.length) {
                        searchResults.innerHTML = '<p class="muted" style="padding:6px 8px;">没有找到匹配内容</p>';
                        return;
                    }
                    items.forEach(function (item) {
                        var a = document.createElement('a');
                        a.className = 'toc-hit';
                        a.href = ctx + '/books/' + bookId + '/read?chapterId=' + item.chapter_id;
                        var title = document.createElement('span');
                        title.className = 'toc-hit-title';
                        title.textContent = item.title;
                        var snippet = document.createElement('span');
                        snippet.className = 'toc-hit-snippet';
                        snippet.textContent = item.snippet;
                        a.appendChild(title);
                        a.appendChild(snippet);
                        searchResults.appendChild(a);
                    });
                }).catch(function () {
                    searchResults.hidden = false;
                    searchResults.innerHTML = '<p class="muted" style="padding:6px 8px;">搜索失败</p>';
                });
            });

            var scrollBtn = document.getElementById('toggleScroll');
            var scrollTimer = null;
            scrollBtn.addEventListener('click', function () {
                if (scrollTimer) {
                    clearInterval(scrollTimer);
                    scrollTimer = null;
                    scrollBtn.classList.remove('on');
                    return;
                }
                scrollBtn.classList.add('on');
                scrollTimer = setInterval(function () {
                    var max = document.documentElement.scrollHeight - window.innerHeight;
                    if (max <= 0 || window.scrollY >= max - 1) {
                        clearInterval(scrollTimer);
                        scrollTimer = null;
                        scrollBtn.classList.remove('on');
                        return;
                    }
                    window.scrollBy(0, 1);
                }, 40);
            });

            var notePop = document.getElementById('notePop');
            var notePopBtn = document.getElementById('notePopBtn');
            var noteMask = document.getElementById('noteMask');
            var noteQuotePreview = document.getElementById('noteQuotePreview');
            var noteText = document.getElementById('noteText');
            var selectedQuote = '';

            function hideNotePop() {
                notePop.hidden = true;
            }

            document.addEventListener('mouseup', function (e) {
                if (!content || noteMask.hidden === false) return;
                if (notePop.contains(e.target)) return;
                setTimeout(function () {
                    var selection = window.getSelection();
                    var text = selection ? selection.toString().trim() : '';
                    if (!text || !selection.rangeCount) {
                        hideNotePop();
                        return;
                    }
                    var range = selection.getRangeAt(0);
                    if (!content.contains(range.commonAncestorContainer)) {
                        hideNotePop();
                        return;
                    }
                    if (text.length > 2000) {
                        text = text.substring(0, 2000);
                    }
                    selectedQuote = text;
                    notePop.style.left = Math.min(window.innerWidth - 130, e.clientX) + 'px';
                    notePop.style.top = Math.min(window.innerHeight - 60, e.clientY + 12) + 'px';
                    notePop.hidden = false;
                }, 0);
            });

            document.addEventListener('click', function (e) {
                if (!notePop.hidden && !notePop.contains(e.target)) {
                    var selection = window.getSelection();
                    if (!selection || !selection.toString().trim()) {
                        hideNotePop();
                    }
                }
            });

            notePopBtn.addEventListener('click', function () {
                if (!selectedQuote) return;
                noteQuotePreview.textContent = selectedQuote;
                noteText.value = '';
                noteMask.hidden = false;
                hideNotePop();
                noteText.focus();
            });

            document.getElementById('noteCancel').addEventListener('click', function () {
                noteMask.hidden = true;
            });

            document.getElementById('noteSave').addEventListener('click', function () {
                var params = new URLSearchParams();
                params.append('csrfToken', token);
                params.append('chapter_id', chapterId);
                params.append('quote_text', selectedQuote);
                params.append('note_text', noteText.value);
                fetch(ctx + '/books/' + bookId + '/notes', {
                    method: 'POST',
                    credentials: 'same-origin',
                    headers: { 'X-CSRF-Token': token },
                    body: params
                }).then(function (r) { return r.json(); }).then(function (res) {
                    if (res.code === 200) {
                        window.location.reload();
                    } else {
                        alert(res.message || '保存失败');
                    }
                }).catch(function () {
                    alert('保存失败');
                });
            });

            Array.prototype.slice.call(document.querySelectorAll('.note-del')).forEach(function (btn) {
                btn.addEventListener('click', function () {
                    var params = new URLSearchParams();
                    params.append('csrfToken', token);
                    fetch(ctx + '/books/' + bookId + '/notes/' + btn.getAttribute('data-id') + '/delete', {
                        method: 'POST',
                        credentials: 'same-origin',
                        headers: { 'X-CSRF-Token': token },
                        body: params
                    }).then(function (r) { return r.json(); }).then(function (res) {
                        if (res.code === 200) {
                            window.location.reload();
                        } else {
                            alert(res.message || '删除失败');
                        }
                    });
                });
            });

            var ttsBtn = document.getElementById('toggleTts');
            var speaking = false;
            ttsBtn.addEventListener('click', function () {
                if (!('speechSynthesis' in window)) {
                    alert('当前浏览器不支持朗读');
                    return;
                }
                if (speaking) {
                    window.speechSynthesis.cancel();
                    speaking = false;
                    ttsBtn.classList.remove('on');
                    return;
                }
                var text = content ? content.innerText : '';
                if (!text.trim()) {
                    return;
                }
                var utterance = new SpeechSynthesisUtterance(text);
                utterance.lang = 'zh-CN';
                utterance.onend = function () {
                    speaking = false;
                    ttsBtn.classList.remove('on');
                };
                utterance.onerror = function () {
                    speaking = false;
                    ttsBtn.classList.remove('on');
                };
                window.speechSynthesis.cancel();
                window.speechSynthesis.speak(utterance);
                speaking = true;
                ttsBtn.classList.add('on');
            });

            var mScrollBtn = document.getElementById('mScrollBtn');
            var mTtsBtn = document.getElementById('mTtsBtn');
            if (mScrollBtn) {
                mScrollBtn.addEventListener('click', function () {
                    scrollBtn.click();
                    var on = scrollBtn.classList.contains('on');
                    mScrollBtn.classList.toggle('on', on);
                    mScrollBtn.textContent = on ? '停止' : '开启';
                });
            }
            if (mTtsBtn) {
                mTtsBtn.addEventListener('click', function () {
                    ttsBtn.click();
                    var on = ttsBtn.classList.contains('on');
                    mTtsBtn.classList.toggle('on', on);
                    mTtsBtn.textContent = on ? '停止' : '开启';
                });
            }
        })();
    </script>
    <script>
        (function () {
            var mobileQuery = window.matchMedia('(max-width: 768px)');
            var top = document.querySelector('.reader-mobile-top');
            var bottom = document.querySelector('.reader-mobile-bottom');
            var tocDrawer = document.getElementById('tocDrawer');
            if (!top || !bottom || !tocDrawer) return;

            var mask = document.getElementById('readerMobileMask');

            function closePanels() {
                document.body.classList.remove('reader-toc-open', 'reader-settings-open');
            }

            if (mobileQuery.matches) {
                document.body.classList.add('reader-chrome-visible');
                setTimeout(function () {
                    if (mobileQuery.matches) {
                        document.body.classList.remove('reader-chrome-visible');
                    }
                }, 2600);
            }

            document.addEventListener('click', function (e) {
                if (!mobileQuery.matches) return;
                if (e.target.closest('.toc, .reader-settings-sheet, .reader-mobile-top, .reader-mobile-bottom, .note-mask, .note-pop, a, button, input, textarea')) {
                    return;
                }
                var selection = window.getSelection();
                if (selection && selection.toString().trim()) return;
                document.body.classList.toggle('reader-chrome-visible');
            });

            document.getElementById('mobileTocBtn').addEventListener('click', function () {
                document.body.classList.add('reader-toc-open');
            });
            document.getElementById('mobileSettingsBtn').addEventListener('click', function () {
                document.body.classList.add('reader-settings-open');
            });
            document.getElementById('mobileBookmarkBtn').addEventListener('click', function () {
                var add = document.getElementById('addBookmark');
                if (add) add.click();
            });

            document.getElementById('tocClose').addEventListener('click', closePanels);
            document.getElementById('settingsClose').addEventListener('click', closePanels);
            if (mask) mask.addEventListener('click', closePanels);

            var tabs = document.querySelectorAll('.toc-tab');
            Array.prototype.forEach.call(tabs, function (tab) {
                tab.addEventListener('click', function () {
                    Array.prototype.forEach.call(tabs, function (item) { item.classList.remove('active'); });
                    tab.classList.add('active');
                    Array.prototype.forEach.call(document.querySelectorAll('.toc-panel'), function (panel) {
                        panel.classList.remove('active');
                    });
                    var target = document.getElementById(tab.getAttribute('data-panel'));
                    if (target) target.classList.add('active');
                });
            });
        })();
    </script>
</c:if>

<%@ include file="../common/foot.jspf" %>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="pageTitle" value="图书"/>
<c:set var="navActive" value="books"/>
<c:set var="hasFilter"
       value="${not empty keyword or not empty category_id or not empty tag or not empty shelf}"/>
<%@ include file="../common/head.jspf" %>

<div class="shelf-layout">
    <aside class="shelf-rail shelf-rail-left">
        <section class="zone">
            <div class="zone-head">
                <h2 class="zone-title">继续阅读</h2>
                <a class="zone-link" href="${ctx}/reading">全部</a>
            </div>
            <c:choose>
                <c:when test="${empty recentBooks}">
                    <p class="rail-empty">还没有阅读记录</p>
                </c:when>
                <c:otherwise>
                    <div class="continue-list">
                        <c:forEach var="rb" items="${recentBooks}">
                            <c:url var="readUrl" value="/books/${rb.id}/read">
                                <c:if test="${not empty rb.progress_chapter_id}">
                                    <c:param name="chapterId" value="${rb.progress_chapter_id}"/>
                                </c:if>
                            </c:url>
                            <a class="continue-item" href="${readUrl}">
                                <span class="continue-cover">
                                    <c:choose>
                                        <c:when test="${not empty rb.cover_url}">
                                            <img src="<c:out value='${rb.cover_url}'/>" alt="">
                                        </c:when>
                                        <c:otherwise>无封面</c:otherwise>
                                    </c:choose>
                                </span>
                                <span class="continue-info">
                                    <span class="continue-title"><c:out value="${rb.title}"/></span>
                                    <span class="continue-meta">
                                        <c:choose>
                                            <c:when test="${not empty rb.progress_chapter_title}">读到 <c:out value="${rb.progress_chapter_title}"/> · </c:when>
                                            <c:otherwise>已读 </c:otherwise>
                                        </c:choose>${rb.progress_percent_text}%
                                    </span>
                                    <span class="continue-bar"><span style="width: ${rb.progress_percent_text}%"></span></span>
                                </span>
                            </a>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>

        <section class="zone">
            <div class="zone-head">
                <h2 class="zone-title">快捷入口</h2>
            </div>
            <div class="rail-links">
                <c:if test="${canUploadBook}">
                    <a class="rail-link" href="${ctx}/books/new">新增图书</a>
                </c:if>
                <a class="rail-link" href="${ctx}/books/fulltext">搜正文</a>
                <a class="rail-link" href="${ctx}/books/random">随便看看</a>
            </div>
        </section>
    </aside>

    <main class="shelf-main">
<div class="shelf-head">
    <h2 class="zone-title">我的书架</h2>
    <div class="shelf-tools">
        <button type="button" class="icon-btn" id="searchToggle" title="搜索" aria-label="搜索">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor"
                 stroke-width="2" stroke-linecap="round">
                <circle cx="11" cy="11" r="7"></circle>
                <path d="M20 20l-3.5-3.5"></path>
            </svg>
        </button>
        <button type="button" class="icon-btn" id="filterToggle" title="筛选" aria-label="筛选">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor"
                 stroke-width="2" stroke-linecap="round">
                <path d="M4 6h16M7 12h10M10 18h4"></path>
            </svg>
            <c:if test="${hasFilter}"><span class="badge-dot"></span></c:if>
        </button>
        <c:if test="${canUploadBook}">
            <a class="btn small" href="${ctx}/books/new">新增图书</a>
        </c:if>
    </div>
</div>

<c:if test="${canBatchBook}">
    <form id="batchForm" class="batch-bar" action="${ctx}/books/batch" method="post">
        <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
        <label class="choice"><input type="checkbox" id="batchAll"> 全选</label>
        <span class="muted" id="batchCount">已选 0 本</span>
        <select class="select" name="category_id">
            <option value="">选择分类</option>
            <c:forEach var="cat" items="${categories}">
                <option value="${cat.id}"><c:out value="${cat.name}"/></option>
            </c:forEach>
        </select>
        <button type="submit" class="btn small ghost" name="action" value="category">设为分类</button>
        <input class="input" type="text" name="tag" placeholder="标签" maxlength="50">
        <button type="submit" class="btn small ghost" name="action" value="tag">添加标签</button>
        <c:if test="${canEditBook}">
            <button type="submit" class="btn small ghost" name="action" value="cover"
                    onclick="return confirm('为选中的书自动搜索并应用封面？单次最多处理 30 本。');">补封面</button>
        </c:if>
        <c:if test="${canDeleteBook}">
            <button type="submit" class="btn small danger" name="action" value="delete"
                    onclick="return confirm('确定删除选中的图书吗？关联章节和阅读数据会一并删除。');">删除</button>
        </c:if>
        <button type="button" class="btn small ghost" id="batchExit">取消</button>
    </form>
</c:if>

<div class="shelf-menu" id="shelfMenu" hidden>
    <button type="button" id="shelfMenuBatch">批量管理</button>
</div>

<c:if test="${hasFilter}">
    <div class="active-filters">
        <span>筛选：</span>
        <c:if test="${groupMode}"><span class="filter-chip">按系列</span></c:if>
        <c:if test="${shelf == 'want'}"><span class="filter-chip">想读</span></c:if>
        <c:if test="${shelf == 'reading'}"><span class="filter-chip">在读</span></c:if>
        <c:if test="${shelf == 'finished'}"><span class="filter-chip">读过</span></c:if>
        <c:forEach var="cat" items="${categories}">
            <c:if test="${cat.id == category_id}">
                <span class="filter-chip"><c:out value="${cat.name}"/></span>
            </c:if>
        </c:forEach>
        <c:if test="${not empty tag}">
            <span class="filter-chip"><c:out value="${tag}"/></span>
        </c:if>
        <c:choose>
            <c:when test="${sort == 'recent_read'}"><span class="filter-chip">最近在读</span></c:when>
            <c:when test="${sort == 'title'}"><span class="filter-chip">按书名</span></c:when>
            <c:when test="${sort == 'chapters'}"><span class="filter-chip">按章节数</span></c:when>
            <c:when test="${sort == 'words'}"><span class="filter-chip">按字数</span></c:when>
        </c:choose>
        <a class="active-clear" href="${ctx}/books">清除</a>
    </div>
</c:if>

<form class="shelf-filters" id="shelfFilters" action="${ctx}/books" method="get">
    <div class="filter-row search-row">
        <input class="input" type="text" name="keyword" placeholder="搜索书名或作者"
               value="<c:out value='${keyword}'/>">
        <button type="submit" class="btn">搜索</button>
        <a class="btn ghost" href="${ctx}/books/fulltext">搜正文</a>
    </div>

    <div class="filter-popover" id="filterPopover">
        <div class="filter-group">
            <span class="filter-label">状态</span>
            <label class="seg"><input type="radio" name="shelf" value=""
                   ${empty shelf ? 'checked' : ''}>全部</label>
            <label class="seg"><input type="radio" name="shelf" value="want"
                   ${shelf == 'want' ? 'checked' : ''}>想读</label>
            <label class="seg"><input type="radio" name="shelf" value="reading"
                   ${shelf == 'reading' ? 'checked' : ''}>在读</label>
            <label class="seg"><input type="radio" name="shelf" value="finished"
                   ${shelf == 'finished' ? 'checked' : ''}>读过</label>
        </div>
        <div class="filter-grid">
            <select class="select" name="category_id">
                <option value="">全部分类</option>
                <c:forEach var="cat" items="${categories}">
                    <option value="${cat.id}" ${cat.id == category_id ? 'selected' : ''}>
                        <c:out value="${cat.name}"/>
                    </option>
                </c:forEach>
            </select>
            <select class="select" name="tag">
                <option value="">全部标签</option>
                <c:forEach var="tagItem" items="${tags}">
                    <option value="<c:out value='${tagItem.name}'/>" ${tagItem.name == tag ? 'selected' : ''}>
                        <c:out value="${tagItem.name}"/>
                    </option>
                </c:forEach>
            </select>
            <select class="select" name="sort">
                <option value="updated" ${sort == 'updated' ? 'selected' : ''}>最近添加</option>
                <option value="recent_read" ${sort == 'recent_read' ? 'selected' : ''}>最近在读</option>
                <option value="title" ${sort == 'title' ? 'selected' : ''}>书名</option>
                <option value="chapters" ${sort == 'chapters' ? 'selected' : ''}>章节数</option>
                <option value="words" ${sort == 'words' ? 'selected' : ''}>字数</option>
            </select>
            <select class="select" name="group">
                <option value="">不分组</option>
                <option value="series" ${group == 'series' ? 'selected' : ''}>按系列分组</option>
            </select>
        </div>
        <div class="filter-actions">
            <a class="btn ghost" href="${ctx}/books">重置</a>
            <button type="submit" class="btn">应用</button>
        </div>
    </div>
    <div class="filter-mask" id="filterMask"></div>
</form>

<c:choose>
    <c:when test="${groupMode and empty shelfGroups}">
        <p class="empty">还没有图书</p>
    </c:when>
    <c:when test="${groupMode}">
        <c:forEach var="entry" items="${shelfGroups}">
            <div class="shelf-group">
                <div class="zone-head">
                    <h2 class="zone-title"><c:out value="${entry.key}"/></h2>
                    <span class="muted">${fn:length(entry.value)} 本</span>
                </div>
                <div class="shelf">
                    <c:forEach var="b" items="${entry.value}">
                        <%@ include file="shelf-item.jspf" %>
                    </c:forEach>
                </div>
            </div>
        </c:forEach>
    </c:when>
    <c:when test="${empty pageResult.data}">
        <p class="empty">
            <c:choose>
                <c:when test="${hasFilter}">没有匹配的图书</c:when>
                <c:otherwise>还没有图书</c:otherwise>
            </c:choose>
        </p>
    </c:when>
    <c:otherwise>
        <div class="shelf">
            <c:forEach var="b" items="${pageResult.data}">
                <%@ include file="shelf-item.jspf" %>
            </c:forEach>
        </div>
    </c:otherwise>
</c:choose>

<c:if test="${not groupMode and pageResult.total_pages > 1}">
    <%@ include file="../common/pagination.jspf" %>
</c:if>
    </main>

    <aside class="shelf-rail shelf-rail-right">
        <section class="zone">
            <div class="zone-head">
                <h2 class="zone-title">年度目标</h2>
            </div>
            <c:choose>
                <c:when test="${not empty overview}">
                    <div class="rail-goal">
                        <span class="rail-goal-value">${overview.finished_this_year}<span class="rail-goal-total"> 本</span></span>
                        <span class="rail-goal-label">今年读完</span>
                        <c:if test="${overview.year_goal > 0}">
                            <c:set var="goalPercent"
                                   value="${overview.finished_this_year >= overview.year_goal ? 100 : (overview.finished_this_year * 100 / overview.year_goal)}"/>
                            <span class="goal-bar"><span style="width: ${goalPercent}%"></span></span>
                            <span class="rail-goal-text">目标 ${overview.year_goal} 本 · 完成 ${goalPercent}%</span>
                        </c:if>
                    </div>
                    <c:choose>
                        <c:when test="${overview.year_goal > 0}">
                            <a class="rail-link" href="${ctx}/books/stats">查看阅读统计</a>
                        </c:when>
                        <c:otherwise>
                            <a class="rail-link" href="${ctx}/books/stats">设置年度目标</a>
                        </c:otherwise>
                    </c:choose>
                </c:when>
                <c:otherwise>
                    <p class="rail-empty">还没有阅读数据</p>
                </c:otherwise>
            </c:choose>
        </section>
    </aside>
</div>

<script>
    (function () {
        var form = document.getElementById('shelfFilters');
        var searchToggle = document.getElementById('searchToggle');
        var filterToggle = document.getElementById('filterToggle');
        var mask = document.getElementById('filterMask');
        if (!form || !searchToggle || !filterToggle) return;

        function closeAll() {
            form.classList.remove('search-open', 'filter-open');
        }

        searchToggle.addEventListener('click', function () {
            if (window.matchMedia('(max-width: 768px)').matches) {
                window.location.href = '${ctx}/books/search';
                return;
            }
            form.classList.toggle('search-open');
            if (form.classList.contains('search-open')) {
                form.classList.remove('filter-open');
                var input = form.querySelector('input[name="keyword"]');
                if (input) input.focus();
            }
        });

        filterToggle.addEventListener('click', function () {
            form.classList.toggle('filter-open');
            if (form.classList.contains('filter-open')) {
                form.classList.remove('search-open');
            }
        });

        if (mask) mask.addEventListener('click', closeAll);

        document.addEventListener('click', function (e) {
            if (!form.classList.contains('search-open') && !form.classList.contains('filter-open')) return;
            if (form.contains(e.target) || searchToggle.contains(e.target) || filterToggle.contains(e.target)) return;
            closeAll();
        });
    })();
</script>

<script>
    (function () {
        var batchForm = document.getElementById('batchForm');
        if (!batchForm) return;
        var all = document.getElementById('batchAll');
        var count = document.getElementById('batchCount');
        var boxes = Array.prototype.slice.call(document.querySelectorAll('input[name="ids"][form="batchForm"]'));
        var exitBtn = document.getElementById('batchExit');
        var menu = document.getElementById('shelfMenu');
        var menuBatch = document.getElementById('shelfMenuBatch');
        var menuTarget = null;
        var pressTimer = null;
        var longPressed = false;
        var startX = 0;
        var startY = 0;

        function checkedBoxes() {
            return boxes.filter(function (box) { return box.checked; });
        }

        function update() {
            var picked = checkedBoxes().length;
            if (count) count.textContent = '已选 ' + picked + ' 本';
            if (all) all.checked = boxes.length > 0 && picked === boxes.length;
        }

        function closeMenu() {
            if (menu) menu.hidden = true;
        }

        function openMenu(x, y, slot) {
            if (!menu) return;
            menuTarget = slot;
            menu.hidden = false;
            var width = 150;
            var height = 52;
            menu.style.left = Math.max(8, Math.min(x, window.innerWidth - width - 8)) + 'px';
            menu.style.top = Math.max(8, Math.min(y, window.innerHeight - height - 8)) + 'px';
        }

        function enterBatch(slot) {
            document.body.classList.add('batch-mode');
            if (slot) {
                var box = slot.querySelector('input[name="ids"]');
                if (box) box.checked = true;
            }
            update();
            closeMenu();
        }

        function exitBatch() {
            document.body.classList.remove('batch-mode');
            boxes.forEach(function (box) { box.checked = false; });
            if (all) all.checked = false;
            update();
            closeMenu();
        }

        if (all) {
            all.addEventListener('change', function () {
                boxes.forEach(function (box) { box.checked = all.checked; });
                update();
            });
        }
        boxes.forEach(function (box) { box.addEventListener('change', update); });

        if (exitBtn) exitBtn.addEventListener('click', exitBatch);
        if (menuBatch) {
            menuBatch.addEventListener('click', function () { enterBatch(menuTarget); });
        }

        document.querySelectorAll('.shelf-slot').forEach(function (slot) {
            slot.addEventListener('contextmenu', function (e) {
                if (document.body.classList.contains('batch-mode')) return;
                e.preventDefault();
                openMenu(e.clientX, e.clientY, slot);
            });
            slot.addEventListener('touchstart', function (e) {
                if (e.touches.length !== 1 || document.body.classList.contains('batch-mode')) return;
                var touch = e.touches[0];
                startX = touch.clientX;
                startY = touch.clientY;
                clearTimeout(pressTimer);
                pressTimer = setTimeout(function () {
                    longPressed = true;
                    openMenu(touch.clientX, touch.clientY, slot);
                }, 520);
            }, { passive: true });
            slot.addEventListener('touchmove', function (e) {
                if (!pressTimer) return;
                var touch = e.touches[0];
                if (Math.abs(touch.clientX - startX) > 10 || Math.abs(touch.clientY - startY) > 10) {
                    clearTimeout(pressTimer);
                    pressTimer = null;
                }
            }, { passive: true });
            slot.addEventListener('touchend', function (e) {
                if (pressTimer) {
                    clearTimeout(pressTimer);
                    pressTimer = null;
                }
                if (longPressed) {
                    e.preventDefault();
                    e.stopPropagation();
                    longPressed = false;
                }
            }, { passive: false });
        });

        document.addEventListener('click', function (e) {
            if (!menu || menu.hidden) return;
            if (!menu.contains(e.target)) closeMenu();
        }, true);

        document.addEventListener('click', function (e) {
            if (!document.body.classList.contains('batch-mode')) return;
            var slot = e.target.closest('.shelf-slot');
            if (!slot) return;
            if (e.target.closest('.batch-check')) return;
            e.preventDefault();
            e.stopPropagation();
            var box = slot.querySelector('input[name="ids"]');
            if (box) box.checked = !box.checked;
            update();
        }, true);

        document.addEventListener('keydown', function (e) {
            if (e.key === 'Escape') {
                closeMenu();
                if (document.body.classList.contains('batch-mode')) exitBatch();
            }
        });

        batchForm.addEventListener('submit', function (e) {
            if (checkedBoxes().length === 0) {
                e.preventDefault();
                alert('请先勾选要操作的图书');
            }
        });
        update();        update();
    })();
</script>

<%@ include file="../common/foot.jspf" %>

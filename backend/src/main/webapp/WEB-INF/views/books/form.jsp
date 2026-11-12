<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="pageTitle" value="${empty bookId ? '新增图书' : '编辑图书'}"/>
<c:set var="navActive" value="books"/>
<%@ include file="../common/head.jspf" %>

<div class="card">
    <div class="section-title">${empty bookId ? '新增图书' : '编辑图书'}</div>

    <c:if test="${not empty error}">
        <p class="form-error"><c:out value="${error}"/></p>
    </c:if>

    <form action="${ctx}/books${empty bookId ? '' : '/'.concat(bookId).concat('/edit')}"
          method="post" enctype="multipart/form-data">
        <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
        <input type="hidden" name="cover_url" value="<c:out value='${bookForm.cover_url}'/>">

        <div class="form-row">
            <label class="form-label" for="title">书名</label>
            <input class="input" type="text" id="title" name="title" maxlength="255" required
                   value="<c:out value='${bookForm.title}'/>">
            <c:if test="${not empty errors.title}">
                <p class="field-error"><c:out value="${errors.title}"/></p>
            </c:if>
        </div>

        <div class="form-row">
            <label class="form-label" for="author">作者</label>
            <input class="input" type="text" id="author" name="author" maxlength="255"
                   value="<c:out value='${bookForm.author}'/>">
            <c:if test="${not empty errors.author}">
                <p class="field-error"><c:out value="${errors.author}"/></p>
            </c:if>
        </div>

        <div class="form-row">
            <label class="form-label" for="category_id">分类</label>
            <select class="select" id="category_id" name="category_id">
                <option value="">未分类</option>
                <c:forEach var="cat" items="${categories}">
                    <option value="${cat.id}" ${cat.id == bookForm.category_id ? 'selected' : ''}>
                        <c:out value="${cat.name}"/>
                    </option>
                </c:forEach>
            </select>
        </div>

        <div class="form-row">
            <label class="form-label" for="description">简介</label>
            <textarea class="textarea" id="description" name="description" rows="6" maxlength="2000"><c:out value="${bookForm.description}"/></textarea>
            <c:if test="${not empty errors.description}">
                <p class="field-error"><c:out value="${errors.description}"/></p>
            </c:if>
        </div>

        <div class="form-row">
            <label class="form-label" for="publisher">出版社</label>
            <input class="input" type="text" id="publisher" name="publisher" maxlength="100"
                   value="<c:out value='${bookForm.publisher}'/>">
        </div>

        <div class="form-row">
            <label class="form-label" for="publish_year">出版年</label>
            <input class="input" type="number" id="publish_year" name="publish_year" min="1000" max="2100"
                   style="width: 160px;" value="${bookForm.publish_year}">
        </div>

        <div class="form-row">
            <label class="form-label" for="isbn">ISBN</label>
            <input class="input" type="text" id="isbn" name="isbn" maxlength="32"
                   value="<c:out value='${bookForm.isbn}'/>">
        </div>

        <div class="form-row">
            <label class="form-label" for="source_url">来源链接</label>
            <input class="input" type="text" id="source_url" name="source_url" maxlength="500"
                   placeholder="https://" value="<c:out value='${bookForm.source_url}'/>">
        </div>

        <div class="form-row">
            <label class="form-label" for="tags">标签</label>
            <input class="input" type="text" id="tags" name="tags" maxlength="500"
                   placeholder="如：科幻, 短篇" value="<c:out value='${bookForm.tags}'/>">
            <p class="form-hint">多个标签用逗号分隔，最多 10 个</p>
        </div>

        <div class="form-row">
            <label class="form-label" for="cover">封面</label>

            <div id="coverPreviewWrap" style="margin-bottom: 10px; display: ${empty coverUrl ? 'none' : 'block'};">
                <img class="cover-preview" id="coverPreview" src="<c:out value='${coverUrl}'/>" alt="封面预览">
            </div>

            <div class="cover-search">
                <input class="input" type="text" id="coverKeyword" maxlength="60"
                       placeholder="输入书名或关键词搜索封面"
                       value="<c:out value='${bookForm.title}'/>">
                <button type="button" class="btn ghost" id="coverSearchBtn">搜图</button>
            </div>

            <input class="input" type="file" id="cover" name="cover"
                   accept=".jpg,.jpeg,.png,.gif,.webp,.bmp" style="margin-top: 10px;">
            <p class="form-hint">支持 jpg/png/gif/webp/bmp，最大 5MB；也可上传本地图片，不选择则保留现有封面</p>
        </div>

        <c:if test="${empty bookId}">
            <div class="form-row">
                <label class="form-label" for="bookFile">正文文件（可选）</label>
                <div class="import-upload">
                    <input class="input" type="file" id="bookFile" name="bookFile" accept=".txt,.epub">
                    <button type="button" class="btn ghost" id="parseBookFile">解析预览</button>
                </div>
                <p class="form-hint">支持 .txt / .epub，最大 20MB；不选文件则只创建空书，稍后可在章节管理页导入</p>

                <div class="import-summary" id="importSummary" ${empty importPreview ? 'hidden' : ''}>
                    <span class="import-summary-text" id="importSummaryText">
                        <c:if test="${not empty importPreview}">
                            <c:out value="${importPreview.filename}"/> · 共 ${importPreview.chapter_count} 章 · 约 ${importPreview.total_words} 字
                        </c:if>
                    </span>
                    <button type="button" class="btn small ghost" id="previewChapters">查看/编辑章节</button>
                    <button type="button" class="btn small ghost" id="removeImport">移除</button>
                </div>
                <div class="import-error" id="importError" hidden></div>
                <input type="hidden" name="importToken" id="importToken"
                       value="<c:out value='${importToken}'/>">
            </div>
        </c:if>

        <div class="form-actions">
            <button type="submit" class="btn" id="createBookBtn">${empty bookId ? (empty importPreview ? '创建' : '创建并导入') : '保存'}</button>
            <a class="btn ghost" href="${ctx}/books${empty bookId ? '' : '/'.concat(bookId)}">取消</a>
        </div>
    </form>
</div>

<%@ include file="../common/foot.jspf" %>

<div class="picker-mask" id="coverPicker" hidden>
    <div class="picker">
        <div class="picker-head">
            <span>选择封面</span>
            <button type="button" class="link-btn" id="coverPickerClose">关闭</button>
        </div>
        <div class="picker-body" id="coverPickerBody"></div>
        <p class="picker-foot">图片来自网络搜索，请确认已获得使用授权后再采用</p>
    </div>
</div>

<div class="import-mask" id="importMask" hidden>
    <div class="import-dialog">
        <div class="import-head">
            <span id="importTitle">章节预览</span>
            <button type="button" class="link-btn" id="importClose">关闭</button>
        </div>
        <div class="import-body">
            <div class="import-list" id="importList"></div>
            <div class="import-editor" id="importEditor" hidden>
                <input class="input" type="text" id="chapterTitle" maxlength="255" placeholder="章节标题">
                <textarea class="textarea" id="chapterContent" rows="12" placeholder="章节正文"></textarea>
                <div class="import-editor-actions">
                    <button type="button" class="btn small" id="saveChapter">保存本章</button>
                    <span class="muted" id="chapterWords"></span>
                </div>
            </div>
        </div>
        <div class="import-foot">
            <span class="muted" id="importFootText"></span>
            <button type="button" class="btn" id="importDone">使用此文件</button>
        </div>
    </div>
</div>

<script>
    (function () {
        var btn = document.getElementById('coverSearchBtn');
        if (!btn) return;

        var keywordInput = document.getElementById('coverKeyword');
        var mask = document.getElementById('coverPicker');
        var body = document.getElementById('coverPickerBody');
        var closeBtn = document.getElementById('coverPickerClose');
        var previewWrap = document.getElementById('coverPreviewWrap');
        var preview = document.getElementById('coverPreview');
        var coverUrlField = document.querySelector('input[name="cover_url"]');
        var csrfToken = document.querySelector('input[name="csrfToken"]').value;
        var ctx = '${ctx}';

        function closePicker() {
            mask.hidden = true;
        }

        function openPicker() {
            mask.hidden = false;
        }

        function setCover(url) {
            coverUrlField.value = url;
            preview.src = url;
            previewWrap.style.display = 'block';
        }

        closeBtn.addEventListener('click', closePicker);
        mask.addEventListener('click', function (e) {
            if (e.target === mask) closePicker();
        });
        document.addEventListener('keydown', function (e) {
            if (e.key === 'Escape' && !mask.hidden) closePicker();
        });

        btn.addEventListener('click', function () {
            var keyword = (keywordInput.value || '').trim();
            if (!keyword) {
                alert('请输入书名或关键词');
                return;
            }
            btn.disabled = true;
            btn.textContent = '搜索中...';
            body.innerHTML = '<p class="picker-empty">正在搜索...</p>';
            openPicker();

            fetch(ctx + '/books/cover-search?keyword=' + encodeURIComponent(keyword) + '&limit=12',
                { credentials: 'same-origin' })
                .then(function (r) { return r.json(); })
                .then(function (res) {
                    var items = (res && res.data) || [];
                    body.innerHTML = '';
                    if (!items.length) {
                        body.innerHTML = '<p class="picker-empty">没有搜到图片，换个关键词试试</p>';
                        return;
                    }
                    items.forEach(function (item) {
                        var img = document.createElement('img');
                        img.src = item.thumb_url || item.image_url;
                        img.loading = 'lazy';
                        img.alt = '';
                        img.title = '点击使用这张图';
                        img.addEventListener('click', function () {
                            pick(item.image_url, img);
                        });
                        body.appendChild(img);
                    });
                })
                .catch(function () {
                    body.innerHTML = '<p class="picker-empty">搜索失败，请稍后重试</p>';
                })
                .then(function () {
                    btn.disabled = false;
                    btn.textContent = '搜图';
                });
        });

        function pick(url, img) {
            var params = new URLSearchParams();
            params.append('url', url);
            img.style.opacity = '.4';

            fetch(ctx + '/books/cover-download', {
                method: 'POST',
                credentials: 'same-origin',
                headers: { 'X-CSRF-Token': csrfToken },
                body: params
            }).then(function (r) { return r.json(); }).then(function (res) {
                if (res.code !== 200 || !res.data) {
                    alert(res.message || '下载失败');
                    img.style.opacity = '';
                    return;
                }
                setCover(res.data.fileUrl);
                closePicker();
            }).catch(function () {
                alert('下载失败');
                img.style.opacity = '';
            });
        }
    })();
</script>
<script src="${ctx}/assets/book-import.js" data-ctx="${ctx}"></script>
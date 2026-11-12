(function () {
    var fileInput = document.getElementById('bookFile');
    if (!fileInput) return;

    var script = document.currentScript;
    var ctx = script ? (script.getAttribute('data-ctx') || '') : '';
    var csrf = document.querySelector('input[name="csrfToken"]').value;
    var parseBtn = document.getElementById('parseBookFile');
    var summary = document.getElementById('importSummary');
    var summaryText = document.getElementById('importSummaryText');
    var tokenInput = document.getElementById('importToken');
    var previewBtn = document.getElementById('previewChapters');
    var removeBtn = document.getElementById('removeImport');
    var errorBox = document.getElementById('importError');
    var submitBtn = document.getElementById('createBookBtn');

    var mask = document.getElementById('importMask');
    var listEl = document.getElementById('importList');
    var editor = document.getElementById('importEditor');
    var titleInput = document.getElementById('chapterTitle');
    var contentInput = document.getElementById('chapterContent');
    var wordsLabel = document.getElementById('chapterWords');
    var importTitle = document.getElementById('importTitle');
    var footText = document.getElementById('importFootText');

    var chapters = [];
    var currentIndex = -1;

    function getToken() {
        return (tokenInput.value || '').trim();
    }

    function setError(message) {
        if (!message) {
            errorBox.hidden = true;
            errorBox.textContent = '';
            return;
        }
        errorBox.textContent = message;
        errorBox.hidden = false;
    }

    function updateSummary(data) {
        if (!data) {
            tokenInput.value = '';
            summary.hidden = true;
            summaryText.textContent = '';
            submitBtn.textContent = '创建';
            chapters = [];
            currentIndex = -1;
            return;
        }
        tokenInput.value = data.token;
        summaryText.textContent = data.filename + ' · 共 ' + data.chapter_count + ' 章 · 约 ' + data.total_words + ' 字';
        summary.hidden = false;
        submitBtn.textContent = '创建并导入';
        chapters = data.chapters || [];
    }

    function discardToken(token) {
        if (!token) return;
        var params = new URLSearchParams();
        params.append('csrfToken', csrf);
        fetch(ctx + '/books/import/preview/' + token + '/discard', {
            method: 'POST',
            credentials: 'same-origin',
            headers: { 'X-CSRF-Token': csrf },
            body: params
        }).catch(function () {
        });
    }

    function resetImport() {
        setError('');
        fileInput.value = '';
        updateSummary(null);
    }

    fileInput.addEventListener('change', function () {
        setError('');
        if (getToken()) {
            discardToken(getToken());
            updateSummary(null);
        }
    });

    parseBtn.addEventListener('click', function () {
        var file = fileInput.files && fileInput.files[0];
        if (!file) {
            setError('请先选择文件');
            return;
        }
        if (getToken()) {
            discardToken(getToken());
            updateSummary(null);
        }
        setError('');
        parseBtn.disabled = true;
        parseBtn.textContent = '解析中…';
        var formData = new FormData();
        formData.append('file', file);
        fetch(ctx + '/books/import/preview', {
            method: 'POST',
            credentials: 'same-origin',
            headers: { 'X-CSRF-Token': csrf },
            body: formData
        }).then(function (r) {
            return r.json();
        }).then(function (res) {
            if (res.code !== 200) {
                setError(res.message || '解析失败');
                return;
            }
            parseBtn.textContent = '重新解析';
            updateSummary(res.data);
        }).catch(function () {
            setError('解析失败，请稍后重试');
        }).then(function () {
            parseBtn.disabled = false;
            if (parseBtn.textContent === '解析中…') {
                parseBtn.textContent = '解析预览';
            }
        });
    });

    removeBtn.addEventListener('click', function () {
        discardToken(getToken());
        resetImport();
        parseBtn.textContent = '解析预览';
    });

    function openModal() {
        mask.hidden = false;
        importTitle.textContent = '章节预览（共 ' + chapters.length + ' 章）';
        footText.textContent = '共 ' + chapters.length + ' 章';
        renderList();
        if (chapters.length) {
            selectChapter(0);
        } else {
            editor.hidden = true;
        }
    }

    function renderList() {
        listEl.innerHTML = '';
        chapters.forEach(function (chapter, index) {
            var row = document.createElement('div');
            row.className = 'import-chapter' + (index === currentIndex ? ' active' : '');

            var main = document.createElement('button');
            main.type = 'button';
            main.className = 'import-chapter-main';
            var title = document.createElement('span');
            title.className = 'import-chapter-title';
            title.textContent = chapter.title;
            var meta = document.createElement('span');
            meta.className = 'import-chapter-meta';
            meta.textContent = (index + 1) + ' · ' + chapter.word_count + ' 字';
            main.appendChild(title);
            main.appendChild(meta);
            main.addEventListener('click', function () {
                selectChapter(index);
            });

            var del = document.createElement('button');
            del.type = 'button';
            del.className = 'import-chapter-del';
            del.textContent = '×';
            del.title = '删除本章';
            del.addEventListener('click', function () {
                deleteChapter(index);
            });

            row.appendChild(main);
            row.appendChild(del);
            listEl.appendChild(row);
        });
    }

    function selectChapter(index) {
        if (index < 0 || index >= chapters.length) return;
        currentIndex = index;
        renderList();
        var row = listEl.children[index];
        if (row) row.scrollIntoView({ block: 'nearest' });
        fetch(ctx + '/books/import/preview/' + getToken() + '/chapters/' + index, {
            credentials: 'same-origin'
        }).then(function (r) {
            return r.json();
        }).then(function (res) {
            if (res.code !== 200) {
                alert(res.message || '读取章节失败');
                return;
            }
            editor.hidden = false;
            titleInput.value = res.data.title;
            contentInput.value = res.data.content;
            wordsLabel.textContent = res.data.word_count + ' 字';
        }).catch(function () {
            alert('读取章节失败');
        });
    }

    document.getElementById('saveChapter').addEventListener('click', function () {
        if (currentIndex < 0) return;
        var params = new URLSearchParams();
        params.append('csrfToken', csrf);
        params.append('title', titleInput.value);
        params.append('content', contentInput.value);
        fetch(ctx + '/books/import/preview/' + getToken() + '/chapters/' + currentIndex, {
            method: 'POST',
            credentials: 'same-origin',
            headers: { 'X-CSRF-Token': csrf },
            body: params
        }).then(function (r) {
            return r.json();
        }).then(function (res) {
            if (res.code !== 200) {
                alert(res.message || '保存失败');
                return;
            }
            updateSummary(res.data);
            renderList();
            importTitle.textContent = '章节预览（共 ' + chapters.length + ' 章）';
            if (chapters[currentIndex]) {
                wordsLabel.textContent = chapters[currentIndex].word_count + ' 字';
            }
        }).catch(function () {
            alert('保存失败');
        });
    });

    function deleteChapter(index) {
        if (!confirm('确定删除这一章吗？')) return;
        var params = new URLSearchParams();
        params.append('csrfToken', csrf);
        fetch(ctx + '/books/import/preview/' + getToken() + '/chapters/' + index + '/delete', {
            method: 'POST',
            credentials: 'same-origin',
            headers: { 'X-CSRF-Token': csrf },
            body: params
        }).then(function (r) {
            return r.json();
        }).then(function (res) {
            if (res.code !== 200) {
                alert(res.message || '删除失败');
                return;
            }
            updateSummary(res.data);
            importTitle.textContent = '章节预览（共 ' + chapters.length + ' 章）';
            footText.textContent = '共 ' + chapters.length + ' 章';
            currentIndex = -1;
            renderList();
            if (chapters.length) {
                selectChapter(0);
            } else {
                editor.hidden = true;
                titleInput.value = '';
                contentInput.value = '';
                wordsLabel.textContent = '';
            }
        }).catch(function () {
            alert('删除失败');
        });
    }

    previewBtn.addEventListener('click', function () {
        if (!getToken()) return;
        setError('');
        fetch(ctx + '/books/import/preview/' + getToken(), {
            credentials: 'same-origin'
        }).then(function (r) {
            return r.json();
        }).then(function (res) {
            if (res.code !== 200) {
                setError(res.message || '预览已失效');
                return;
            }
            updateSummary(res.data);
            openModal();
        }).catch(function () {
            setError('读取预览失败');
        });
    });

    document.getElementById('importClose').addEventListener('click', function () {
        mask.hidden = true;
    });
    document.getElementById('importDone').addEventListener('click', function () {
        mask.hidden = true;
    });
    mask.addEventListener('click', function (e) {
        if (e.target === mask) mask.hidden = true;
    });
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape' && !mask.hidden) mask.hidden = true;
    });
})();

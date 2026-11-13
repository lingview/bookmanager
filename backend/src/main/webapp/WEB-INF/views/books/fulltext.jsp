<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="pageTitle" value="全文搜索"/>
<c:set var="navActive" value="books"/>
<%@ include file="../common/head.jspf" %>

<form class="toolbar fulltext-bar" action="${ctx}/books/fulltext" method="get">
    <input class="input" type="text" name="q" placeholder="搜索所有书籍的章节正文" maxlength="50"
           value="<c:out value='${q}'/>">
    <select class="select" name="category_id">
        <option value="">全部分类</option>
        <c:forEach var="cat" items="${categories}">
            <option value="${cat.id}" ${cat.id == category_id ? 'selected' : ''}>
                <c:out value="${cat.name}"/>
            </option>
        </c:forEach>
    </select>
    <select class="select" name="book_id">
        <option value="">全部图书</option>
        <c:forEach var="fb" items="${filterBooks}">
            <option value="${fb.id}" ${fb.id == book_id ? 'selected' : ''}>
                <c:out value="${fb.title}"/>
            </option>
        </c:forEach>
    </select>
    <button type="submit" class="btn">搜索</button>
    <a class="btn ghost" href="${ctx}/books">返回书架</a>
</form>

<c:url var="searchUrl" value="/books/search">
    <c:if test="${not empty q}">
        <c:param name="q" value="${q}"/>
    </c:if>
</c:url>
<div class="search-scope">
    <a class="scope-tab" href="${searchUrl}">书名/作者</a>
    <a class="scope-tab active" href="${ctx}/books/fulltext">全站正文</a>
</div>

<c:choose>
    <c:when test="${empty q}">
        <p class="empty">输入关键词，搜索所有书籍的章节标题和正文</p>
    </c:when>
    <c:when test="${pageResult.total == 0}">
        <p class="empty">没有找到匹配内容</p>
    </c:when>
    <c:otherwise>
        <p class="muted" style="margin-bottom: 10px;">共 ${pageResult.total} 条结果</p>
        <div class="fulltext-results">
            <c:forEach var="hit" items="${pageResult.data}">
                <c:url var="hitUrl" value="/books/${hit.book_id}/read">
                    <c:param name="chapterId" value="${hit.chapter_id}"/>
                    <c:param name="q" value="${q}"/>
                </c:url>
                <div class="fulltext-hit">
                    <a class="fulltext-title" href="${hitUrl}">
                        <c:out value="${hit.book_title}"/> · <c:out value="${hit.chapter_title}"/>
                    </a>
                    <p class="fulltext-snippet"><c:out value="${hit.snippet}"/></p>
                </div>
            </c:forEach>
        </div>
        <c:if test="${pageResult.total_pages > 1}">
            <%@ include file="../common/pagination.jspf" %>
        </c:if>
    </c:otherwise>
</c:choose>

<script>
    (function () {
        var input = document.querySelector('input[name="q"]');
        var keyword = input ? input.value.trim() : '';
        if (!keyword) return;
        var needle = keyword.toLowerCase();

        function highlight(el) {
            var text = el.textContent || '';
            var lower = text.toLowerCase();
            var index = lower.indexOf(needle);
            if (index < 0) return;
            var fragment = document.createDocumentFragment();
            var position = 0;
            while (index >= 0) {
                if (index > position) {
                    fragment.appendChild(document.createTextNode(text.slice(position, index)));
                }
                var mark = document.createElement('mark');
                mark.className = 'search-hl';
                mark.textContent = text.slice(index, index + keyword.length);
                fragment.appendChild(mark);
                position = index + keyword.length;
                index = lower.indexOf(needle, position);
            }
            if (position < text.length) {
                fragment.appendChild(document.createTextNode(text.slice(position)));
            }
            el.textContent = '';
            el.appendChild(fragment);
        }

        document.querySelectorAll('.fulltext-title, .fulltext-snippet').forEach(highlight);
    })();
</script>

<%@ include file="../common/foot.jspf" %>

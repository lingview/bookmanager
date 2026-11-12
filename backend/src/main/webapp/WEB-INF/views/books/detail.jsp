<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="pageTitle" value="${book.title}"/>
<c:set var="navActive" value="books"/>
<%@ include file="../common/head.jspf" %>

<div class="reader">
    <div class="card detail-cover-card">
        <div class="cover detail-cover">
            <c:choose>
                <c:when test="${not empty book.cover_url}">
                    <img src="<c:out value='${book.cover_url}'/>" alt="">
                </c:when>
                <c:otherwise>无封面</c:otherwise>
            </c:choose>
        </div>
    </div>

    <div class="stack">
        <div class="card">
            <h1 class="chapter-title"><c:out value="${book.title}"/></h1>
            <div class="muted">
                <c:out value="${book.author}"/>
                <c:if test="${not empty book.author and not empty book.category_name}"> · </c:if>
                <c:out value="${book.category_name}"/>
                · ${book.chapter_count} 章
                · 更新于 ${book.update_time}
            </div>
            <c:if test="${not empty book.tag_names}">
                <div class="tag-row">
                    <c:forTokens items="${book.tag_names}" delims="," var="tagName">
                        <span class="chip"><c:out value="${tagName}"/></span>
                    </c:forTokens>
                </div>
            </c:if>
            <c:if test="${not empty book.publisher or not empty book.publish_year or not empty book.isbn}">
                <div class="muted" style="margin-top: 6px;">
                    <c:if test="${not empty book.publisher}">出版社：<c:out value="${book.publisher}"/></c:if>
                    <c:if test="${not empty book.publisher and not empty book.publish_year}"> · </c:if>
                    <c:if test="${not empty book.publish_year}">${book.publish_year} 年</c:if>
                    <c:if test="${not empty book.isbn}">
                        <c:if test="${not empty book.publisher or not empty book.publish_year}"> · </c:if>
                        ISBN：<c:out value="${book.isbn}"/>
                    </c:if>
                    <c:if test="${not empty book.source_url}">
                        · <a href="<c:out value='${book.source_url}'/>" target="_blank" rel="noopener noreferrer">来源链接</a>
                    </c:if>
                </div>
            </c:if>

            <c:if test="${not empty book.description}">
                <div class="text-block"><c:out value="${book.description}"/></div>
            </c:if>

            <div class="actions-row">
                <c:choose>
                    <c:when test="${not empty book.progress_chapter_id}">
                        <a class="btn" href="${ctx}/books/${book.id}/read?chapterId=${book.progress_chapter_id}">
                            继续阅读 · <c:out value="${book.progress_chapter_title}"/>
                        </a>
                    </c:when>
                    <c:when test="${not empty toc}">
                        <a class="btn" href="${ctx}/books/${book.id}/read">开始阅读</a>
                    </c:when>
                </c:choose>

                <c:if test="${canManage}">
                    <a class="btn ghost" href="${ctx}/books/${book.id}/edit">编辑</a>
                    <a class="btn ghost" href="${ctx}/books/${book.id}/chapters">管理章节</a>
                    <form action="${ctx}/books/${book.id}/delete" method="post" class="inline"
                          onsubmit="return confirm('确定删除这本书吗？');">
                        <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                        <button type="submit" class="btn danger">删除</button>
                    </form>
                </c:if>
            </div>
        </div>

        <div class="card">
            <div class="section-title">我的阅读</div>
            <form action="${ctx}/books/${book.id}/my-reading" method="post">
                <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                <div class="form-row">
                    <span class="form-label">阅读状态</span>
                    <div class="choice-row">
                        <label class="choice"><input type="radio" name="status" value=""
                               ${empty book.shelf_status ? 'checked' : ''}> 未标记</label>
                        <label class="choice"><input type="radio" name="status" value="want"
                               ${book.shelf_status == 'want' ? 'checked' : ''}> 想读</label>
                        <label class="choice"><input type="radio" name="status" value="reading"
                               ${book.shelf_status == 'reading' ? 'checked' : ''}> 在读</label>
                        <label class="choice"><input type="radio" name="status" value="finished"
                               ${book.shelf_status == 'finished' ? 'checked' : ''}> 读过</label>
                    </div>
                </div>
                <div class="form-row">
                    <span class="form-label">我的评分</span>
                    <div class="rating" id="ratingStars">
                        <input type="hidden" name="rating" id="ratingValue"
                               value="${empty book.my_rating ? 0 : book.my_rating}">
                        <button type="button" class="star" data-value="1">★</button>
                        <button type="button" class="star" data-value="2">★</button>
                        <button type="button" class="star" data-value="3">★</button>
                        <button type="button" class="star" data-value="4">★</button>
                        <button type="button" class="star" data-value="5">★</button>
                        <span class="muted" id="ratingText"></span>
                    </div>
                </div>
                <div class="form-row">
                    <label class="form-label" for="review">短评</label>
                    <textarea class="textarea" id="review" name="review" rows="3" maxlength="1000"
                              placeholder="写点感受……"><c:out value="${book.my_review}"/></textarea>
                </div>
                <div class="form-actions">
                    <button type="submit" class="btn">保存</button>
                </div>
            </form>
        </div>

        <div class="card">
            <div class="section-title">目录</div>
            <c:choose>
                <c:when test="${empty toc}">
                    <p class="muted">还没有章节</p>
                </c:when>
                <c:otherwise>
                    <div class="toc toc-card" style="max-height: 420px;">
                        <c:forEach var="chapter" items="${toc}">
                            <a href="${ctx}/books/${book.id}/read?chapterId=${chapter.id}">
                                <c:out value="${chapter.title}"/>
                            </a>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<script>
    (function () {
        var wrap = document.getElementById('ratingStars');
        if (!wrap) return;
        var input = document.getElementById('ratingValue');
        var text = document.getElementById('ratingText');
        var stars = Array.prototype.slice.call(wrap.querySelectorAll('.star'));

        function paint(value) {
            stars.forEach(function (star, index) {
                star.classList.toggle('on', index < value);
            });
            text.textContent = value > 0 ? value + ' 星' : '未评分';
        }

        stars.forEach(function (star) {
            star.addEventListener('click', function () {
                var value = parseInt(star.getAttribute('data-value'), 10);
                if (parseInt(input.value, 10) === value) {
                    input.value = 0;
                    paint(0);
                } else {
                    input.value = value;
                    paint(value);
                }
            });
        });

        paint(parseInt(input.value, 10) || 0);
    })();
</script>

<%@ include file="../common/foot.jspf" %>
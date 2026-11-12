<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="pageTitle" value="阅读"/>
<c:set var="navActive" value="reading"/>
<%@ include file="common/head.jspf" %>

<section class="zone">
    <div class="zone-head">
        <h2 class="zone-title">继续阅读</h2>
    </div>
    <c:choose>
        <c:when test="${empty recentBooks}">
            <p class="empty">还没有阅读记录，去书架挑一本开始吧</p>
        </c:when>
        <c:otherwise>
            <div class="continue-list">
                <c:forEach var="rb" items="${recentBooks}">
                    <a class="continue-item" href="${ctx}/books/${rb.id}/read?chapterId=${rb.progress_chapter_id}">
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
        <h2 class="zone-title">阅读数据</h2>
        <c:if test="${empty overview or overview.year_goal <= 0}">
            <a class="zone-link" href="${ctx}/books/stats">设置年度目标</a>
        </c:if>
    </div>
    <c:choose>
        <c:when test="${not empty overview and (overview.total_seconds > 0 or overview.reading_book_count > 0 or overview.finished_this_year > 0)}">
            <div class="data-strip">
                <div class="data-item">
                    <span class="data-label">本周阅读</span>
                    <strong class="data-value">${overview.week_text}</strong>
                </div>
                <div class="data-item">
                    <span class="data-label">累计阅读</span>
                    <strong class="data-value">${overview.total_text}</strong>
                </div>
                <div class="data-item">
                    <span class="data-label">连续阅读</span>
                    <strong class="data-value">${overview.streak_days} 天</strong>
                </div>
                <div class="data-item">
                    <span class="data-label">在读</span>
                    <strong class="data-value">${overview.reading_book_count} 本</strong>
                </div>
                <div class="data-item">
                    <span class="data-label">今年读完</span>
                    <strong class="data-value">${overview.finished_this_year} 本</strong>
                </div>
                <c:if test="${overview.year_goal > 0}">
                    <div class="data-item">
                        <span class="data-label">年度目标</span>
                        <strong class="data-value">${overview.finished_this_year} / ${overview.year_goal}</strong>
                    </div>
                </c:if>
            </div>
            <c:if test="${overview.year_goal > 0}">
                <c:set var="goalPercent"
                       value="${overview.finished_this_year >= overview.year_goal ? 100 : (overview.finished_this_year * 100 / overview.year_goal)}"/>
                <div class="goal-row">
                    <span class="goal-bar"><span style="width: ${goalPercent}%"></span></span>
                    <span class="goal-text">完成 ${goalPercent}%</span>
                </div>
            </c:if>
        </c:when>
        <c:otherwise>
            <p class="empty">开始阅读后，这里会显示每周时长、连续天数、年度目标等数据</p>
        </c:otherwise>
    </c:choose>
</section>

<%@ include file="common/foot.jspf" %>

<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="pageTitle" value="图书统计"/>
<c:set var="navActive" value="stats"/>
<%@ include file="../common/head.jspf" %>

<style>
    .stats-cards {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
        gap: 16px;
        margin-bottom: 20px;
    }

    .stat-card {
        background: #fff;
        border: 1px solid #ebeef5;
        border-radius: 10px;
        padding: 18px 20px;
    }

    .stat-label { color: #909399; font-size: 13px; }

    .stat-value {
        margin-top: 8px;
        font-size: 24px;
        font-weight: 600;
        color: #303133;
        line-height: 1.2;
        font-variant-numeric: tabular-nums;
    }

    .stat-sub { margin-top: 6px; color: #c0c4cc; font-size: 12px; }

    .stats-grid {
        display: grid;
        grid-template-columns: repeat(2, minmax(0, 1fr));
        gap: 16px;
        margin-bottom: 20px;
    }

    .chart-card { padding: 18px 20px 10px; }

    .chart-title { font-size: 14px; font-weight: 600; color: #303133; margin-bottom: 8px; }

    .chart { width: 100%; height: 300px; }

    .data-table { width: 100%; border-collapse: collapse; }

    .data-table th, .data-table td {
        padding: 10px 12px;
        border-bottom: 1px solid #ebeef5;
        text-align: left;
        font-size: 13px;
    }

    .data-table th { color: #909399; font-weight: 500; }
    .data-table td.num, .data-table th.num { text-align: right; font-variant-numeric: tabular-nums; }
    .data-table tr:last-child td { border-bottom: none; }

    html.theme-dark .stat-card { background: #232328; border-color: #2f2f35; }
    html.theme-dark .stat-value,
    html.theme-dark .chart-title { color: #e8e8ea; }
    html.theme-dark .stat-label { color: #8a8b90; }
    html.theme-dark .stat-sub { color: #6b6c70; }
    html.theme-dark .data-table th,
    html.theme-dark .data-table td { border-color: #2f2f35; }
    html.theme-dark .data-table th { color: #8a8b90; }

    .stats-switch {
        display: inline-flex;
        align-items: center;
        gap: 2px;
        padding: 3px;
        background: #eef1f6;
        border-radius: 8px;
        margin-bottom: 18px;
    }

    .stats-scope {
        border: none;
        background: none;
        padding: 6px 18px;
        border-radius: 6px;
        font-size: 13px;
        color: #606266;
        cursor: pointer;
        font-family: inherit;
    }

    .stats-scope.active {
        background: #fff;
        color: #303133;
        font-weight: 600;
        box-shadow: 0 1px 3px rgba(0, 0, 0, .08);
    }

    .stats-scope:focus-visible { outline: 2px solid #409eff; outline-offset: 1px; }

    html.theme-dark .stats-switch { background: #2a2a30; }
    html.theme-dark .stats-scope { color: #a8a9ad; }
    html.theme-dark .stats-scope.active {
        background: #3a3a42;
        color: #e8e8ea;
        box-shadow: 0 1px 3px rgba(0, 0, 0, .4);
    }

    @media (max-width: 900px) {
        .stats-grid { grid-template-columns: minmax(0, 1fr); }
    }

    @media (max-width: 768px) {
        .stats-cards { grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px; }
        .stat-card { padding: 14px; }
        .stat-value { font-size: 20px; }
        .chart { height: 260px; }
        .data-table th, .data-table td { padding: 8px; }
        .stats-scope { padding: 6px 14px; }
    }
</style>

<div class="stats-switch">
    <button type="button" class="stats-scope active" id="scopeGlobal">全站</button>
    <button type="button" class="stats-scope" id="scopeMine">我的</button>
</div>

<input type="hidden" id="csrfTokenHolder" value="<c:out value='${csrfToken}'/>">

<div id="globalView">
<div class="stats-cards">
    <div class="stat-card">
        <div class="stat-label">在架图书</div>
        <div class="stat-value" id="bookTotal">-</div>
        <div class="stat-sub" id="bookSub"></div>
    </div>
    <div class="stat-card">
        <div class="stat-label">章节总数</div>
        <div class="stat-value" id="chapterTotal">-</div>
        <div class="stat-sub" id="chapterSub"></div>
    </div>
    <div class="stat-card">
        <div class="stat-label">累计字数</div>
        <div class="stat-value" id="wordTotal">-</div>
        <div class="stat-sub" id="wordSub"></div>
    </div>
    <div class="stat-card">
        <div class="stat-label">累计阅读时长</div>
        <div class="stat-value" id="readTotal">-</div>
        <div class="stat-sub" id="readSub"></div>
    </div>
    <div class="stat-card">
        <div class="stat-label">阅读人数</div>
        <div class="stat-value" id="readerCount">-</div>
        <div class="stat-sub" id="readerSub"></div>
    </div>
    <div class="stat-card">
        <div class="stat-label">平均完读率</div>
        <div class="stat-value" id="completeRate">-</div>
        <div class="stat-sub">按用户已读章节占比</div>
    </div>
</div>

<div class="stats-grid">
    <section class="card chart-card">
        <div class="chart-title">近 14 天阅读时长（分钟）</div>
        <div class="chart" id="trendChart"></div>
    </section>
    <section class="card chart-card">
        <div class="chart-title">图书字数 Top 10</div>
        <div class="chart" id="topBooksChart"></div>
    </section>
    <section class="card chart-card">
        <div class="chart-title">分类字数占比</div>
        <div class="chart" id="categoryChart"></div>
    </section>
    <section class="card chart-card">
        <div class="chart-title">章节字数分布</div>
        <div class="chart" id="bucketChart"></div>
    </section>
</div>

<section class="card">
    <div class="chart-title">分类明细</div>
    <table class="data-table">
        <thead>
        <tr>
            <th>分类</th>
            <th class="num">图书数</th>
            <th class="num">章节数</th>
            <th class="num">字数</th>
        </tr>
        </thead>
        <tbody id="categoryTable"></tbody>
    </table>
    <p class="empty" id="categoryEmpty" style="display:none;">暂无分类数据</p>
</section>
</div>

<div id="personalView" hidden>
    <div class="stats-cards">
        <div class="stat-card">
            <div class="stat-label">累计阅读</div>
            <div class="stat-value" id="pTotal">-</div>
            <div class="stat-sub" id="pTotalSub"></div>
        </div>
        <div class="stat-card">
            <div class="stat-label">本周阅读</div>
            <div class="stat-value" id="pWeek">-</div>
            <div class="stat-sub">从周一开始计算</div>
        </div>
        <div class="stat-card">
            <div class="stat-label">阅读天数</div>
            <div class="stat-value" id="pDays">-</div>
            <div class="stat-sub" id="pStreak"></div>
        </div>
        <div class="stat-card">
            <div class="stat-label">读完的书</div>
            <div class="stat-value" id="pFinished">-</div>
            <div class="stat-sub" id="pYearFinished"></div>
        </div>
        <div class="stat-card">
            <div class="stat-label">年度目标</div>
            <div class="stat-value" id="pGoal">-</div>
            <div class="stat-sub" id="pGoalSub"></div>
        </div>
        <div class="stat-card">
            <div class="stat-label">设置年度目标</div>
            <div class="toolbar" style="margin: 8px 0 0;">
                <input class="input" type="number" id="goalInput" min="0" max="9999"
                       style="width: 100px;" placeholder="本数">
                <button class="btn small" id="goalSave" type="button">保存</button>
            </div>
        </div>
    </div>

    <div class="stats-grid">
        <section class="card chart-card">
            <div class="chart-title">阅读日历（最近一年）</div>
            <div class="chart" id="heatmapChart" style="height: 240px;"></div>
        </section>
        <section class="card chart-card">
            <div class="chart-title">我的分类偏好</div>
            <div class="chart" id="personalCategoryChart"></div>
        </section>
    </div>

    <section class="card" style="margin-bottom: 20px;">
        <div class="chart-title">年度小结 <span class="muted" id="reportYear"></span></div>
        <p class="muted" id="yearSummary"></p>
    </section>

    <section class="card">
        <div class="chart-title">读完的书</div>
        <table class="data-table">
            <thead>
            <tr>
                <th>书名</th>
                <th>作者</th>
                <th>读完时间</th>
            </tr>
            </thead>
            <tbody id="finishedList"></tbody>
        </table>
        <p class="empty" id="finishedEmpty" style="display: none;">还没有标记为"读过"的书</p>
    </section>
</div>

<script src="${ctx}/echarts.min.js?v=5.5.1"></script>
<script>
    (function () {
        var numberFormat = new Intl.NumberFormat('zh-CN');

        function compact(n) {
            n = Number(n) || 0;
            if (n >= 100000000) return (n / 100000000).toFixed(2) + ' 亿';
            if (n >= 10000) return (n / 10000).toFixed(2) + ' 万';
            return numberFormat.format(n);
        }

        function duration(seconds) {
            seconds = Math.round(Number(seconds) || 0);
            var h = Math.floor(seconds / 3600);
            var m = Math.floor(seconds % 3600 / 60);
            if (h > 0) return h + ' 小时 ' + m + ' 分';
            if (m > 0) return m + ' 分钟';
            return seconds + ' 秒';
        }

        function dailyMinutes(item) {
            return Math.round((Number(item.seconds) || 0) / 6) / 10;
        }

        function makeChart(id) {
            return echarts.init(document.getElementById(id), null, { renderer: 'canvas' });
        }

        function chartColors() {
            var dark = document.documentElement.classList.contains('theme-dark');
            return {
                dark: dark,
                axis: dark ? '#3a3a41' : '#dcdfe6',
                label: dark ? '#8a8b90' : '#909399',
                labelStrong: dark ? '#c8c9cc' : '#606266',
                split: dark ? '#2f2f35' : '#f2f3f5',
                surface: dark ? '#232328' : '#fff',
                empty: dark ? '#6b6c70' : '#c0c4cc'
            };
        }

        function emptyGraphic(text) {
            return [{
                type: 'text',
                left: 'center',
                top: 'middle',
                style: { text: text, fill: chartColors().empty, fontSize: 13 }
            }];
        }

        function renderCards(data) {
            document.getElementById('bookTotal').textContent = numberFormat.format(data.book_active || 0);
            document.getElementById('bookSub').textContent =
                (data.book_deleted || 0) > 0
                    ? '已删除 ' + data.book_deleted + ' 本，不计入统计'
                    : '共 ' + (data.book_total || 0) + ' 本';

            document.getElementById('chapterTotal').textContent = numberFormat.format(data.chapter_total || 0);
            document.getElementById('chapterSub').textContent = '分类 ' + (data.category_total || 0) + ' 个';

            document.getElementById('wordTotal').textContent = compact(data.word_total);
            document.getElementById('wordSub').textContent =
                '章节平均 ' + numberFormat.format(data.avg_chapter_words || 0) + ' 字';

            document.getElementById('readTotal').textContent = duration(data.reading_seconds_total);
            document.getElementById('readSub').textContent = '累计 ' + (data.book_read_count || 0) + ' 本被阅读';

            document.getElementById('readerCount').textContent = numberFormat.format(data.reader_count || 0);
            document.getElementById('readerSub').textContent = '有阅读记录的账号';

            document.getElementById('completeRate').textContent = (data.complete_rate || 0).toFixed(1) + '%';
        }

        function renderTrend(chart, items) {
            var dates = [];
            var values = [];
            (items || []).forEach(function (item) {
                dates.push(item.stat_date ? item.stat_date.substring(5) : '');
                values.push(dailyMinutes(item));
            });
            var total = values.reduce(function (a, b) { return a + b; }, 0);
            var c = chartColors();
            chart.setOption({
                tooltip: { trigger: 'axis', valueFormatter: function (v) { return v + ' 分钟'; } },
                grid: { left: 40, right: 16, top: 24, bottom: 28 },
                xAxis: {
                    type: 'category',
                    data: dates,
                    axisLine: { lineStyle: { color: c.axis } },
                    axisLabel: { color: c.label }
                },
                yAxis: {
                    type: 'value',
                    axisLabel: { color: c.label },
                    splitLine: { lineStyle: { color: c.split } }
                },
                series: [{
                    type: 'line',
                    data: values,
                    smooth: true,
                    symbol: 'circle',
                    symbolSize: 6,
                    itemStyle: { color: '#409eff' },
                    lineStyle: { width: 2, color: '#409eff' },
                    areaStyle: {
                        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                            { offset: 0, color: 'rgba(64,158,255,0.22)' },
                            { offset: 1, color: 'rgba(64,158,255,0.02)' }
                        ])
                    }
                }],
                graphic: total === 0 ? emptyGraphic('暂无阅读时长记录') : []
            });
        }

        function renderTopBooks(chart, items) {
            items = items || [];
            if (!items.length) {
                chart.setOption({ graphic: emptyGraphic('暂无图书数据') });
                return;
            }
            var reversed = items.slice().reverse();
            var c = chartColors();
            chart.setOption({
                tooltip: {
                    trigger: 'axis',
                    axisPointer: { type: 'shadow' },
                    valueFormatter: function (v) { return compact(v) + ' 字'; }
                },
                grid: { left: 10, right: 70, top: 16, bottom: 16, containLabel: true },
                xAxis: {
                    type: 'value',
                    axisLabel: { color: c.label, formatter: compact },
                    splitLine: { lineStyle: { color: c.split } }
                },
                yAxis: {
                    type: 'category',
                    data: reversed.map(function (b) { return b.title; }),
                    axisLine: { lineStyle: { color: c.axis } },
                    axisLabel: { color: c.labelStrong, width: 110, overflow: 'truncate' }
                },
                series: [{
                    type: 'bar',
                    data: reversed.map(function (b) { return b.word_count; }),
                    barMaxWidth: 16,
                    itemStyle: { color: '#409eff', borderRadius: [0, 4, 4, 0] },
                    label: {
                        show: true,
                        position: 'right',
                        color: c.label,
                        fontSize: 11,
                        formatter: function (p) { return compact(p.value); }
                    }
                }]
            });
        }

        function renderCategory(chart, items) {
            items = (items || []).filter(function (c) { return c.word_count > 0; });
            if (!items.length) {
                chart.setOption({ graphic: emptyGraphic('暂无分类字数数据') });
                return;
            }
            var c = chartColors();
            chart.setOption({
                tooltip: { trigger: 'item', formatter: function (p) { return p.name + '：' + compact(p.value) + ' 字（' + p.percent + '%）'; } },
                legend: { bottom: 0, type: 'scroll', textStyle: { color: c.labelStrong, fontSize: 12 } },
                color: ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#9b8afb', '#36cfc9', '#ff9e6d'],
                series: [{
                    type: 'pie',
                    radius: ['38%', '62%'],
                    center: ['50%', '44%'],
                    avoidLabelOverlap: true,
                    itemStyle: { borderColor: c.surface, borderWidth: 2 },
                    label: { formatter: '{b}\n{d}%', color: c.labelStrong, fontSize: 12 },
                    data: items.map(function (item) { return { name: item.name, value: item.word_count }; })
                }]
            });
        }

        function renderBuckets(chart, items) {
            items = items || [];
            var total = items.reduce(function (sum, b) { return sum + (b.item_count || 0); }, 0);
            if (total === 0) {
                chart.setOption({ graphic: emptyGraphic('暂无章节数据') });
                return;
            }
            var c = chartColors();
            chart.setOption({
                tooltip: { trigger: 'item', formatter: '{b}：{c} 章（{d}%）' },
                legend: { bottom: 0, textStyle: { color: c.labelStrong, fontSize: 12 } },
                color: ['#909399', '#409eff', '#67c23a', '#e6a23c'],
                series: [{
                    type: 'pie',
                    radius: ['38%', '62%'],
                    center: ['50%', '44%'],
                    itemStyle: { borderColor: c.surface, borderWidth: 2 },
                    label: { formatter: '{b}\n{c} 章', color: c.labelStrong, fontSize: 12 },
                    data: items.map(function (item) { return { name: item.label, value: item.item_count }; })
                }]
            });
        }

        function renderTable(items) {
            var body = document.getElementById('categoryTable');
            var empty = document.getElementById('categoryEmpty');
            body.innerHTML = '';
            items = items || [];
            if (!items.length) {
                empty.style.display = '';
                return;
            }
            empty.style.display = 'none';
            items.forEach(function (c) {
                var tr = document.createElement('tr');
                [c.name, numberFormat.format(c.book_count || 0),
                    numberFormat.format(c.chapter_count || 0), compact(c.word_count)]
                    .forEach(function (text, index) {
                        var td = document.createElement('td');
                        td.textContent = text;
                        if (index > 0) td.className = 'num';
                        tr.appendChild(td);
                    });
                body.appendChild(tr);
            });
        }

        var charts = {
            trend: makeChart('trendChart'),
            topBooks: makeChart('topBooksChart'),
            category: makeChart('categoryChart'),
            bucket: makeChart('bucketChart')
        };

        var lastData = null;
        var lastPersonal = null;
        var personalCharts = { heatmap: null, category: null };
        var csrfToken = (document.getElementById('csrfTokenHolder') || {}).value || '';

        document.addEventListener('app-theme-change', function () {
            if (lastData) {
                Object.keys(charts).forEach(function (key) { charts[key].clear(); });
                renderTrend(charts.trend, lastData.daily_reading);
                renderTopBooks(charts.topBooks, lastData.top_books);
                renderCategory(charts.category, lastData.categories);
                renderBuckets(charts.bucket, lastData.chapter_buckets);
            }
            if (lastPersonal && !document.getElementById('personalView').hidden) {
                renderHeatmap(lastPersonal);
                renderPersonalCategory(lastPersonal);
            }
        });

        function formatDate(date) {
            return date.getFullYear() + '-' + ('0' + (date.getMonth() + 1)).slice(-2) + '-' +
                ('0' + date.getDate()).slice(-2);
        }

        function renderHeatmap(data) {
            if (!personalCharts.heatmap) personalCharts.heatmap = makeChart('heatmapChart');
            var c = chartColors();
            var end = new Date();
            var start = new Date();
            start.setDate(end.getDate() - 364);
            var values = (data.daily || []).map(function (item) {
                return [item.stat_date, Math.round((Number(item.seconds) || 0) / 6) / 10];
            });
            var max = 10;
            values.forEach(function (v) { if (v[1] > max) max = v[1]; });
            var narrow = window.innerWidth <= 768;
            personalCharts.heatmap.setOption({
                tooltip: { formatter: function (p) { return p.value[0] + '：' + p.value[1] + ' 分钟'; } },
                visualMap: {
                    min: 0,
                    max: max,
                    show: false,
                    inRange: { color: c.dark ? ['#2b2b31', '#409eff'] : ['#e8f3ff', '#409eff'] }
                },
                calendar: {
                    range: [formatDate(start), formatDate(end)],
                    cellSize: ['auto', narrow ? 12 : 14],
                    top: narrow ? 12 : 16,
                    left: narrow ? 32 : 42,
                    right: narrow ? 8 : 16,
                    bottom: narrow ? 24 : 30,
                    splitLine: { show: false },
                    itemStyle: { color: 'transparent', borderWidth: 1, borderColor: c.split },
                    dayLabel: { color: c.label, fontSize: narrow ? 9 : 10, nameMap: 'ZH' },
                    monthLabel: { color: c.label, fontSize: narrow ? 9 : 10, nameMap: 'ZH' },
                    yearLabel: { show: false }
                },
                series: [{ type: 'heatmap', coordinateSystem: 'calendar', data: values }]
            }, true);
        }

        function renderPersonalCategory(data) {
            if (!personalCharts.category) personalCharts.category = makeChart('personalCategoryChart');
            var c = chartColors();
            var items = (data.categories || []).filter(function (x) { return x.seconds > 0; });
            personalCharts.category.clear();
            if (!items.length) {
                personalCharts.category.setOption({ graphic: emptyGraphic('暂无阅读时长') });
                return;
            }
            personalCharts.category.setOption({
                tooltip: {
                    trigger: 'item',
                    formatter: function (p) { return p.name + '：' + p.value + ' 分钟（' + p.percent + '%）'; }
                },
                legend: { bottom: 0, type: 'scroll', textStyle: { color: c.labelStrong, fontSize: 12 } },
                color: ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#9b8afb', '#36cfc9', '#ff9e6d'],
                series: [{
                    type: 'pie',
                    radius: ['38%', '62%'],
                    center: ['50%', '44%'],
                    itemStyle: { borderColor: c.surface, borderWidth: 2 },
                    label: { formatter: '{b}\n{d}%', color: c.labelStrong, fontSize: 12 },
                    data: items.map(function (x) {
                        return { name: x.name, value: Math.max(1, Math.round(x.seconds / 60)) };
                    })
                }]
            }, true);
        }

        function renderPersonal(data) {
            document.getElementById('pTotal').textContent = data.total_text || '0 分钟';
            document.getElementById('pTotalSub').textContent = '累计读完 ' + (data.finished_count || 0) + ' 本';
            document.getElementById('pWeek').textContent = data.week_text || '0 分钟';
            document.getElementById('pDays').textContent = (data.days_count || 0) + ' 天';
            document.getElementById('pStreak').textContent = '连续阅读 ' + (data.streak_days || 0) + ' 天';
            document.getElementById('pFinished').textContent = (data.finished_count || 0) + ' 本';
            document.getElementById('pYearFinished').textContent = '今年读完 ' + (data.year_finished || 0) + ' 本';

            var goal = data.year_goal || 0;
            document.getElementById('pGoal').textContent = goal > 0
                ? (data.year_finished || 0) + ' / ' + goal
                : '未设置';
            document.getElementById('pGoalSub').textContent = goal > 0
                ? '完成 ' + Math.min(100, Math.round((data.year_finished || 0) / goal * 100)) + '%'
                : '设置目标激励自己';
            document.getElementById('goalInput').value = goal > 0 ? goal : '';

            renderHeatmap(data);
            renderPersonalCategory(data);

            var finished = data.finished_books || [];
            var tbody = document.getElementById('finishedList');
            var empty = document.getElementById('finishedEmpty');
            tbody.innerHTML = '';
            empty.style.display = finished.length ? 'none' : '';
            finished.forEach(function (item) {
                var tr = document.createElement('tr');
                [item.title, item.author || '-', item.finish_time || '-'].forEach(function (text) {
                    var td = document.createElement('td');
                    td.textContent = text;
                    tr.appendChild(td);
                });
                tbody.appendChild(tr);
            });

            var topCategory = (data.categories && data.categories.length) ? data.categories[0].name : '暂无';
            document.getElementById('reportYear').textContent = data.year + ' 年';
            document.getElementById('yearSummary').textContent =
                data.year + ' 年读完 ' + (data.year_finished || 0) + ' 本，累计阅读 ' + (data.total_text || '0 分钟') +
                '，连续阅读 ' + (data.streak_days || 0) + ' 天；最爱分类：' + topCategory + '。';

            lastPersonal = data;
        }

        function loadPersonal() {
            fetch('${ctx}/books/stats/personal', {
                credentials: 'same-origin',
                headers: { 'Accept': 'application/json' }
            }).then(function (r) { return r.json(); }).then(function (body) {
                if (body && body.data) renderPersonal(body.data);
            }).catch(function () {
            });
        }

        var globalBtn = document.getElementById('scopeGlobal');
        var mineBtn = document.getElementById('scopeMine');

        function showScope(mine) {
            document.getElementById('globalView').hidden = mine;
            document.getElementById('personalView').hidden = !mine;
            globalBtn.classList.toggle('active', !mine);
            mineBtn.classList.toggle('active', mine);
            if (mine) {
                loadPersonal();
            } else {
                Object.keys(charts).forEach(function (key) { charts[key].resize(); });
            }
        }

        globalBtn.addEventListener('click', function () { showScope(false); });
        mineBtn.addEventListener('click', function () { showScope(true); });

        document.getElementById('goalSave').addEventListener('click', function () {
            var target = parseInt(document.getElementById('goalInput').value, 10);
            if (isNaN(target) || target < 0 || target > 9999) {
                alert('请输入 0~9999 之间的目标本数');
                return;
            }
            var year = (lastPersonal && lastPersonal.year) || new Date().getFullYear();
            var params = new URLSearchParams();
            params.append('csrfToken', csrfToken);
            params.append('year', String(year));
            params.append('target', String(target));
            fetch('${ctx}/books/goal', {
                method: 'POST',
                credentials: 'same-origin',
                headers: { 'X-CSRF-Token': csrfToken },
                body: params
            }).then(function (r) { return r.json(); }).then(function (res) {
                if (res.code === 200) {
                    loadPersonal();
                } else {
                    alert(res.message || '保存失败');
                }
            }).catch(function () {
                alert('保存失败');
            });
        });

        var resizeTimer = null;
        window.addEventListener('resize', function () {
            clearTimeout(resizeTimer);
            resizeTimer = setTimeout(function () {
                Object.keys(charts).forEach(function (key) { charts[key].resize(); });
                Object.keys(personalCharts).forEach(function (key) {
                    if (personalCharts[key]) personalCharts[key].resize();
                });
            }, 120);
        });

        fetch('${ctx}/books/stats/data', { headers: { 'Accept': 'application/json' } })
            .then(function (res) {
                if (res.status === 401) {
                    window.location.href = '${ctx}/login?redirect=' +
                        encodeURIComponent('${ctx}/books/stats');
                    return null;
                }
                if (!res.ok) throw new Error('加载失败');
                return res.json();
            })
            .then(function (body) {
                if (!body) return;
                var data = body.data || {};
                lastData = data;
                renderCards(data);
                renderTrend(charts.trend, data.daily_reading);
                renderTopBooks(charts.topBooks, data.top_books);
                renderCategory(charts.category, data.categories);
                renderBuckets(charts.bucket, data.chapter_buckets);
                renderTable(data.categories);
            })
            .catch(function () {
                document.getElementById('bookTotal').parentNode.querySelector('.stat-sub').textContent = '统计数据加载失败';
            });
    })();
</script>

<%@ include file="../common/foot.jspf" %>

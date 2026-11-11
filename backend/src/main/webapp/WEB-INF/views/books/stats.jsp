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

    @media (max-width: 900px) {
        .stats-grid { grid-template-columns: minmax(0, 1fr); }
    }
</style>

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

<script src="${ctx}/echarts.min.js"></script>
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

        function emptyGraphic(text) {
            return [{
                type: 'text',
                left: 'center',
                top: 'middle',
                style: { text: text, fill: '#c0c4cc', fontSize: 13 }
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
            chart.setOption({
                tooltip: { trigger: 'axis', valueFormatter: function (v) { return v + ' 分钟'; } },
                grid: { left: 40, right: 16, top: 24, bottom: 28 },
                xAxis: {
                    type: 'category',
                    data: dates,
                    axisLine: { lineStyle: { color: '#dcdfe6' } },
                    axisLabel: { color: '#909399' }
                },
                yAxis: {
                    type: 'value',
                    axisLabel: { color: '#909399' },
                    splitLine: { lineStyle: { color: '#f2f3f5' } }
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
            chart.setOption({
                tooltip: {
                    trigger: 'axis',
                    axisPointer: { type: 'shadow' },
                    valueFormatter: function (v) { return compact(v) + ' 字'; }
                },
                grid: { left: 10, right: 70, top: 16, bottom: 16, containLabel: true },
                xAxis: {
                    type: 'value',
                    axisLabel: { color: '#909399', formatter: compact },
                    splitLine: { lineStyle: { color: '#f2f3f5' } }
                },
                yAxis: {
                    type: 'category',
                    data: reversed.map(function (b) { return b.title; }),
                    axisLine: { lineStyle: { color: '#dcdfe6' } },
                    axisLabel: { color: '#606266', width: 110, overflow: 'truncate' }
                },
                series: [{
                    type: 'bar',
                    data: reversed.map(function (b) { return b.word_count; }),
                    barMaxWidth: 16,
                    itemStyle: { color: '#409eff', borderRadius: [0, 4, 4, 0] },
                    label: {
                        show: true,
                        position: 'right',
                        color: '#909399',
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
            chart.setOption({
                tooltip: { trigger: 'item', formatter: function (p) { return p.name + '：' + compact(p.value) + ' 字（' + p.percent + '%）'; } },
                legend: { bottom: 0, type: 'scroll', textStyle: { color: '#606266', fontSize: 12 } },
                color: ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#9b8afb', '#36cfc9', '#ff9e6d'],
                series: [{
                    type: 'pie',
                    radius: ['38%', '62%'],
                    center: ['50%', '44%'],
                    avoidLabelOverlap: true,
                    itemStyle: { borderColor: '#fff', borderWidth: 2 },
                    label: { formatter: '{b}\n{d}%', color: '#606266', fontSize: 12 },
                    data: items.map(function (c) { return { name: c.name, value: c.word_count }; })
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
            chart.setOption({
                tooltip: { trigger: 'item', formatter: '{b}：{c} 章（{d}%）' },
                legend: { bottom: 0, textStyle: { color: '#606266', fontSize: 12 } },
                color: ['#909399', '#409eff', '#67c23a', '#e6a23c'],
                series: [{
                    type: 'pie',
                    radius: ['38%', '62%'],
                    center: ['50%', '44%'],
                    itemStyle: { borderColor: '#fff', borderWidth: 2 },
                    label: { formatter: '{b}\n{c} 章', color: '#606266', fontSize: 12 },
                    data: items.map(function (b) { return { name: b.label, value: b.item_count }; })
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

        var resizeTimer = null;
        window.addEventListener('resize', function () {
            clearTimeout(resizeTimer);
            resizeTimer = setTimeout(function () {
                Object.keys(charts).forEach(function (key) { charts[key].resize(); });
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

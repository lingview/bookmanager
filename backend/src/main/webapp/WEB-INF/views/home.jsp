<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>首页</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI",
            Roboto, "Helvetica Neue", Arial, sans-serif;
            background-color: #f5f7fa;
            color: #303133;
            padding: 32px 24px;
        }

        .bar {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 16px;
            padding-bottom: 16px;
            border-bottom: 1px solid #ebeef5;
            margin-bottom: 24px;
        }

        .title {
            font-size: 18px;
            font-weight: 600;
        }

        .bar-left {
            display: flex;
            align-items: center;
            gap: 16px;
        }

        .entry {
            font-size: 14px;
            color: #409eff;
            text-decoration: none;
        }

        .entry:hover {
            text-decoration: underline;
        }

        .user {
            font-size: 14px;
            color: #606266;
        }

        .btn {
            padding: 8px 16px;
            font-size: 14px;
            color: #606266;
            background: none;
            border: 1px solid #dcdfe6;
            border-radius: 6px;
            cursor: pointer;
        }

        .btn:hover {
            color: #409eff;
            border-color: #409eff;
        }

        .content {
            font-size: 14px;
            color: #606266;
            line-height: 1.8;
        }
    </style>
</head>

<body>
<div class="bar">
    <div class="bar-left">
        <span class="title">Astra Framework</span>
        <a class="entry" href="${pageContext.request.contextPath}/books">图书管理</a>
    </div>
    <span class="user">当前用户：<c:out value="${username}"/></span>
    <form action="${pageContext.request.contextPath}/logout" method="post">
        <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
        <button type="submit" class="btn">退出登录</button>
    </form>
</div>

<div class="content">
    <p>JSP 模板引擎已就绪，页面受会话认证与权限拦截器保护。</p>
    <p>前往 <a class="entry" href="${pageContext.request.contextPath}/books">图书管理</a> 浏览图书列表与阅读器。</p>
</div>
</body>
</html>
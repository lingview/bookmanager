<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>登录</title>
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
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 24px;
        }

        .container {
            width: 100%;
            max-width: 420px;
        }

        .card {
            background: #ffffff;
            border-radius: 10px;
            border: 1px solid #ebeef5;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
            overflow: hidden;
        }

        .card-header {
            padding: 20px 24px;
            border-bottom: 1px solid #ebeef5;
            text-align: center;
            font-size: 18px;
            font-weight: 600;
            color: #303133;
        }

        .card-body {
            padding: 24px;
        }

        .form-group {
            margin-bottom: 18px;
        }

        .form-label {
            display: block;
            margin-bottom: 6px;
            font-size: 14px;
            color: #606266;
        }

        .form-control {
            width: 100%;
            padding: 10px 12px;
            font-size: 14px;
            color: #303133;
            border: 1px solid #dcdfe6;
            border-radius: 6px;
            outline: none;
        }

        .form-control:focus {
            border-color: #409eff;
        }

        .captcha-row {
            display: flex;
            align-items: center;
            gap: 12px;
        }

        .captcha-row .form-control {
            flex: 1;
            min-width: 0;
        }

        .captcha-row img {
            width: 120px;
            height: 40px;
            border-radius: 6px;
            border: 1px solid #dcdfe6;
        }

        .error {
            margin-bottom: 18px;
            font-size: 14px;
            color: #f56c6c;
            line-height: 1.6;
        }

        .btn {
            width: 100%;
            padding: 10px 20px;
            font-size: 14px;
            font-weight: 500;
            color: #ffffff;
            background-color: #409eff;
            border: none;
            border-radius: 6px;
            cursor: pointer;
        }

        .btn:hover {
            background-color: #337ecc;
        }
    </style>
</head>

<body>
<div class="container">
    <div class="card">
        <div class="card-header">登录</div>

        <div class="card-body">
            <c:if test="${not empty error}">
                <p class="error"><c:out value="${error}"/></p>
            </c:if>

            <form action="${pageContext.request.contextPath}/login" method="post">
                <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                <input type="hidden" name="redirect" value="<c:out value='${redirect}'/>">
                <input type="hidden" name="captchaKey" value="<c:out value='${captchaKey}'/>">

                <div class="form-group">
                    <label class="form-label" for="username">用户名</label>
                    <input class="form-control" type="text" id="username" name="username"
                           value="<c:out value='${username}'/>" required autocomplete="username">
                </div>

                <div class="form-group">
                    <label class="form-label" for="password">密码</label>
                    <input class="form-control" type="password" id="password" name="password"
                           required autocomplete="current-password">
                </div>

                <div class="form-group">
                    <label class="form-label" for="captcha">验证码</label>
                    <div class="captcha-row">
                        <input class="form-control" type="text" id="captcha" name="captcha"
                               required autocomplete="off" maxlength="4">
                        <img src="<c:out value='${captchaImage}'/>" alt="验证码">
                    </div>
                </div>

                <button type="submit" class="btn">登录</button>
            </form>
        </div>
    </div>
</div>
</body>
</html>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>注册</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }

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

        .container { width: 100%; max-width: 420px; }

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

        .card-body { padding: 24px; }
        .form-group { margin-bottom: 16px; }
        .form-label { display: block; margin-bottom: 6px; font-size: 14px; color: #606266; }

        .form-control {
            width: 100%;
            padding: 10px 12px;
            font-size: 14px;
            color: #303133;
            border: 1px solid #dcdfe6;
            border-radius: 6px;
            outline: none;
            font-family: inherit;
        }

        .form-control:focus { border-color: #409eff; }

        .captcha-row { display: flex; align-items: center; gap: 12px; }
        .captcha-row .form-control { flex: 1; min-width: 0; }
        .captcha-row img {
            width: 120px;
            height: 40px;
            border-radius: 6px;
            border: 1px solid #dcdfe6;
            cursor: pointer;
        }

        .error { margin-bottom: 14px; font-size: 14px; color: #f56c6c; line-height: 1.6; }
        .success { margin-bottom: 14px; font-size: 14px; color: #67c23a; line-height: 1.6; }

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
            font-family: inherit;
        }

        .btn:hover { background-color: #337ecc; }
        .btn[disabled] { background-color: #a0cfff; cursor: default; }

        .links { margin-top: 16px; font-size: 13px; text-align: center; }
        .links a { color: #409eff; text-decoration: none; }
    </style>
</head>

<body>
<div class="container">
    <div class="card">
        <div class="card-header">注册账号</div>
        <div class="card-body">
            <p class="error" id="errorBox" hidden></p>
            <p class="success" id="successBox" hidden></p>

            <form id="registerForm">
                <input type="hidden" id="captchaKey" value="<c:out value='${captchaKey}'/>">

                <div class="form-group">
                    <label class="form-label" for="username">用户名</label>
                    <input class="form-control" type="text" id="username" required
                           autocomplete="username" maxlength="50">
                </div>

                <div class="form-group">
                    <label class="form-label" for="email">邮箱</label>
                    <input class="form-control" type="email" id="email" autocomplete="email" maxlength="100">
                </div>

                <div class="form-group">
                    <label class="form-label" for="phone">手机号（选填）</label>
                    <input class="form-control" type="text" id="phone" autocomplete="tel" maxlength="11">
                </div>

                <div class="form-group">
                    <label class="form-label" for="password">密码</label>
                    <input class="form-control" type="password" id="password" required
                           autocomplete="new-password" minlength="6">
                </div>

                <div class="form-group">
                    <label class="form-label" for="confirmPassword">确认密码</label>
                    <input class="form-control" type="password" id="confirmPassword" required
                           autocomplete="new-password" minlength="6">
                </div>

                <div class="form-group">
                    <label class="form-label" for="captcha">验证码</label>
                    <div class="captcha-row">
                        <input class="form-control" type="text" id="captcha" required
                               autocomplete="off" maxlength="4">
                        <img src="<c:out value='${captchaImage}'/>" alt="验证码" title="点击刷新"
                             onclick="location.reload()">
                    </div>
                </div>

                <button type="submit" class="btn" id="submitBtn">注册</button>
            </form>

            <div class="links">
                已有账号？<a href="${pageContext.request.contextPath}/login">返回登录</a>
            </div>
        </div>
    </div>
</div>

<script>
    (function () {
        var form = document.getElementById('registerForm');
        var btn = document.getElementById('submitBtn');
        var errorBox = document.getElementById('errorBox');
        var successBox = document.getElementById('successBox');
        var captchaKey = document.getElementById('captchaKey').value;

        function showError(message) {
            successBox.hidden = true;
            errorBox.textContent = message;
            errorBox.hidden = false;
        }

        form.addEventListener('submit', function (e) {
            e.preventDefault();
            errorBox.hidden = true;
            successBox.hidden = true;

            var password = document.getElementById('password').value;
            var confirmPassword = document.getElementById('confirmPassword').value;
            if (password !== confirmPassword) {
                showError('两次输入的密码不一致');
                return;
            }

            var payload = {
                username: document.getElementById('username').value.trim(),
                email: document.getElementById('email').value.trim(),
                phone: document.getElementById('phone').value.trim() || null,
                password: password,
                captcha: document.getElementById('captcha').value.trim(),
                captchaKey: captchaKey
            };

            btn.disabled = true;
            fetch('${pageContext.request.contextPath}/api/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            }).then(function (r) { return r.json(); }).then(function (res) {
                if (res.code === 200) {
                    successBox.textContent = '注册成功，正在跳转到登录页…';
                    successBox.hidden = false;
                    setTimeout(function () {
                        window.location.href = '${pageContext.request.contextPath}/login';
                    }, 1200);
                } else {
                    showError(res.message || '注册失败');
                    btn.disabled = false;
                }
            }).catch(function () {
                showError('注册失败，请稍后再试');
                btn.disabled = false;
            });
        });
    })();
</script>
</body>
</html>

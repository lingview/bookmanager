<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>找回密码</title>
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

        .error { margin-bottom: 14px; font-size: 14px; color: #f56c6c; line-height: 1.6; }
        .success { margin-bottom: 14px; font-size: 14px; color: #67c23a; line-height: 1.6; }
        .hint { font-size: 12px; color: #909399; margin-top: 4px; }
        .step-title { font-size: 14px; font-weight: 600; color: #303133; margin-bottom: 12px; }

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
        .btn.ghost { background: #fff; color: #606266; border: 1px solid #dcdfe6; }
        .btn.ghost:hover { color: #409eff; border-color: #409eff; }

        .links { margin-top: 16px; font-size: 13px; text-align: center; }
        .links a { color: #409eff; text-decoration: none; }
        .btn-row { display: flex; gap: 10px; }
        .btn-row .btn { flex: 1; }
    </style>
</head>

<body>
<div class="container">
    <div class="card">
        <div class="card-header">找回密码</div>
        <div class="card-body">
            <p class="error" id="errorBox" hidden></p>
            <p class="success" id="successBox" hidden></p>

            <div id="stepSend">
                <div class="step-title">1. 发送验证码</div>
                <div class="form-group">
                    <label class="form-label" for="username">用户名</label>
                    <input class="form-control" type="text" id="username" maxlength="50">
                </div>
                <div class="form-group">
                    <label class="form-label" for="email">注册邮箱</label>
                    <input class="form-control" type="email" id="email" maxlength="100">
                </div>
                <button type="button" class="btn" id="sendBtn">发送验证码</button>
                <p class="hint">验证码会发送到你的注册邮箱</p>
            </div>

            <div id="stepVerify" hidden>
                <div class="step-title">2. 输入邮箱验证码</div>
                <div class="form-group">
                    <label class="form-label" for="captcha">验证码</label>
                    <input class="form-control" type="text" id="captcha" maxlength="10">
                </div>
                <div class="btn-row">
                    <button type="button" class="btn ghost" id="resendBtn">重新发送</button>
                    <button type="button" class="btn" id="verifyBtn">验证</button>
                </div>
            </div>

            <div id="stepReset" hidden>
                <div class="step-title">3. 设置新密码</div>
                <div class="form-group">
                    <label class="form-label" for="newPassword">新密码</label>
                    <input class="form-control" type="password" id="newPassword" minlength="6"
                           autocomplete="new-password">
                </div>
                <div class="form-group">
                    <label class="form-label" for="confirmPassword">确认新密码</label>
                    <input class="form-control" type="password" id="confirmPassword" minlength="6"
                           autocomplete="new-password">
                </div>
                <button type="button" class="btn" id="resetBtn">重置密码</button>
            </div>

            <div class="links">
                <a href="${pageContext.request.contextPath}/login">返回登录</a>
            </div>
        </div>
    </div>
</div>

<script>
    (function () {
        var ctx = '${pageContext.request.contextPath}';
        var errorBox = document.getElementById('errorBox');
        var successBox = document.getElementById('successBox');
        var stepSend = document.getElementById('stepSend');
        var stepVerify = document.getElementById('stepVerify');
        var stepReset = document.getElementById('stepReset');

        function showError(message) {
            successBox.hidden = true;
            errorBox.textContent = message;
            errorBox.hidden = false;
        }

        function showSuccess(message) {
            errorBox.hidden = true;
            successBox.textContent = message;
            successBox.hidden = false;
        }

        function request(path, payload) {
            return fetch(ctx + path, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            }).then(function (r) { return r.json(); });
        }

        function credentials() {
            return {
                username: document.getElementById('username').value.trim(),
                email: document.getElementById('email').value.trim()
            };
        }

        document.getElementById('sendBtn').addEventListener('click', function () {
            var data = credentials();
            if (!data.username || !data.email) {
                showError('请填写用户名和邮箱');
                return;
            }
            errorBox.hidden = true;
            request('/api/forgot-password/send-captcha', data).then(function (res) {
                if (res.code === 200) {
                    showSuccess('验证码已发送，请查收邮箱');
                    stepVerify.hidden = false;
                    stepReset.hidden = true;
                } else {
                    showError(res.message || '发送失败');
                }
            }).catch(function () { showError('发送失败，请稍后再试'); });
        });

        document.getElementById('resendBtn').addEventListener('click', function () {
            var data = credentials();
            request('/api/forgot-password/send-captcha', data).then(function (res) {
                if (res.code === 200) {
                    showSuccess('验证码已重新发送');
                } else {
                    showError(res.message || '发送失败');
                }
            }).catch(function () { showError('发送失败，请稍后再试'); });
        });

        document.getElementById('verifyBtn').addEventListener('click', function () {
            var data = credentials();
            data.captcha = document.getElementById('captcha').value.trim();
            if (!data.captcha) {
                showError('请输入验证码');
                return;
            }
            request('/api/forgot-password/verify', data).then(function (res) {
                if (res.code === 200) {
                    showSuccess('验证成功，请设置新密码');
                    stepReset.hidden = false;
                } else {
                    showError(res.message || '验证失败');
                }
            }).catch(function () { showError('验证失败，请稍后再试'); });
        });

        document.getElementById('resetBtn').addEventListener('click', function () {
            var data = credentials();
            data.newPassword = document.getElementById('newPassword').value;
            data.confirmNewPassword = document.getElementById('confirmPassword').value;
            if (!data.newPassword || data.newPassword.length < 6) {
                showError('新密码长度不能少于6位');
                return;
            }
            if (data.newPassword !== data.confirmNewPassword) {
                showError('两次输入的密码不一致');
                return;
            }
            request('/api/forgot-password/reset', data).then(function (res) {
                if (res.code === 200) {
                    showSuccess('密码已重置，3 秒后跳转到登录页…');
                    setTimeout(function () {
                        window.location.href = ctx + '/login';
                    }, 3000);
                } else {
                    showError(res.message || '重置失败');
                }
            }).catch(function () { showError('重置失败，请稍后再试'); });
        });
    })();
</script>
</body>
</html>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dang nhap - Tuyen Sinh DH 2026</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <style>
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #0d6efd 0%, #0a58ca 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .login-container {
            width: 100%;
            max-width: 420px;
            padding: 20px;
        }
        .card {
            border: none;
            border-radius: 16px;
            box-shadow: 0 15px 50px rgba(0,0,0,0.2);
        }
        .card-header {
            background: white;
            border-bottom: none;
            padding: 2rem 2rem 0;
            text-align: center;
            border-radius: 16px 16px 0 0 !important;
        }
        .card-header .brand {
            color: #0d6efd;
            font-size: 1.5rem;
            font-weight: 700;
        }
        .card-body {
            padding: 2rem;
        }
        .form-label {
            font-weight: 500;
            color: #333;
        }
        .input-group-text {
            background: #f8f9fa;
            border: 1px solid #ced4da;
            border-right: none;
        }
        .form-control {
            border-left: none;
            padding: 12px 15px;
        }
        .form-control:focus {
            border-color: #0d6efd;
            box-shadow: none;
        }
        .btn-primary {
            background: #0d6efd;
            border: none;
            padding: 12px;
            font-weight: 600;
            border-radius: 8px;
            font-size: 1rem;
        }
        .btn-primary:hover {
            background: #0a58ca;
        }
        .register-link {
            text-align: center;
            margin-top: 1.5rem;
        }
        .register-link a {
            color: #0d6efd;
            text-decoration: none;
            font-weight: 500;
        }
        .register-link a:hover {
            text-decoration: underline;
        }
        .alert {
            border-radius: 8px;
        }
    </style>
</head>
<body>
    <div class="login-container">
        <div class="card">
            <div class="card-header">
                <div class="brand mb-2">
                    <i class="bi bi-mortarboard-fill me-2"></i>TUYEN SINH DH 2026
                </div>
                <h4 class="text-muted mb-0">Dang nhap he thong</h4>
            </div>
            <div class="card-body">
                <c:if test="${not empty sessionScope.message}">
                    <div class="alert alert-${sessionScope.messageType != null ? sessionScope.messageType : 'danger'} alert-dismissible fade show" role="alert">
                        ${sessionScope.message}
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                    <c:remove var="message" scope="session"/>
                    <c:remove var="messageType" scope="session"/>
                </c:if>

                <form action="${pageContext.request.contextPath}/login" method="post">
                    <div class="mb-4">
                        <label for="username" class="form-label">Ten dang nhap</label>
                        <div class="input-group">
                            <span class="input-group-text"><i class="bi bi-person"></i></span>
                            <input type="text" class="form-control" id="username" name="username" 
                                   placeholder="Nhap ten dang nhap" required autofocus>
                        </div>
                    </div>
                    <div class="mb-4">
                        <label for="password" class="form-label">Mat khau</label>
                        <div class="input-group">
                            <span class="input-group-text"><i class="bi bi-lock"></i></span>
                            <input type="password" class="form-control" id="password" name="password" 
                                   placeholder="Nhap mat khau" required>
                        </div>
                    </div>
                    <div class="d-grid">
                        <button type="submit" class="btn btn-primary">
                            <i class="bi bi-box-arrow-in-right me-2"></i>Dang nhap
                        </button>
                    </div>
                </form>

                <div class="register-link">
                    <p class="text-muted mb-0">
                        Ban chua co tai khoan? 
                        <a href="${pageContext.request.contextPath}/register">Dang ky ngay</a>
                    </p>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>

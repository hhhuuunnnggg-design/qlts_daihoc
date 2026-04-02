<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lỗi - Tuyển sinh ĐH 2026</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="d-flex align-items-center justify-content-center min-vh-100">
    <div class="text-center">
        <h1 class="display-1 text-danger">${param.code != null ? param.code : "500"}</h1>
        <h3 class="mb-4">Đã xảy ra lỗi!</h3>
        <p class="text-muted mb-4">Vui lòng quay lại trang chủ hoặc liên hệ quản trị viên.</p>
        <a href="${pageContext.request.contextPath}/login" class="btn btn-primary">
            <i class="bi bi-house me-2"></i>Quay lại trang chủ
        </a>
    </div>
</body>
</html>

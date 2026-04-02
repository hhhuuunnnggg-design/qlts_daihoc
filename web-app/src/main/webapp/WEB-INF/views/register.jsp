<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng ký - Tuyển sinh ĐH 2026</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <style>
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: #f5f5f5;
        }
        .register-container {
            padding: 40px 0;
        }
        .card {
            border: none;
            border-radius: 16px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.1);
        }
        .card-header {
            background: linear-gradient(135deg, #0d6efd 0%, #0a58ca 100%);
            color: white;
            border-radius: 16px 16px 0 0 !important;
            padding: 1.5rem 2rem;
            text-align: center;
        }
        .card-header h3 {
            margin: 0;
            font-weight: 600;
        }
        .card-header p {
            margin: 5px 0 0;
            opacity: 0.9;
        }
        .card-body {
            padding: 2rem;
        }
        .form-section {
            background: #f8f9fa;
            border-radius: 12px;
            padding: 1.5rem;
            margin-bottom: 1.5rem;
        }
        .form-section-title {
            font-weight: 600;
            color: #0d6efd;
            margin-bottom: 1rem;
            padding-bottom: 0.5rem;
            border-bottom: 2px solid #0d6efd;
        }
        .form-label {
            font-weight: 500;
            color: #333;
        }
        .form-control, .form-select {
            padding: 10px 12px;
            border-radius: 8px;
        }
        .form-control:focus, .form-select:focus {
            border-color: #0d6efd;
            box-shadow: 0 0 0 0.2rem rgba(13, 110, 253, 0.15);
        }
        .btn-primary {
            background: #0d6efd;
            border: none;
            padding: 12px 30px;
            font-weight: 600;
            border-radius: 8px;
            font-size: 1rem;
        }
        .btn-primary:hover {
            background: #0a58ca;
        }
        .btn-secondary {
            padding: 12px 30px;
            font-weight: 500;
            border-radius: 8px;
        }
        .login-link {
            text-align: center;
            margin-top: 1.5rem;
        }
        .login-link a {
            color: #0d6efd;
            text-decoration: none;
            font-weight: 500;
        }
        .login-link a:hover {
            text-decoration: underline;
        }
        .text-danger {
            font-size: 0.85rem;
        }
        .is-invalid {
            border-color: #dc3545;
        }
    </style>
</head>
<body>
    <div class="register-container">
        <div class="container">
            <div class="row justify-content-center">
                <div class="col-lg-8">
                    <div class="card">
                        <div class="card-header">
                            <h3><i class="bi bi-person-plus me-2"></i>Đăng ký tài khoản</h3>
                            <p>Đăng ký để tham gia xét tuyển đại học 2026</p>
                        </div>
                        <div class="card-body">
                            <c:if test="${not empty sessionScope.message}">
                                <div class="alert alert-${sessionScope.messageType != null ? sessionScope.messageType : 'danger'} alert-dismissible fade show" role="alert">
                                    ${sessionScope.message}
                                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                                </div>
                                <c:remove var="message" scope="session"/>
                                <c:remove var="messageType" scope="session"/>
                            </c:if>

                            <form action="${pageContext.request.contextPath}/register" method="post" id="registerForm">
                                <div class="form-section">
                                    <div class="form-section-title">
                                        <i class="bi bi-key me-2"></i>Thông tin đăng nhập
                                    </div>
                                    <div class="row">
                                        <div class="col-md-4 mb-3">
                                            <label for="username" class="form-label">Tên đăng nhập <span class="text-danger">*</span></label>
                                            <input type="text" class="form-control" id="username" name="username" required>
                                        </div>
                                        <div class="col-md-4 mb-3">
                                            <label for="password" class="form-label">Mật khẩu <span class="text-danger">*</span></label>
                                            <input type="password" class="form-control" id="password" name="password" required minlength="6">
                                        </div>
                                        <div class="col-md-4 mb-3">
                                            <label for="confirmPassword" class="form-label">Xác nhận mật khẩu <span class="text-danger">*</span></label>
                                            <input type="password" class="form-control" id="confirmPassword" name="confirmPassword" required>
                                        </div>
                                    </div>
                                </div>

                                <div class="form-section">
                                    <div class="form-section-title">
                                        <i class="bi bi-person me-2"></i>Thông tin cá nhân
                                    </div>
                                    <div class="row">
                                        <div class="col-md-6 mb-3">
                                            <label for="ho" class="form-label">Họ <span class="text-danger">*</span></label>
                                            <input type="text" class="form-control" id="ho" name="ho" required>
                                        </div>
                                        <div class="col-md-6 mb-3">
                                            <label for="ten" class="form-label">Tên <span class="text-danger">*</span></label>
                                            <input type="text" class="form-control" id="ten" name="ten" required>
                                        </div>
                                    </div>
                                    <div class="row">
                                        <div class="col-md-6 mb-3">
                                            <label for="cccd" class="form-label">Số CCCD <span class="text-danger">*</span></label>
                                            <input type="text" class="form-control" id="cccd" name="cccd" required pattern="[0-9]{9,12}" title="CCCD phải là số, 9–12 chữ số">
                                        </div>
                                        <div class="col-md-6 mb-3">
                                            <label for="ngaySinh" class="form-label">Ngày sinh <span class="text-danger">*</span></label>
                                            <input type="date" class="form-control" id="ngaySinh" name="ngaySinh" required>
                                        </div>
                                    </div>
                                    <div class="row">
                                        <div class="col-md-6 mb-3">
                                            <label class="form-label">Giới tính <span class="text-danger">*</span></label>
                                            <div>
                                                <div class="form-check form-check-inline">
                                                    <input class="form-check-input" type="radio" name="gioiTinh" id="gioiTinhNam" value="Nam" checked>
                                                    <label class="form-check-label" for="gioiTinhNam">Nam</label>
                                                </div>
                                                <div class="form-check form-check-inline">
                                                    <input class="form-check-input" type="radio" name="gioiTinh" id="gioiTinhNu" value="Nu">
                                                    <label class="form-check-label" for="gioiTinhNu">Nữ</label>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="col-md-6 mb-3">
                                            <label for="noiSinh" class="form-label">Nơi sinh</label>
                                            <input type="text" class="form-control" id="noiSinh" name="noiSinh">
                                        </div>
                                    </div>
                                </div>

                                <div class="form-section">
                                    <div class="form-section-title">
                                        <i class="bi bi-telephone me-2"></i>Thông tin liên hệ
                                    </div>
                                    <div class="row">
                                        <div class="col-md-6 mb-3">
                                            <label for="dienThoai" class="form-label">Điện thoại <span class="text-danger">*</span></label>
                                            <input type="tel" class="form-control" id="dienThoai" name="dienThoai" required pattern="[0-9]{10,11}">
                                        </div>
                                        <div class="col-md-6 mb-3">
                                            <label for="email" class="form-label">Email <span class="text-danger">*</span></label>
                                            <input type="email" class="form-control" id="email" name="email" required>
                                        </div>
                                    </div>
                                </div>

                                <div class="form-section">
                                    <div class="form-section-title">
                                        <i class="bi bi-star me-2"></i>Ưu tiên xét tuyển
                                    </div>
                                    <div class="row">
                                        <div class="col-md-6 mb-3">
                                            <label for="doituongId" class="form-label">Đối tượng ưu tiên</label>
                                            <select class="form-select" id="doituongId" name="doituongId">
                                                <option value="">-- Chọn đối tượng --</option>
                                                <c:forEach var="dt" items="${danhSachDoiTuong}">
                                                    <option value="${dt.doituongId}">${dt.maDoituong} - ${dt.tenDoituong}</option>
                                                </c:forEach>
                                            </select>
                                        </div>
                                        <div class="col-md-6 mb-3">
                                            <label for="khuvucId" class="form-label">Khu vực ưu tiên</label>
                                            <select class="form-select" id="khuvucId" name="khuvucId">
                                                <option value="">-- Chọn khu vực --</option>
                                                <c:forEach var="kv" items="${danhSachKhuVuc}">
                                                    <option value="${kv.khuvucId}">${kv.maKhuvuc} - ${kv.tenKhuvuc}</option>
                                                </c:forEach>
                                            </select>
                                        </div>
                                    </div>
                                </div>

                                <div class="d-flex justify-content-between mt-4">
                                    <a href="${pageContext.request.contextPath}/login" class="btn btn-secondary">
                                        <i class="bi bi-arrow-left me-2"></i>Quay lại
                                    </a>
                                    <button type="submit" class="btn btn-primary">
                                        <i class="bi bi-check2 me-2"></i>Đăng ký
                                    </button>
                                </div>
                            </form>

                            <div class="login-link">
                                <p class="text-muted mb-0">
                                    Đã có tài khoản? <a href="${pageContext.request.contextPath}/login">Đăng nhập ngay</a>
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        document.getElementById('registerForm').addEventListener('submit', function(e) {
            var password = document.getElementById('password').value;
            var confirmPassword = document.getElementById('confirmPassword').value;
            
            if (password !== confirmPassword) {
                e.preventDefault();
                alert('Mật khẩu xác nhận không khớp!');
                return false;
            }
            
            if (password.length < 6) {
                e.preventDefault();
                alert('Mật khẩu phải có ít nhất 6 ký tự!');
                return false;
            }
            
            return true;
        });
    </script>
</body>
</html>

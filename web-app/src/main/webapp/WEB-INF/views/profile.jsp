<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Ho so ca nhan - Tuyen Sinh DH 2026</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <style>
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: #f5f5f5;
        }
        .page-header {
            background: linear-gradient(135deg, #0d6efd 0%, #0a58ca 100%);
            color: white;
            padding: 2rem 0;
            margin-bottom: 2rem;
        }
        .card {
            border: none;
            border-radius: 12px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.05);
        }
        .card-header {
            background: white;
            border-bottom: 2px solid #f5f5f5;
            padding: 1.25rem 1.5rem;
        }
        .card-body {
            padding: 1.5rem;
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
        .form-control:disabled, .form-select:disabled {
            background-color: #e9ecef;
            cursor: not-allowed;
        }
        .btn-primary {
            background: #0d6efd;
            border: none;
            padding: 12px 30px;
            font-weight: 600;
            border-radius: 8px;
        }
        .btn-primary:hover {
            background: #0a58ca;
        }
        .btn-secondary {
            padding: 12px 30px;
            font-weight: 500;
            border-radius: 8px;
        }
        .info-badge {
            font-size: 0.8rem;
            background: #e7f1ff;
            color: #0d6efd;
            padding: 0.25rem 0.5rem;
            border-radius: 4px;
        }
    </style>
</head>
<body>
    <jsp:include page="header.jsp"/>

    <div class="page-header">
        <div class="container">
            <h2 class="mb-1"><i class="bi bi-person-badge me-2"></i>Ho so ca nhan</h2>
            <p class="mb-0 opacity-75">Xem va cap nhat thong tin ca nhan cua ban</p>
        </div>
    </div>

    <div class="container mb-4">
        <c:if test="${not empty sessionScope.message}">
            <div class="alert alert-${sessionScope.messageType != null ? sessionScope.messageType : 'success'} alert-dismissible fade show" role="alert">
                ${sessionScope.message}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
            <c:remove var="message" scope="session"/>
            <c:remove var="messageType" scope="session"/>
        </c:if>

        <form action="${pageContext.request.contextPath}/profile" method="post">
            <div class="row">
                <div class="col-lg-8">
                    <div class="card mb-4">
                        <div class="card-header">
                            <i class="bi bi-person me-2"></i>Thong tin ca nhan
                        </div>
                        <div class="card-body">
                            <div class="form-section">
                                <div class="form-section-title">
                                    <i class="bi bi-credit-card me-2"></i>Thong tin dinh danh
                                </div>
                                <div class="row">
                                    <div class="col-md-6 mb-3">
                                        <label for="cccd" class="form-label">So CCCD <span class="text-danger">*</span></label>
                                        <input type="text" class="form-control" id="cccd" name="cccd"
                                               value="${thiSinh.cccd}"
                                               required pattern="[0-9]{9,12}" maxlength="12"
                                               title="CCCD: 9-12 chu so">
                                        <small class="text-muted">Nhap va co the chinh sua khi can (khong trung voi thi sinh khac).</small>
                                    </div>
                                    <div class="col-md-6 mb-3">
                                        <label for="sobaodanh" class="form-label">So bao danh</label>
                                        <input type="text" class="form-control" id="sobaodanh" 
                                               value="${thiSinh.sobaodanh != null ? thiSinh.sobaodanh : 'Chua cap'}" readonly>
                                        <small class="text-muted">Duoc cap tu dong khi duyet ho so</small>
                                    </div>
                                </div>
                            </div>

                            <div class="form-section">
                                <div class="form-section-title">
                                    <i class="bi bi-person me-2"></i>Thong tin ca nhan
                                </div>
                                <div class="row">
                                    <div class="col-md-6 mb-3">
                                        <label for="ho" class="form-label">Ho <span class="text-danger">*</span></label>
                                        <input type="text" class="form-control" id="ho" name="ho"
                                               value="${thiSinh.ho}" required>
                                    </div>
                                    <div class="col-md-6 mb-3">
                                        <label for="ten" class="form-label">Ten <span class="text-danger">*</span></label>
                                        <input type="text" class="form-control" id="ten" name="ten"
                                               value="${thiSinh.ten}" required>
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="col-md-6 mb-3">
                                        <label for="ngaySinh" class="form-label">Ngay sinh</label>
                                        <input type="date" class="form-control" id="ngaySinh" name="ngaySinh" 
                                               value="${thiSinh.ngaySinh}">
                                    </div>
                                    <div class="col-md-6 mb-3">
                                        <label class="form-label">Gioi tinh</label>
                                        <div>
                                            <div class="form-check form-check-inline">
                                                <input class="form-check-input" type="radio" name="gioiTinh" 
                                                       id="gioiTinhNam" value="Nam" 
                                                       ${thiSinh.gioiTinh == 'Nam' ? 'checked' : ''}>
                                                <label class="form-check-label" for="gioiTinhNam">Nam</label>
                                            </div>
                                            <div class="form-check form-check-inline">
                                                <input class="form-check-input" type="radio" name="gioiTinh" 
                                                       id="gioiTinhNu" value="Nu" 
                                                       ${thiSinh.gioiTinh == 'Nu' ? 'checked' : ''}>
                                                <label class="form-check-label" for="gioiTinhNu">Nu</label>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="col-md-12 mb-3">
                                        <label for="noiSinh" class="form-label">Noi sinh</label>
                                        <input type="text" class="form-control" id="noiSinh" name="noiSinh" 
                                               value="${thiSinh.noiSinh}">
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-lg-4">
                    <div class="card mb-4">
                        <div class="card-header">
                            <i class="bi bi-telephone me-2"></i>Thong tin lien he
                        </div>
                        <div class="card-body">
                            <div class="mb-3">
                                <label for="dienThoai" class="form-label">Dien thoai <span class="text-danger">*</span></label>
                                <input type="tel" class="form-control" id="dienThoai" name="dienThoai" 
                                       value="${thiSinh.dienThoai}" required>
                            </div>
                            <div class="mb-3">
                                <label for="email" class="form-label">Email <span class="text-danger">*</span></label>
                                <input type="email" class="form-control" id="email" name="email" 
                                       value="${thiSinh.email}" required>
                            </div>
                        </div>
                    </div>

                    <div class="card mb-4">
                        <div class="card-header">
                            <i class="bi bi-star me-2"></i>Uu tien xet tuyen
                        </div>
                        <div class="card-body">
                            <div class="mb-3">
                                <label for="doituongId" class="form-label">Doi tuong uu tien</label>
                                <select class="form-select" id="doituongId" name="doituongId">
                                    <option value="">-- Chon doi tuong --</option>
                                    <c:forEach var="dt" items="${danhSachDoiTuong}">
                                        <option value="${dt.doituongId}" 
                                                ${thiSinh.doiTuongUutien != null && thiSinh.doiTuongUutien.doituongId == dt.doituongId ? 'selected' : ''}>
                                            ${dt.maDoituong} - ${dt.tenDoituong}
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="mb-3">
                                <label for="khuvucId" class="form-label">Khu vuc uu tien</label>
                                <select class="form-select" id="khuvucId" name="khuvucId">
                                    <option value="">-- Chon khu vuc --</option>
                                    <c:forEach var="kv" items="${danhSachKhuVuc}">
                                        <option value="${kv.khuvucId}" 
                                                ${thiSinh.khuVucUutien != null && thiSinh.khuVucUutien.khuvucId == kv.khuvucId ? 'selected' : ''}>
                                            ${kv.maKhuvuc} - ${kv.tenKhuvuc}
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                    </div>

                    <div class="d-grid gap-2">
                        <button type="submit" class="btn btn-primary">
                            <i class="bi bi-check2 me-2"></i>Luu thay doi
                        </button>
                        <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-secondary">
                            <i class="bi bi-arrow-left me-2"></i>Quay lai
                        </a>
                    </div>
                </div>
            </div>
        </form>
    </div>

    <jsp:include page="footer.jsp"/>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>

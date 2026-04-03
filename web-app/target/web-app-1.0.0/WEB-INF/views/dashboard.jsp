<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Trang chủ - Tuyển sinh ĐH 2026</title>
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
        .stat-card {
            background: white;
            border-radius: 12px;
            padding: 1.5rem;
            text-align: center;
            box-shadow: 0 2px 10px rgba(0,0,0,0.05);
            transition: transform 0.3s, box-shadow 0.3s;
            height: 100%;
        }
        .stat-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 5px 20px rgba(0,0,0,0.1);
        }
        .stat-icon {
            font-size: 2.5rem;
            margin-bottom: 0.5rem;
        }
        .stat-number {
            font-size: 2rem;
            font-weight: 700;
            color: #0d6efd;
        }
        .stat-label {
            color: #6c757d;
            font-size: 0.9rem;
        }
        .card {
            border: none;
            border-radius: 12px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.05);
        }
        .card-header {
            background: white;
            border-bottom: 2px solid #f5f5f5;
            padding: 1rem 1.5rem;
            font-weight: 600;
        }
        .card-header .btn {
            font-weight: 500;
        }
        .table {
            margin-bottom: 0;
        }
        .table th {
            background: #f8f9fa;
            font-weight: 600;
            border: none;
            padding: 12px;
        }
        .table td {
            vertical-align: middle;
            padding: 12px;
        }
        .badge-pending {
            background: #ffc107;
            color: #000;
        }
        .badge-success {
            background: #198754;
        }
        .badge-danger {
            background: #dc3545;
        }
        .badge-info {
            background: #0dcaf0;
            color: #000;
        }
        .quick-action-btn {
            padding: 1.5rem;
            text-align: center;
            border-radius: 12px;
            transition: all 0.3s;
            text-decoration: none;
            display: block;
        }
        .quick-action-btn:hover {
            transform: translateY(-3px);
            box-shadow: 0 5px 15px rgba(0,0,0,0.1);
        }
        .quick-action-btn .icon {
            font-size: 2rem;
            margin-bottom: 0.5rem;
        }
        .quick-action-btn .label {
            font-weight: 600;
            color: #333;
        }
    </style>
</head>
<body>
    <jsp:include page="header.jsp"/>

    <div class="page-header">
        <div class="container">
            <div class="d-flex justify-content-between align-items-center">
                <div>
                    <h2 class="mb-1"><i class="bi bi-house-door me-2"></i>Xin chào, ${thiSinh.ho} ${thiSinh.ten}!</h2>
                    <p class="mb-0 opacity-75">Chào mừng bạn đến với hệ thống tuyển sinh đại học 2026</p>
                </div>
                <div class="text-end">
                    <small class="opacity-75">Số báo danh:</small>
                    <h5 class="mb-0">${thiSinh.sobaodanh != null ? thiSinh.sobaodanh : 'Chưa cấp'}</h5>
                </div>
            </div>
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

        <div class="row g-4 mb-4">
            <div class="col-md-4">
                <div class="stat-card">
                    <div class="stat-icon text-primary">
                        <i class="bi bi-list-ol"></i>
                    </div>
                    <div class="stat-number">${soLuongNguyenVong}</div>
                    <div class="stat-label">Nguyện vọng đăng ký</div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="stat-card">
                    <div class="stat-icon text-success">
                        <i class="bi bi-clipboard-check"></i>
                    </div>
                    <div class="stat-number">${soLuongDiemThi}</div>
                    <div class="stat-label">Bảng điểm thi</div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="stat-card">
                    <div class="stat-icon text-info">
                        <i class="bi bi-check-circle"></i>
                    </div>
                    <div class="stat-number">${soTrungTuyen}</div>
                    <div class="stat-label">Nguyện vọng trúng tuyển</div>
                </div>
            </div>
        </div>

        <div class="row g-4 mb-4">
            <div class="col-md-3">
                <a href="${pageContext.request.contextPath}/profile" class="quick-action-btn bg-white">
                    <div class="icon text-primary"><i class="bi bi-person-gear"></i></div>
                    <div class="label">Cập nhật hồ sơ</div>
                </a>
            </div>
            <div class="col-md-3">
                <a href="${pageContext.request.contextPath}/scores" class="quick-action-btn bg-white">
                    <div class="icon text-success"><i class="bi bi-plus-circle"></i></div>
                    <div class="label">Nhập điểm thi</div>
                </a>
            </div>
            <div class="col-md-3">
                <a href="${pageContext.request.contextPath}/add-nguyenvong" class="quick-action-btn bg-white">
                    <div class="icon text-warning"><i class="bi bi-plus-square"></i></div>
                    <div class="label">Đăng ký nguyện vọng</div>
                </a>
            </div>
            <div class="col-md-3">
                <a href="${pageContext.request.contextPath}/nguyenvong" class="quick-action-btn bg-white">
                    <div class="icon text-info"><i class="bi bi-list-check"></i></div>
                    <div class="label">Quản lý nguyện vọng</div>
                </a>
            </div>
        </div>

        <div class="row g-4">
            <div class="col-lg-8">
                <div class="card">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <span><i class="bi bi-list-ol me-2"></i>Nguyện vọng gần nhất</span>
                        <a href="${pageContext.request.contextPath}/nguyenvong" class="btn btn-sm btn-outline-primary">
                            Xem tất cả <i class="bi bi-arrow-right ms-1"></i>
                        </a>
                    </div>
                    <div class="table-responsive">
                        <table class="table table-hover mb-0">
                            <thead>
                                <tr>
                                    <th>STT</th>
                                    <th>Mã ngành</th>
                                    <th>Tên ngành</th>
                                    <th>Phương thức</th>
                                    <th>Điểm xét tuyển</th>
                                    <th>Kết quả</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${not empty danhSachNguyenVong}">
                                        <c:forEach var="nv" items="${danhSachNguyenVong}" varStatus="loop">
                                            <c:if test="${loop.index < 5}">
                                                <tr>
                                                    <td><strong>${nv.thuTu}</strong></td>
                                                    <td>${nv.nganh.maNganh}</td>
                                                    <td>${nv.nganh.tenNganh}</td>
                                                    <td>${nv.phuongThuc.tenPhuongthuc}</td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${nv.diemXettuyen != null}">
                                                                <fmt:formatNumber value="${nv.diemXettuyen}" pattern="#,##0.00"/>
                                                            </c:when>
                                                            <c:otherwise>-</c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${nv.ketQua == 'TRUNG_TUYEN'}">
                                                                <span class="badge badge-success">Trúng tuyển</span>
                                                            </c:when>
                                                            <c:when test="${nv.ketQua == 'TRUOT'}">
                                                                <span class="badge badge-danger">Trượt</span>
                                                            </c:when>
                                                            <c:when test="${nv.ketQua == 'CHO_XET'}">
                                                                <span class="badge badge-pending">Chờ xét</span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="badge badge-info">${nv.ketQua}</span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                </tr>
                                            </c:if>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <tr>
                                            <td colspan="6" class="text-center text-muted py-4">
                                                <i class="bi bi-inbox fs-4 d-block mb-2"></i>
                                                Bạn chưa đăng ký nguyện vọng nào.
                                                <br>
                                                <a href="${pageContext.request.contextPath}/add-nguyenvong" class="btn btn-sm btn-primary mt-2">
                                                    <i class="bi bi-plus-circle me-1"></i>Đăng ký nguyện vọng
                                                </a>
                                            </td>
                                        </tr>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <div class="col-lg-4">
                <div class="card">
                    <div class="card-header">
                        <i class="bi bi-person-badge me-2"></i>Thông tin cá nhân
                    </div>
                    <div class="card-body">
                        <table class="table table-sm mb-0">
                            <tr>
                                <td class="text-muted">CCCD:</td>
                                <td class="fw-semibold">${thiSinh.cccd}</td>
                            </tr>
                            <tr>
                                <td class="text-muted">Ngày sinh:</td>
                                <td>
                                    ${not empty thiSinh.ngaySinhDisplay ? thiSinh.ngaySinhDisplay : '-'}
                                </td>
                            </tr>
                            <tr>
                                <td class="text-muted">Giới tính:</td>
                                <td>${thiSinh.gioiTinh}</td>
                            </tr>
                            <tr>
                                <td class="text-muted">Điện thoại:</td>
                                <td>${thiSinh.dienThoai}</td>
                            </tr>
                            <tr>
                                <td class="text-muted">Email:</td>
                                <td>${thiSinh.email}</td>
                            </tr>
                            <tr>
                                <td class="text-muted">Nơi sinh:</td>
                                <td>${thiSinh.noiSinh}</td>
                            </tr>
                        </table>
                        <div class="mt-3">
                            <a href="${pageContext.request.contextPath}/profile" class="btn btn-outline-primary btn-sm w-100">
                                <i class="bi bi-pencil-square me-1"></i>Chỉnh sửa thông tin
                            </a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <jsp:include page="footer.jsp"/>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>

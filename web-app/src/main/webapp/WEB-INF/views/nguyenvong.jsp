<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nguyen vong - Tuyen Sinh DH 2026</title>
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
            padding: 1rem 1.5rem;
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
        .btn-primary {
            background: #0d6efd;
            border: none;
            padding: 10px 20px;
            font-weight: 600;
            border-radius: 8px;
        }
        .btn-primary:hover {
            background: #0a58ca;
        }
        .btn-danger {
            padding: 6px 12px;
            font-size: 0.85rem;
        }
        .badge-trung-tuyen {
            background: #198754;
        }
        .badge-truot {
            background: #dc3545;
        }
        .badge-cho-xet {
            background: #ffc107;
            color: #000;
        }
        .badge-phoi-du-kien {
            background: #0dcaf0;
            color: #000;
        }
        .priority-badge {
            display: inline-block;
            width: 28px;
            height: 28px;
            line-height: 28px;
            text-align: center;
            border-radius: 50%;
            background: #0d6efd;
            color: white;
            font-weight: 700;
        }
    </style>
</head>
<body>
    <jsp:include page="header.jsp"/>

    <div class="page-header">
        <div class="container">
            <div class="d-flex justify-content-between align-items-center">
                <div>
                    <h2 class="mb-1"><i class="bi bi-list-ol me-2"></i>Nguyen vong xet tuyen</h2>
                    <p class="mb-0 opacity-75">Quan ly danh sach nguyen vong dang ky</p>
                </div>
                <a href="${pageContext.request.contextPath}/add-nguyenvong" class="btn btn-light">
                    <i class="bi bi-plus-circle me-2"></i>Them nguyen vong
                </a>
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

        <div class="alert alert-info mb-4">
            <i class="bi bi-info-circle me-2"></i>
            <strong>Luu y:</strong> Ban co the dang ky toi da <strong>5 nguyen vong</strong>. 
            Cac nguyen vong se duoc sap xep theo thu tu uu tien, nguyen vong 1 la uu tien cao nhat.
        </div>

        <div class="card">
            <div class="card-header">
                <i class="bi bi-list-ul me-2"></i>Danh sach nguyen vong
                <span class="badge bg-primary ms-2">${danhSachNguyenVong.size()} / 5</span>
            </div>
            <div class="table-responsive">
                <table class="table table-hover mb-0">
                    <thead>
                        <tr>
                            <th style="width: 60px;">Thu tu</th>
                            <th>Ma nganh</th>
                            <th>Ten nganh</th>
                            <th>To hop</th>
                            <th>Phuong thuc</th>
                            <th>Diem xet tuyen</th>
                            <th>Diem san</th>
                            <th>Ket qua</th>
                            <th style="width: 100px;">Thao tac</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty danhSachNguyenVong}">
                                <c:forEach var="nv" items="${danhSachNguyenVong}">
                                    <tr>
                                        <td>
                                            <span class="priority-badge">${nv.thuTu}</span>
                                        </td>
                                        <td>
                                            <strong>${nv.nganh.maNganh}</strong>
                                        </td>
                                        <td>${nv.nganh.tenNganh}</td>
                                        <td>
                                            <span class="badge bg-secondary">${nv.nganhToHop.toHop.maTohop}</span>
                                            <br>
                                            <small class="text-muted">${nv.nganhToHop.toHop.tenTohop}</small>
                                        </td>
                                        <td>
                                            <span class="badge bg-info text-dark">${nv.phuongThuc.maPhuongthuc}</span>
                                            <br>
                                            <small class="text-muted">${nv.phuongThuc.tenPhuongthuc}</small>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${nv.diemXettuyen != null}">
                                                    <strong class="text-primary">
                                                        <fmt:formatNumber value="${nv.diemXettuyen}" pattern="#,##0.00"/>
                                                    </strong>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="text-muted">-</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${nv.nganh.diemSan != null}">
                                                    <fmt:formatNumber value="${nv.nganh.diemSan}" pattern="#,##0.00"/>
                                                </c:when>
                                                <c:otherwise>-</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${nv.ketQua == 'TRUNG_TUYEN'}">
                                                    <span class="badge badge-trung-tuyen">
                                                        <i class="bi bi-check-circle me-1"></i>Trung tuyen
                                                    </span>
                                                </c:when>
                                                <c:when test="${nv.ketQua == 'TRUOT'}">
                                                    <span class="badge badge-truot">
                                                        <i class="bi bi-x-circle me-1"></i>Truot
                                                    </span>
                                                </c:when>
                                                <c:when test="${nv.ketQua == 'CHO_XET'}">
                                                    <span class="badge badge-cho-xet">
                                                        <i class="bi bi-clock me-1"></i>Cho xet
                                                    </span>
                                                </c:when>
                                                <c:when test="${nv.ketQua == 'PHOI_DU_KIEN'}">
                                                    <span class="badge badge-phoi-du-kien">
                                                        <i class="bi bi-question-circle me-1"></i>Phoi du kien
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge bg-secondary">${nv.ketQua}</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <form action="${pageContext.request.contextPath}/nguyenvong" method="post" 
                                                  onsubmit="return confirm('Ban co chac chan muon xoa nguyen vong nay?')">
                                                <input type="hidden" name="action" value="delete">
                                                <input type="hidden" name="nguyenvongId" value="${nv.nguyenvongId}">
                                                <button type="submit" class="btn btn-danger">
                                                    <i class="bi bi-trash"></i>
                                                </button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="9" class="text-center text-muted py-5">
                                        <i class="bi bi-inbox fs-1 d-block mb-3"></i>
                                        <p class="mb-0">Ban chua dang ky nguyen vong nao.</p>
                                        <p class="small">Nhan nut "Them nguyen vong" de bat dau.</p>
                                        <a href="${pageContext.request.contextPath}/add-nguyenvong" class="btn btn-primary mt-2">
                                            <i class="bi bi-plus-circle me-2"></i>Them nguyen vong
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

    <jsp:include page="footer.jsp"/>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>

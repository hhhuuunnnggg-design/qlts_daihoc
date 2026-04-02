<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Điểm thi - Tuyển sinh ĐH 2026</title>
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
        .score-badge {
            font-size: 0.9rem;
            padding: 0.5rem 1rem;
            border-radius: 8px;
        }
        .score-high {
            background: #d1e7dd;
            color: #0f5132;
        }
        .score-medium {
            background: #fff3cd;
            color: #664d03;
        }
        .score-low {
            background: #f8d7da;
            color: #842029;
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
        .modal-header {
            background: linear-gradient(135deg, #0d6efd 0%, #0a58ca 100%);
            color: white;
        }
        .form-label {
            font-weight: 500;
        }
    </style>
</head>
<body>
    <jsp:include page="header.jsp"/>

    <div class="page-header">
        <div class="container">
            <div class="d-flex justify-content-between align-items-center">
                <div>
                    <h2 class="mb-1"><i class="bi bi-clipboard-check me-2"></i>Điểm thi</h2>
                    <p class="mb-0 opacity-75">Quản lý thông tin điểm thi tuyển sinh</p>
                </div>
                <button type="button" class="btn btn-light" data-bs-toggle="modal" data-bs-target="#addScoreModal">
                    <i class="bi bi-plus-circle me-2"></i>Nhập điểm mới
                </button>
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

        <div class="card">
            <div class="card-header">
                <i class="bi bi-list-ul me-2"></i>Danh sách điểm thi
            </div>
            <div class="table-responsive">
                <table class="table table-hover mb-0">
                    <thead>
                        <tr>
                            <th>STT</th>
                            <th>Phương thức</th>
                            <th>Số báo danh</th>
                            <th>Năm tuyển sinh</th>
                            <th>Điểm tổng kết</th>
                            <th>Thang điểm</th>
                            <th>Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty danhSachDiemThi}">
                                <c:forEach var="dt" items="${danhSachDiemThi}" varStatus="loop">
                                    <tr>
                                        <td><strong>${loop.index + 1}</strong></td>
                                        <td>
                                            <span class="badge bg-primary">${dt.phuongThuc.maPhuongthuc}</span>
                                            <br>
                                            <small class="text-muted">${dt.phuongThuc.tenPhuongthuc}</small>
                                        </td>
                                        <td>${dt.sobaodanh != null ? dt.sobaodanh : '-'}</td>
                                        <td>${dt.namTuyensinh}</td>
                                        <td>
                                            <c:if test="${not empty dt.danhSachDiemChiTiet}">
                                                <c:set var="tongDiem" value="0"/>
                                                <c:forEach var="ct" items="${dt.danhSachDiemChiTiet}">
                                                    <c:if test="${ct.diemGoc != null}">
                                                        <c:set var="tongDiem" value="${tongDiem + ct.diemGoc}"/>
                                                    </c:if>
                                                </c:forEach>
                                                <fmt:formatNumber value="${tongDiem}" pattern="#,##0.00"/>
                                            </c:if>
                                        </td>
                                        <td>${dt.phuongThuc.thangDiem}</td>
                                        <td>
                                            <button type="button" class="btn btn-sm btn-outline-primary" 
                                                    onclick="viewScoreDetails(${dt.diemthiId}, '${dt.phuongThuc.tenPhuongthuc}')">
                                                <i class="bi bi-eye"></i> Chi tiết
                                            </button>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="7" class="text-center text-muted py-5">
                                        <i class="bi bi-clipboard-x fs-1 d-block mb-3"></i>
                                        <p class="mb-0">Bạn chưa có thông tin điểm thi nào.</p>
                                        <p class="small">Nhấn nút «Nhập điểm mới» để thêm điểm thi.</p>
                                    </td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <div class="modal fade" id="addScoreModal" tabindex="-1">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title"><i class="bi bi-plus-circle me-2"></i>Nhập điểm thi mới</h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                </div>
                <form action="${pageContext.request.contextPath}/scores" method="post">
                    <div class="modal-body">
                        <div class="mb-3">
                            <label for="phuongthucId" class="form-label">Phương thức xét tuyển <span class="text-danger">*</span></label>
                            <select class="form-select" id="phuongthucId" name="phuongthucId" required>
                                <option value="">-- Chọn phương thức --</option>
                                <c:forEach var="pt" items="${danhSachPhuongThuc}">
                                    <option value="${pt.phuongthucId}">${pt.maPhuongthuc} - ${pt.tenPhuongthuc}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="mb-3">
                            <label for="namTuyensinh" class="form-label">Năm tuyển sinh</label>
                            <input type="number" class="form-control" id="namTuyensinh" name="namTuyensinh" 
                                   value="2026" required>
                        </div>
                        <div class="mb-3">
                            <label for="sobaodanh" class="form-label">Số báo danh</label>
                            <input type="text" class="form-control" id="sobaodanh" name="sobaodanh">
                        </div>
                        <hr>
                        <h6 class="mb-3"><i class="bi bi-pencil me-2"></i>Nhập điểm theo môn</h6>
                        <div class="row" id="scoreInputs">
                            <c:forEach var="mon" items="${danhSachMon}">
                                <div class="col-md-6 mb-3">
                                    <label for="diem_${mon.monId}" class="form-label">${mon.tenMon} (${mon.maMon})</label>
                                    <input type="number" step="0.01" min="0" max="10" 
                                           class="form-control" id="diem_${mon.monId}" 
                                           name="diem_${mon.monId}" placeholder="Nhập điểm">
                                </div>
                            </c:forEach>
                        </div>
                        <div class="mb-3">
                            <label for="ghiChu" class="form-label">Ghi chú</label>
                            <textarea class="form-control" id="ghiChu" name="ghiChu" rows="2"></textarea>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                        <button type="submit" class="btn btn-primary">
                            <i class="bi bi-check2 me-2"></i>Lưu điểm thi
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <div class="modal fade" id="scoreDetailsModal" tabindex="-1">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="scoreDetailsTitle">Chi tiết điểm thi</h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body" id="scoreDetailsBody">
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button>
                </div>
            </div>
        </div>
    </div>

    <jsp:include page="footer.jsp"/>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        const scoreDetails = {};
        <c:forEach var="dt" items="${danhSachDiemThi}">
            scoreDetails[${dt.diemthiId}] = {
                phuongThuc: '${dt.phuongThuc.tenPhuongthuc}',
                soBaoDanh: '${dt.sobaodanh}',
                namTuyenSinh: '${dt.namTuyensinh}',
                ghiChu: '${dt.ghiChu}',
                chiTiet: []
            };
            <c:forEach var="ct" items="${dt.danhSachDiemChiTiet}">
                scoreDetails[${dt.diemthiId}].chiTiet.push({
                    mon: '${ct.mon.tenMon}',
                    maMon: '${ct.mon.maMon}',
                    diemGoc: '${ct.diemGoc}',
                    diemQuyDoi: '${ct.diemQuydoi}',
                    diemSuDung: '${ct.diemSudung}'
                });
            </c:forEach>
        </c:forEach>

        function viewScoreDetails(id, title) {
            const data = scoreDetails[id];
            if (!data) return;

            document.getElementById('scoreDetailsTitle').textContent = 'Chi tiết: ' + title;
            
            let html = '<table class="table table-sm">';
            html += '<tr><td class="fw-semibold" style="width:150px;">Phương thức:</td><td>' + data.phuongThuc + '</td></tr>';
            html += '<tr><td class="fw-semibold">Số báo danh:</td><td>' + (data.soBaoDanh || '-') + '</td></tr>';
            html += '<tr><td class="fw-semibold">Năm tuyển sinh:</td><td>' + data.namTuyenSinh + '</td></tr>';
            if (data.ghiChu) {
                html += '<tr><td class="fw-semibold">Ghi chú:</td><td>' + data.ghiChu + '</td></tr>';
            }
            html += '</table>';
            
            if (data.chiTiet.length > 0) {
                html += '<hr><h6>Điểm chi tiết theo môn:</h6>';
                html += '<table class="table table-bordered">';
                html += '<thead class="table-light"><tr><th>Môn</th><th>Mã môn</th><th>Điểm gốc</th><th>Điểm quy đổi</th><th>Điểm sử dụng</th></tr></thead>';
                html += '<tbody>';
                data.chiTiet.forEach(function(ct) {
                    html += '<tr>';
                    html += '<td>' + ct.mon + '</td>';
                    html += '<td>' + ct.maMon + '</td>';
                    html += '<td>' + (ct.diemGoc || '-') + '</td>';
                    html += '<td>' + (ct.diemQuyDoi || '-') + '</td>';
                    html += '<td>' + (ct.diemSuDung || '-') + '</td>';
                    html += '</tr>';
                });
                html += '</tbody></table>';
            }
            
            document.getElementById('scoreDetailsBody').innerHTML = html;
            
            const modal = new bootstrap.Modal(document.getElementById('scoreDetailsModal'));
            modal.show();
        }
    </script>
</body>
</html>

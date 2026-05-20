<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tra cứu điểm xét tuyển - TUYỂN SINH ĐẠI HỌC SÀI GÒN 2026</title>
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
        .form-label {
            font-weight: 500;
        }
        .result-card {
            border-left: 4px solid #0d6efd;
        }
        .result-pass {
            border-left-color: #198754;
        }
        .result-fail {
            border-left-color: #dc3545;
        }
        .score-display {
            font-size: 1.5rem;
            font-weight: 700;
        }
        .score-label {
            font-size: 0.8rem;
            color: #6c757d;
            text-transform: uppercase;
        }
        .divider {
            border-top: 1px dashed #dee2e6;
            margin: 1.5rem 0;
        }
        .info-box {
            background: #e7f1ff;
            border: 1px solid #b6d4fe;
            border-radius: 8px;
            padding: 1rem;
        }
        .info-box.warning {
            background: #fff3cd;
            border-color: #ffecb5;
        }
        .pt-badge {
            display: inline-block;
            padding: 0.25rem 0.75rem;
            border-radius: 6px;
            font-weight: 600;
            font-size: 0.85rem;
        }
        .pt-dgnl { background: #d1e7dd; color: #0f5132; }
        .pt-vsat { background: #fff3cd; color: #664d03; }
        .pt-thpt { background: #f8d7da; color: #842029; }
        .table-bordered-custom {
            border: 1px solid #dee2e6;
            border-radius: 8px;
            overflow: hidden;
        }
        .table-bordered-custom th {
            background: #f8f9fa;
            font-weight: 600;
        }
        .btn-primary {
            background: #0d6efd;
            border: none;
            padding: 10px 20px;
            font-weight: 600;
            border-radius: 8px;
        }
        .btn-primary:hover { background: #0a58ca; }
    </style>
</head>
<body>
    <jsp:include page="header.jsp"/>

    <div class="page-header">
        <div class="container">
            <div>
                <h2 class="mb-1"><i class="bi bi-calculator me-2"></i>Tra cứu điểm xét tuyển</h2>
                <p class="mb-0 opacity-75">Tính và kiểm tra điểm xét tuyển theo từng phương thức</p>
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

        <div class="row g-4">
            <!-- Form nhập thông tin -->
            <div class="col-lg-5">
                <div class="card">
                    <div class="card-header">
                        <i class="bi bi-sliders me-2"></i>Nhập thông tin xét tuyển
                    </div>
                    <div class="card-body">
                        <form id="traCuuForm" action="${pageContext.request.contextPath}/tra-cuu-diem" method="post">

                            <!-- Chọn phương thức -->
                            <div class="mb-3">
                                <label class="form-label">Phương thức xét tuyển <span class="text-danger">*</span></label>
                                <select class="form-select" id="phuongThucSelect" name="phuongThucId" required onchange="onPhuongThucChange()">
                                    <option value="">-- Chọn phương thức --</option>
                                    <c:forEach var="pt" items="${danhSachPhuongThuc}">
                                        <c:if test="${pt.maPhuongthuc.contains('THPT') || pt.maPhuongthuc.contains('DGNL') || pt.maPhuongthuc.contains('VSAT') || pt.maPhuongthuc.contains('PT2') || pt.maPhuongthuc.contains('PT3')}">
                                            <option value="${pt.phuongthucId}"
                                                data-is-dgnl="${pt.maPhuongthuc.contains('DGNL') || pt.maPhuongthuc.contains('PT2') || pt.tenPhuongthuc.contains('DANH GIA')}"
                                                data-is-vsat="${pt.maPhuongthuc.contains('VSAT') || pt.maPhuongthuc.contains('PT3') || pt.tenPhuongthuc.contains('VSAT')}"
                                                data-is-thpt="${!(pt.maPhuongthuc.contains('DGNL') || pt.maPhuongthuc.contains('PT2') || pt.tenPhuongthuc.contains('DANH GIA') || pt.maPhuongthuc.contains('VSAT') || pt.maPhuongthuc.contains('PT3') || pt.tenPhuongthuc.contains('VSAT'))}"
                                                ${param.phuongThucId == pt.phuongthucId ? 'selected' : ''}>
                                                ${pt.maPhuongthuc} - ${pt.tenPhuongthuc}
                                            </option>
                                        </c:if>
                                    </c:forEach>
                                </select>
                            </div>

                            <!-- Chọn ngành + tổ hợp (cascading) -->
                            <div class="mb-3">
                                <label class="form-label">Ngành xét tuyển <span class="text-danger">*</span></label>
                                <select class="form-select" id="nganhSelect" required onchange="onNganhChange()">
                                    <option value="">-- Chọn ngành --</option>
                                    <c:forEach var="nganh" items="${danhSachNganh}">
                                        <option value="${nganh.nganhId}">${nganh.maNganh} - ${nganh.tenNganh}</option>
                                    </c:forEach>
                                </select>
                                <input type="hidden" id="nganhToHopId" name="nganhToHopId" value="">
                            </div>

                            <div class="mb-3" id="toHopGroup" style="display:none;">
                                <label class="form-label">Tổ hợp môn <span class="text-danger">*</span></label>
                                <select class="form-select" id="toHopSelect" required onchange="onToHopChange()">
                                    <option value="">-- Chọn tổ hợp --</option>
                                </select>
                            </div>

                            <div class="divider"></div>

                            <!-- Ưu tiên -->
                            <h6 class="mb-3"><i class="bi bi-star me-1"></i>Thông tin ưu tiên</h6>

                            <div class="row">
                                <div class="col-md-6 mb-3">
                                    <label class="form-label">Đối tượng ưu tiên</label>
                                    <select class="form-select" name="doiTuongId">
                                        <option value="">-- Không chọn --</option>
                                        <c:forEach var="dt" items="${danhSachDoiTuong}">
                                            <option value="${dt.doituongId}">${dt.maDoituong} - ${dt.tenDoituong} (+${dt.mucDiem} điểm)</option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div class="col-md-6 mb-3">
                                    <label class="form-label">Khu vực ưu tiên</label>
                                    <select class="form-select" name="khuVucId">
                                        <option value="">-- Không chọn --</option>
                                        <c:forEach var="kv" items="${danhSachKhuVuc}">
                                            <option value="${kv.khuvucId}">${kv.maKhuVuc} - ${kv.tenKhuvuc} (+${kv.mucDiem} điểm)</option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>

                            <div class="divider"></div>

                            <!-- Nhập điểm DGNL -->
                            <div id="dgnlSection" style="display:none;">
                                <h6 class="mb-3"><i class="bi bi-card-text me-1"></i>Điểm Đánh giá Năng lực</h6>
                                <div class="info-box mb-3">
                                    <i class="bi bi-info-circle me-2"></i>
                                    Điểm ĐGNL được nhập theo <strong>thang điểm 1200</strong>. Hệ thống sẽ tự động quy đổi về thang điểm 30.
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">Tổng điểm ĐGNL (thang 1200)</label>
                                    <input type="number" step="1" min="0" max="1200" class="form-control" id="diemDgnl" name="diemDgnl" placeholder="VD: 850">
                                </div>
                            </div>

                            <!-- Nhập điểm THPT/VSAT -->
                            <div id="thptVsatSection" style="display:none;">
                                <h6 class="mb-3"><i class="bi bi-card-text me-1"></i>Điểm thi THPT / VSAT</h6>
                                <div class="info-box mb-3">
                                    <i class="bi bi-info-circle me-2"></i>
                                    Nhập điểm từng môn theo <strong>thang điểm 10</strong>. Hệ thống sẽ tự động quy đổi và tính điểm xét tuyển.
                                </div>
                                <div id="monInputsContainer">
                                    <!-- Sẽ được điền bằng JavaScript khi chọn tổ hợp -->
                                </div>
                            </div>

                            <div class="mt-3">
                                <button type="submit" class="btn btn-primary w-100">
                                    <i class="bi bi-calculator me-2"></i>Tính điểm xét tuyển
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>

            <!-- Kết quả -->
            <div class="col-lg-7">
                <c:choose>
                    <c:when test="${not empty ketQua}">
                        <%-- Thông tin ngành --%>
                        <div class="card mb-3 result-card">
                            <div class="card-header">
                                <i class="bi bi-building me-2"></i>Thông tin ngành xét tuyển
                            </div>
                            <div class="card-body">
                                <div class="row">
                                    <div class="col-md-6">
                                        <div class="mb-2">
                                            <span class="score-label">Ngành</span>
                                            <div class="fw-semibold">
                                                ${ketQua.nganh.maNganh} - ${ketQua.nganh.tenNganh}
                                            </div>
                                        </div>
                                        <div class="mb-2">
                                            <span class="score-label">Phương thức</span>
                                            <div>
                                                <span class="pt-badge pt-${ketQua.phuongThuc.maPhuongthuc.contains('DGNL') || ketQua.phuongThuc.maPhuongthuc.contains('PT2') ? 'dgnl' : ketQua.phuongThuc.maPhuongthuc.contains('VSAT') || ketQua.phuongThuc.maPhuongthuc.contains('PT3') ? 'vsat' : 'thpt'}">
                                                    ${ketQua.phuongThuc.maPhuongthuc} - ${ketQua.phuongThuc.tenPhuongthuc}
                                                </span>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-md-6">
                                        <div class="mb-2">
                                            <span class="score-label">Tổ hợp môn</span>
                                            <div class="fw-semibold">
                                                ${ketQua.nganhToHop.ma} - ${ketQua.nganhToHop.ten}
                                            </div>
                                        </div>
                                        <div class="mb-2">
                                            <span class="score-label">Độ lệch</span>
                                            <div>${ketQua.nganhToHop.doLech != null && ketQua.nganhToHop.doLech > 0 ? ketQua.nganhToHop.doLech : '0.00'} điểm</div>
                                        </div>
                                    </div>
                                </div>
                                <div class="row mt-2">
                                    <div class="col-md-4">
                                        <span class="score-label">Điểm sàn ngành</span>
                                        <div class="fw-semibold text-primary">
                                            <fmt:formatNumber value="${ketQua.nganh.diemSan != null ? ketQua.nganh.diemSan : 0}" pattern="#,##0.00"/>
                                        </div>
                                    </div>
                                    <div class="col-md-4">
                                        <span class="score-label">Điểm trúng tuyển</span>
                                        <div class="fw-semibold text-success">
                                            <fmt:formatNumber value="${ketQua.nganh.diemTrungTuyen != null ? ketQua.nganh.diemTrungTuyen : '---'}" pattern="#,##0.00"/>
                                        </div>
                                    </div>
                                    <div class="col-md-4">
                                        <span class="score-label">Chỉ tiêu</span>
                                        <div class="fw-semibold">${ketQua.nganh.chiTieu}</div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <%-- Kết quả DGNL --%>
                        <c:if test="${ketQua.phuongThuc.maPhuongthuc.contains('DGNL') || ketQua.phuongThuc.maPhuongthuc.contains('PT2')}">
                            <div class="card mb-3 result-card">
                                <div class="card-header">
                                    <i class="bi bi-bar-chart-steps me-2"></i>Chi tiết tính điểm ĐGNL
                                </div>
                                <div class="card-body">

                                    <%-- Công thức điểm DGNL --%>
                                    <div class="info-box mb-3" style="background:#e7f1ff;border:1px solid #b6d4fe;">
                                        <div class="mb-1"><strong><i class="bi bi-calculator me-1"></i>Công thức áp dụng (theo tài liệu SGU)</strong></div>
                                        <div class="small">
                                            <strong>Bước 1:</strong> Điểm tổ hợp gốc (ĐTHGXT) = ĐDGNL &times; 30 / 1200<br>
                                            <strong>Bước 2:</strong> Trừ độ lệch tổ hợp (nếu có)<br>
                                            <strong>Bước 3:</strong> Điểm xét tuyển = ĐTHGXT + Điểm cộng (≤ 30)
                                        </div>
                                    </div>

                                    <table class="table table-bordered-custom table-sm mb-3">
                                        <thead>
                                            <tr>
                                                <th style="width:40px;">Bước</th>
                                                <th>Nội dung</th>
                                                <th class="text-end">Kết quả</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <tr>
                                                <td class="text-center"><span class="badge bg-primary">1</span></td>
                                                <td>
                                                    Điểm ĐGNL gốc (thang 1200)
                                                    <div class="small text-muted">Nhập điểm bài thi ĐGNL của thí sinh</div>
                                                </td>
                                                <td class="text-end fw-bold text-dark">
                                                    <fmt:formatNumber value="${ketQua.diemDgnl != null ? ketQua.diemDgnl : 0}" pattern="#,##0.00"/>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td class="text-center"><span class="badge bg-info text-dark">2</span></td>
                                                <td>
                                                    Tính điểm tổ hợp gốc (ĐTHGXT) theo tổ hợp ${ketQua.nganhToHop.ma}
                                                    <div class="small text-muted">ĐTHGXT = ĐDGNL &times; 30 / 1200</div>
                                                </td>
                                                <td class="text-end">
                                                    <c:choose>
                                                        <c:when test="${not empty ketQua.diemThgxtDisplay}">
                                                            <div class="fw-bold text-primary">${ketQua.diemThgxtDisplay}</div>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="text-muted">Chưa nhập điểm</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td class="text-center"><span class="badge bg-secondary">3</span></td>
                                                <td>
                                                    Trừ độ lệch tổ hợp
                                                    <c:if test="${not empty ketQua.ghiChuDoLech}">
                                                        <div class="small text-muted">${ketQua.ghiChuDoLech}</div>
                                                    </c:if>
                                                </td>
                                                <td class="text-end">
                                                    <fmt:formatNumber value="${ketQua.diemThgxt}" pattern="#,##0.000"/>
                                                    <small class="text-muted"> / 30</small>
                                                </td>
                                            </tr>
                                            <tr class="table-light">
                                                <td colspan="3" class="text-center fw-bold">
                                                    <i class="bi bi-arrow-down me-1"></i>TÍNH ĐIỂM XÉT TUYỂN (ĐXT = ĐTHGXT + Điểm cộng ưu tiên)
                                                </td>
                                            </tr>
                                        </tbody>
                                    </table>

                                    <div class="row g-3">
                                        <div class="col-md-3 text-center">
                                            <div class="small text-muted text-uppercase">Điểm tổ hợp gốc (ĐTHGXT)</div>
                                            <div class="score-display text-secondary">
                                                <fmt:formatNumber value="${ketQua.diemThgxt}" pattern="#,##0.000"/>
                                            </div>
                                        </div>
                                        <div class="col-md-3 text-center">
                                            <div class="small text-muted text-uppercase">Điểm cộng ưu tiên</div>
                                            <div class="score-display text-warning">
                                                <fmt:formatNumber value="${ketQua.diemCong}" pattern="#,##0.000"/>
                                            </div>
                                            <c:if test="${not empty ketQua.diemUuTienMap.ghiChuCong}">
                                                <div class="small text-muted">${ketQua.diemUuTienMap.ghiChuCong}</div>
                                            </c:if>
                                            <c:if test="${ketQua.diemUuTienMap.kvDiem > 0}">
                                                <div class="small text-muted">${ketQua.diemUuTienMap.ghiChuKv}</div>
                                            </c:if>
                                            <c:if test="${ketQua.diemUuTienMap.dtDiem > 0}">
                                                <div class="small text-muted">${ketQua.diemUuTienMap.ghiChuDt}</div>
                                            </c:if>
                                        </div>
                                        <div class="col-md-6 text-center border-start border-2">
                                            <div class="small text-muted text-uppercase">Điểm xét tuyển (ĐXT)</div>
                                            <div class="score-display text-success">
                                                <fmt:formatNumber value="${ketQua.diemXetTuyen}" pattern="#,##0.000"/>
                                            </div>
                                            <div class="small text-muted">/ 30 điểm</div>
                                        </div>
                                    </div>

                                    <c:if test="${empty ketQua.diemDgnl}">
                                        <div class="alert alert-warning mt-2 mb-0 py-2">
                                            <i class="bi bi-exclamation-triangle me-1"></i>
                                            Vui lòng nhập điểm ĐGNL để xem kết quả tính toán.
                                        </div>
                                    </c:if>
                                </div>
                            </div>
                        </c:if>

                        <%-- Kết quả VSAT --%>
                        <c:if test="${(ketQua.phuongThuc.maPhuongthuc.contains('VSAT') || ketQua.phuongThuc.maPhuongthuc.contains('PT3') || ketQua.phuongThuc.tenPhuongthuc.contains('VSAT')) && not (ketQua.phuongThuc.maPhuongthuc.contains('DGNL') || ketQua.phuongThuc.maPhuongthuc.contains('PT2'))}">
                            <div class="card mb-3 result-card">
                                <div class="card-header">
                                    <i class="bi bi-bar-chart-steps me-2"></i>Chi tiết tính điểm ${ketQua.phuongThuc.tenPhuongthuc}
                                </div>
                                <div class="card-body">

                                    <%-- Công thức VSAT --%>
                                    <div class="info-box mb-3" style="background:#fff3cd;border:1px solid #ffecb5;">
                                        <div class="mb-1"><strong><i class="bi bi-calculator me-1"></i>Công thức áp dụng (theo tài liệu SGU)</strong></div>
                                        <div class="small">
                                            <strong>Bước 1:</strong> Điểm gốc nhập vào theo <strong>thang điểm 150</strong><br>
                                            <strong>Bước 2:</strong> Quy đổi từng môn: tìm trong bảng quy đổi VSAT theo <code>phuongthuc_id</code> + <code>mon_id</code>. Nếu không có → <code>ĐQD = ĐGốc × 10 / 150</code><br>
                                            <strong>Bước 3:</strong> Điểm tổ hợp gốc (ĐTHGXT) = (d<sub>1</sub>&times;w<sub>1</sub> + d<sub>2</sub>&times;w<sub>2</sub> + d<sub>3</sub>&times;w<sub>3</sub>) / (w<sub>1</sub>+w<sub>2</sub>+w<sub>3</sub>) &times; 3<br>
                                            <strong>Bước 4:</strong> Trừ độ lệch tổ hợp (nếu có)<br>
                                            <strong>Bước 5:</strong> Điểm xét tuyển = ĐTHGXT + Điểm cộng (≤ 30)
                                        </div>
                                    </div>

                                    <%-- Bảng điểm các môn --%>
                                    <table class="table table-bordered-custom table-sm mb-3">
                                        <thead>
                                            <tr>
                                                <th>Môn</th>
                                                <th class="text-center">Hệ số</th>
                                                <th class="text-end">Điểm gốc (thang 150)</th>
                                                <th class="text-end">Sau quy đổi (thang 10)</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="m" items="${ketQua.diemMonList}">
                                                <tr>
                                                    <td>
                                                        <strong>${m.tenMon}</strong>
                                                        <span class="badge bg-secondary ms-1">${m.maMon}</span>
                                                        <c:if test="${m.heSo > 1}">
                                                            <span class="badge bg-warning text-dark ms-1">Hệ số ${m.heSo}</span>
                                                        </c:if>
                                                        <c:if test="${not empty m.ghiChuQd}">
                                                            <div class="small text-muted">${m.ghiChuQd}</div>
                                                        </c:if>
                                                    </td>
                                                    <td class="text-center">${m.heSo}</td>
                                                    <td class="text-end">
                                                        <c:choose>
                                                            <c:when test="${m.diemGoc != null}">
                                                                <fmt:formatNumber value="${m.diemGoc}" pattern="#,##0.00"/>
                                                            </c:when>
                                                            <c:otherwise><span class="text-muted">-</span></c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td class="text-end fw-bold text-primary">
                                                        <c:choose>
                                                            <c:when test="${m.diemSauQd != null}">
                                                                <fmt:formatNumber value="${m.diemSauQd}" pattern="#,##0.000"/>
                                                            </c:when>
                                                            <c:otherwise><span class="text-muted">-</span></c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>

                                    <table class="table table-bordered-custom table-sm mb-3">
                                        <thead>
                                            <tr>
                                                <th style="width:40px;">Bước</th>
                                                <th>Nội dung</th>
                                                <th class="text-end">Kết quả</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <tr>
                                                <td class="text-center"><span class="badge bg-primary">1</span></td>
                                                <td>
                                                    Tính điểm tổ hợp gốc (ĐTHGXT)
                                                    <c:if test="${not empty ketQua.diemThgxtDisplay}">
                                                        <div class="small text-muted fst-italic">${ketQua.diemThgxtDisplay}</div>
                                                    </c:if>
                                                </td>
                                                <td class="text-end fw-bold text-primary">
                                                    <fmt:formatNumber value="${ketQua.diemThgxt}" pattern="#,##0.000"/>
                                                    <small class="text-muted"> / 30</small>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td class="text-center"><span class="badge bg-secondary">2</span></td>
                                                <td>
                                                    Trừ độ lệch tổ hợp
                                                    <c:if test="${not empty ketQua.ghiChuDoLech}">
                                                        <div class="small text-muted">${ketQua.ghiChuDoLech}</div>
                                                    </c:if>
                                                </td>
                                                <td class="text-end fw-bold">
                                                    <fmt:formatNumber value="${ketQua.diemThxt}" pattern="#,##0.000"/>
                                                    <small class="text-muted"> / 30</small>
                                                </td>
                                            </tr>
                                            <tr class="table-light">
                                                <td colspan="3" class="text-center fw-bold">
                                                    <i class="bi bi-arrow-down me-1"></i>TÍNH ĐIỂM XÉT TUYỂN (ĐXT = ĐTHGXT + Điểm cộng ưu tiên)
                                                </td>
                                            </tr>
                                        </tbody>
                                    </table>

                                    <div class="row g-3">
                                        <div class="col-md-3 text-center">
                                            <div class="small text-muted text-uppercase">Điểm tổ hợp gốc (ĐTHGXT)</div>
                                            <div class="score-display text-secondary">
                                                <fmt:formatNumber value="${ketQua.diemThgxt}" pattern="#,##0.000"/>
                                            </div>
                                        </div>
                                        <div class="col-md-3 text-center">
                                            <div class="small text-muted text-uppercase">Điểm cộng ưu tiên</div>
                                            <div class="score-display text-warning">
                                                <fmt:formatNumber value="${ketQua.diemCong}" pattern="#,##0.000"/>
                                            </div>
                                            <c:if test="${not empty ketQua.diemUuTienMap.ghiChuCong}">
                                                <div class="small text-muted">${ketQua.diemUuTienMap.ghiChuCong}</div>
                                            </c:if>
                                            <c:if test="${ketQua.diemUuTienMap.kvDiem > 0}">
                                                <div class="small text-muted">${ketQua.diemUuTienMap.ghiChuKv}</div>
                                            </c:if>
                                            <c:if test="${ketQua.diemUuTienMap.dtDiem > 0}">
                                                <div class="small text-muted">${ketQua.diemUuTienMap.ghiChuDt}</div>
                                            </c:if>
                                        </div>
                                        <div class="col-md-6 text-center border-start border-2">
                                            <div class="small text-muted text-uppercase">Điểm xét tuyển (ĐXT)</div>
                                            <div class="score-display text-success">
                                                <fmt:formatNumber value="${ketQua.diemXetTuyen}" pattern="#,##0.000"/>
                                            </div>
                                            <div class="small text-muted">/ 30 điểm</div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </c:if>

                        <%-- Kết quả THPT --%>
                        <c:if test="${not (ketQua.phuongThuc.maPhuongthuc.contains('DGNL') || ketQua.phuongThuc.maPhuongthuc.contains('PT2') || ketQua.phuongThuc.maPhuongthuc.contains('VSAT') || ketQua.phuongThuc.maPhuongthuc.contains('PT3') || ketQua.phuongThuc.tenPhuongthuc.contains('VSAT'))}">
                            <div class="card mb-3 result-card">
                                <div class="card-header">
                                    <i class="bi bi-bar-chart-steps me-2"></i>Chi tiết tính điểm ${ketQua.phuongThuc.tenPhuongthuc}
                                </div>
                                <div class="card-body">

                                    <%-- Công thức THPT --%>
                                    <div class="info-box mb-3" style="background:#e7f1ff;border:1px solid #b6d4fe;">
                                        <div class="mb-1"><strong><i class="bi bi-calculator me-1"></i>Công thức áp dụng (theo tài liệu SGU)</strong></div>
                                        <div class="small">
                                            <strong>Bước 1:</strong> Quy đổi điểm mỗi môn về thang 10 (nếu có bảng quy đổi)<br>
                                            <strong>Bước 2:</strong> Điểm tổ hợp gốc (ĐTHGXT) = (d<sub>1</sub>&times;w<sub>1</sub> + d<sub>2</sub>&times;w<sub>2</sub> + d<sub>3</sub>&times;w<sub>3</sub>) / (w<sub>1</sub>+w<sub>2</sub>+w<sub>3</sub>) &times; 3<br>
                                            <strong>Bước 3:</strong> Trừ độ lệch tổ hợp (nếu có)<br>
                                            <strong>Bước 4:</strong> Điểm xét tuyển = ĐTHGXT + Điểm cộng (≤ 30)
                                        </div>
                                    </div>

                                    <%-- Bảng điểm các môn --%>
                                    <table class="table table-bordered-custom table-sm mb-3">
                                        <thead>
                                            <tr>
                                                <th>Môn</th>
                                                <th class="text-center">Hệ số</th>
                                                <th class="text-end">Điểm gốc</th>
                                                <th class="text-end">Sau quy đổi</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="m" items="${ketQua.diemMonList}">
                                                <tr>
                                                    <td>
                                                        <strong>${m.tenMon}</strong>
                                                        <span class="badge bg-secondary ms-1">${m.maMon}</span>
                                                        <c:if test="${m.heSo > 1}">
                                                            <span class="badge bg-warning text-dark ms-1">Hệ số ${m.heSo}</span>
                                                        </c:if>
                                                        <c:if test="${not empty m.ghiChuQd}">
                                                            <div class="small text-muted">${m.ghiChuQd}</div>
                                                        </c:if>
                                                    </td>
                                                    <td class="text-center">${m.heSo}</td>
                                                    <td class="text-end">
                                                        <c:choose>
                                                            <c:when test="${m.diemGoc != null}">
                                                                <fmt:formatNumber value="${m.diemGoc}" pattern="#,##0.00"/>
                                                            </c:when>
                                                            <c:otherwise><span class="text-muted">-</span></c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td class="text-end fw-bold text-primary">
                                                        <c:choose>
                                                            <c:when test="${m.diemSauQd != null}">
                                                                <fmt:formatNumber value="${m.diemSauQd}" pattern="#,##0.000"/>
                                                            </c:when>
                                                            <c:otherwise><span class="text-muted">-</span></c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>

                                    <table class="table table-bordered-custom table-sm mb-3">
                                        <thead>
                                            <tr>
                                                <th style="width:40px;">Bước</th>
                                                <th>Nội dung</th>
                                                <th class="text-end">Kết quả</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <tr>
                                                <td class="text-center"><span class="badge bg-primary">1</span></td>
                                                <td>
                                                    Tính điểm tổ hợp gốc (ĐTHGXT)
                                                    <c:if test="${not empty ketQua.diemThgxtDisplay}">
                                                        <div class="small text-muted fst-italic">${ketQua.diemThgxtDisplay}</div>
                                                    </c:if>
                                                </td>
                                                <td class="text-end fw-bold text-primary">
                                                    <fmt:formatNumber value="${ketQua.diemThgxt}" pattern="#,##0.000"/>
                                                    <small class="text-muted"> / 30</small>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td class="text-center"><span class="badge bg-secondary">2</span></td>
                                                <td>
                                                    Trừ độ lệch tổ hợp
                                                    <c:if test="${not empty ketQua.ghiChuDoLech}">
                                                        <div class="small text-muted">${ketQua.ghiChuDoLech}</div>
                                                    </c:if>
                                                </td>
                                                <td class="text-end fw-bold">
                                                    <fmt:formatNumber value="${ketQua.diemThxt}" pattern="#,##0.000"/>
                                                    <small class="text-muted"> / 30</small>
                                                </td>
                                            </tr>
                                            <tr class="table-light">
                                                <td colspan="3" class="text-center fw-bold">
                                                    <i class="bi bi-arrow-down me-1"></i>TÍNH ĐIỂM XÉT TUYỂN (ĐXT = ĐTHGXT + Điểm cộng ưu tiên)
                                                </td>
                                            </tr>
                                        </tbody>
                                    </table>

                                    <div class="row g-3">
                                        <div class="col-md-3 text-center">
                                            <div class="small text-muted text-uppercase">Điểm tổ hợp gốc (ĐTHGXT)</div>
                                            <div class="score-display text-secondary">
                                                <fmt:formatNumber value="${ketQua.diemThgxt}" pattern="#,##0.000"/>
                                            </div>
                                        </div>
                                        <div class="col-md-3 text-center">
                                            <div class="small text-muted text-uppercase">Điểm cộng ưu tiên</div>
                                            <div class="score-display text-warning">
                                                <fmt:formatNumber value="${ketQua.diemCong}" pattern="#,##0.000"/>
                                            </div>
                                            <c:if test="${not empty ketQua.diemUuTienMap.ghiChuCong}">
                                                <div class="small text-muted">${ketQua.diemUuTienMap.ghiChuCong}</div>
                                            </c:if>
                                            <c:if test="${ketQua.diemUuTienMap.kvDiem > 0}">
                                                <div class="small text-muted">${ketQua.diemUuTienMap.ghiChuKv}</div>
                                            </c:if>
                                            <c:if test="${ketQua.diemUuTienMap.dtDiem > 0}">
                                                <div class="small text-muted">${ketQua.diemUuTienMap.ghiChuDt}</div>
                                            </c:if>
                                        </div>
                                        <div class="col-md-6 text-center border-start border-2">
                                            <div class="small text-muted text-uppercase">Điểm xét tuyển (ĐXT)</div>
                                            <div class="score-display text-success">
                                                <fmt:formatNumber value="${ketQua.diemXetTuyen}" pattern="#,##0.000"/>
                                            </div>
                                            <div class="small text-muted">/ 30 điểm</div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </c:if>

                        <%-- Kết quả đạt/không đạt --%>
                        <div class="card mb-3 result-card ${ketQua.dat ? 'result-pass' : 'result-fail'}">
                            <div class="card-header">
                                <i class="bi bi-clipboard-check me-2"></i>Kết quả xét tuyển
                            </div>
                            <div class="card-body text-center">
                                <div class="mb-3">
                                    <c:choose>
                                        <c:when test="${ketQua.dat}">
                                            <div class="score-display text-success">
                                                <i class="bi bi-check-circle-fill me-2"></i>ĐẠT
                                            </div>
                                            <p class="text-muted mt-2 mb-0">
                                                Bạn đủ điều kiện xét tuyển vào ngành
                                                <strong>${ketQua.nganh.tenNganh}</strong>
                                                theo phương thức <strong>${ketQua.phuongThuc.tenPhuongthuc}</strong>.
                                            </p>
                                        </c:when>
                                        <c:otherwise>
                                            <div class="score-display text-danger">
                                                <i class="bi bi-x-circle-fill me-2"></i>KHÔNG ĐẠT
                                            </div>
                                            <p class="text-muted mt-2 mb-0">
                                                ${ketQua.lyDo}
                                            </p>
                                        </c:otherwise>
                                    </c:choose>
                                </div>

                                <div class="row g-3 mt-2">
                                    <div class="col-md-4">
                                        <div class="info-box">
                                            <div class="score-label">Điểm xét tuyển của bạn</div>
                                            <div class="score-display text-primary">
                                                <fmt:formatNumber value="${ketQua.diemXetTuyen}" pattern="#,##0.000"/>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-md-4">
                                        <div class="info-box warning">
                                            <div class="score-label">Điểm sàn ngành</div>
                                            <div class="score-display text-warning">
                                                <fmt:formatNumber value="${ketQua.nganh.diemSan != null ? ketQua.nganh.diemSan : 0}" pattern="#,##0.00"/>
                                            </div>
                                            <c:if test="${ketQua.dat && ketQua.nganh.diemSan != null}">
                                                <div class="mt-1">
                                                    <i class="bi bi-arrow-up text-success"></i>
                                                    <fmt:formatNumber value="${ketQua.diemXetTuyen.subtract(ketQua.nganh.diemSan)}" pattern="#,##0.000"/>
                                                    điểm trên sàn
                                                </div>
                                            </c:if>
                                        </div>
                                    </div>
                                    <div class="col-md-4">
                                        <div class="info-box warning">
                                            <div class="score-label">Điểm trúng tuyển</div>
                                            <div class="score-display text-success">
                                                <c:choose>
                                                    <c:when test="${ketQua.nganh.diemTrungTuyen != null}">
                                                        <fmt:formatNumber value="${ketQua.nganh.diemTrungTuyen}" pattern="#,##0.00"/>
                                                    </c:when>
                                                    <c:otherwise>---</c:otherwise>
                                                </c:choose>
                                            </div>
                                            <c:if test="${ketQua.dat && ketQua.nganh.diemTrungTuyen != null}">
                                                <div class="mt-1">
                                                    <i class="bi bi-arrow-up text-success"></i>
                                                    <fmt:formatNumber value="${ketQua.diemXetTuyen.subtract(ketQua.nganh.diemTrungTuyen)}" pattern="#,##0.000"/>
                                                    điểm trên điểm chuẩn
                                                </div>
                                            </c:if>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="card">
                            <div class="card-body text-center py-5">
                                <i class="bi bi-clipboard-data fs-1 text-muted d-block mb-3"></i>
                                <h5 class="text-muted">Chưa có kết quả tra cứu</h5>
                                <p class="text-muted">Vui lòng chọn phương thức, ngành và nhập điểm thi để tra cứu.</p>
                                <div class="info-box">
                                    <i class="bi bi-lightbulb me-2"></i>
                                    <strong>Hướng dẫn:</strong> Chọn phương thức xét tuyển, chọn ngành và tổ hợp môn,
                                    nhập điểm thi, sau đó nhấn <strong>"Tính điểm xét tuyển"</strong> để xem kết quả.
                                </div>
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>

    <jsp:include page="footer.jsp"/>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // Dữ liệu tổ hợp theo ngành từ server (DTO - tránh lazy loading)
        const nganhToHopData = [
            <c:forEach var="nth" items="${danhSachNganhToHopDto}" varStatus="loop">
                { nganhId: ${nth.nganhId}, id: ${nth.id}, ma: '${nth.ma}', ten: '${nth.ten}',
                  doLech: ${nth.doLech != null ? nth.doLech : 0},
                  monIds: [
                    <c:forEach var="m" items="${nth.monList}" varStatus="mLoop">
                        {monId: ${m.monId}, tenMon: '${m.tenMon}', maMon: '${m.maMon}', heSo: ${m.heSo}}
                        ${!mLoop.last ? ',' : ''}
                    </c:forEach>
                  ]
                }${!loop.last ? ',' : ''}
            </c:forEach>
        ];

        const allMonData = [
            <c:forEach var="mon" items="${danhSachMon}" varStatus="loop">
                { id: ${mon.monId}, ma: '${mon.maMon}', ten: '${mon.tenMon}' }${!loop.last ? ',' : ''}
            </c:forEach>
        ];

        function onNganhChange() {
            const nganhId = document.getElementById('nganhSelect').value;
            const toHopSelect = document.getElementById('toHopSelect');
            const toHopGroup = document.getElementById('toHopGroup');
            const monInputsContainer = document.getElementById('monInputsContainer');

            toHopSelect.innerHTML = '<option value="">-- Chọn tổ hợp --</option>';
            monInputsContainer.innerHTML = '';
            document.getElementById('nganhToHopId').value = '';

            if (!nganhId) {
                toHopGroup.style.display = 'none';
                return;
            }

            const filtered = nganhToHopData.filter(function(item) {
                return item.nganhId == nganhId;
            });

            if (filtered.length > 0) {
                toHopGroup.style.display = 'block';
                filtered.forEach(function(item) {
                    var opt = document.createElement('option');
                    opt.value = item.id;
                    opt.textContent = item.ma + ' - ' + item.ten;
                    toHopSelect.appendChild(opt);
                });
            } else {
                toHopGroup.style.display = 'none';
            }
        }

        function onToHopChange() {
            const toHopId = document.getElementById('toHopSelect').value;
            const nganhToHopIdInput = document.getElementById('nganhToHopId');
            const monInputsContainer = document.getElementById('monInputsContainer');

            monInputsContainer.innerHTML = '';

            if (!toHopId) {
                nganhToHopIdInput.value = '';
                return;
            }

            nganhToHopIdInput.value = toHopId;

            const selected = nganhToHopData.find(function(item) {
                return item.id == toHopId;
            });

            if (!selected) return;

            const phuongThucSelect = document.getElementById('phuongThucSelect');
            const selectedOpt = phuongThucSelect.options[phuongThucSelect.selectedIndex];
            const isDgnl = selectedOpt && selectedOpt.dataset.isDgnl === 'true';
            const isVsat = selectedOpt && selectedOpt.dataset.isVsat === 'true';

            if (!isDgnl && selected.monIds && selected.monIds.length > 0) {
                let html = '<div class="row">';
                selected.monIds.forEach(function(m) {
                    // VSAT: thang diem 150; THPT: thang diem 10
                    const maxVal = isVsat ? '150' : '10';
                    const thongBao = isVsat
                        ? '<small class="text-muted">Thang điểm 150</small>'
                        : '<small class="text-muted">Thang điểm 10</small>';
                    html += '<div class="col-md-6 mb-3">';
                    html += '<label class="form-label">' + m.tenMon + ' (' + m.maMon + ')'
                        + (m.heSo > 1 ? ' <span class="badge bg-warning text-dark">Hệ số ' + m.heSo + '</span>' : '')
                        + '</label>';
                    html += '<input type="number" step="0.01" min="0" max="' + maxVal + '" class="form-control" name="diem_mon_' + m.monId + '" placeholder="0.00">';
                    html += thongBao;
                    html += '</div>';
                });
                html += '</div>';
                monInputsContainer.innerHTML = html;
            }
        }

        function onPhuongThucChange() {
            const phuongThucSelect = document.getElementById('phuongThucSelect');
            const selectedOpt = phuongThucSelect.options[phuongThucSelect.selectedIndex];
            const isDgnl = selectedOpt && selectedOpt.dataset.isDgnl === 'true';

            document.getElementById('dgnlSection').style.display = isDgnl ? 'block' : 'none';
            document.getElementById('thptVsatSection').style.display = isDgnl ? 'none' : 'block';

            // Re-render mon inputs if tohop already selected
            onToHopChange();
        }

        // Init on page load
        document.addEventListener('DOMContentLoaded', function() {
            onPhuongThucChange();
        });
    </script>
</body>
</html>

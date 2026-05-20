<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tra cứu kết quả xét tuyển - TUYỂN SINH ĐẠI HỌC SÀI GÒN 2026</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <style>
        body { font-family: 'Segoe UI', Arial, sans-serif; background: #f0f2f5; }
        .search-header {
            background: linear-gradient(135deg, #0d6efd 0%, #0a58ca 100%);
            color: white;
            padding: 2.5rem 0;
            margin-bottom: 2rem;
        }
        .search-box {
            border: none;
            border-radius: 12px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.08);
            background: white;
            padding: 2rem;
        }
        .result-card {
            border: none;
            border-radius: 12px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.06);
            margin-bottom: 1.5rem;
            overflow: hidden;
        }
        .card-header-custom {
            background: white;
            border-bottom: 2px solid #f0f0f0;
            padding: 1rem 1.5rem;
            font-weight: 600;
        }
        .nv-badge {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 36px;
            height: 36px;
            border-radius: 50%;
            font-weight: 700;
            font-size: 1rem;
            background: #e7f1ff;
            color: #0d47a1;
            flex-shrink: 0;
        }
        .ketqua-badge {
            display: inline-block;
            padding: 0.3rem 0.8rem;
            border-radius: 6px;
            font-weight: 600;
            font-size: 0.85rem;
        }
        .ketqua-trung { background: #d1e7dd; color: #0f5132; }
        .ketqua-truot { background: #f8d7da; color: #842029; }
        .ketqua-cho { background: #fff3cd; color: #664d03; }
        .ketqua-phoi { background: #cfe2ff; color: #084298; }
        .ketqua-macdinh { background: #e2e3e5; color: #41464b; }
        .score-box {
            background: #f8f9fa;
            border-radius: 10px;
            padding: 1rem;
            text-align: center;
            border: 1px solid #e9ecef;
        }
        .score-big {
            font-size: 1.8rem;
            font-weight: 800;
            line-height: 1.2;
        }
        .score-label-sm {
            font-size: 0.7rem;
            text-transform: uppercase;
            color: #adb5bd;
            letter-spacing: 0.5px;
        }
        .pt-badge {
            display: inline-block;
            padding: 0.2rem 0.6rem;
            border-radius: 5px;
            font-weight: 600;
            font-size: 0.78rem;
        }
        .pt-dgnl { background: #d1e7dd; color: #0f5132; }
        .pt-vsat { background: #fff3cd; color: #664d03; }
        .pt-thpt { background: #f8d7da; color: #842029; }
        .pt-xtt { background: #e7f1ff; color: #0d47a1; }
        .pt-nk { background: #e2e3e5; color: #41464b; }
        .pt-macdinh { background: #e2e3e5; color: #41464b; }
        .divider-row { border-top: 1px dashed #dee2e6; margin: 1rem 0; }
        .diem-table { font-size: 0.85rem; }
        .diem-table th { background: #f8f9fa; font-weight: 600; white-space: nowrap; }
        .info-highlight {
            background: #e7f1ff;
            border: 1px solid #b6d4fe;
            border-radius: 8px;
            padding: 0.75rem 1rem;
        }
        .info-highlight.yellow {
            background: #fff8e1;
            border-color: #ffe082;
        }
        .thong-tin-ts {
            background: linear-gradient(135deg, #0d6efd 0%, #0a58ca 100%);
            color: white;
            border-radius: 10px;
            padding: 1.25rem 1.5rem;
            margin-bottom: 1.5rem;
        }
        .no-result {
            text-align: center;
            padding: 3rem;
            color: #6c757d;
        }
        .no-result i { font-size: 3rem; margin-bottom: 1rem; }
    </style>
</head>
<body>

<jsp:include page="header.jsp"/>

<!-- Search Header -->
<div class="search-header">
    <div class="container">
        <div class="text-center">
            <h2 class="mb-2"><i class="bi bi-search me-2"></i>Tra cứu kết quả xét tuyển</h2>
            <p class="mb-0 opacity-75">Nhập số CCCD/CMND hoặc SBD để xem chi tiết điểm xét tuyển theo từng phương thức và nguyện vọng</p>
        </div>
    </div>
</div>

<div class="container mb-5">

    <!-- Alert messages -->
    <c:if test="${not empty sessionScope.message}">
        <div class="alert alert-${sessionScope.messageType != null ? sessionScope.messageType : 'info'} alert-dismissible fade show" role="alert">
            ${sessionScope.message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        <c:remove var="message" scope="session"/>
        <c:remove var="messageType" scope="session"/>
    </c:if>

    <!-- Search Form -->
    <div class="row justify-content-center">
        <div class="col-lg-8">
            <div class="search-box">
                <form action="${pageContext.request.contextPath}/tra-cuu-diem-cccd" method="post">
                    <div class="row g-3 align-items-end">
                        <div class="col">
                            <label class="form-label fw-semibold">
                                <i class="bi bi-person-vcard me-1"></i>Số CCCD / CMND / SBD
                            </label>
                            <input type="text"
                                   class="form-control form-control-lg"
                                   name="cccd"
                                   placeholder="VD: 079123456789 hoặc TS_0013"
                                   value="${param.cccd}"
                                   required
                                   autofocus
                                   maxlength="15">
                        </div>
                        <div class="col-auto">
                            <button type="submit" class="btn btn-primary btn-lg px-4">
                                <i class="bi bi-search me-1"></i>Tra cứu
                            </button>
                        </div>
                    </div>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- Thông tin thí sinh + Kết quả -->
    <c:if test="${not empty thiSinh}">
        <div class="row mt-4">

            <!-- Thông tin thí sinh -->
            <div class="col-12">
                <div class="thong-tin-ts">
                    <div class="row align-items-center">
                        <div class="col-md-1 text-center">
                            <i class="bi bi-person-circle fs-1 opacity-75"></i>
                        </div>
                        <div class="col-md-11">
                            <div class="row">
                                <div class="col-md-3">
                                    <div class="score-label-sm mb-1">Họ và tên</div>
                                    <div class="fw-bold fs-5">${thiSinh.ho != null ? thiSinh.ho : ''} ${thiSinh.ten}</div>
                                </div>
                                <div class="col-md-2">
                                    <div class="score-label-sm mb-1">Ngày sinh</div>
                                    <div class="fw-semibold">
                                        <c:choose>
                                            <c:when test="${not empty thiSinh.ngaySinh}">
                                                ${thiSinh.ngaySinhDisplay}
                                            </c:when>
                                            <c:otherwise>—</c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                                <div class="col-md-2">
                                    <div class="score-label-sm mb-1">CCCD</div>
                                    <div class="fw-semibold">${thiSinh.cccd}</div>
                                </div>
                                <div class="col-md-2">
                                    <div class="score-label-sm mb-1">Số báo danh</div>
                                    <div class="fw-semibold">${thiSinh.sobaodanh != null ? thiSinh.sobaodanh : '—'}</div>
                                </div>
                        <div class="col-md-3">
                            <div class="score-label-sm mb-1">Đối tượng / Khu vực ưu tiên</div>
                            <div class="fw-semibold">
                                ${thiSinh.doiTuong != null ? thiSinh.doiTuong.tenDoituong : '—'}
                                ${thiSinh.doiTuong != null && thiSinh.khuVuc != null ? ' / ' : ''}
                                ${thiSinh.khuVuc != null ? thiSinh.khuVuc.tenKhuvuc : '—'}
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Danh sách nguyện vọng -->
        <c:choose>
            <c:when test="${not empty ketQuaList}">

                <!-- Tóm tắt -->
                <div class="row g-3 mb-4">
                    <div class="col-md-3">
                        <div class="score-box">
                            <div class="score-label-sm">Tổng nguyện vọng</div>
                            <div class="score-big text-primary">${ketQuaList.size()}</div>
                        </div>
                    </div>
                    <div class="col-md-3">
                        <div class="score-box">
                            <div class="score-label-sm">Trúng tuyển</div>
                            <div class="score-big text-success">
                                <c:set var="countTrung" value="0"/>
                                <c:forEach var="r" items="${ketQuaList}">
                                    <c:if test="${r.ketQua == 'TRUNG_TUYEN'}">
                                        <c:set var="countTrung" value="${countTrung + 1}"/>
                                    </c:if>
                                </c:forEach>
                                ${countTrung}
                            </div>
                        </div>
                    </div>
                    <div class="col-md-3">
                        <div class="score-box">
                            <div class="score-label-sm">Không trúng tuyển</div>
                            <div class="score-big text-danger">
                                <c:set var="countTruot" value="0"/>
                                <c:forEach var="r" items="${ketQuaList}">
                                    <c:if test="${r.ketQua == 'TRUOT'}">
                                        <c:set var="countTruot" value="${countTruot + 1}"/>
                                    </c:if>
                                </c:forEach>
                                ${countTruot}
                            </div>
                        </div>
                    </div>
                    <div class="col-md-3">
                        <div class="score-box">
                            <div class="score-label-sm">Chưa xét / Phôi</div>
                            <div class="score-big text-warning">
                                ${ketQuaList.size() - countTrung - countTruot}
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Chi tiết từng nguyện vọng -->
                <c:forEach var="r" items="${ketQuaList}" varStatus="st">
                    <div class="result-card">
                        <!-- Card Header: Nguyện vọng + Kết quả -->
                        <div class="card-header-custom d-flex align-items-center gap-3">
                            <div class="nv-badge">NV${r.thuTu}</div>
                            <div class="flex-grow-1">
                                <div class="fw-semibold fs-6">
                                    ${r.nganh.maNganh} — ${r.nganh.tenNganh}
                                </div>
                                <div class="small text-muted">
                                    Tổ hợp: <strong>${r.toHop.ma}</strong>
                                    ${r.toHop.ten != null ? '— ' : ''}${r.toHop.ten}
                                    ${r.toHop.doLech != null && r.toHop.doLech > 0 ? ' · Độ lệch: ' : ''}${r.toHop.doLech != null && r.toHop.doLech > 0 ? r.toHop.doLech : ''}
                                </div>
                            </div>
                            <div>
                                <c:choose>
                                    <c:when test="${r.ketQua == 'TRUNG_TUYEN'}">
                                        <span class="ketqua-badge ketqua-trung">
                                            <i class="bi bi-check-circle-fill me-1"></i>Trúng tuyển
                                        </span>
                                    </c:when>
                                    <c:when test="${r.ketQua == 'TRUOT'}">
                                        <span class="ketqua-badge ketqua-truot">
                                            <i class="bi bi-x-circle-fill me-1"></i>Không trúng tuyển
                                        </span>
                                    </c:when>
                                    <c:when test="${r.ketQua == 'CHO_XET'}">
                                        <span class="ketqua-badge ketqua-cho">
                                            <i class="bi bi-hourglass-split me-1"></i>Chờ xét
                                        </span>
                                    </c:when>
                                    <c:when test="${r.ketQua == 'PHOI_DU_KIEN'}">
                                        <span class="ketqua-badge ketqua-phoi">
                                            <i class="bi bi-info-circle-fill me-1"></i>Phôi dự kiến
                                        </span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="ketqua-badge ketqua-macdinh">
                                            <i class="bi bi-dash-circle-fill me-1"></i>Chưa xét
                                        </span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>

                        <div class="card-body p-3">

                            <!-- Thông tin điểm cơ bản -->
                            <div class="row g-3 mb-3">
                                <!-- Điểm sàn / Điểm chuẩn -->
                                <div class="col-md-4">
                                    <div class="info-highlight">
                                        <div class="d-flex justify-content-between align-items-center">
                                            <div>
                                                <div class="score-label-sm">Điểm sàn ngành</div>
                                                <div class="fw-bold text-primary fs-6">
                                                    <fmt:formatNumber value="${r.nganh.diemSan != null ? r.nganh.diemSan : 0}" pattern="#,##0.00"/>
                                                </div>
                                            </div>
                                            <i class="bi bi-arrows-expand text-primary fs-4 opacity-50"></i>
                                        </div>
                                        <div class="mt-2">
                                            <div class="score-label-sm">Điểm trúng tuyển</div>
                                            <div class="fw-bold text-success fs-6">
                                                <c:choose>
                                                    <c:when test="${r.nganh.diemTrungTuyen != null}">
                                                        <fmt:formatNumber value="${r.nganh.diemTrungTuyen}" pattern="#,##0.00"/>
                                                    </c:when>
                                                    <c:otherwise>—</c:otherwise>
                                                </c:choose>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <!-- Điểm xét tuyển chính (tốt nhất) -->
                                <div class="col-md-4">
                                    <div class="score-box" style="background:#d1e7dd;">
                                        <div class="score-label-sm">Điểm xét tuyển (tốt nhất)</div>
                                        <c:choose>
                                            <c:when test="${not empty r.diemInfo && r.diemInfo.diemXettuyen != null}">
                                                <div class="score-big text-success">
                                                    <fmt:formatNumber value="${r.diemInfo.diemXettuyen}" pattern="#,##0.000"/>
                                                </div>
                                                <div class="mt-1">
                                                    <c:choose>
                                                        <c:when test="${r.diemInfo.phuongThucDiemTotNhat == 'THPT'}">
                                                            <span class="pt-badge pt-thpt">THPT</span>
                                                        </c:when>
                                                        <c:when test="${r.diemInfo.phuongThucDiemTotNhat == 'VSAT'}">
                                                            <span class="pt-badge pt-vsat">VSAT</span>
                                                        </c:when>
                                                        <c:when test="${r.diemInfo.phuongThucDiemTotNhat == 'DGNL'}">
                                                            <span class="pt-badge pt-dgnl">DGNL</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="pt-badge pt-macdinh">${r.diemInfo.phuongThucDiemTotNhat}</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </div>
                                            </c:when>
                                            <c:otherwise>
                                                <div class="score-big text-muted">—</div>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>

                                <!-- Điểm cộng ưu tiên -->
                                <div class="col-md-4">
                                    <div class="score-box">
                                        <div class="score-label-sm">Điểm cộng ưu tiên</div>
                                        <c:choose>
                                            <c:when test="${not empty r.diemInfo && r.diemInfo.diemCong != null}">
                                                <div class="score-big text-warning">
                                                    <fmt:formatNumber value="${r.diemInfo.diemCong}" pattern="#,##0.000"/>
                                                </div>
                                                <div class="mt-1 small text-muted">
                                                    KV: <fmt:formatNumber value="${r.diemInfo.diemUutien != null ? r.diemInfo.diemUutien : 0}" pattern="#,##0.00"/> đ
                                                </div>
                                            </c:when>
                                            <c:otherwise>
                                                <div class="score-big text-muted">—</div>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                            </div>

                            <!-- Chi tiết điểm theo từng phương thức -->
                            <c:if test="${not empty r.danhSachDiemTheoPhuongThuc}">
                                <div class="divider-row"></div>
                                <h6 class="mb-3">
                                    <i class="bi bi-bar-chart-steps me-1"></i>
                                    Chi tiết điểm theo phương thức xét tuyển
                                </h6>

                                <div class="row g-3">
                                    <c:forEach var="pt" items="${r.danhSachDiemTheoPhuongThuc}">
                                        <div class="col-md-6 col-lg-4">
                                            <div class="score-box h-100">
                                                <!-- Tên phương thức -->
                                                <div class="mb-2">
                                                    <c:choose>
                                                        <c:when test="${pt.maPhuongThuc.contains('DGNL') || pt.maPhuongThuc.contains('PT2')}">
                                                            <span class="pt-badge pt-dgnl">${pt.maPhuongThuc}</span>
                                                        </c:when>
                                                        <c:when test="${pt.maPhuongThuc.contains('VSAT') || pt.maPhuongThuc.contains('PT3')}">
                                                            <span class="pt-badge pt-vsat">${pt.maPhuongThuc}</span>
                                                        </c:when>
                                                        <c:when test="${pt.maPhuongThuc.contains('THPT') || pt.maPhuongThuc.contains('PT1')}">
                                                            <span class="pt-badge pt-thpt">${pt.maPhuongThuc}</span>
                                                        </c:when>
                                                        <c:when test="${pt.maPhuongThuc == 'XTT'}">
                                                            <span class="pt-badge pt-xtt">${pt.maPhuongThuc}</span>
                                                        </c:when>
                                                        <c:when test="${pt.maPhuongThuc.contains('NK')}">
                                                            <span class="pt-badge pt-nk">${pt.maPhuongThuc}</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="pt-badge pt-macdinh">${pt.maPhuongThuc}</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                    <div class="small text-muted mt-1">${pt.tenPhuongThuc}</div>
                                                </div>

                                                <!-- Điểm xét tuyển -->
                                                <div class="mb-2">
                                                    <div class="score-label-sm">Điểm xét tuyển</div>
                                                    <div class="fw-bold text-primary fs-5">
                                                        <fmt:formatNumber value="${pt.diemXettuyen}" pattern="#,##0.000"/>
                                                        <small class="text-muted fw-normal">/ 30</small>
                                                    </div>
                                                </div>

                                                <!-- Công thức -->
                                                <div class="text-start">
                                                    <table class="table table-sm diem-table mb-1">
                                                        <tr>
                                                            <td class="text-muted ps-0" style="width:55%;">Điểm tổ hợp (ĐTHXT)</td>
                                                            <td class="text-end fw-semibold pe-0">
                                                                <fmt:formatNumber value="${pt.diemThxt != null ? pt.diemThxt : 0}" pattern="#,##0.000"/>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td class="text-muted ps-0">Điểm cộng (UT)</td>
                                                            <td class="text-end fw-semibold text-warning pe-0">
                                                                +<fmt:formatNumber value="${pt.diemCong != null ? pt.diemCong : 0}" pattern="#,##0.000"/>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td class="text-muted ps-0">Điểm cộng ưu tiên</td>
                                                            <td class="text-end fw-semibold text-info pe-0">
                                                                <fmt:formatNumber value="${pt.diemUutien != null ? pt.diemUutien : 0}" pattern="#,##0.000"/>
                                                            </td>
                                                        </tr>
                                                    </table>

                                                    <c:if test="${pt.coBangQuyDoi}">
                                                        <div class="small">
                                                            <i class="bi bi-patch-check-fill text-success me-1"></i>
                                                            <span class="text-success">Đã quy đổi qua Bảng quy đổi</span>
                                                        </div>
                                                    </c:if>

                                                    <c:if test="${not empty pt.ghiChu}">
                                                        <div class="small text-muted mt-1 fst-italic">
                                                                ${pt.ghiChu}
                                                        </div>
                                                    </c:if>
                                                </div>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </div>
                            </c:if>

                            <!-- So sánh điểm tốt nhất vs điểm sàn -->
                            <c:if test="${not empty r.diemInfo && r.diemInfo.diemXettuyen != null && r.nganh.diemSan != null}">
                                <div class="divider-row"></div>
                                <div class="row g-3">
                                    <div class="col-md-6">
                                        <div class="info-highlight yellow">
                                            <div class="d-flex justify-content-between align-items-center">
                                                <div>
                                                    <div class="score-label-sm">Điểm của bạn</div>
                                                    <div class="fw-bold text-primary fs-5">
                                                        <fmt:formatNumber value="${r.diemInfo.diemXettuyen}" pattern="#,##0.000"/>
                                                    </div>
                                                </div>
                                                <div>
                                                    <div class="score-label-sm">Điểm sàn</div>
                                                    <div class="fw-bold text-danger fs-5">
                                                        <fmt:formatNumber value="${r.nganh.diemSan}" pattern="#,##0.00"/>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-md-6 d-flex align-items-center">
                                        <c:choose>
                                            <c:when test="${r.ketQua == 'TRUNG_TUYEN'}">
                                                <div class="text-success fw-semibold">
                                                    <i class="bi bi-check-circle-fill me-1"></i>
                                                    Đạt điểm sàn ngành — Đủ điều kiện xét tuyển
                                                </div>
                                            </c:when>
                                            <c:otherwise>
                                                <div class="text-danger fw-semibold">
                                                    <i class="bi bi-x-circle-fill me-1"></i>
                                                    Dưới điểm sàn ngành
                                                    (<fmt:formatNumber value="${r.diemInfo.diemXettuyen.subtract(r.nganh.diemSan)}" pattern="#,##0.000"/> đ)
                                                </div>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                            </c:if>

                        </div>
                    </div>
                </c:forEach>

            </c:when>
            <c:otherwise>
                <div class="result-card mt-4">
                    <div class="card-body text-center py-5 no-result">
                        <i class="bi bi-inbox text-muted d-block mb-3"></i>
                        <h5 class="text-muted">Không có nguyện vọng xét tuyển</h5>
                        <p class="text-muted">Thí sinh chưa đăng ký nguyện vọng xét tuyển nào.</p>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </c:if>

    <!-- Hướng dẫn (khi chưa tra cứu) -->
    <c:if test="${empty thiSinh}">
        <div class="row mt-5">
            <div class="col-12">
                <div class="text-center text-muted py-5">
                    <i class="bi bi-arrow-down-circle fs-1 d-block mb-3 opacity-25"></i>
                    <p>Nhập số CCCD ở trên để tra cứu kết quả xét tuyển của bạn.</p>
                </div>
            </div>
        </div>
    </c:if>
</div>

<jsp:include page="footer.jsp"/>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>

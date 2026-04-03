<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thêm nguyện vọng - Tuyển sinh ĐH 2026</title>
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
        .info-box {
            background: #e7f1ff;
            border: 1px solid #b6d4fe;
            border-radius: 8px;
            padding: 1rem;
            margin-bottom: 1.5rem;
        }
        .info-box i {
            color: #0d6efd;
        }
        .slot-info {
            background: white;
            border-radius: 8px;
            padding: 1rem;
            margin-top: 1rem;
        }
        .slot-available {
            color: #198754;
        }
        .slot-full {
            color: #dc3545;
        }
    </style>
</head>
<body>
    <jsp:include page="header.jsp"/>

    <div class="page-header">
        <div class="container">
            <h2 class="mb-1"><i class="bi bi-plus-square me-2"></i>Thêm nguyện vọng</h2>
            <p class="mb-0 opacity-75">Đăng ký nguyện vọng xét tuyển mới</p>
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

        <div class="info-box">
            <div class="d-flex align-items-start">
                <i class="bi bi-info-circle-fill fs-4 me-3 mt-1"></i>
                <div>
                    <h5 class="mb-2">Hướng dẫn đăng ký nguyện vọng</h5>
                    <ul class="mb-0">
                        <li>Chọn ngành học bạn muốn xét tuyển</li>
                        <li>Chọn tổ hợp môn thi phù hợp với ngành</li>
                        <li>Chọn phương thức xét tuyển (tối đa 5 nguyện vọng)</li>
                        <li>Nguyện vọng sẽ tự động được sắp xếp theo thứ tự ưu tiên</li>
                    </ul>
                </div>
            </div>
        </div>

        <form action="${pageContext.request.contextPath}/add-nguyenvong" method="post" id="addNguyenVongForm">
            <div class="row">
                <div class="col-lg-8">
                    <div class="card mb-4">
                        <div class="card-header">
                            <i class="bi bi-pencil me-2"></i>Thông tin nguyện vọng
                        </div>
                        <div class="card-body">
                            <div class="form-section">
                                <div class="form-section-title">
                                    <i class="bi bi-building me-2"></i>Chọn ngành học
                                </div>
                                <div class="mb-3">
                                    <label for="nganhId" class="form-label">Ngành học <span class="text-danger">*</span></label>
                                    <select class="form-select" id="nganhId" name="nganhId" required>
                                        <option value="">-- Chọn ngành học --</option>
                                        <c:forEach var="nganh" items="${danhSachNganh}">
                                            <option value="${nganh.nganhId}">
                                                ${nganh.maNganh} - ${nganh.tenNganh}
                                            </option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div id="nganhInfo" class="slot-info d-none">
                                    <div class="row">
                                        <div class="col-md-4">
                                            <div class="text-muted small">Chỉ tiêu</div>
                                            <div class="fw-bold" id="chiTieu">-</div>
                                        </div>
                                        <div class="col-md-4">
                                            <div class="text-muted small">Điểm sàn</div>
                                            <div class="fw-bold" id="diemSan">-</div>
                                        </div>
                                        <div class="col-md-4">
                                            <div class="text-muted small">Trạng thái</div>
                                            <div class="fw-bold slot-available" id="slotStatus">Đang mở</div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="form-section">
                                <div class="form-section-title">
                                    <i class="bi bi-journal-text me-2"></i>Chọn tổ hợp môn
                                </div>
                                <div class="mb-3">
                                    <label for="nganhTohopId" class="form-label">Tổ hợp môn <span class="text-danger">*</span></label>
                                    <select class="form-select" id="nganhTohopId" name="nganhTohopId" required disabled>
                                        <option value="">-- Vui lòng chọn ngành học trước --</option>
                                    </select>
                                    <small class="text-muted">Chọn ngành học trước để hiển thị danh sách tổ hợp môn</small>
                                </div>
                                <div id="toHopInfo" class="slot-info d-none">
                                    <div class="row">
                                        <div class="col-md-6">
                                            <div class="text-muted small">Mã tổ hợp</div>
                                            <div class="fw-bold" id="toHopMa">-</div>
                                        </div>
                                        <div class="col-md-6">
                                            <div class="text-muted small">Tên tổ hợp</div>
                                            <div class="fw-bold" id="toHopTen">-</div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="form-section">
                                <div class="form-section-title">
                                    <i class="bi bi-check2-square me-2"></i>Chọn phương thức xét tuyển
                                </div>
                                <div class="mb-3">
                                    <label for="phuongthucId" class="form-label">Phương thức <span class="text-danger">*</span></label>
                                    <select class="form-select" id="phuongthucId" name="phuongthucId" required>
                                        <option value="">-- Chọn phương thức --</option>
                                        <c:forEach var="pt" items="${danhSachPhuongThuc}">
                                            <option value="${pt.phuongthucId}">
                                                ${pt.maPhuongthuc} - ${pt.tenPhuongthuc} (thang điểm: ${pt.thangDiem})
                                            </option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-lg-4">
                    <div class="card mb-4">
                        <div class="card-header">
                            <i class="bi bi-list-ol me-2"></i>Nguyện vọng hiện tại
                        </div>
                        <div class="card-body">
                            <div class="d-flex justify-content-between align-items-center mb-3">
                                <span>Số nguyện vọng:</span>
                                <span class="badge bg-primary fs-6">${soNguyenVongHienTai} / 5</span>
                            </div>
                            <div class="progress mb-3" style="height: 10px;">
                                <div class="progress-bar" role="progressbar" 
                                     style="width: ${soNguyenVongHienTai * 20}%"></div>
                            </div>
                            <c:choose>
                                <c:when test="${soNguyenVongHienTai >= 5}">
                                    <div class="alert alert-warning mb-0">
                                        <i class="bi bi-exclamation-triangle me-2"></i>
                                        Bạn đã hết suất đăng ký nguyện vọng.
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="slot-available">
                                        <i class="bi bi-check-circle me-2"></i>
                                        Bạn còn có thể đăng ký thêm ${5 - soNguyenVongHienTai} nguyện vọng
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <div class="card mb-4">
                        <div class="card-header">
                            <i class="bi bi-exclamation-circle me-2"></i>Lưu ý
                        </div>
                        <div class="card-body">
                            <ul class="mb-0 small">
                                <li>Nguyện vọng 1 có độ ưu tiên cao nhất</li>
                                <li>Nếu trúng tuyển nguyện vọng 1, các nguyện vọng sau sẽ không được xét</li>
                                <li>Điểm xét tuyển phụ thuộc vào phương thức và tổ hợp môn bạn chọn</li>
                            </ul>
                        </div>
                    </div>

                    <div class="d-grid gap-2">
                        <button type="submit" class="btn btn-primary" ${soNguyenVongHienTai >= 5 ? 'disabled' : ''}>
                            <i class="bi bi-check2 me-2"></i>Đăng ký nguyện vọng
                        </button>
                        <a href="${pageContext.request.contextPath}/nguyenvong" class="btn btn-secondary">
                            <i class="bi bi-arrow-left me-2"></i>Quay lại
                        </a>
                    </div>
                </div>
            </div>
        </form>
    </div>

    <jsp:include page="footer.jsp"/>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        const nganhToHopData = {};
        
        <c:forEach var="nt" items="${danhSachNganhToHop}">
            if (!nganhToHopData[${nt.nganh.nganhId}]) {
                nganhToHopData[${nt.nganh.nganhId}] = [];
            }
            nganhToHopData[${nt.nganh.nganhId}].push({
                id: ${nt.nganhTohopId},
                ma: '${nt.toHop.maTohop}',
                ten: '${nt.toHop.tenTohop}'
            });
        </c:forEach>

        const nganhInfoData = {};
        <c:forEach var="nganh" items="${danhSachNganh}">
            nganhInfoData[${nganh.nganhId}] = {
                chiTieu: ${nganh.chiTieu},
                diemSan: ${nganh.diemSan != null ? nganh.diemSan : 'null'}
            };
        </c:forEach>

        document.getElementById('nganhId').addEventListener('change', function() {
            const nganhId = this.value;
            const toHopSelect = document.getElementById('nganhTohopId');
            const nganhInfo = document.getElementById('nganhInfo');
            const toHopInfo = document.getElementById('toHopInfo');

            toHopSelect.innerHTML = '<option value="">-- Đang tải --</option>';
            toHopInfo.classList.add('d-none');

            if (nganhId && nganhToHopData[nganhId]) {
                toHopSelect.innerHTML = '<option value="">-- Chọn tổ hợp môn --</option>';
                nganhToHopData[nganhId].forEach(function(item) {
                    toHopSelect.innerHTML += '<option value="' + item.id + '">' + item.ma + ' - ' + item.ten + '</option>';
                });
                toHopSelect.disabled = false;

                const info = nganhInfoData[nganhId];
                document.getElementById('chiTieu').textContent = info.chiTieu;
                document.getElementById('diemSan').textContent = info.diemSan ? info.diemSan.toFixed(2) : 'Không có';
                nganhInfo.classList.remove('d-none');
            } else {
                toHopSelect.innerHTML = '<option value="">-- Vui lòng chọn ngành học trước --</option>';
                toHopSelect.disabled = true;
                nganhInfo.classList.add('d-none');
            }
        });

        document.getElementById('nganhTohopId').addEventListener('change', function() {
            const nganhId = document.getElementById('nganhId').value;
            const selectedId = parseInt(this.value);
            const toHopInfo = document.getElementById('toHopInfo');

            if (nganhId && selectedId && nganhToHopData[nganhId]) {
                const selected = nganhToHopData[nganhId].find(function(item) {
                    return item.id === selectedId;
                });
                if (selected) {
                    document.getElementById('toHopMa').textContent = selected.ma;
                    document.getElementById('toHopTen').textContent = selected.ten;
                    toHopInfo.classList.remove('d-none');
                }
            } else {
                toHopInfo.classList.add('d-none');
            }
        });
    </script>
</body>
</html>

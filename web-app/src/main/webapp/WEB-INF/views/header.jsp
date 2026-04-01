<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${pageTitle != null ? pageTitle : 'Tuyen Sinh Dai Hoc 2026'}</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css" rel="stylesheet">
    <style>
        body { font-family: 'Segoe UI', Arial, sans-serif; background: #f0f2f5; }
        .navbar-brand { font-weight: 700; font-size: 1.25rem; }
        .nav-link { font-weight: 500; }
        .nav-link.active { color: #0d6efd !important; font-weight: 600; }
    </style>
</head>
<body>
    <nav class="navbar navbar-expand-lg navbar-dark bg-primary shadow-sm">
        <div class="container">
            <a class="navbar-brand" href="${pageContext.request.contextPath}/dashboard">
                <i class="bi bi-mortarboard-fill me-2"></i>TUYEN SINH DH 2026
            </a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbarNav">
                <ul class="navbar-nav ms-auto">
                    <li class="nav-item">
                        <a class="nav-link ${currentPage == 'dashboard' ? 'active' : ''}"
                           href="${pageContext.request.contextPath}/dashboard">
                            <i class="bi bi-speedometer2 me-1"></i> Trang chu
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link ${currentPage == 'scores' ? 'active' : ''}"
                           href="${pageContext.request.contextPath}/scores">
                            <i class="bi bi-graph-up me-1"></i> Diem thi
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link ${currentPage == 'nguyenvong' ? 'active' : ''}"
                           href="${pageContext.request.contextPath}/nguyenvong">
                            <i class="bi bi-list-check me-1"></i> Nguyen vong
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link ${currentPage == 'profile' ? 'active' : ''}"
                           href="${pageContext.request.contextPath}/profile">
                            <i class="bi bi-person-circle me-1"></i> ${sessionScope.nguoidung != null ? sessionScope.nguoidung.username : 'Tai khoan'}
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link text-warning" href="${pageContext.request.contextPath}/logout">
                            <i class="bi bi-box-arrow-right me-1"></i> Dang xuat
                        </a>
                    </li>
                </ul>
            </div>
        </div>
    </nav>

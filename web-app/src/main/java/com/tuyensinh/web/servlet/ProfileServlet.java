package com.tuyensinh.web.servlet;

import com.tuyensinh.entity.DoiTuongUutien;
import com.tuyensinh.entity.KhuVucUutien;
import com.tuyensinh.entity.NguoiDung;
import com.tuyensinh.entity.ThiSinh;
import com.tuyensinh.service.DoiTuongService;
import com.tuyensinh.service.KhuVucService;
import com.tuyensinh.service.NguoiDungService;
import com.tuyensinh.service.ThiSinhService;
import com.tuyensinh.util.DateUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class ProfileServlet extends BaseServlet {

    private final ThiSinhService thiSinhService = new ThiSinhService();
    private final NguoiDungService nguoiDungService = new NguoiDungService();
    private final DoiTuongService doiTuongService = new DoiTuongService();
    private final KhuVucService khuVucService = new KhuVucService();

    @Override
    protected void handleGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        NguoiDung loggedInUser = getLoggedInUser(request);
        if (loggedInUser == null) {
            requireLogin(request, response);
            return;
        }

        try {
            ThiSinh thiSinh = thiSinhService.findByNguoiDungId(loggedInUser.getNguoidungId()).orElse(null);
            List<DoiTuongUutien> danhSachDoiTuong = doiTuongService.findAll();
            List<KhuVucUutien> danhSachKhuVuc = khuVucService.findAll();

            setAttribute(request, "thiSinh", thiSinh);
            setAttribute(request, "danhSachDoiTuong", danhSachDoiTuong);
            setAttribute(request, "danhSachKhuVuc", danhSachKhuVuc);

            forward(request, response, getViewPath("profile"));

        } catch (Exception e) {
            setMessage(request, "Da xay ra loi: " + e.getMessage(), "danger");
            redirect(response, request.getContextPath() + "/dashboard");
        }
    }

    @Override
    protected void handlePost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        NguoiDung loggedInUser = getLoggedInUser(request);
        if (loggedInUser == null) {
            requireLogin(request, response);
            return;
        }

        try {
            String cccd = request.getParameter("cccd");
            String ho = request.getParameter("ho");
            String ten = request.getParameter("ten");
            String ngaySinhStr = request.getParameter("ngaySinh");
            String gioiTinh = request.getParameter("gioiTinh");
            String dienThoai = request.getParameter("dienThoai");
            String email = request.getParameter("email");
            String noiSinh = request.getParameter("noiSinh");
            String doituongIdStr = request.getParameter("doituongId");
            String khuvucIdStr = request.getParameter("khuvucId");

            if (isNullOrEmpty(cccd) || isNullOrEmpty(ho) || isNullOrEmpty(ten)
                    || isNullOrEmpty(dienThoai) || isNullOrEmpty(email)) {
                setMessage(request, "CCCD, Ho, Ten, Dien thoai, Email la bat buoc.", "danger");
                redirect(response, request.getContextPath() + "/profile");
                return;
            }

            cccd = cccd.trim();
            if (!cccd.matches("[0-9]{9,12}")) {
                setMessage(request, "So CCCD phai la 9-12 chu so.", "danger");
                redirect(response, request.getContextPath() + "/profile");
                return;
            }

            Optional<ThiSinh> optTs = thiSinhService.findByNguoiDungId(loggedInUser.getNguoidungId());

            if (optTs.isEmpty()) {
                if (thiSinhService.findByCccd(cccd).isPresent()) {
                    setMessage(request, "So CCCD da duoc su dung boi tai khoan khac.", "danger");
                    redirect(response, request.getContextPath() + "/profile");
                    return;
                }
                NguoiDung nd = nguoiDungService.findById(loggedInUser.getNguoidungId());
                if (nd == null) {
                    setMessage(request, "Khong tim thay tai khoan.", "danger");
                    redirect(response, request.getContextPath() + "/login");
                    return;
                }
                ThiSinh ts = new ThiSinh();
                ts.setNguoiDung(nd);
                ts.setCccd(cccd);
                applyHoSoFields(ts, ho, ten, ngaySinhStr, gioiTinh, dienThoai, email, noiSinh,
                        doituongIdStr, khuvucIdStr);
                thiSinhService.save(ts);
                setMessage(request, "Tao ho so thi sinh thanh cong!", "success");
            } else {
                ThiSinh thiSinh = optTs.get();
                Optional<ThiSinh> trungCccd = thiSinhService.findByCccd(cccd);
                if (trungCccd.isPresent()
                        && !trungCccd.get().getThisinhId().equals(thiSinh.getThisinhId())) {
                    setMessage(request, "So CCCD da duoc su dung boi tai khoan khac.", "danger");
                    redirect(response, request.getContextPath() + "/profile");
                    return;
                }
                thiSinh.setCccd(cccd);
                applyHoSoFields(thiSinh, ho, ten, ngaySinhStr, gioiTinh, dienThoai, email, noiSinh,
                        doituongIdStr, khuvucIdStr);
                thiSinhService.update(thiSinh);
                setMessage(request, "Cap nhat thong tin thanh cong!", "success");
            }

            redirect(response, request.getContextPath() + "/dashboard");

        } catch (Exception e) {
            setMessage(request, "Da xay ra loi khi cap nhat thong tin: " + e.getMessage(), "danger");
            redirect(response, request.getContextPath() + "/profile");
        }
    }

    private void applyHoSoFields(ThiSinh thiSinh, String ho, String ten, String ngaySinhStr,
            String gioiTinh, String dienThoai, String email, String noiSinh,
            String doituongIdStr, String khuvucIdStr) {
        thiSinh.setHo(ho.trim());
        thiSinh.setTen(ten.trim());

        if (!isNullOrEmpty(ngaySinhStr)) {
            LocalDate d = DateUtil.parseDate(ngaySinhStr.trim());
            thiSinh.setNgaySinh(d);
        } else {
            thiSinh.setNgaySinh(null);
        }

        thiSinh.setGioiTinh(gioiTinh);
        thiSinh.setDienThoai(dienThoai != null ? dienThoai.trim() : null);
        thiSinh.setEmail(email != null ? email.trim() : null);
        thiSinh.setNoiSinh(noiSinh != null ? noiSinh.trim() : null);

        if (!isNullOrEmpty(doituongIdStr)) {
            try {
                DoiTuongUutien dt = doiTuongService.findById(Integer.parseInt(doituongIdStr));
                thiSinh.setDoiTuongUutien(dt);
            } catch (Exception e) {
                thiSinh.setDoiTuongUutien(null);
            }
        } else {
            thiSinh.setDoiTuongUutien(null);
        }

        if (!isNullOrEmpty(khuvucIdStr)) {
            try {
                KhuVucUutien kv = khuVucService.findById(Integer.parseInt(khuvucIdStr));
                thiSinh.setKhuVucUutien(kv);
            } catch (Exception e) {
                thiSinh.setKhuVucUutien(null);
            }
        } else {
            thiSinh.setKhuVucUutien(null);
        }
    }
}

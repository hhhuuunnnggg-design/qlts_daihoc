package com.tuyensinh.web.servlet;

import com.tuyensinh.entity.NguoiDung;
import com.tuyensinh.entity.NguyenVong;
import com.tuyensinh.entity.ThiSinh;
import com.tuyensinh.service.ThiSinhService;
import com.tuyensinh.service.XetTuyenService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class DashboardServlet extends BaseServlet {

    private final ThiSinhService thiSinhService = new ThiSinhService();
    private final XetTuyenService xetTuyenService = new XetTuyenService();

    @Override
    protected void handleGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        NguoiDung loggedInUser = getLoggedInUser(request);
        if (loggedInUser == null) {
            requireLogin(request, response);
            return;
        }

        try {
            ThiSinh thiSinh = thiSinhService.findById(loggedInUser.getNguoidungId());

            if (thiSinh == null) {
                setMessage(request, "Khong tim thay thong tin thi sinh.", "warning");
                redirect(response, request.getContextPath() + "/profile");
                return;
            }

            List<NguyenVong> danhSachNguyenVong = xetTuyenService.findNguyenVongByThiSinh(thiSinh.getThisinhId());
            List<?> danhSachDiemThi = xetTuyenService.findDiemThiByThiSinh(thiSinh.getThisinhId());

            int soLuongNguyenVong = danhSachNguyenVong.size();
            int soLuongDiemThi = danhSachDiemThi.size();
            int soTrungTuyen = 0;

            for (NguyenVong nv : danhSachNguyenVong) {
                if (NguyenVong.KetQua.TRUNG_TUYEN.equals(nv.getKetQua())) {
                    soTrungTuyen++;
                }
            }

            setAttribute(request, "thiSinh", thiSinh);
            setAttribute(request, "danhSachNguyenVong", danhSachNguyenVong);
            setAttribute(request, "soLuongNguyenVong", soLuongNguyenVong);
            setAttribute(request, "soLuongDiemThi", soLuongDiemThi);
            setAttribute(request, "soTrungTuyen", soTrungTuyen);

            forward(request, response, getViewPath("dashboard"));

        } catch (Exception e) {
            setMessage(request, "Da xay ra loi: " + e.getMessage(), "danger");
            forward(request, response, getViewPath("dashboard"));
        }
    }

    @Override
    protected void handlePost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleGet(request, response);
    }
}

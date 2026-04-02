package com.tuyensinh.web.servlet;

import com.tuyensinh.entity.Nganh;
import com.tuyensinh.entity.NganhToHop;
import com.tuyensinh.entity.NguoiDung;
import com.tuyensinh.entity.NguyenVong;
import com.tuyensinh.entity.PhuongThuc;
import com.tuyensinh.entity.ThiSinh;
import com.tuyensinh.service.ThiSinhService;
import com.tuyensinh.service.XetTuyenService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class AddNguyenVongServlet extends BaseServlet {

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
            ThiSinh thiSinh = thiSinhService.findByNguoiDungId(loggedInUser.getNguoidungId()).orElse(null);

            if (thiSinh == null) {
                setMessage(request, "Khong tim thay thong tin thi sinh.", "warning");
                redirect(response, request.getContextPath() + "/profile");
                return;
            }

            List<Nganh> danhSachNganh = xetTuyenService.findActiveNganh();
            List<PhuongThuc> danhSachPhuongThuc = xetTuyenService.findActivePhuongThuc();
            List<NganhToHop> danhSachNganhToHop = xetTuyenService.findAllNganhToHop();

            List<NguyenVong> existingNguyenVong = xetTuyenService.findNguyenVongByThiSinh(thiSinh.getThisinhId());
            int soNguyenVongHienTai = existingNguyenVong.size();

            setAttribute(request, "thiSinh", thiSinh);
            setAttribute(request, "danhSachNganh", danhSachNganh);
            setAttribute(request, "danhSachPhuongThuc", danhSachPhuongThuc);
            setAttribute(request, "danhSachNganhToHop", danhSachNganhToHop);
            setAttribute(request, "soNguyenVongHienTai", soNguyenVongHienTai);

            forward(request, response, getViewPath("add-nguyenvong-form"));

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
            ThiSinh thiSinh = thiSinhService.findByNguoiDungId(loggedInUser.getNguoidungId()).orElse(null);

            if (thiSinh == null) {
                setMessage(request, "Khong tim thay thong tin thi sinh.", "danger");
                redirect(response, request.getContextPath() + "/add-nguyenvong");
                return;
            }

            List<NguyenVong> existingNguyenVong = xetTuyenService.findNguyenVongByThiSinh(thiSinh.getThisinhId());
            if (existingNguyenVong.size() >= 5) {
                setMessage(request, "Ban da dang ky toi da 5 nguyen vong.", "warning");
                redirect(response, request.getContextPath() + "/nguyenvong");
                return;
            }

            String nganhIdStr = request.getParameter("nganhId");
            String nganhTohopIdStr = request.getParameter("nganhTohopId");
            String phuongthucIdStr = request.getParameter("phuongthucId");

            if (isNullOrEmpty(nganhIdStr) || isNullOrEmpty(nganhTohopIdStr) || isNullOrEmpty(phuongthucIdStr)) {
                setMessage(request, "Vui long dien day du thong tin.", "danger");
                redirect(response, request.getContextPath() + "/add-nguyenvong");
                return;
            }

            Integer nganhId = Integer.parseInt(nganhIdStr);
            Integer nganhTohopId = Integer.parseInt(nganhTohopIdStr);
            Short phuongthucId = Short.parseShort(phuongthucIdStr);

            Nganh nganh = null;
            for (Nganh n : xetTuyenService.findActiveNganh()) {
                if (n.getNganhId().equals(nganhId)) {
                    nganh = n;
                    break;
                }
            }

            if (nganh == null) {
                setMessage(request, "Khong tim thay nganh hoc.", "danger");
                redirect(response, request.getContextPath() + "/add-nguyenvong");
                return;
            }

            NganhToHop nganhToHop = null;
            List<NganhToHop> nganhToHops = xetTuyenService.findNganhToHopByNganh(nganhId);
            for (NganhToHop nth : nganhToHops) {
                if (nth.getNganhTohopId().equals(nganhTohopId)) {
                    nganhToHop = nth;
                    break;
                }
            }

            if (nganhToHop == null) {
                setMessage(request, "Khong tim thay to hop mon.", "danger");
                redirect(response, request.getContextPath() + "/add-nguyenvong");
                return;
            }

            PhuongThuc phuongThuc = null;
            List<PhuongThuc> phuongThucs = xetTuyenService.findActivePhuongThuc();
            for (PhuongThuc pt : phuongThucs) {
                if (pt.getPhuongthucId().equals(phuongthucId)) {
                    phuongThuc = pt;
                    break;
                }
            }

            if (phuongThuc == null) {
                setMessage(request, "Khong tim thay phuong thuc xet tuyen.", "danger");
                redirect(response, request.getContextPath() + "/add-nguyenvong");
                return;
            }

            for (NguyenVong existing : existingNguyenVong) {
                if (existing.getNganh().getNganhId().equals(nganhId) &&
                    existing.getNganhToHop().getNganhTohopId().equals(nganhTohopId) &&
                    existing.getPhuongThuc().getPhuongthucId().equals(phuongthucId)) {
                    setMessage(request, "Nguyen vong nay da ton tai.", "warning");
                    redirect(response, request.getContextPath() + "/nguyenvong");
                    return;
                }
            }

            int nextOrder = existingNguyenVong.size() + 1;

            NguyenVong nguyenVong = new NguyenVong();
            nguyenVong.setThiSinh(thiSinh);
            nguyenVong.setNganh(nganh);
            nguyenVong.setNganhToHop(nganhToHop);
            nguyenVong.setPhuongThuc(phuongThuc);
            nguyenVong.setThuTu(nextOrder);
            nguyenVong.setKetQua(NguyenVong.KetQua.CHO_XET);

            xetTuyenService.saveNguyenVong(nguyenVong);

            setMessage(request, "Dang ky nguyen vong thanh cong!", "success");
            redirect(response, request.getContextPath() + "/nguyenvong");

        } catch (Exception e) {
            setMessage(request, "Da xay ra loi khi dang ky nguyen vong: " + e.getMessage(), "danger");
            redirect(response, request.getContextPath() + "/add-nguyenvong");
        }
    }
}

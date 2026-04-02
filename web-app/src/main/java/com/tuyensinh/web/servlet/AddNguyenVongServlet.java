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
                setMessage(request, "Không tìm thấy thông tin thí sinh.", "warning");
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
            setMessage(request, "Đã xảy ra lỗi: " + e.getMessage(), "danger");
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
                setMessage(request, "Không tìm thấy thông tin thí sinh.", "danger");
                redirect(response, request.getContextPath() + "/add-nguyenvong");
                return;
            }

            List<NguyenVong> existingNguyenVong = xetTuyenService.findNguyenVongByThiSinh(thiSinh.getThisinhId());
            if (existingNguyenVong.size() >= 5) {
                setMessage(request, "Bạn đã đăng ký tối đa 5 nguyện vọng.", "warning");
                redirect(response, request.getContextPath() + "/nguyenvong");
                return;
            }

            String nganhIdStr = request.getParameter("nganhId");
            String nganhTohopIdStr = request.getParameter("nganhTohopId");
            String phuongthucIdStr = request.getParameter("phuongthucId");

            if (isNullOrEmpty(nganhIdStr) || isNullOrEmpty(nganhTohopIdStr) || isNullOrEmpty(phuongthucIdStr)) {
                setMessage(request, "Vui lòng điền đầy đủ thông tin.", "danger");
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
                setMessage(request, "Không tìm thấy ngành học.", "danger");
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
                setMessage(request, "Không tìm thấy tổ hợp môn.", "danger");
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
                setMessage(request, "Không tìm thấy phương thức xét tuyển.", "danger");
                redirect(response, request.getContextPath() + "/add-nguyenvong");
                return;
            }

            for (NguyenVong existing : existingNguyenVong) {
                if (existing.getNganh().getNganhId().equals(nganhId) &&
                    existing.getNganhToHop().getNganhTohopId().equals(nganhTohopId) &&
                    existing.getPhuongThuc().getPhuongthucId().equals(phuongthucId)) {
                    setMessage(request, "Nguyện vọng này đã tồn tại.", "warning");
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

            setMessage(request, "Đăng ký nguyện vọng thành công!", "success");
            redirect(response, request.getContextPath() + "/nguyenvong");

        } catch (Exception e) {
            setMessage(request, "Đã xảy ra lỗi khi đăng ký nguyện vọng: " + e.getMessage(), "danger");
            redirect(response, request.getContextPath() + "/add-nguyenvong");
        }
    }
}

package com.tuyensinh.web.servlet;

import com.tuyensinh.entity.NguoiDung;
import com.tuyensinh.entity.ThiSinh;
import com.tuyensinh.service.DoiTuongService;
import com.tuyensinh.service.KhuVucService;
import com.tuyensinh.service.NguoiDungService;
import com.tuyensinh.service.ThiSinhService;
import com.tuyensinh.web.servlet.base.ModelAndView;
import com.tuyensinh.web.servlet.dto.HoSoForm;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class ProfileServlet extends BaseServlet {

    private final ThiSinhService thiSinhService = new ThiSinhService();
    private final NguoiDungService nguoiDungService = new NguoiDungService();
    private final DoiTuongService doiTuongService = new DoiTuongService();
    private final KhuVucService khuVucService = new KhuVucService();

    @Override
    protected ModelAndView handleGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ModelAndView auth = authController.requireLogin(request, response);
        if (auth != null) return auth;

        try {
            ThiSinh thiSinh = thiSinhService.findByNguoiDungId(
                authController.getCurrentUser(request).getNguoidungId()).orElse(null);

            return viewResolver.view("profile")
                .addObject("thiSinh", thiSinh)
                .addObject("danhSachDoiTuong", doiTuongService.findAll())
                .addObject("danhSachKhuVuc", khuVucService.findAll());

        } catch (Exception e) {
            setMessage(request, "Đã xảy ra lỗi: " + e.getMessage(), "danger");
            return viewResolver.redirect("/dashboard");
        }
    }

    @Override
    protected ModelAndView handlePost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ModelAndView auth = authController.requireLogin(request, response);
        if (auth != null) return auth;

        try {
            HoSoForm form = new HoSoForm(request);
            Optional<String> validationError = form.validate();
            if (validationError.isPresent()) {
                setMessage(request, validationError.get(), "danger");
                return viewResolver.redirect("/profile");
            }

            Optional<ThiSinh> optTs = thiSinhService.findByNguoiDungId(
                authController.getCurrentUser(request).getNguoidungId());

            if (optTs.isEmpty()) {
                if (thiSinhService.findByCccd(form.getCccd()).isPresent()) {
                    setMessage(request, "Số CCCD đã được sử dụng bởi tài khoản khác.", "danger");
                    return viewResolver.redirect("/profile");
                }
                NguoiDung nd = nguoiDungService.findById(authController.getCurrentUser(request).getNguoidungId());
                if (nd == null) {
                    setMessage(request, "Không tìm thấy tài khoản.", "danger");
                    return viewResolver.redirect("/login");
                }
                ThiSinh ts = new ThiSinh();
                ts.setNguoiDung(nd);
                form.bindToEntity(ts, doiTuongService, khuVucService);
                thiSinhService.save(ts);
                setMessage(request, "Tạo hồ sơ thí sinh thành công!", "success");
            } else {
                ThiSinh thiSinh = optTs.get();
                Optional<ThiSinh> trungCccd = thiSinhService.findByCccd(form.getCccd());
                if (trungCccd.isPresent()
                        && !trungCccd.get().getThisinhId().equals(thiSinh.getThisinhId())) {
                    setMessage(request, "Số CCCD đã được sử dụng bởi tài khoản khác.", "danger");
                    return viewResolver.redirect("/profile");
                }
                form.bindToEntity(thiSinh, doiTuongService, khuVucService);
                thiSinhService.update(thiSinh);
                setMessage(request, "Cập nhật thông tin thành công!", "success");
            }

            return viewResolver.redirect("/dashboard");

        } catch (Exception e) {
            setMessage(request, "Đã xảy ra lỗi khi cập nhật thông tin: " + e.getMessage(), "danger");
            return viewResolver.redirect("/profile");
        }
    }
}

package com.tuyensinh.service;

import com.tuyensinh.dao.*;
import com.tuyensinh.entity.*;
import com.tuyensinh.service.interfaceService.IAuthService;
import com.tuyensinh.util.PasswordUtil;
import java.util.List;
import java.util.Optional;

public class AuthService implements IAuthService {

    private final NguoiDungDao nguoiDungDao = new NguoiDungDao();

    public Optional<NguoiDung> login(String username, String password) {
        Optional<NguoiDung> optNd = nguoiDungDao.findByUsername(username);
        if (optNd.isPresent()) {
            NguoiDung nd = optNd.get();
            if (nd.getIsActive() && PasswordUtil.checkPassword(password, nd.getPasswordHash())) {
                return Optional.of(nd);
            }
        }
        return Optional.empty();
    }

    public NguoiDung register(NguoiDung nguoiDung, String rawPassword) {
        String hash = PasswordUtil.hashPassword(rawPassword);
        nguoiDung.setPasswordHash(hash);
        return nguoiDungDao.save(nguoiDung);
    }

    public void updatePassword(NguoiDung nd, String newPassword) {
        String hash = PasswordUtil.hashPassword(newPassword);
        nguoiDungDao.updatePassword(nd, hash);
    }

    public void changePassword(NguoiDung nd, String oldPassword, String newPassword) {
        if (!PasswordUtil.checkPassword(oldPassword, nd.getPasswordHash())) {
            throw new IllegalArgumentException("Mat khau cu khong dung");
        }
        updatePassword(nd, newPassword);
    }
}

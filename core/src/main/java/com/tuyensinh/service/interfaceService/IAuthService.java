package com.tuyensinh.service.interfaceService;

import com.tuyensinh.entity.NguoiDung;
import java.util.Optional;

public interface IAuthService {

    Optional<NguoiDung> login(String username, String password);

    NguoiDung register(NguoiDung nguoiDung, String rawPassword);

    void updatePassword(NguoiDung nd, String newPassword);

    void changePassword(NguoiDung nd, String oldPassword, String newPassword);
}

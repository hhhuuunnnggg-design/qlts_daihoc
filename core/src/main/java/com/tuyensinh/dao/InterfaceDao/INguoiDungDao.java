package com.tuyensinh.dao.InterfaceDao;

import com.tuyensinh.entity.NguoiDung;
import java.util.List;
import java.util.Optional;

public interface INguoiDungDao extends IBaseDao<NguoiDung> {

    Optional<NguoiDung> findByUsername(String username);

    Optional<NguoiDung> findByEmail(String email);

    List<NguoiDung> findByVaiTro(Short vaitroId);

    List<NguoiDung> findActiveUsers();

    List<NguoiDung> searchByUsernameOrHoTen(String keyword);

    long countAll();

    void updatePassword(NguoiDung nd, String newPasswordHash);

    void toggleActive(NguoiDung nd);
}

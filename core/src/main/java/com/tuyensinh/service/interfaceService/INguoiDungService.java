package com.tuyensinh.service.interfaceService;

import com.tuyensinh.entity.NguoiDung;
import com.tuyensinh.entity.VaiTro;
import java.util.List;
import java.util.Optional;

public interface INguoiDungService {

    List<NguoiDung> findAll();

    List<NguoiDung> findByPage(int page, int pageSize);

    NguoiDung findById(Integer id);

    Optional<NguoiDung> findByUsername(String username);

    NguoiDung save(NguoiDung nd);

    void update(NguoiDung nd);

    void delete(NguoiDung nd);

    void toggleActive(NguoiDung nd);

    void changeRole(NguoiDung nd, VaiTro newRole);

    List<NguoiDung> search(String keyword);

    List<NguoiDung> findByRole(Short vaitroId);

    List<NguoiDung> findActiveUsers();

    long countAll();

    int getTotalPages(long total, int pageSize);

    void updatePassword(NguoiDung nd, String newPassword);
}

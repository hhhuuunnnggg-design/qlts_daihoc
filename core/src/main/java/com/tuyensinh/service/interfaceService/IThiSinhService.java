package com.tuyensinh.service.interfaceService;

import com.tuyensinh.entity.DiemThi;
import com.tuyensinh.entity.ThiSinh;
import java.util.List;
import java.util.Optional;

public interface IThiSinhService {

    List<ThiSinh> findAll();

    List<ThiSinh> findByPage(int page, int pageSize);

    List<ThiSinh> findByPageWithSearch(String keyword, int page, int pageSize);

    ThiSinh findById(Integer id);

    Optional<ThiSinh> findByCccd(String cccd);

    Optional<ThiSinh> findBySoBaoDanh(String sobaodanh);

    /** Thi sinh gan voi tai khoan dang nhap (1-1 qua nguoidung_id). */
    Optional<ThiSinh> findByNguoiDungId(Integer nguoidungId);

    ThiSinh save(ThiSinh ts);

    void update(ThiSinh ts);

    void delete(ThiSinh ts);

    List<ThiSinh> search(String keyword);

    long countBySearch(String keyword);

    String generateSoBaoDanh();

    List<DiemThi> getDiemThiList(Integer thisinhId);

    int getTotalPages(long total, int pageSize);
}

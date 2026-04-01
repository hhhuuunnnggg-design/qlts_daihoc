package com.tuyensinh.dao.InterfaceDao;

import com.tuyensinh.entity.ThiSinh;
import java.util.List;
import java.util.Optional;

public interface IThiSinhDao extends IBaseDao<ThiSinh> {

    Optional<ThiSinh> findByCccd(String cccd);

    Optional<ThiSinh> findBySoBaoDanh(String sobaodanh);

    List<ThiSinh> searchByCccdOrHoTen(String keyword);

    List<ThiSinh> findByPageWithSearch(String keyword, int page, int pageSize);

    long countBySearch(String keyword);

    List<ThiSinh> findByNguoiDungId(Integer nguoidungId);

    String generateSoBaoDanh();
}

package com.tuyensinh.dao.InterfaceDao;

import com.tuyensinh.entity.BangQuyDoi;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface IBangQuyDoiDao extends IBaseDao<BangQuyDoi> {

    Optional<BangQuyDoi> findByMa(String maQuydoi);

    List<BangQuyDoi> findByPhuongThuc(Short phuongthucId);

    List<BangQuyDoi> search(String keyword);

    BangQuyDoi quyDoiDiem(Short phuongthucId, Integer tohopId, Integer monId, BigDecimal diemGoc);
}

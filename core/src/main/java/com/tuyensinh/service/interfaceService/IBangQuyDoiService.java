package com.tuyensinh.service.interfaceService;

import com.tuyensinh.entity.BangQuyDoi;
import java.util.List;

public interface IBangQuyDoiService {

    List<BangQuyDoi> findAll();

    BangQuyDoi findById(Integer id);

    BangQuyDoi findByMa(String maQuydoi);

    List<BangQuyDoi> findByPhuongThuc(Short phuongthucId);

    List<BangQuyDoi> search(String keyword);

    BangQuyDoi save(BangQuyDoi entity);

    void update(BangQuyDoi entity);

    void delete(BangQuyDoi entity);
}

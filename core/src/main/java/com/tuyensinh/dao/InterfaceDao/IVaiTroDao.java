package com.tuyensinh.dao.InterfaceDao;

import com.tuyensinh.entity.VaiTro;
import java.util.List;

public interface IVaiTroDao extends IBaseDao<VaiTro> {

    List<VaiTro> findAll();

    VaiTro findByMa(String maVaitro);

    VaiTro findById(Short id);
}

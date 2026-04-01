package com.tuyensinh.service;

import com.tuyensinh.dao.VaiTroDao;
import com.tuyensinh.entity.VaiTro;
import java.util.List;

public class VaiTroService {

    private final VaiTroDao dao = new VaiTroDao();

    public List<VaiTro> findAllVaiTro() {
        return dao.findAll();
    }

    public VaiTro findById(Short id) {
        return dao.findById(id);
    }

    public VaiTro findByMa(String ma) {
        return dao.findByMa(ma);
    }
}

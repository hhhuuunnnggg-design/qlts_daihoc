package com.tuyensinh.service;

import com.tuyensinh.dao.DiemThiDao;
import com.tuyensinh.entity.DiemThi;
import com.tuyensinh.entity.DiemThiChiTiet;
import com.tuyensinh.service.interfaceService.IDiemThiService;

import java.util.List;

public class DiemThiService implements IDiemThiService {

    private final DiemThiDao dao = new DiemThiDao();

    public List<DiemThi> findAll() {
        return dao.findAll();
    }

    public DiemThi findById(Integer id) {
        return dao.findById(id);
    }

    public List<DiemThi> findByThiSinhId(Integer thisinhId) {
        return dao.findByThiSinhId(thisinhId);
    }

    public List<DiemThi> findByPhuongThuc(Short phuongthucId) {
        return dao.findByPhuongThuc(phuongthucId);
    }

    public List<DiemThi> searchDiemThi(String keyword, Short phuongthucId) {
        return dao.searchByCccdOrSoBaoDanh(keyword, phuongthucId);
    }

    public DiemThi save(DiemThi entity) {
        return dao.save(entity);
    }

    public void update(DiemThi entity) {
        dao.update(entity);
    }

    public void delete(DiemThi entity) {
        dao.delete(entity);
    }

    public List<Object[]> thongKeDiemTheoMon(Short phuongthucId) {
        return dao.thongKeDiemTheoMon(phuongthucId);
    }
}

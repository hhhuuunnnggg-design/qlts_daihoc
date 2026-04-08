package com.tuyensinh.service;

import com.tuyensinh.dao.DiemThiDao;
import com.tuyensinh.entity.DiemThi;
import com.tuyensinh.entity.DiemThiChiTiet;
import com.tuyensinh.service.interfaceService.IDiemThiService;
import com.tuyensinh.entity.Mon;
import com.tuyensinh.entity.PhuongThuc;
import com.tuyensinh.entity.ThiSinh;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;

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
    @Override
    public DiemThi importOrReplaceScoreSheet(ThiSinh thiSinh,
                                             PhuongThuc phuongThuc,
                                             Short namTuyensinh,
                                             String soBaoDanh,
                                             String ghiChu,
                                             Map<Mon, BigDecimal> diemTheoMon) {
        if (thiSinh == null || thiSinh.getThisinhId() == null) {
            throw new IllegalArgumentException("Thi sinh khong hop le.");
        }
        if (phuongThuc == null || phuongThuc.getPhuongthucId() == null) {
            throw new IllegalArgumentException("Phuong thuc khong hop le.");
        }
        if (namTuyensinh == null) {
            throw new IllegalArgumentException("Nam tuyen sinh khong duoc de trong.");
        }
        if (diemTheoMon == null || diemTheoMon.isEmpty()) {
            throw new IllegalArgumentException("Khong co diem de import.");
        }

        DiemThi diemThi = new DiemThi();
        diemThi.setThiSinh(thiSinh);
        diemThi.setPhuongThuc(phuongThuc);
        diemThi.setNamTuyensinh(namTuyensinh);
        diemThi.setSobaodanh(soBaoDanh != null && !soBaoDanh.trim().isEmpty() ? soBaoDanh.trim() : thiSinh.getSobaodanh());
        diemThi.setGhiChu(ghiChu);
        diemThi.setDanhSachDiemChiTiet(new ArrayList<>());

        for (Map.Entry<Mon, BigDecimal> entry : diemTheoMon.entrySet()) {
            Mon mon = entry.getKey();
            BigDecimal diem = entry.getValue();
            if (mon == null || diem == null) {
                continue;
            }

            DiemThiChiTiet ct = new DiemThiChiTiet();
            ct.setDiemThi(diemThi);
            ct.setMon(mon);
            ct.setDiemGoc(diem);
            ct.setDiemQuydoi(diem);
            ct.setDiemSudung(diem);
            diemThi.getDanhSachDiemChiTiet().add(ct);
        }

        if (diemThi.getDanhSachDiemChiTiet().isEmpty()) {
            throw new IllegalArgumentException("Khong co diem hop le de luu.");
        }

        return dao.replaceForUniqueKey(diemThi);
    }
}

package com.tuyensinh.service;

import com.tuyensinh.dao.DiemCongDao;
import com.tuyensinh.entity.DiemCong;
import com.tuyensinh.entity.DiemCongChiTiet;
import com.tuyensinh.service.interfaceService.IDiemCongService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class DiemCongService implements IDiemCongService {

    private final DiemCongDao dao = new DiemCongDao();
    private final DiemCongChiTietService chiTietService = new DiemCongChiTietService();

    @Override
    public List<DiemCong> findAll() {
        return dao.findAll();
    }

    @Override
    public DiemCong findById(Integer id) {
        return dao.findById(id);
    }

    @Override
    public List<DiemCong> findByThiSinhId(Integer thisinhId) {
        return dao.findByThiSinhId(thisinhId);
    }

    @Override
    public List<DiemCong> findByPhuongThuc(Short phuongthucId) {
        return dao.findByPhuongThuc(phuongthucId);
    }

    @Override
    public List<DiemCong> findByNganhToHopId(Integer nganhToHopId) {
        return dao.findByNganhToHopId(nganhToHopId);
    }

    @Override
    public Optional<DiemCong> findByThiSinhNganhToHopPhuongThuc(Integer thisinhId, Integer nganhToHopId, Short phuongthucId) {
        return dao.findByThiSinhNganhToHopPhuongThuc(thisinhId, nganhToHopId, phuongthucId);
    }

    @Override
    public DiemCong save(DiemCong entity) {
        return dao.save(entity);
    }

    @Override
    public void update(DiemCong entity) {
        dao.update(entity);
    }

    @Override
    public void delete(DiemCong entity) {
        dao.delete(entity);
    }

    @Override
    public void recalculateTongHop(Integer diemCongId) {
        if (diemCongId == null) return;

        DiemCong dc = dao.findById(diemCongId);
        if (dc == null) return;

        List<DiemCongChiTiet> details = chiTietService.findByDiemCongId(diemCongId);

        BigDecimal tongChungChi = BigDecimal.ZERO;
        BigDecimal tongUuTienXt = BigDecimal.ZERO;
        BigDecimal tongUuTienQc = BigDecimal.ZERO;

        if (details != null) {
            for (DiemCongChiTiet ct : details) {
                if (ct == null || Boolean.FALSE.equals(ct.getIsApDung())) continue;

                BigDecimal diemCong = ct.getDiemCongGiaTri() != null
                        ? ct.getDiemCongGiaTri()
                        : BigDecimal.ZERO;

                if (ct.getLoaiNguon() == null) continue;

                switch (ct.getLoaiNguon()) {
                    case CC_NGOAI_NGU:
                        tongChungChi = tongChungChi.add(diemCong);
                        break;

                    case UUTIEN_KHUVUC:
                    case UUTIEN_DOITUONG:
                        tongUuTienQc = tongUuTienQc.add(diemCong);
                        break;

                    case UTXT_HSG_QUOCGIA:
                    case UTXT_HSG_TINH:
                    case UTXT_KHKT:
                    case UTXT_NGHE_THUAT:
                        tongUuTienXt = tongUuTienXt.add(diemCong);
                        break;

                    default:
                        break;
                }
            }
        }

        BigDecimal tongThucTe = tongChungChi.add(tongUuTienXt).add(tongUuTienQc);
        BigDecimal tongSauTran = tongThucTe.min(new BigDecimal("3.00"));

        dc.setTongDiemChungChi(tongChungChi);
        dc.setTongDiemUutienXt(tongUuTienXt);
        dc.setTongDiemUutienQuyChe(tongUuTienQc);
        dc.setTongDiemCong(tongSauTran);

        if (tongThucTe.compareTo(new BigDecimal("3.00")) > 0) {
            dc.setGhiChuTong("Tong diem cong thuc te = " + tongThucTe.toPlainString() + ", ap tran SGU = 3.00");
        } else {
            dc.setGhiChuTong(null);
        }

        dao.update(dc);
    }
}
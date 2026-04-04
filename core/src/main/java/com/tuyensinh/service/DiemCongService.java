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
    public void recalculateTongHop(Integer diemcongId) {
        DiemCong dc = dao.findById(diemcongId);
        if (dc == null) return;

        List<DiemCongChiTiet> list = chiTietService.findAppliedByDiemCongId(diemcongId);

        BigDecimal tongChungChi = BigDecimal.ZERO;
        BigDecimal tongUuTienXt = BigDecimal.ZERO;
        BigDecimal tongUuTienQuyChe = BigDecimal.ZERO;

        for (DiemCongChiTiet ct : list) {
            BigDecimal diem = ct.getDiemCongGiaTri() != null
                    ? ct.getDiemCongGiaTri()
                    : BigDecimal.ZERO;

            switch (ct.getLoaiNguon()) {
                case CC_NGOAI_NGU:
                    tongChungChi = tongChungChi.add(diem);
                    break;

                case UTXT_HSG_QUOCGIA:
                case UTXT_HSG_TINH:
                case UTXT_KHKT:
                case UTXT_NGHE_THUAT:
                    tongUuTienXt = tongUuTienXt.add(diem);
                    break;

                case UUTIEN_KHUVUC:
                case UUTIEN_DOITUONG:
                    tongUuTienQuyChe = tongUuTienQuyChe.add(diem);
                    break;
            }
        }

        dc.setTongDiemChungChi(tongChungChi);
        dc.setTongDiemUutienXt(tongUuTienXt);
        dc.setTongDiemUutienQuyChe(tongUuTienQuyChe);
        dc.setTongDiemCong(
                tongChungChi.add(tongUuTienXt).add(tongUuTienQuyChe)
        );

        dao.update(dc);
    }
}
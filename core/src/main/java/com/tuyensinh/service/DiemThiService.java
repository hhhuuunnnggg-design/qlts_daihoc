package com.tuyensinh.service;

import com.tuyensinh.dao.DiemThiDao;
import com.tuyensinh.dao.BangQuyDoiDao;
import com.tuyensinh.entity.DiemThi;
import com.tuyensinh.entity.DiemThiChiTiet;
import com.tuyensinh.entity.BangQuyDoi;
import com.tuyensinh.service.interfaceService.IDiemThiService;
import com.tuyensinh.entity.Mon;
import com.tuyensinh.entity.PhuongThuc;
import com.tuyensinh.entity.ThiSinh;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Map;

import java.util.List;

public class DiemThiService implements IDiemThiService {

    private final DiemThiDao dao = new DiemThiDao();
    private final BangQuyDoiDao bangQuyDoiDao = new BangQuyDoiDao();

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

    public DiemThiChiTiet findChiTietById(Long id) {
        return dao.findChiTietById(id);
    }

    public DiemThiChiTiet findChiTietByDiemThiAndMon(Integer diemthiId, Integer monId) {
        return dao.findChiTietByDiemThiAndMon(diemthiId, monId);
    }

    public DiemThiChiTiet saveChiTiet(DiemThiChiTiet entity) {
        return dao.saveChiTiet(entity);
    }

    public void updateChiTiet(DiemThiChiTiet entity) {
        dao.updateChiTiet(entity);
    }

    public void deleteChiTiet(DiemThiChiTiet entity) {
        dao.deleteChiTiet(entity);
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

            BigDecimal diemGoc = diem;
            BigDecimal diemQuydoi = tinhDiemQuyDoiKhiImport(diemGoc, phuongThuc, mon);

            DiemThiChiTiet ct = new DiemThiChiTiet();
            ct.setDiemThi(diemThi);
            ct.setMon(mon);
            ct.setDiemGoc(diemGoc);
            ct.setDiemQuydoi(diemQuydoi);

            // THPT/NK dùng trực tiếp điểm gốc.
            // VSAT/DGNL nếu không quy đổi được thì không lấy điểm gốc làm điểm sử dụng,
            // tránh hiển thị sai kiểu 538 hoặc 118.50 ở cột điểm sử dụng.
            ct.setDiemSudung(diemQuydoi != null ? diemQuydoi : diemSuDungFallback(diemGoc, phuongThuc));

            diemThi.getDanhSachDiemChiTiet().add(ct);
        }

        if (diemThi.getDanhSachDiemChiTiet().isEmpty()) {
            throw new IllegalArgumentException("Khong co diem hop le de luu.");
        }

        return dao.replaceForUniqueKey(diemThi);
    }

    /**
     * Quy doi diem khi import de hien thi tren Panel Diem thi.
     *
     * Nguyen tac:
     * - THPT/NK: diem goc da la thang 10 -> giu nguyen.
     * - VSAT: quy doi tung mon ve thang 10 theo xt_bangquydoi.
     * - DGNL: diem goc la tong diem DGNL, bang quy doi trong DB dang la thang 30,
     *   nen hien thi diem_quydoi/diem_sudung theo thang 30 de dung voi DTHGXT_DGNL.
     *
     * Luu y:
     * - DGNL khi xet tuyen van lay diem_goc de tinh theo nguyen vong/to hop.
     * - diem_quydoi/diem_sudung cua THPT/NK/VSAT hien thi thang 10; rieng DGNL hien thi thang 30.
     */
    private BigDecimal tinhDiemQuyDoiKhiImport(BigDecimal diemGoc, PhuongThuc phuongThuc, Mon mon) {
        if (diemGoc == null) return null;

        if (phuongThuc == null || phuongThuc.getPhuongthucId() == null) {
            return capThang10(diemGoc);
        }

        String maPt = phuongThuc.getMaPhuongthuc() != null
                ? phuongThuc.getMaPhuongthuc().trim().toUpperCase()
                : "";

        // THPT va NK da la thang 10
        if (PhuongThuc.THPT.equalsIgnoreCase(maPt) || PhuongThuc.NK.equalsIgnoreCase(maPt)) {
            return capThang10(diemGoc);
        }

        // VSAT: quy doi theo tung mon ve thang 10
        if (PhuongThuc.VSAT.equalsIgnoreCase(maPt)) {
            Integer monId = mon != null ? mon.getMonId() : null;

            BangQuyDoi bqd = bangQuyDoiDao.quyDoiDiem(
                    phuongThuc.getPhuongthucId(),
                    null,
                    monId,
                    diemGoc
            );

            if (bqd == null) {
                return null;
            }

            BigDecimal diemQd = noiSuyDiemQuyDoi(diemGoc, bqd);
            return capThang10(diemQd);
        }

        // DGNL: bang quy doi la thang 30, hien thi dung thang 30.
        if (PhuongThuc.DGNL.equalsIgnoreCase(maPt)) {
            BangQuyDoi bqd = bangQuyDoiDao.quyDoiDiemBatKyToHop(
                    phuongThuc.getPhuongthucId(),
                    null,
                    diemGoc
            );

            if (bqd == null) {
                return null;
            }

            BigDecimal diemQdThang30 = noiSuyDiemQuyDoi(diemGoc, bqd);
            return capThang30(diemQdThang30);
        }

        return capThang10(diemGoc);
    }

    private BigDecimal noiSuyDiemQuyDoi(BigDecimal diemGoc, BangQuyDoi bqd) {
        if (diemGoc == null || bqd == null) return null;

        BigDecimal diemTu = bqd.getDiemTu();
        BigDecimal diemDen = bqd.getDiemDen();
        BigDecimal qdTu = bqd.getDiemQuydoiTu();
        BigDecimal qdDen = bqd.getDiemQuydoiDen();

        if (diemTu == null || diemDen == null || qdTu == null || qdDen == null) {
            return null;
        }

        BigDecimal khoangGoc = diemDen.subtract(diemTu);
        if (khoangGoc.compareTo(BigDecimal.ZERO) == 0) {
            return qdTu.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal tyLe = diemGoc.subtract(diemTu)
                .multiply(qdDen.subtract(qdTu))
                .divide(khoangGoc, 6, RoundingMode.HALF_UP);

        return qdTu.add(tyLe).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal diemSuDungFallback(BigDecimal diemGoc, PhuongThuc phuongThuc) {
        if (diemGoc == null) return null;
        if (phuongThuc == null || phuongThuc.getMaPhuongthuc() == null) {
            return capThang10(diemGoc);
        }

        String maPt = phuongThuc.getMaPhuongthuc().trim().toUpperCase();

        // Chỉ THPT/NK được phép lấy điểm gốc làm điểm sử dụng.
        if (PhuongThuc.THPT.equalsIgnoreCase(maPt) || PhuongThuc.NK.equalsIgnoreCase(maPt)) {
            return capThang10(diemGoc);
        }

        // VSAT/DGNL không tìm thấy bảng quy đổi thì để null,
        // tránh hiển thị sai thang điểm.
        return null;
    }

    private BigDecimal capThang10(BigDecimal value) {
        if (value == null) return null;

        BigDecimal min = BigDecimal.ZERO;
        BigDecimal max = new BigDecimal("10");

        BigDecimal result = value.setScale(2, RoundingMode.HALF_UP);
        if (result.compareTo(min) < 0) return min.setScale(2, RoundingMode.HALF_UP);
        if (result.compareTo(max) > 0) return max.setScale(2, RoundingMode.HALF_UP);

        return result;
    }

    private BigDecimal capThang30(BigDecimal value) {
        if (value == null) return null;

        BigDecimal min = BigDecimal.ZERO;
        BigDecimal max = new BigDecimal("30");

        BigDecimal result = value.setScale(2, RoundingMode.HALF_UP);
        if (result.compareTo(min) < 0) return min.setScale(2, RoundingMode.HALF_UP);
        if (result.compareTo(max) > 0) return max.setScale(2, RoundingMode.HALF_UP);

        return result;
    }

    private BigDecimal scaleScore(BigDecimal value) {
        return value == null ? null : value.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public DiemThi findByIdWithDetails(Integer id) {
        return dao.findByIdWithDetails(id);
    }

    @Override
    public List<DiemThi> findByThiSinhIdWithDetails(Integer thisinhId) {
        return dao.findByThiSinhIdWithDetails(thisinhId);
    }

    public List<DiemThi> findPage(int page, int pageSize) {
        return dao.findPage(page, pageSize);
    }

    public long countAll() {
        return dao.countAll();
    }

    public List<DiemThi> findByPhuongThucPage(Short phuongthucId, int page, int pageSize) {
        return dao.findByPhuongThucAndPage(phuongthucId, page, pageSize);
    }

    public long countByPhuongThuc(Short phuongthucId) {
        return dao.countByPhuongThuc(phuongthucId);
    }

    public List<DiemThi> searchDiemThiPage(String keyword, Short phuongthucId, int page, int pageSize) {
        return dao.searchByCccdOrSoBaoDanhPage(keyword, phuongthucId, page, pageSize);
    }

    public long countSearchDiemThi(String keyword, Short phuongthucId) {
        return dao.countSearchByCccdOrSoBaoDanh(keyword, phuongthucId);
    }

}
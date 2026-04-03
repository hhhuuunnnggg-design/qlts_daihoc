package com.tuyensinh.service;

import com.tuyensinh.dao.*;
import com.tuyensinh.entity.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Engine tinh diem xet tuyen.
 * <p>
 * Cong thuc tong quat:<br>
 * diemThxt = sum(diem_sudung_i * heSo_i) - doLech<br>
 * diemCong  = diemChungchi + diemUutienXt<br>
 * diemXetTuyen = diemThxt + diemCong
 * <p>
 * Cac buoc xu ly:
 * <ol>
 *   <li>Tich hop BangQuyDoi neu co ban ghi phu hop</li>
 *   <li>Chi lay diem cac mon thuoc to hop cua nguyen vong</li>
 *   <li>Ap dung he so tu NganhToHopMon (neu co)</li>
 *   <li>Ap dung doLech cua NganhToHop</li>
 *   <li>Cong diem tu DiemCong</li>
 * </ol>
 */
public class TinhDiemService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final int SCALE = 5;
    private static final RoundingMode ROUND = RoundingMode.HALF_UP;

    private final BangQuyDoiDao bangQuyDoiDao = new BangQuyDoiDao();
    private final DiemThiDao diemThiDao = new DiemThiDao();
    private final DiemCongDao diemCongDao = new DiemCongDao();
    private final ToHopDao toHopDao = new ToHopDao();
    private final PhuongThucDao phuongThucDao = new PhuongThucDao();

    /**
     * Ket qua tinh diem cho mot nguyen vong.
     */
    public static class KetQuaDiem {
        public BigDecimal diemThxt;       // tong hop (chua cong diem)
        public BigDecimal diemCong;       // diem cong (chung chi + uu tien)
        public BigDecimal diemUutien;     // chi diem uu tien (phan tach)
        public BigDecimal diemXettuyen;   // diem cuoi cung
        public boolean coBangQuyDoi;     // da su dung bang quy doi
        public String ghiChu;

        @Override
        public String toString() {
            return String.format(
                "THXT=%.2f | Cong=%.2f (UT=%.2f) | XetTuyen=%.2f | BQD=%s",
                diemThxt, diemCong, diemUutien, diemXettuyen, coBangQuyDoi);
        }
    }

    /**
     * Tinh diem xet tuyen cho mot nguyen vong.
     *
     * @param nv          nguyen vong
     * @param diemThi     ban ghi diem thi cua thi sinh theo phuong thuc cua nv
     * @param diemCongOpt diem cong (Optional — co the null)
     * @return KetQuaDiem, khong bao gio null
     */
    public KetQuaDiem tinhDiem(NguyenVong nv, DiemThi diemThi, Optional<DiemCong> diemCongOpt) {
        KetQuaDiem kq = new KetQuaDiem();
        kq.diemThxt = ZERO;
        kq.diemCong = ZERO;
        kq.diemUutien = ZERO;
        kq.coBangQuyDoi = false;

        if (diemThi == null || diemThi.getDanhSachDiemChiTiet() == null
                || diemThi.getDanhSachDiemChiTiet().isEmpty()) {
            kq.diemXettuyen = ZERO;
            kq.ghiChu = "Khong co du lieu diem thi";
            return kq;
        }

        NganhToHop nth = nv.getNganhToHop();
        if (nth == null) {
            kq.diemXettuyen = ZERO;
            kq.ghiChu = "Khong co thong tin nganh-to-hop";
            return kq;
        }

        ToHop toHop = nth.getToHop();
        if (toHop == null) {
            kq.diemXettuyen = ZERO;
            kq.ghiChu = "Khong co thong tin to hop";
            return kq;
        }

        PhuongThuc pt = nv.getPhuongThuc();

        // Lay danh sach mon cua to hop
        List<ToHopMon> monToHop = toHopDao.findMonByToHopId(toHop.getTohopId());
        Set<Integer> monToHopIds = new HashSet<>();
        for (ToHopMon thm : monToHop) {
            if (thm.getMon() != null) monToHopIds.add(thm.getMon().getMonId());
        }

        // Lay he so / mon chinh tu NganhToHopMon (override)
        Map<Integer, BigDecimal> heSoMap = new HashMap<>();
        Map<Integer, Boolean> monChinhMap = new HashMap<>();
        if (nth.getDanhSachNganhToHopMon() != null) {
            for (NganhToHopMon nthm : nth.getDanhSachNganhToHopMon()) {
                if (nthm.getMon() != null) {
                    heSoMap.put(nthm.getMon().getMonId(), new BigDecimal(nthm.getHeSo()));
                    monChinhMap.put(nthm.getMon().getMonId(), nthm.getIsMonChinh());
                }
            }
        }

        // Tong hop diem
        BigDecimal tongDiem = ZERO;
        for (DiemThiChiTiet ct : diemThi.getDanhSachDiemChiTiet()) {
            if (ct.getMon() == null || !monToHopIds.contains(ct.getMon().getMonId())) {
                continue; // chi lay mon thuoc to hop
            }

            BigDecimal diemDung = ct.getDiemSudung();
            if (diemDung == null) diemDung = ct.getDiemGoc() != null ? ct.getDiemGoc() : ZERO;

            // Quy doi BangQuyDoi
            BigDecimal diemSauQD = timDiemQuyDoi(diemDung, pt, toHop, ct.getMon());
            if (diemSauQD != null) {
                diemDung = diemSauQD;
                kq.coBangQuyDoi = true;
            }

            // He so
            BigDecimal heSo = heSoMap.get(ct.getMon().getMonId());
            if (heSo == null) heSo = BigDecimal.ONE;

            tongDiem = tongDiem.add(diemDung.multiply(heSo).setScale(SCALE, ROUND));
        }

        kq.diemThxt = tongDiem.setScale(SCALE, ROUND);

        // Tru diemLech
        BigDecimal doLech = nth.getDoLech();
        if (doLech != null && doLech.compareTo(ZERO) > 0) {
            kq.diemThxt = kq.diemThxt.subtract(doLech).setScale(SCALE, ROUND);
        }

        // Diem cong
        if (diemCongOpt != null && diemCongOpt.isPresent()) {
            DiemCong dc = diemCongOpt.get();
            kq.diemCong = dc.getDiemTong() != null ? dc.getDiemTong() : ZERO;
            // Tach rieng diem uu tien (uutien = diemTong - chungchi)
            // cc = dc.getDiemChungchi() != null ? dc.getDiemChungchi() : ZERO;
            BigDecimal ut = dc.getDiemUutienXt() != null ? dc.getDiemUutienXt() : ZERO;
            kq.diemUutien = ut;
        }

        kq.diemXettuyen = kq.diemThxt.add(kq.diemCong).setScale(SCALE, ROUND);
        kq.ghiChu = null;
        return kq;
    }

    /**
     * Quy doi diem bang BangQuyDoi.
     * Tra ve null neu khong co ban ghi quy doi phu hop.
     */
    public BigDecimal timDiemQuyDoi(BigDecimal diemGoc, PhuongThuc pt, ToHop toHop, Mon mon) {
        if (pt == null || diemGoc == null) return null;

        BangQuyDoi bqd = bangQuyDoiDao.quyDoiDiem(
            pt.getPhuongthucId(),
            toHop != null ? toHop.getTohopId() : null,
            mon != null ? mon.getMonId() : null,
            diemGoc
        );
        if (bqd == null) return null;

        // Phep chia ty le
        BigDecimal tu = diemGoc.subtract(bqd.getDiemTu());
        BigDecimal den = bqd.getDiemDen().subtract(bqd.getDiemTu());
        BigDecimal qdDen = bqd.getDiemQuydoiDen().subtract(bqd.getDiemQuydoiTu());

        if (den.compareTo(ZERO) == 0) return null;
        BigDecimal diemQd = bqd.getDiemQuydoiTu()
            .add(tu.multiply(qdDen).divide(den, SCALE, ROUND));
        return diemQd.setScale(SCALE, ROUND);
    }

    /**
     * Tinh diem cong tu dong cho mot thi sinh, tat ca cac nganh-to-hop.
     * Su dung mucDiem cua KhuVucUutien va DoiTuongUutien.
     */
    public List<DiemCong> taoDiemCongTuDong(ThiSinh ts, PhuongThuc pt) {
        List<DiemCong> list = new ArrayList<>();

        BigDecimal diemUuTien = ZERO;
        if (ts.getKhuVucUutien() != null && ts.getKhuVucUutien().getMucDiem() != null) {
            diemUuTien = diemUuTien.add(ts.getKhuVucUutien().getMucDiem());
        }
        if (ts.getDoiTuongUutien() != null && ts.getDoiTuongUutien().getMucDiem() != null) {
            diemUuTien = diemUuTien.add(ts.getDoiTuongUutien().getMucDiem());
        }

        // Lay tat ca nganh-to-hop ma thi sinh da dang ky nguyen vong
        // Hoac lay tat ca nganh-to-hop hien co (neu muon tao cho tat ca)
        // O day ta chi tao cho nhung nganh-to-hop co nguyen vong
        List<NguyenVong> nvs = new NguyenVongDao().findByThiSinhId(ts.getThisinhId());
        Set<Integer> daCo = new HashSet<>();
        for (NguyenVong nv : nvs) {
            if (nv.getNganhToHop() != null && nv.getPhuongThuc() != null) {
                int key = nv.getNganhToHop().getNganhTohopId() * 1000
                    + (nv.getPhuongThuc().getPhuongthucId() != null
                       ? nv.getPhuongThuc().getPhuongthucId() : 0);
                daCo.add(key);
            }
        }

        for (Integer key : daCo) {
            // decode key: tao lai nganh-to-hop va phuong thuc
            // Vì key = nganhToHopId * 1000 + phuongThucId
            int nthId = key / 1000;
            short ptId = (short) (key % 1000);
            if (ptId == 0 && pt != null) ptId = pt.getPhuongthucId();

            NganhToHop nth = new NganhToHopDao().findById(nthId);
            PhuongThuc ptReal = ptId > 0 ? phuongThucDao.findById((int) ptId) : pt;
            if (nth == null || ptReal == null) continue;

            Optional<DiemCong> opt = diemCongDao.findByThiSinhNganhToHopPhuongThuc(
                ts.getThisinhId(), nthId, ptReal.getPhuongthucId());

            DiemCong dc;
            if (opt.isPresent()) {
                dc = opt.get();
                dc.setDiemUutienXt(diemUuTien);
            } else {
                dc = new DiemCong();
                dc.setThiSinh(ts);
                dc.setNganhToHop(nth);
                dc.setPhuongThuc(ptReal);
                dc.setDiemChungchi(ZERO);
                dc.setDiemUutienXt(diemUuTien);
            }
            dc.setDiemTong(dc.getDiemChungchi().add(diemUuTien));
            list.add(dc);
        }
        return list;
    }

    /**
     * Tinh diem cho mot nguyen vong — tra ve diem xet tuyen cuoi cung.
     */
    public BigDecimal tinhDiemXettuyen(NguyenVong nv) {
        if (nv.getThiSinh() == null || nv.getPhuongThuc() == null) return null;

        DiemThi diemThi = diemThiDao.findByThiSinhAndPhuongThuc(
            nv.getThiSinh().getThisinhId(),
            nv.getPhuongThuc().getPhuongthucId(),
            (short) 2026  // TODO: lay nam tu nhien cua nguyen vong
        ).orElse(null);

        Optional<DiemCong> diemCong = Optional.empty();
        if (nv.getNganhToHop() != null) {
            diemCong = diemCongDao.findByThiSinhNganhToHopPhuongThuc(
                nv.getThiSinh().getThisinhId(),
                nv.getNganhToHop().getNganhTohopId(),
                nv.getPhuongThuc().getPhuongthucId()
            );
        }

        KetQuaDiem kq = tinhDiem(nv, diemThi, diemCong);
        return kq.diemXettuyen;
    }
}

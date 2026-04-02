package com.tuyensinh.service.interfaceService;

import com.tuyensinh.entity.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IXetTuyenService {

    // Phuong thuc
    List<PhuongThuc> findAllPhuongThuc();
    List<PhuongThuc> findActivePhuongThuc();
    Optional<PhuongThuc> findPhuongThucByMa(String ma);

    // Nganh
    List<Nganh> findAllNganh();
    List<Nganh> findActiveNganh();
    Optional<Nganh> findNganhByMa(String ma);
    Nganh findNganhById(Integer id);
    Nganh saveNganh(Nganh n);
    void updateNganh(Nganh n);
    void deleteNganh(Nganh n);
    List<Nganh> searchNganh(String keyword);
    List<Nganh> findNganhByPage(int page, int pageSize);
    long countNganh();

    // To hop
    List<ToHop> findAllToHop();
    Optional<ToHop> findToHopByMa(String ma);
    ToHop saveToHop(ToHop th);
    void updateToHop(ToHop th);
    void deleteToHop(ToHop th);
    List<ToHop> searchToHop(String keyword);
    List<ToHopMon> getMonByToHop(Integer tohopId);
    List<ToHop> findNangKhieuToHop();

    // Nganh To Hop
    List<NganhToHop> findNganhToHopByNganh(Integer nganhId);
    NganhToHop saveNganhToHop(NganhToHop nt);
    void deleteNganhToHop(NganhToHop nt);
    List<NganhToHop> findAllNganhToHop();

    /**
     * Dong bo cac lien ket nganh–to hop: xoa lien ket khong con trong danh sach,
     * them/cap nhat do lech. Dong dau tien (neu co) duoc gan lam to hop goc ({@link Nganh#setToHopGoc}).
     */
    void syncNganhToHopForNganh(Integer nganhId, List<Map.Entry<Integer, BigDecimal>> links);

    // Diem Thi
    List<DiemThi> findDiemThiByThiSinh(Integer thisinhId);
    DiemThi saveDiemThi(DiemThi dt);
    void updateDiemThi(DiemThi dt);
    void deleteDiemThi(DiemThi dt);
    Optional<DiemThi> findDiemThi(Integer thisinhId, Short phuongthucId, Short nam);
    List<DiemThi> findDiemThiByPhuongThuc(Short phuongthucId);
    List<DiemThi> searchDiemThi(String keyword, Short phuongthucId);
    List<Object[]> thongKeDiemTheoMon(Short phuongthucId);

    // Nguyen Vong
    List<NguyenVong> findNguyenVongByThiSinh(Integer thisinhId);
    NguyenVong saveNguyenVong(NguyenVong nv);
    void updateNguyenVong(NguyenVong nv);
    void deleteNguyenVong(NguyenVong nv);
    List<NguyenVong> findAllNguyenVong();
    List<NguyenVong> findNguyenVongByNganhPhuongThuc(Integer nganhId, Short phuongthucId);
    int countTrungTuyen(Integer nganhId, Short phuongthucId);

    int getTotalPages(long total, int pageSize);
}

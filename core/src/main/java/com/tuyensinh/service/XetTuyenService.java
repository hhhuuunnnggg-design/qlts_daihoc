package com.tuyensinh.service;

import com.tuyensinh.dao.*;
import com.tuyensinh.entity.*;
import com.tuyensinh.service.interfaceService.IXetTuyenService;

import java.util.List;
import java.util.Optional;

public class XetTuyenService implements IXetTuyenService {

    private final PhuongThucDao phuongThucDao = new PhuongThucDao();
    private final NganhDao nganhDao = new NganhDao();
    private final ToHopDao toHopDao = new ToHopDao();
    private final NganhToHopDao nganhToHopDao = new NganhToHopDao();
    private final DiemThiDao diemThiDao = new DiemThiDao();
    private final NguyenVongDao nguyenVongDao = new NguyenVongDao();

    // --- Phuong thuc ---
    public List<PhuongThuc> findAllPhuongThuc() {
        return phuongThucDao.findAll();
    }

    public List<PhuongThuc> findActivePhuongThuc() {
        return phuongThucDao.findActive();
    }

    public Optional<PhuongThuc> findPhuongThucByMa(String ma) {
        return phuongThucDao.findByMa(ma);
    }

    // --- Nganh ---
    public List<Nganh> findAllNganh() {
        return nganhDao.findAll();
    }

    public List<Nganh> findActiveNganh() {
        return nganhDao.findActive();
    }

    public Optional<Nganh> findNganhByMa(String ma) {
        return nganhDao.findByMa(ma);
    }

    public Nganh findNganhById(Integer id) {
        return nganhDao.findById(id);
    }

    public Nganh saveNganh(Nganh n) {
        return nganhDao.save(n);
    }

    public void updateNganh(Nganh n) {
        nganhDao.update(n);
    }

    public void deleteNganh(Nganh n) {
        nganhDao.delete(n);
    }

    public List<Nganh> searchNganh(String keyword) {
        return nganhDao.searchByMaOrTen(keyword);
    }

    public List<Nganh> findNganhByPage(int page, int pageSize) {
        return nganhDao.findByPage(page, pageSize);
    }

    public long countNganh() {
        return nganhDao.countAll();
    }

    // --- To hop ---
    public List<ToHop> findAllToHop() {
        return toHopDao.findAll();
    }

    public Optional<ToHop> findToHopByMa(String ma) {
        return toHopDao.findByMa(ma);
    }

    public ToHop saveToHop(ToHop th) {
        return toHopDao.save(th);
    }

    public void updateToHop(ToHop th) {
        toHopDao.update(th);
    }

    public void deleteToHop(ToHop th) {
        toHopDao.delete(th);
    }

    public List<ToHop> searchToHop(String keyword) {
        return toHopDao.searchByMaOrTen(keyword);
    }

    public List<ToHopMon> getMonByToHop(Integer tohopId) {
        return toHopDao.findMonByToHopId(tohopId);
    }

    public List<ToHop> findNangKhieuToHop() {
        return toHopDao.findNangKhieuToHop();
    }

    // --- Nganh To Hop ---
    public List<NganhToHop> findNganhToHopByNganh(Integer nganhId) {
        return nganhToHopDao.findByNganhId(nganhId);
    }

    public NganhToHop saveNganhToHop(NganhToHop nt) {
        return nganhToHopDao.save(nt);
    }

    public void deleteNganhToHop(NganhToHop nt) {
        nganhToHopDao.delete(nt);
    }

    public List<NganhToHop> findAllNganhToHop() {
        return nganhToHopDao.findAll();
    }

    // --- Diem Thi ---
    public List<DiemThi> findDiemThiByThiSinh(Integer thisinhId) {
        return diemThiDao.findByThiSinhId(thisinhId);
    }

    public DiemThi saveDiemThi(DiemThi dt) {
        return diemThiDao.save(dt);
    }

    public void updateDiemThi(DiemThi dt) {
        diemThiDao.update(dt);
    }

    public void deleteDiemThi(DiemThi dt) {
        diemThiDao.delete(dt);
    }

    public Optional<DiemThi> findDiemThi(Integer thisinhId, Short phuongthucId, Short nam) {
        return diemThiDao.findByThiSinhAndPhuongThuc(thisinhId, phuongthucId, nam);
    }

    public List<DiemThi> findDiemThiByPhuongThuc(Short phuongthucId) {
        return diemThiDao.findByPhuongThuc(phuongthucId);
    }

    public List<DiemThi> searchDiemThi(String keyword, Short phuongthucId) {
        return diemThiDao.searchByCccdOrSoBaoDanh(keyword, phuongthucId);
    }

    public List<Object[]> thongKeDiemTheoMon(Short phuongthucId) {
        return diemThiDao.thongKeDiemTheoMon(phuongthucId);
    }

    // --- Nguyen Vong ---
    public List<NguyenVong> findNguyenVongByThiSinh(Integer thisinhId) {
        return nguyenVongDao.findByThiSinhId(thisinhId);
    }

    public NguyenVong saveNguyenVong(NguyenVong nv) {
        return nguyenVongDao.save(nv);
    }

    public void updateNguyenVong(NguyenVong nv) {
        nguyenVongDao.update(nv);
    }

    public void deleteNguyenVong(NguyenVong nv) {
        nguyenVongDao.delete(nv);
    }

    public List<NguyenVong> findAllNguyenVong() {
        return nguyenVongDao.findAll();
    }

    public List<NguyenVong> findNguyenVongByNganhPhuongThuc(Integer nganhId, Short phuongthucId) {
        return nguyenVongDao.findByNganhIdAndPhuongThuc(nganhId, phuongthucId);
    }

    public int countTrungTuyen(Integer nganhId, Short phuongthucId) {
        return nguyenVongDao.countByNganhAndPhuongThuc(nganhId, phuongthucId, NguyenVong.KetQua.TRUNG_TUYEN);
    }

    public int getTotalPages(long total, int pageSize) {
        return (int) Math.ceil((double) total / pageSize);
    }
}

package com.tuyensinh.service;

import com.tuyensinh.dao.ThongKeTuyenSinhDao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class ThongKeTuyenSinhService {

    private final ThongKeTuyenSinhDao dao = new ThongKeTuyenSinhDao();

    public DashboardData loadDashboard() {
        DashboardData data = new DashboardData();
        data.tongQuan = getTongQuan();
        data.theoNganh = getThongKeTheoNganh();
        data.theoPhuongThuc = getThongKeTheoPhuongThuc();
        data.topNganh = getTopNganhNhieuNguyenVong(10);
        data.thiSinhTheoDoiTuong = dao.thongKeThiSinhTheoDoiTuong();
        data.thiSinhTheoKhuVuc = dao.thongKeThiSinhTheoKhuVuc();
        return data;
    }

    public TongQuanDto getTongQuan() {
        Object[] row = dao.thongKeTongQuan();

        TongQuanDto dto = new TongQuanDto();
        dto.tongThiSinh = toLong(row[0]);
        dto.tongNganh = toLong(row[1]);
        dto.tongNguyenVong = toLong(row[2]);
        dto.soTrungTuyen = toLong(row[3]);
        dto.soTruot = toLong(row[4]);
        dto.soChuaXet = toLong(row[5]);

        return dto;
    }

    public List<TheoNganhDto> getThongKeTheoNganh() {
        List<TheoNganhDto> result = new ArrayList<>();

        for (Object[] row : dao.thongKeTheoNganh()) {
            TheoNganhDto dto = new TheoNganhDto();
            dto.maNganh = toString(row[0]);
            dto.tenNganh = toString(row[1]);
            dto.chiTieu = toInteger(row[2]);
            dto.tongNguyenVong = toLong(row[3]);
            dto.soTrungTuyen = toLong(row[4]);
            dto.soTruot = toLong(row[5]);
            dto.diemTrungTuyen = toBigDecimal(row[6]);
            result.add(dto);
        }

        return result;
    }

    public List<TheoPhuongThucDto> getThongKeTheoPhuongThuc() {
        List<TheoPhuongThucDto> result = new ArrayList<>();

        for (Object[] row : dao.thongKeTheoPhuongThuc()) {
            TheoPhuongThucDto dto = new TheoPhuongThucDto();
            dto.maPhuongThuc = toString(row[0]);
            dto.tenPhuongThuc = toString(row[1]);
            dto.tongNguyenVong = toLong(row[2]);
            dto.soTrungTuyen = toLong(row[3]);
            dto.soTruot = toLong(row[4]);
            dto.diemXetTuyenTrungBinh = toBigDecimal(row[5]);
            result.add(dto);
        }

        return result;
    }

    public List<TopNganhDto> getTopNganhNhieuNguyenVong(int limit) {
        List<TopNganhDto> result = new ArrayList<>();

        for (Object[] row : dao.topNganhNhieuNguyenVong(limit)) {
            TopNganhDto dto = new TopNganhDto();
            dto.maNganh = toString(row[0]);
            dto.tenNganh = toString(row[1]);
            dto.tongNguyenVong = toLong(row[2]);
            result.add(dto);
        }

        return result;
    }

    private long toLong(Object value) {
        if (value == null) return 0L;
        return ((Number) value).longValue();
    }

    private Integer toInteger(Object value) {
        if (value == null) return 0;
        return ((Number) value).intValue();
    }

    private String toString(Object value) {
        return value == null ? "" : value.toString();
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Number) return BigDecimal.valueOf(((Number) value).doubleValue());
        return null;
    }

    public static class DashboardData {
        public TongQuanDto tongQuan;
        public List<TheoNganhDto> theoNganh;
        public List<TheoPhuongThucDto> theoPhuongThuc;
        public List<TopNganhDto> topNganh;
        public java.util.List<ThongKeThiSinhGroupDto> thiSinhTheoDoiTuong;
        public java.util.List<ThongKeThiSinhGroupDto> thiSinhTheoKhuVuc;
    }

    public static class TongQuanDto {
        public long tongThiSinh;
        public long tongNganh;
        public long tongNguyenVong;
        public long soTrungTuyen;
        public long soTruot;
        public long soChuaXet;
    }

    public static class TheoNganhDto {
        public String maNganh;
        public String tenNganh;
        public Integer chiTieu;
        public long tongNguyenVong;
        public long soTrungTuyen;
        public long soTruot;
        public BigDecimal diemTrungTuyen;
    }

    public static class TheoPhuongThucDto {
        public String maPhuongThuc;
        public String tenPhuongThuc;
        public long tongNguyenVong;
        public long soTrungTuyen;
        public long soTruot;
        public BigDecimal diemXetTuyenTrungBinh;
    }

    public static class TopNganhDto {
        public String maNganh;
        public String tenNganh;
        public long tongNguyenVong;
    }

    public static class ThongKeThiSinhGroupDto {
        public String ma;
        public String ten;
        public Long soLuong;

        public ThongKeThiSinhGroupDto(String ma, String ten, Number soLuong) {
            this.ma = ma;
            this.ten = ten;
            this.soLuong = soLuong != null ? soLuong.longValue() : 0L;
        }
    }

    public static class ThiSinhDetailDto {
        public Integer thisinhId;
        public String cccd;
        public String sobaodanh;
        public String hoTen;
        public String ngaySinh;
        public String gioiTinh;
        public String doiTuong;
        public String khuVuc;

        public ThiSinhDetailDto(Object[] row) {
            this.thisinhId = row[0] != null ? ((Number) row[0]).intValue() : null;
            this.cccd = row[1] != null ? row[1].toString() : "";
            this.sobaodanh = row[2] != null ? row[2].toString() : "";
            this.hoTen = row[3] != null ? row[3].toString() : "";
            this.ngaySinh = row[4] != null ? row[4].toString() : "";
            this.gioiTinh = row[5] != null ? row[5].toString() : "";
            this.doiTuong = row[6] != null ? row[6].toString() : "";
            this.khuVuc = row[7] != null ? row[7].toString() : "";
        }
    }

    public static class DiemThiSinhDto {
        public String phuongThuc;
        public Integer namTuyenSinh;
        public String sobaodanh;
        public String maMon;
        public String tenMon;
        public BigDecimal diemGoc;
        public BigDecimal diemQuyDoi;
        public BigDecimal diemSuDung;

        public DiemThiSinhDto(Object[] row) {
            this.phuongThuc = row[0] != null ? row[0].toString() : "";
            this.namTuyenSinh = row[1] != null ? ((Number) row[1]).intValue() : null;
            this.sobaodanh = row[2] != null ? row[2].toString() : "";
            this.maMon = row[3] != null ? row[3].toString() : "";
            this.tenMon = row[4] != null ? row[4].toString() : "";
            this.diemGoc = row[5] instanceof BigDecimal ? (BigDecimal) row[5] : null;
            this.diemQuyDoi = row[6] instanceof BigDecimal ? (BigDecimal) row[6] : null;
            this.diemSuDung = row[7] instanceof BigDecimal ? (BigDecimal) row[7] : null;
        }
    }

    public ThiSinhDetailDto findThiSinhDetail(String key) {
        Object[] row = dao.findThiSinhDetail(key);
        return row == null ? null : new ThiSinhDetailDto(row);
    }

    public java.util.List<DiemThiSinhDto> findDiemThiSinh(String key) {
        java.util.List<Object[]> rows = dao.findDiemThiSinh(key);
        java.util.List<DiemThiSinhDto> result = new java.util.ArrayList<>();

        for (Object[] row : rows) {
            result.add(new DiemThiSinhDto(row));
        }

        return result;
    }
}
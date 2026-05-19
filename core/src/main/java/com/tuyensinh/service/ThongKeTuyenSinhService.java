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
}
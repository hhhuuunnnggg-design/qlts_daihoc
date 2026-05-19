package com.tuyensinh.web.servlet.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public class NganhToHopDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private int nganhId;
    private int id;
    private String ma;
    private String ten;
    private BigDecimal doLech;
    private List<MonDto> monList;

    public NganhToHopDto() {}

    public NganhToHopDto(int nganhId, int id, String ma, String ten, BigDecimal doLech, List<MonDto> monList) {
        this.nganhId = nganhId;
        this.id = id;
        this.ma = ma;
        this.ten = ten;
        this.doLech = doLech;
        this.monList = monList;
    }

    public int getNganhId() { return nganhId; }
    public int getId() { return id; }
    public String getMa() { return ma; }
    public String getTen() { return ten; }
    public BigDecimal getDoLech() { return doLech; }
    public List<MonDto> getMonList() { return monList; }

    public static class MonDto implements Serializable {
        private static final long serialVersionUID = 1L;

        private int monId;
        private String maMon;
        private String tenMon;
        private int heSo;

        public MonDto() {}
        public MonDto(int monId, String maMon, String tenMon, int heSo) {
            this.monId = monId;
            this.maMon = maMon;
            this.tenMon = tenMon;
            this.heSo = heSo;
        }

        public int getMonId() { return monId; }
        public String getMaMon() { return maMon; }
        public String getTenMon() { return tenMon; }
        public int getHeSo() { return heSo; }
    }
}

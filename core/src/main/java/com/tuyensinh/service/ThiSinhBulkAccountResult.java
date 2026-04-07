package com.tuyensinh.service;

import java.util.ArrayList;
import java.util.List;

public class ThiSinhBulkAccountResult {
    private int totalCandidates;
    private int createdCount;
    private int skippedCount;
    private int errorCount;
    private final List<String> details = new ArrayList<>();

    public int getTotalCandidates() {
        return totalCandidates;
    }

    public void setTotalCandidates(int totalCandidates) {
        this.totalCandidates = totalCandidates;
    }

    public int getCreatedCount() {
        return createdCount;
    }

    public int getSkippedCount() {
        return skippedCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void incrementCreated() {
        this.createdCount++;
    }

    public void incrementSkipped() {
        this.skippedCount++;
    }

    public void incrementError() {
        this.errorCount++;
    }

    public void addDetail(String detail) {
        if (detail != null && !detail.trim().isEmpty()) {
            details.add(detail);
        }
    }

    public List<String> getDetails() {
        return details;
    }

    public String getDetailsText() {
        if (details.isEmpty()) {
            return "Khong co chi tiet.";
        }
        return String.join("\n", details);
    }

    public String toHumanMessage() {
        return "Tong so thi sinh chua co tai khoan: " + totalCandidates + "\n"
                + "Tao moi thanh cong: " + createdCount + "\n"
                + "Bo qua: " + skippedCount + "\n"
                + "Loi: " + errorCount;
    }
}
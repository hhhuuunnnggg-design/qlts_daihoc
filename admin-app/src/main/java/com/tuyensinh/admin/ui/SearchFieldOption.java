package com.tuyensinh.admin.ui;

/**
 * Lua chon truong tim kiem tren cac panel admin.
 * key duoc gui xuong Service/DAO; label dung de hien thi tren combobox.
 */
public class SearchFieldOption {
    private final String key;
    private final String label;

    public SearchFieldOption(String key, String label) {
        this.key = key;
        this.label = label;
    }

    public String getKey() {
        return key;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}

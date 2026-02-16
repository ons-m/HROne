package com.recruitx.hrone;

public class TypeItem {

    private final String code;
    private final String label;

    public TypeItem(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label; // THIS is what ListView shows
    }
}

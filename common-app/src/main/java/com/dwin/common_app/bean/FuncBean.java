package com.dwin.common_app.bean;

public class FuncBean {
    private int functionId;
    private String name;
    private int iconResId;

    public FuncBean(String name, int iconResId) {
        this.name = name;
        this.iconResId = iconResId;
    }

    public FuncBean(int functionId, String name, int iconResId) {
        this.name = name;
        this.iconResId = iconResId;
        this.functionId = functionId;
    }

    public int getFunctionId() {
        return functionId;
    }

    public void setFunctionId(int functionId) {
        this.functionId = functionId;
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }
}

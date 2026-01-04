package com.dwin.common_app.bean;

public class StorageBean {
    private String path;
    private boolean isSdcard;
    private boolean isUSB;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isSdcard() {
        return isSdcard;
    }

    public void setSdcard(boolean sdcard) {
        isSdcard = sdcard;
    }

    public boolean isUSB() {
        return isUSB;
    }

    public void setUSB(boolean USB) {
        isUSB = USB;
    }

    @Override
    public String toString() {
        return "StorageBean{" +
                "path='" + path + '\'' +
                ", isSdcard=" + isSdcard +
                ", isUSB=" + isUSB +
                '}';
    }
}

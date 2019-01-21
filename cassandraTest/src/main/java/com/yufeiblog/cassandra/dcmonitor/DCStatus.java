package com.yufeiblog.cassandra.dcmonitor;

public class DCStatus {
    private String activeDC;
    private String currentDC;

    public String getActiveDC() {
        return activeDC;
    }

    public void setActiveDC(String activeDC) {
        this.activeDC = activeDC;
    }

    public String getCurrentDC() {
        return currentDC;
    }

    public void setCurrentDC(String currentDC) {
        this.currentDC = currentDC;
    }
}

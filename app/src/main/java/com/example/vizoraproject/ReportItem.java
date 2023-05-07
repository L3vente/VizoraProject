package com.example.vizoraproject;

import java.util.Date;

public class ReportItem {
    private String userId;
    private String reportId;
    private String address;
    private String factoryNum;
    private String date;
    private String currentTimerPosition;
    private String imageFileName;

    public ReportItem(String address, String factoryNum, String date, String currentTimerPosition, String imageFileName) {

        this.address = address;
        this.factoryNum = factoryNum;
        this.date = date;
        this.currentTimerPosition = currentTimerPosition;
        this.imageFileName = imageFileName;
    }

    public ReportItem() {
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCurrentTimerPosition() {
        return currentTimerPosition;
    }

    public void setCurrentTimerPosition(String currentTimerPosition) {
        this.currentTimerPosition = currentTimerPosition;
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public void setImageFileName(String imagesFileName) {
        this.imageFileName = imagesFileName;
    }

    public String getFactoryNum() {
        return factoryNum;
    }

    public void setFactoryNum(String factoryNum) {
        this.factoryNum = factoryNum;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getUserId() {
        return userId;
    }

    public String _getReportId() {
        return reportId;
    }

}

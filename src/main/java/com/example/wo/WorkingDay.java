package com.example.wo;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WorkingDay  {

    private String beginTime;
    private String endTime;
    private double totalPayment;

    private final StringProperty date = new SimpleStringProperty();

    private double totalHours;




    public WorkingDay(String beginTime, String endTime, double totalPayment,String date) {
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.totalPayment = totalPayment;
        this.date.set(date);

    }

    public String getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public double getTotalPayment() throws ParseException {
        return totalPayment;
    }

    public void setTotalPayment(double totalPayment) {
        this.totalPayment = totalPayment;
    }


    public String getDate() {
        return date.get();
    }

    public StringProperty dateProperty() {
        return date;
    }

    public void setDate(String date) {
        this.date.set(date);
    }

    public double getTotalHours() throws ParseException {

        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("hh:mm a");
        java.util.Date beginTimeUtil = dateFormat.parse(beginTime);
        java.util.Date endTimeUtil = dateFormat.parse(endTime);

        if (endTimeUtil.before(beginTimeUtil)) {
            // Adjust the end time to be on the following day
            endTimeUtil.setTime(endTimeUtil.getTime() + 24 * 60 * 60 * 1000);
        }

// Convert java.util.Date to java.sql.Date
        java.sql.Date beginTimeSql = new java.sql.Date(beginTimeUtil.getTime());
        java.sql.Date endTimeSql = new java.sql.Date(endTimeUtil.getTime());

        long millisecondsWorked = endTimeSql.getTime() - beginTimeSql.getTime();
        double hoursWorked = millisecondsWorked / (60.0 * 60.0 * 1000.0);


        return hoursWorked;
    }

}

package com.example.wo;

import javafx.scene.image.ImageView;

public class Employee {
   private String id;
   private String name;
    private ImageView image;
    private double rate;
    private boolean isWorking;

    private double totalPayment;
    private double totalHours;


    public Employee(){}

    public Employee(String id, String name, ImageView image, double rate ,boolean isWorking) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.rate = rate;
        this.isWorking = isWorking;
    }

    public Employee(String name,double totalPayment,double totalHours) {
        this.name = name;
        this.totalPayment =totalPayment;
        this.totalHours = totalHours;

    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ImageView getImage() {
        return image;
    }

    public void setImage(ImageView image) {
        this.image = image;
    }

    public double getRate() {
        return rate;
    }

    public boolean getIsWorking() {
        return isWorking;
    }

    public void setIsWorking(boolean isWorking) {
        this.isWorking = isWorking;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public double getTotalPayment() {

        return totalPayment;
    }

    public double getTotalHours() {
        return totalHours;
    }



    public void setTotalHours(double totalHours) {
        this.totalHours = totalHours;
    }
    public void setTotalPayment(double totalPayment) {
        this.totalPayment = totalPayment;
    }
}

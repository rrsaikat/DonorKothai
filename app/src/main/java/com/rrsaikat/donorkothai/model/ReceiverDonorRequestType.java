package com.rrsaikat.donorkothai.model;


public class ReceiverDonorRequestType {


    String latitude;
    String longitude;
    String bGp;
    String purpose;
    String fName;
    String lName;
    String phone;
    String instanceId;

    public ReceiverDonorRequestType() {
    }

    public ReceiverDonorRequestType(String latitude, String longitude, String bGp, String purpose, String fName, String lName, String phone) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.bGp = bGp;
        this.purpose = purpose;
        this.fName = fName;
        this.lName = lName;
        this.phone = phone;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getfName() {
        return fName;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }

    public String getlName() {
        return lName;
    }

    public void setlName(String lName) {
        this.lName = lName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getbGp() {
        return bGp;
    }

    public void setbGp(String bGp) {
        this.bGp = bGp;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}

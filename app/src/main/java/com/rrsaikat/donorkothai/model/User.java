package com.rrsaikat.donorkothai.model;


import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;


@IgnoreExtraProperties
public class User implements Serializable {

  private String id;

  public String getImg() {
    return img;
  }

  public void setImg(String img) {
    this.img = img;
  }

  public String img;
  public String fName;
  public String lName;
  public String email;
  public String bloodGroup;
  public String dob;
  public int sex;
  public double latitude;
  public double longitude;

  public User() {
  }


  public User(String id) {
        this.id = id;
    }
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
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

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getBloodGroup() {
    return bloodGroup;
  }

  public void setBloodGroup(String bloodGroup) {
    this.bloodGroup = bloodGroup;
  }

  public String getDob() {
    return dob;
  }

  public void setDob(String dob) {
    this.dob = dob;
  }

  public int getSex() {
    return sex;
  }

  public void setSex(int sex) {
    this.sex = sex;
  }

  public double getLatitude() {
    return latitude;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }



}

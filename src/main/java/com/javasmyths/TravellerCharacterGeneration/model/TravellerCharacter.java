/*
 * Copyright (C) 2018 JavaSmyths javasmyths@javasmyths.com
 */
package com.javasmyths.travellercharactergeneration.model;

/**
 *
 * @author Richard
 */
public class TravellerCharacter {
  
  private String Name;
  private Characteristics characteristics;
  private int age;
  

  public TravellerCharacter() {
  }

  public TravellerCharacter(int age) {
    this.age = age;
  }

  public int getAge() {
    return age;
  }

  public void setAge(int age) {
    this.age = age;
  }
  
  public Characteristics getCharacteristics() {
    return characteristics;
  }

  public void setCharacteristics(Characteristics characteristics) {
    this.characteristics = characteristics;
  }

  public String getName() {
    return Name;
  }

  public void setName(String Name) {
    this.Name = Name;
  }

  @Override
  public String toString() {
    return "TravellerCharacter{" + "Name=" + Name + ", age=" + age + ", characteristics=" + characteristics + '}';
  }

}

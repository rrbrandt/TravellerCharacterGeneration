/*
 * Copyright (C) 2018 JavaSmyths javasmyths@javasmyths.com
 */
package com.javasmyths.traveller.model;

import com.javasmyths.traveller.services.Dice;

/**
 *
 * @author Richard
 */
public class Characteristics {
  
  private int strength;
  private int dexterity;
  private int endurance;
  private int intelligence;
  private int education;
  private int socialStanding;

  public Characteristics() {
    this(new Dice());
  }

  public Characteristics(Dice dice) {
    strength = dice.rollTwoDice();
    dexterity = dice.rollTwoDice();
    endurance = dice.rollTwoDice();
    intelligence = dice.rollTwoDice();
    education = dice.rollTwoDice();
    socialStanding = dice.rollTwoDice();
  }

  
  public int getSocialStanding() {
    return socialStanding;
  }
  public void setSocialStanding(int socialStanding) {
    this.socialStanding = socialStanding;
  }
  public int getEducation() {
    return education;
  }
  public void setEducation(int education) {
    this.education = education;
  }
  public int getIntelligence() {
    return intelligence;
  }
  public void setIntelligence(int intelligence) {
    this.intelligence = intelligence;
  }
  public int getEndurance() {
    return endurance;
  }
  public void setEndurance(int endurance) {
    this.endurance = endurance;
  }
  public int getDexterity() {
    return dexterity;
  }
  public void setDexterity(int dexterity) {
    this.dexterity = dexterity;
  }
  public int getStrength() {
    return strength;
  }
  public void setStrength(int strength) {
    this.strength = strength;
  }

  public String getUpp() {
    return encode(strength) + encode(dexterity) + encode(endurance)
        + encode(intelligence) + encode(education) + encode(socialStanding);
  }

  private String encode(int value) {
    return value >= 10 && value <= 15
        ? String.valueOf((char) ('A' + value - 10))
        : Integer.toString(value);
  }
          
  @Override
  public String toString() {
    return "Characteristics{" + "strength=" + strength + ", dexterity=" + dexterity + ", endurance=" + endurance + ", intelligence=" + intelligence + ", education=" + education + ", socialStanding=" + socialStanding + '}';
  }
}

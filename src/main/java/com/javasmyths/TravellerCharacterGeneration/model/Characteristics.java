/*
 * Copyright (C) 2018 JavaSmyths javasmyths@javasmyths.com
 */
package com.javasmyths.travellercharactergeneration.model;

import com.javasmyths.TravellerCharacterGeneration.services.Dice;

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
    Dice dice = new Dice();
    strength = dice.role2Dice();
    dexterity = dice.role2Dice();
    endurance = dice.role2Dice();
    intelligence = dice.role2Dice();
    education = dice.role2Dice();
    socialStanding = dice.role2Dice();
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
  public void setInteliegence(int inteligence) {
    this.intelligence = inteligence;
  }
  public int getEndurance() {
    return endurance;
  }
  public void setEndurance(int Endurance) {
    this.endurance = Endurance;
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

  public String UPP (){
    return "" + strength + dexterity + endurance + intelligence + education + socialStanding;
  }
          
  @Override
  public String toString() {
    return "Characteristics{" + "strength=" + strength + ", dexterity=" + dexterity + ", endurance=" + endurance + ", intellegence=" + intelligence + ", education=" + education + ", socialStanding=" + socialStanding + '}';
  }
}

/*
 * Copyright (C) 2018 JavaSmyths javasmyths@javasmyths.com
 */
package com.javasmyths.TravellerCharacterGeneration.model;

/**
 *
 * @author Richard
 */
public class Skills {

  private String dieRole;
  private String personalDevelopment;
  private String serviceSkill;
  private String advancedEducation;
  private String advancedEducationPlusEduc;
  private int survival;
  private int commission;
  private int promotion;
  private int reenlist;
  public Skills(String dieRole, String personalDevelopment, String serviceSkill, String advancedEducation, String advancedEducationPlusEduc) {
    this.dieRole = dieRole;
    this.personalDevelopment = personalDevelopment;
    this.serviceSkill = serviceSkill;
    this.advancedEducation = advancedEducation;
    this.advancedEducationPlusEduc = advancedEducationPlusEduc;
  }


  public String getDieRole() {
    return dieRole;
  }
  public void setDieRole(String dieRole) {
    this.dieRole = dieRole;
  }
  public String getPersonalDevelopment() {
    return personalDevelopment;
  }

  public void setPersonalDevelopment(String personalDevelopment) {
    this.personalDevelopment = personalDevelopment;
  }
  public String getServiceSkill() {
    return serviceSkill;
  }
  public void setServiceSkill(String serviceSkill) {
    this.serviceSkill = serviceSkill;
  }
  public String getAdvancedEducation() {
    return advancedEducation;
  }
  public void setAdvancedEducation(String advancedEducation) {
    this.advancedEducation = advancedEducation;
  }
  public String getAdvancedEducationPlusEduc() {
    return advancedEducationPlusEduc;
  }
  public void setAdvancedEducationPlusEduc(String advancedEducationPlusEduc) {
    this.advancedEducationPlusEduc = advancedEducationPlusEduc;
  }
  public int getSurvival() {
    return survival;
  }
  public void setSurvival(int survival) {
    this.survival = survival;
  }
  public int getCommission() {
    return commission;
  }
  public void setCommission(int commission) {
    this.commission = commission;
  }
  public int getPromotion() {
    return promotion;
  }
  public void setPromotion(int promotion) {
    this.promotion = promotion;
  }
  public int getReenlist() {
    return reenlist;
  }
  public void setReenlist(int reenlist) {
    this.reenlist = reenlist;
  }

  @Override
  public String toString() {
    return "Skills{" + "dieRole=" + dieRole + ", personalDevelopment=" + personalDevelopment + ", serviceSkill=" + serviceSkill + ", advancedEducation=" + advancedEducation + ", advancedEducationPlusEduc=" + advancedEducationPlusEduc + ", survival=" + survival + ", commission=" + commission + ", promotion=" + promotion + ", reenlist=" + reenlist + '}';
  }

}

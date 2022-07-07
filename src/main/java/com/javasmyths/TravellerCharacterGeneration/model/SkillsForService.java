/*
 * Copyright (C) 2018 JavaSmyths javasmyths@javasmyths.com
 */
package com.javasmyths.TravellerCharacterGeneration.model;

import com.javasmyths.TravellerCharacterGeneration.services.Dice;

/**
 *
 * @author Richard
 */
public class SkillsForService {
  private RolableSkills [] personalDevelopment = new RolableSkills[Dice.MAX_DIE];
  private RolableSkills [] serviceSkills = new RolableSkills[Dice.MAX_DIE];
  private RolableSkills [] advancedEducation = new RolableSkills[Dice.MAX_DIE];
  private RolableSkills [] advancedEducationPlusEduc = new RolableSkills[Dice.MAX_DIE];

  public SkillsForService(RolableSkills [] personalDevelopment, RolableSkills [] serviceSkills, RolableSkills [] advancedEducation, RolableSkills [] advancedEducationPlusEduc) {
    this.personalDevelopment = personalDevelopment;
    this.serviceSkills = serviceSkills;
    this.advancedEducation = advancedEducation;
    this.advancedEducationPlusEduc = advancedEducationPlusEduc;
  }


  
  public RolableSkills[] getPersonalDevelopment() {
    return personalDevelopment;
  }

  public void setPersonalDevelopment(RolableSkills[] personalDevelopment) {
    this.personalDevelopment = personalDevelopment;
  }

  public RolableSkills[] getServiceSkills() {
    return serviceSkills;
  }

  public void setServiceSkills(RolableSkills[] serviceSkills) {
    this.serviceSkills = serviceSkills;
  }

  public RolableSkills[] getAdvancedEducation() {
    return advancedEducation;
  }

  public void setAdvancedEducation(RolableSkills[] advancedEducation) {
    this.advancedEducation = advancedEducation;
  }

  public RolableSkills[] getAdvancedEducationPlusEduc() {
    return advancedEducationPlusEduc;
  }

  public void setAdvancedEducationPlusEduc(RolableSkills[] advancedEducationPlusEduc) {
    this.advancedEducationPlusEduc = advancedEducationPlusEduc;
  }
}

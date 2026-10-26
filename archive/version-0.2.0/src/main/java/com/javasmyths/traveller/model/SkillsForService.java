/*
 * Copyright (C) 2018 JavaSmyths javasmyths@javasmyths.com
 */
package com.javasmyths.traveller.model;

/**
 *
 * @author Richard
 */
public class SkillsForService {
  private final RollableSkill[] personalDevelopment;
  private final RollableSkill[] serviceSkills;
  private final RollableSkill[] advancedEducation;
  private final RollableSkill[] advancedEducationPlusEducation;

  public SkillsForService(RollableSkill[] personalDevelopment, RollableSkill[] serviceSkills,
      RollableSkill[] advancedEducation, RollableSkill[] advancedEducationPlusEducation) {
    validateTable(personalDevelopment);
    validateTable(serviceSkills);
    validateTable(advancedEducation);
    validateTable(advancedEducationPlusEducation);
    this.personalDevelopment = personalDevelopment;
    this.serviceSkills = serviceSkills;
    this.advancedEducation = advancedEducation;
    this.advancedEducationPlusEducation = advancedEducationPlusEducation;
  }

  private void validateTable(RollableSkill[] table) {
    if (table == null || table.length != 6) {
      throw new IllegalArgumentException("Every skill table must contain exactly six entries");
    }
  }

  public RollableSkill[] getPersonalDevelopment() { return personalDevelopment.clone(); }
  public RollableSkill[] getServiceSkills() { return serviceSkills.clone(); }
  public RollableSkill[] getAdvancedEducation() { return advancedEducation.clone(); }
  public RollableSkill[] getAdvancedEducationPlusEducation() { return advancedEducationPlusEducation.clone(); }
}

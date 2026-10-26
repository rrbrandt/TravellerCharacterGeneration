package com.javasmyths.traveller.model;

public enum SkillTable {
  PERSONAL_DEVELOPMENT("Personal development", false),
  SERVICE_SKILLS("Service skills", false),
  ADVANCED_EDUCATION("Advanced education", false),
  ADVANCED_EDUCATION_8_PLUS("Advanced education (Education 8+)", true);

  private final String displayName;
  private final boolean requiresEducationEight;

  SkillTable(String displayName, boolean requiresEducationEight) {
    this.displayName = displayName;
    this.requiresEducationEight = requiresEducationEight;
  }

  public String getDisplayName() {
    return displayName;
  }

  public boolean isRequiresEducationEight() {
    return requiresEducationEight;
  }

  public RollableSkill[] skillsFrom(SkillsForService tables) {
    return switch (this) {
      case PERSONAL_DEVELOPMENT -> tables.getPersonalDevelopment();
      case SERVICE_SKILLS -> tables.getServiceSkills();
      case ADVANCED_EDUCATION -> tables.getAdvancedEducation();
      case ADVANCED_EDUCATION_8_PLUS -> tables.getAdvancedEducationPlusEducation();
    };
  }
}

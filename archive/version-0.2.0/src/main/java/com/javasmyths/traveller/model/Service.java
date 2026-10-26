/*
 * Copyright (C) 2018 JavaSmyths javasmyths@javasmyths.com
 */
package com.javasmyths.traveller.model;

/**
 *
 * @author Richard
 */
public enum Service {
  NAVY("Navy", 8, 5, 10, 8, 6, "Intelligence 8+ gives +1; Education 9+ gives +2"),
  MARINES("Marines", 9, 6, 9, 9, 6, "Intelligence 8+ gives +1; Strength 8+ gives +2"),
  ARMY("Army", 5, 5, 5, 6, 7, "Dexterity 6+ gives +1; Endurance 5+ gives +2"),
  SCOUTS("Scouts", 7, 7, 0, 0, 3, "Intelligence 6+ gives +1; Strength 8+ gives +2"),
  MERCHANTS("Merchants", 7, 5, 4, 10, 4, "Strength 7+ gives +1; Intelligence 8+ gives +2"),
  OTHER("Other", 3, 5, 0, 0, 5, "No enlistment modifiers");

  private final String displayName;
  private final int enlistmentTarget;
  private final int survivalTarget;
  private final int commissionTarget;
  private final int promotionTarget;
  private final int reenlistmentTarget;
  private final String enlistmentModifiers;

  Service(String displayName, int enlistmentTarget, int survivalTarget,
      int commissionTarget, int promotionTarget, int reenlistmentTarget,
      String enlistmentModifiers) {
    this.displayName = displayName;
    this.enlistmentTarget = enlistmentTarget;
    this.survivalTarget = survivalTarget;
    this.commissionTarget = commissionTarget;
    this.promotionTarget = promotionTarget;
    this.reenlistmentTarget = reenlistmentTarget;
    this.enlistmentModifiers = enlistmentModifiers;
  }

  public String getDisplayName() { return displayName; }
  public int getEnlistmentTarget() { return enlistmentTarget; }
  public int getSurvivalTarget() { return survivalTarget; }
  public int getCommissionTarget() { return commissionTarget; }
  public int getPromotionTarget() { return promotionTarget; }
  public int getReenlistmentTarget() { return reenlistmentTarget; }
  public String getCommissionTargetLabel() { return commissionTarget == 0 ? "—" : commissionTarget + "+"; }
  public String getPromotionTargetLabel() { return promotionTarget == 0 ? "—" : promotionTarget + "+"; }
  public String getEnlistmentModifiers() { return enlistmentModifiers; }

  public int enlistmentModifier(Characteristics c) {
    return switch (this) {
      case NAVY -> (c.getIntelligence() >= 8 ? 1 : 0) + (c.getEducation() >= 9 ? 2 : 0);
      case MARINES -> (c.getIntelligence() >= 8 ? 1 : 0) + (c.getStrength() >= 8 ? 2 : 0);
      case ARMY -> (c.getDexterity() >= 6 ? 1 : 0) + (c.getEndurance() >= 5 ? 2 : 0);
      case SCOUTS -> (c.getIntelligence() >= 6 ? 1 : 0) + (c.getStrength() >= 8 ? 2 : 0);
      case MERCHANTS -> (c.getStrength() >= 7 ? 1 : 0) + (c.getIntelligence() >= 8 ? 2 : 0);
      case OTHER -> 0;
    };
  }

  public int survivalModifier(Characteristics c) {
    return switch (this) {
      case NAVY -> c.getIntelligence() >= 7 ? 2 : 0;
      case MARINES -> c.getEndurance() >= 8 ? 2 : 0;
      case ARMY -> c.getEducation() >= 6 ? 2 : 0;
      case SCOUTS -> c.getEndurance() >= 9 ? 2 : 0;
      case MERCHANTS -> c.getIntelligence() >= 7 ? 2 : 0;
      case OTHER -> c.getIntelligence() >= 9 ? 2 : 0;
    };
  }

  public int commissionModifier(Characteristics c) {
    return switch (this) {
      case NAVY -> c.getSocialStanding() >= 9 ? 1 : 0;
      case MARINES -> c.getEducation() >= 7 ? 1 : 0;
      case ARMY -> c.getEndurance() >= 7 ? 1 : 0;
      case MERCHANTS -> c.getIntelligence() >= 6 ? 1 : 0;
      case SCOUTS, OTHER -> 0;
    };
  }

  public int promotionModifier(Characteristics c) {
    return switch (this) {
      case NAVY -> c.getEducation() >= 8 ? 1 : 0;
      case MARINES -> c.getSocialStanding() >= 8 ? 1 : 0;
      case ARMY -> c.getEducation() >= 7 ? 1 : 0;
      case MERCHANTS -> c.getIntelligence() >= 9 ? 1 : 0;
      case SCOUTS, OTHER -> 0;
    };
  }
}

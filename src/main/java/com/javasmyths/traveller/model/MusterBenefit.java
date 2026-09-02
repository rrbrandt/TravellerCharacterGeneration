package com.javasmyths.traveller.model;

public enum MusterBenefit {
  LOW_PASSAGE("Low Passage"),
  PLUS_ONE_INTELLIGENCE("+1 Intelligence"),
  PLUS_TWO_INTELLIGENCE("+2 Intelligence"),
  PLUS_ONE_EDUCATION("+1 Education"),
  PLUS_TWO_EDUCATION("+2 Education"),
  BLADE("Blade"),
  GUN("Gun"),
  TRAVELLERS_AID_SOCIETY("Travellers' Aid Society membership"),
  HIGH_PASSAGE("High Passage"),
  MIDDLE_PASSAGE("Middle Passage"),
  PLUS_ONE_SOCIAL("+1 Social Standing"),
  PLUS_TWO_SOCIAL("+2 Social Standing"),
  SCOUT_SHIP("Scout Ship"),
  FREE_TRADER("Free Trader"),
  NONE("No material benefit");

  private final String displayName;

  MusterBenefit(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}

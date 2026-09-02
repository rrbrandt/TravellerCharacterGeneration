package com.javasmyths.traveller.model;

public enum RollableSkill {
  PLUS_STRENGTH("+1 Strength"), PLUS_DEXTERITY("+1 Dexterity"),
  PLUS_ENDURANCE("+1 Endurance"), PLUS_INTELLIGENCE("+1 Intelligence"),
  PLUS_EDUCATION("+1 Education"), PLUS_SOCIAL("+1 Social Standing"),
  MINUS_SOCIAL("-1 Social Standing"),
  ADMIN("Admin"), AIR_RAFT("Air/Raft"), BLADE_COMBAT("Blade Combat"),
  BRAWLING("Brawling"), BRIBERY("Bribery"), COMPUTER("Computer"),
  ELECTRONICS("Electronics"), ENGINEERING("Engineering"), FORGERY("Forgery"),
  FORWARD_OBSERVER("Forward Observer"), GAMBLING("Gambling"),
  GUN_COMBAT("Gun Combat"), GUNNERY("Gunnery"), JACK_OF_ALL_TRADES("Jack-of-all-Trades"),
  LEADER("Leader"), MECHANICAL("Mechanical"), MEDICAL("Medical"),
  NAVIGATION("Navigation"), PILOT("Pilot"), SHIPS_BOAT("Ship's Boat"),
  STEWARD("Steward"), STREETWISE("Streetwise"), TACTICS("Tactics"),
  VACC_SUIT("Vacc Suit"), VEHICLE("Vehicle");

  private final String displayName;

  RollableSkill(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}

package com.javasmyths.TravellerCharacterGeneration.model;

import java.util.ArrayList;
import java.util.List;

/** Career counters are held only in the session, never bound from form fields. */
public class CareerProgress {
  private int term = 1;
  private int rollsUsed;
  private int completedTerms;
  private boolean musteredOut;
  private Service service;
  private String message = "";
  private final List<String> history = new ArrayList<>();
  private final List<String> benefits = new ArrayList<>();
  private int benefitRollsUsed;
  private int cashRollsUsed;
  private long credits;
  private boolean gambling;
  private int rank;
  private int bonusSkillRolls;
  private boolean drafted;
  private boolean commissionAttempted;
  private boolean promotionAttempted;
  private final java.util.Set<String> automaticAwards = new java.util.HashSet<>();
  private final java.util.Map<String, Integer> skillLevels = new java.util.TreeMap<>();

  public java.util.Map<String, Integer> getSkillLevels() { return java.util.Collections.unmodifiableMap(skillLevels); }
  public boolean isGenerationComplete() { return musteredOut && getBenefitRollsRemaining() == 0; }
  public List<String> getAdvancementHistory() {
    List<String> entries = new ArrayList<>();
    for (String entry : history) if (entry.contains("Commission:") || entry.contains("Promotion:") || entry.contains("Advancement attempt skipped")) entries.add(entry);
    return entries;
  }
  public List<String> getFullHistory() {
    List<String> entries = new ArrayList<>(history);
    entries.addAll(benefits);
    return entries;
  }
  public String getServiceStatus() { return service == null ? "Not enlisted" : (musteredOut ? "Mustered out" : "In service"); }
  private void addSkill(String name) { skillLevels.put(name, skillLevels.getOrDefault(name, 0) + 1); }
  private void applySkill(RolableSkills result, TravellerCharacter character) {
    Characteristics c = character.getCharacteristics();
    switch (result) {
      case PLUS_STREN: c.setStrength(c.getStrength() + 1); break;
      case PLUS_DEXT: c.setDexterity(c.getDexterity() + 1); break;
      case PLUS_ENDUR: c.setEndurance(c.getEndurance() + 1); break;
      case PLUS_INTEL: c.setIntelligence(c.getIntelligence() + 1); break;
      case PLUS_EDUC: c.setEducation(c.getEducation() + 1); break;
      case PLUS_SOCIAL: c.setSocialStanding(c.getSocialStanding() + 1); break;
      case MINUS_SOCIAL: c.setSocialStanding(c.getSocialStanding() - 1); break;
      default: addSkill(skillName(result));
    }
  }
  private String skillName(RolableSkills skill) {
    switch (skill) {
      case Admin: return "Administration";
      case AirRaft: return "Air/Raft";
      case BladeCbt: return "Blade combat (unspecialized)";
      case FwdObsvr: return "Forward observer";
      case GunCbt: return "Gun combat (unspecialized)";
      case JackOT: return "Jack of all trades";
      case Leader: return "Leadership";
      case ShipsBoat: return "Ship's boat";
      case Streetwise: return "Streetwise";
      case VaccSuit: return "Vacc suit";
      default: return skill.toString();
    }
  }

  public int getRank() { return rank; }
  public int getBonusSkillRolls() { return bonusSkillRolls; }
  public boolean isDrafted() { return drafted; }
  public boolean isRankedService() { return service != null && service != Service.Scouts && service != Service.Other; }
  public int getMaxRank() { return service == Service.Merchants ? 5 : 6; }
  public boolean isCanCommission() { return !musteredOut && isRankedService() && rank == 0 && !commissionAttempted && !(drafted && term == 1); }
  public boolean isCanPromote() { return !musteredOut && isRankedService() && rank > 0 && rank < getMaxRank() && !promotionAttempted; }
  public boolean isAdvancementPending() { return isCanCommission() || isCanPromote(); }
  public int getRankBenefitBonus() { return rank == 0 ? 0 : (rank + 1) / 2; }
  public int getBenefitRollAllowance() { return completedTerms + getRankBenefitBonus(); }
  public boolean isHighRankBonus() { return rank >= 5; }
  public String getRankTitle() {
    if (!isRankedService()) return "No ranks in this service";
    if (rank == 0) return "Uncommissioned";
    String[] titles;
    switch (service) {
      case Navy: titles = new String[]{"Ensign","Lieutenant","Lieutenant Commander","Commander","Captain","Admiral"}; break;
      case Marines: titles = new String[]{"Lieutenant","Captain","Force Commander","Lieutenant Colonel","Colonel","Brigadier"}; break;
      case Army: titles = new String[]{"Lieutenant","Captain","Major","Lieutenant Colonel","Colonel","General"}; break;
      default: titles = new String[]{"Fourth Officer","Third Officer","Second Officer","First Officer","Captain"};
    }
    return titles[rank - 1];
  }
  public int getCommissionTarget() {
    switch (service) { case Navy: return 10; case Marines: return 9; case Army: return 5; case Merchants: return 4; default: return 0; }
  }
  public int getPromotionTarget() {
    switch (service) { case Navy: return 8; case Marines: return 9; case Army: return 6; case Merchants: return 10; default: return 0; }
  }
  public int commissionModifier(Characteristics c) {
    switch (service) { case Navy: return c.getSocialStanding() >= 9 ? 1 : 0; case Marines: return c.getEducation() >= 7 ? 1 : 0; case Army: return c.getEndurance() >= 7 ? 1 : 0; case Merchants: return c.getIntelligence() >= 6 ? 1 : 0; default: return 0; }
  }
  public int promotionModifier(Characteristics c) {
    switch (service) { case Navy: return c.getEducation() >= 8 ? 1 : 0; case Marines: return c.getSocialStanding() >= 8 ? 1 : 0; case Army: return c.getEducation() >= 7 ? 1 : 0; case Merchants: return c.getIntelligence() >= 9 ? 1 : 0; default: return 0; }
  }
  public synchronized void enlist(Service selected, boolean wasDrafted) {
    if (service == null && selected != null) {
      service = selected; drafted = wasDrafted;
      history.add("Term 1: " + (drafted ? "Drafted into " : "Enlisted in ") + service + ".");
    }
  }
  private void award(String key, String text) {
    if (automaticAwards.add(key)) {
      history.add("Automatic award: " + text);
      if (text.endsWith("-1")) addSkill(text.substring(0, text.length() - 2));
    }
  }
  public void serviceAwards() {
    if (service == Service.Marines) award("marine", "Cutlass-1");
    if (service == Service.Army) award("army", "Rifle-1");
    if (service == Service.Scouts) award("scout", "Pilot-1");
  }
  public synchronized String advance(String action, int roll, TravellerCharacter character) {
    boolean commission = "commission".equals(action);
    boolean promotion = "promotion".equals(action);
    if ("skip".equals(action)) {
      if (isCanCommission()) commissionAttempted = true;
      else if (isCanPromote()) promotionAttempted = true;
      else return "No advancement attempt is available.";
      message = "Advancement attempt skipped for term " + term + ".";
    } else {
      if ((!commission && !promotion) || (commission && !isCanCommission()) || (promotion && !isCanPromote())) return "That advancement attempt is not available this term.";
      if (roll < 2 || roll > 12) throw new IllegalArgumentException("Advancement needs 2D6.");
      int modifier = commission ? commissionModifier(character.getCharacteristics()) : promotionModifier(character.getCharacteristics());
      int target = commission ? getCommissionTarget() : getPromotionTarget();
      if (commission) commissionAttempted = true; else promotionAttempted = true;
      boolean success = roll + modifier >= target;
      if (success) {
        rank++;
        bonusSkillRolls++;
        if (service == Service.Marines && rank == 1) award("marineOfficer", "Revolver-1");
        if (service == Service.Army && rank == 1) award("armyOfficer", "SMG-1");
        if (service == Service.Merchants && rank == 4) award("merchantOfficer", "Pilot-1");
        if (service == Service.Navy && rank >= 5 && automaticAwards.add("navyRank" + rank)) {
          character.getCharacteristics().setSocialStanding(character.getCharacteristics().getSocialStanding() + 1);
          history.add("Automatic award: +1 Social Standing for " + getRankTitle());
        }
      }
      message = (commission ? "Commission" : "Promotion") + ": rolled " + roll + " + " + modifier + " = " + (roll + modifier) + " against " + target + "+. "
          + (success ? "Success — " + getRankTitle() + " (rank " + rank + "). One extra skill roll earned." : "Not successful this term.");
    }
    history.add("Term " + term + ": " + message);
    return message;
  }

  public List<String> getBenefits() { return java.util.Collections.unmodifiableList(benefits); }
  public int getBenefitRollsUsed() { return benefitRollsUsed; }
  public int getBenefitRollsRemaining() { return musteredOut ? getBenefitRollAllowance() - benefitRollsUsed : 0; }
  public int getCashRollsUsed() { return cashRollsUsed; }
  public long getCredits() { return credits; }
  public boolean isGambling() { return gambling; }

  public int getTerm() { return term; }
  public int getRollsUsed() { return rollsUsed; }
  public int getCompletedTerms() { return completedTerms; }
  public boolean isMusteredOut() { return musteredOut; }
  public int getRollAllowance() { return (term == 1 || service == Service.Scouts ? 2 : 1) + bonusSkillRolls; }
  public int getRollsRemaining() { return musteredOut ? 0 : getRollAllowance() - rollsUsed; }
  public boolean isTermComplete() { return !isAdvancementPending() && rollsUsed >= getRollAllowance(); }
  public boolean isDecisionReady() { return isTermComplete() && !musteredOut; }
  public String getMessage() { return message; }
  public List<String> getHistory() { return java.util.Collections.unmodifiableList(history); }
  public Service getService() { return service; }
  public void start(Service selected) { enlist(selected, false); }
  public void message(String value) { message = value; }

  public synchronized void recordRoll(int roll, RolableSkills result, TravellerCharacter character) {
    if (musteredOut || isTermComplete() || isAdvancementPending()) throw new IllegalStateException("Resolve advancement before rolling skills; no extra rolls are permitted.");
    rollsUsed++;
    applySkill(result, character);
    if (result == RolableSkills.Gambling) gambling = true;
    history.add("Term " + term + ": rolled " + roll + " — " + result);
    message = "You rolled a " + roll + ". Skill result: " + result;
    if (isTermComplete()) {
      completedTerms++;
      character.setAge(character.getAge() + 4);
      history.add("Term " + term + " completed. Age " + character.getAge() + ".");
    }
  }

  public int getReenlistTarget() {
    if (service == null) return 0;
    switch (service) {
      case Navy: case Marines: return 6;
      case Army: return 7;
      case Scouts: return 3;
      case Merchants: return 4;
      default: return 5;
    }
  }

  public synchronized void decide(boolean wantsReenlist, int roll) {
    if (!isDecisionReady()) { message = "Finish your remaining skill rolls first."; return; }
    if (roll == 12 || (wantsReenlist && term < 7 && roll >= getReenlistTarget())) {
      String reason = roll == 12 ? "Mandatory re-enlistment on a roll of 12." : "Re-enlistment successful.";
      term++;
      rollsUsed = 0;
      bonusSkillRolls = 0;
      commissionAttempted = false;
      promotionAttempted = false;
      message = "Rolled " + roll + ". " + reason + " Begin term " + term + ".";
    } else {
      musteredOut = true;
      message = "Rolled " + roll + ". " + (wantsReenlist ? "Re-enlistment unavailable; " : "")
          + "Mustered out after " + completedTerms + " term(s).";
    }
    history.add(message);
  }

  public synchronized String rollBenefit(String table, int die, boolean useGambling, TravellerCharacter character) {
    return rollBenefit(table, die, useGambling, false, character);
  }
  public synchronized String rollBenefit(String table, int die, boolean useGambling, boolean useRankBonus, TravellerCharacter character) {
    if (!musteredOut) return "Finish your service and muster out before rolling benefits.";
    if (getBenefitRollsRemaining() <= 0) return "All mustering-out benefits have been rolled.";
    if (die < 1 || die > 6) throw new IllegalArgumentException("Die must be 1–6.");
    if (!"cash".equals(table) && !"material".equals(table)) return "Choose Cash or Material benefits.";
    if ("cash".equals(table) && cashRollsUsed >= 3) return "Only three cash rolls are allowed. Choose Material benefits.";
    int row = die - 1;
    String result;
    if ("cash".equals(table)) {
      if (gambling && useGambling) row++;
      int[] amounts = getCashBenefitTable();
      credits += amounts[row];
      cashRollsUsed++;
      result = "Cr " + amounts[row];
    } else {
      if (isHighRankBonus() && useRankBonus) row++;
      String[] items = getMaterialBenefitTable();
      result = items[row];
      Characteristics stats = character.getCharacteristics();
      if (result.endsWith("Intelligence")) stats.setIntelligence(stats.getIntelligence() + (result.startsWith("+2") ? 2 : 1));
      if (result.endsWith("Education")) stats.setEducation(stats.getEducation() + (result.startsWith("+2") ? 2 : 1));
      if (result.endsWith("Social Standing")) stats.setSocialStanding(stats.getSocialStanding() + (result.startsWith("+2") ? 2 : 1));
      if (result.contains("Society") && benefits.stream().anyMatch(b -> b.contains("Society"))) result = "Already a Travellers' Aid Society member; no additional benefit";
    }
    benefitRollsUsed++;
    String entry = "Rolled " + die + (row + 1 != die ? ("cash".equals(table) ? " (+1 Gambling)" : " (+1 rank)") : "") + " on " + table + ": " + result;
    benefits.add(entry);
    return entry;
  }
  public int[] getCashBenefitTable() {
      switch (service) {
        case Navy: return new int[]{1000,5000,5000,10000,20000,50000,50000};
        case Marines: return new int[]{2000,5000,5000,10000,20000,30000,40000};
        case Army: return new int[]{2000,5000,10000,10000,10000,20000,30000};
        case Scouts: return new int[]{20000,20000,30000,30000,50000,50000,50000};
        case Merchants: return new int[]{1000,5000,10000,20000,20000,40000,40000};
        default: return new int[]{1000,5000,10000,10000,10000,50000,100000};
      }
  }
  public String[] getMaterialBenefitTable() {
      switch (service) {
        case Navy: return new String[]{"Low passage","+1 Intelligence","+2 Education","Blade","Travellers' Aid Society membership","High passage","+2 Social Standing"};
        case Marines: return new String[]{"Low passage","+2 Intelligence","+1 Education","Blade","Travellers' Aid Society membership","High passage","+2 Social Standing"};
        case Army: return new String[]{"Low passage","+1 Intelligence","+2 Education","Gun","High passage","Middle passage","+1 Social Standing"};
        case Scouts: return new String[]{"Low passage","+2 Intelligence","+2 Education","Blade","Gun","Scout ship"};
        case Merchants: return new String[]{"Low passage","+1 Intelligence","+1 Education","Gun","Blade","Low passage","Free Trader"};
        default: return new String[]{"Low passage","+1 Intelligence","+1 Education","Gun","High passage","No benefit"};
      }
  }
}

/*
 * Copyright (C) 2018 JavaSmyths javasmyths@javasmyths.com
 */
package com.javasmyths.traveller.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Richard
 */
public class TravellerCharacter {
  
  private String name;
  private Characteristics characteristics;
  private int age;
  private Service service;
  private String enlistmentResult;
  private final Map<RollableSkill, Integer> skills = new EnumMap<>(RollableSkill.class);
  private String lastSkillResult;
  private int termsServed;
  private int skillRollsRemaining;
  private boolean careerActive;
  private boolean alive = true;
  private boolean commissioned;
  private int rank;
  private String lastTermResult;
  private final List<String> termHistory = new ArrayList<>();
  private int musterRollsRemaining;
  private int cashRollsTaken;
  private long credits;
  private int annualPension;
  private String lastMusterResult;
  private final List<String> musterBenefits = new ArrayList<>();
  

  public TravellerCharacter() {
    this(18);
  }

  public TravellerCharacter(int age) {
    this.age = age;
  }

  public int getAge() {
    return age;
  }

  public void setAge(int age) {
    this.age = age;
  }
  
  public Characteristics getCharacteristics() {
    return characteristics;
  }

  public void setCharacteristics(Characteristics characteristics) {
    this.characteristics = characteristics;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Service getService() {
    return service;
  }

  public void setService(Service service) {
    this.service = service;
  }

  public String getEnlistmentResult() {
    return enlistmentResult;
  }

  public void setEnlistmentResult(String enlistmentResult) {
    this.enlistmentResult = enlistmentResult;
  }

  public Map<RollableSkill, Integer> getSkills() {
    return Collections.unmodifiableMap(skills);
  }

  public int addSkill(RollableSkill skill) {
    return skills.merge(skill, 1, Integer::sum);
  }

  public String getLastSkillResult() {
    return lastSkillResult;
  }

  public void setLastSkillResult(String lastSkillResult) {
    this.lastSkillResult = lastSkillResult;
  }

  public int getTermsServed() { return termsServed; }
  public void setTermsServed(int termsServed) { this.termsServed = termsServed; }
  public int getSkillRollsRemaining() { return skillRollsRemaining; }
  public void setSkillRollsRemaining(int skillRollsRemaining) {
    this.skillRollsRemaining = Math.max(0, skillRollsRemaining);
  }
  public void useSkillRoll() {
    if (skillRollsRemaining <= 0) {
      throw new IllegalStateException("No skill rolls remain");
    }
    skillRollsRemaining--;
  }
  public boolean isCareerActive() { return careerActive; }
  public void setCareerActive(boolean careerActive) { this.careerActive = careerActive; }
  public boolean isAlive() { return alive; }
  public void setAlive(boolean alive) { this.alive = alive; }
  public boolean isCommissioned() { return commissioned; }
  public void setCommissioned(boolean commissioned) { this.commissioned = commissioned; }
  public int getRank() { return rank; }
  public void setRank(int rank) { this.rank = rank; }
  public String getRankLabel() {
    if (service == Service.SCOUTS || service == Service.OTHER) {
      return "—";
    }
    return commissioned ? "Officer " + rank : "Uncommissioned";
  }
  public String getLastTermResult() { return lastTermResult; }
  public void setLastTermResult(String lastTermResult) { this.lastTermResult = lastTermResult; }
  public List<String> getTermHistory() { return Collections.unmodifiableList(termHistory); }
  public void addTermResult(String result) {
    lastTermResult = result;
    termHistory.add(result);
  }

  public int getMusterRollsRemaining() { return musterRollsRemaining; }
  public int getCashRollsTaken() { return cashRollsTaken; }
  public long getCredits() { return credits; }
  public int getAnnualPension() { return annualPension; }
  public String getLastMusterResult() { return lastMusterResult; }
  public void setLastMusterResult(String lastMusterResult) {
    this.lastMusterResult = lastMusterResult;
  }
  public List<String> getMusterBenefits() {
    return Collections.unmodifiableList(musterBenefits);
  }
  public boolean isMusteringOut() {
    return alive && !careerActive && musterRollsRemaining > 0;
  }
  public void startMusteringOut(int rolls, int pension) {
    musterRollsRemaining = Math.max(0, rolls);
    cashRollsTaken = 0;
    annualPension = Math.max(0, pension);
    lastMusterResult = null;
  }
  public void addCashBenefit(int amount) {
    credits += amount;
    cashRollsTaken++;
    useMusterRoll();
  }
  public void addMaterialBenefit(String benefit) {
    musterBenefits.add(benefit);
    useMusterRoll();
  }
  private void useMusterRoll() {
    if (musterRollsRemaining <= 0) {
      throw new IllegalStateException("No mustering-out rolls remain");
    }
    musterRollsRemaining--;
  }

  public void prepareCareer() {
    termsServed = 0;
    skillRollsRemaining = 0;
    careerActive = true;
    alive = true;
    commissioned = false;
    rank = 0;
    lastTermResult = null;
    termHistory.clear();
    musterRollsRemaining = 0;
    cashRollsTaken = 0;
    credits = 0;
    annualPension = 0;
    lastMusterResult = null;
    musterBenefits.clear();
  }

  public void resetCareer() {
    service = null;
    enlistmentResult = null;
    skills.clear();
    lastSkillResult = null;
    prepareCareer();
    careerActive = false;
  }

  @Override
  public String toString() {
    return "TravellerCharacter{" + "name=" + name + ", age=" + age + ", characteristics=" + characteristics + ", service=" + service + '}';
  }

}

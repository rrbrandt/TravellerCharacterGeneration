package com.javasmyths.traveller.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import static com.javasmyths.traveller.model.RollableSkill.*;

public final class ServiceSkills {
  private final Map<Service, SkillsForService> tables;

  public ServiceSkills() {
    EnumMap<Service, SkillsForService> values = new EnumMap<>(Service.class);
    values.put(Service.NAVY, table(
        skills(PLUS_STRENGTH, PLUS_DEXTERITY, PLUS_ENDURANCE, PLUS_INTELLIGENCE, PLUS_EDUCATION, PLUS_SOCIAL),
        skills(SHIPS_BOAT, VACC_SUIT, FORWARD_OBSERVER, GUNNERY, BLADE_COMBAT, GUN_COMBAT),
        skills(VACC_SUIT, MECHANICAL, ELECTRONICS, ENGINEERING, GUNNERY, JACK_OF_ALL_TRADES),
        skills(MEDICAL, NAVIGATION, ENGINEERING, COMPUTER, PILOT, ADMIN)));
    values.put(Service.MARINES, table(
        skills(PLUS_STRENGTH, PLUS_DEXTERITY, PLUS_ENDURANCE, GAMBLING, BRAWLING, BLADE_COMBAT),
        skills(VEHICLE, VACC_SUIT, BLADE_COMBAT, GUN_COMBAT, BLADE_COMBAT, GUN_COMBAT),
        skills(VEHICLE, MECHANICAL, ELECTRONICS, TACTICS, BLADE_COMBAT, GUN_COMBAT),
        skills(MEDICAL, TACTICS, TACTICS, COMPUTER, LEADER, ADMIN)));
    values.put(Service.ARMY, table(
        skills(PLUS_STRENGTH, PLUS_DEXTERITY, PLUS_ENDURANCE, GAMBLING, PLUS_EDUCATION, BRAWLING),
        skills(VEHICLE, AIR_RAFT, GUN_COMBAT, FORWARD_OBSERVER, BLADE_COMBAT, GUN_COMBAT),
        skills(VEHICLE, MECHANICAL, ELECTRONICS, TACTICS, BLADE_COMBAT, GUN_COMBAT),
        skills(MEDICAL, TACTICS, TACTICS, COMPUTER, LEADER, ADMIN)));
    values.put(Service.SCOUTS, table(
        skills(PLUS_STRENGTH, PLUS_DEXTERITY, PLUS_ENDURANCE, PLUS_INTELLIGENCE, PLUS_EDUCATION, GUN_COMBAT),
        skills(VEHICLE, VACC_SUIT, MECHANICAL, NAVIGATION, ELECTRONICS, JACK_OF_ALL_TRADES),
        skills(VEHICLE, MECHANICAL, ELECTRONICS, JACK_OF_ALL_TRADES, GUNNERY, MEDICAL),
        skills(MEDICAL, NAVIGATION, ENGINEERING, COMPUTER, PILOT, JACK_OF_ALL_TRADES)));
    values.put(Service.MERCHANTS, table(
        skills(PLUS_STRENGTH, PLUS_DEXTERITY, PLUS_ENDURANCE, PLUS_STRENGTH, BLADE_COMBAT, BRIBERY),
        skills(VEHICLE, VACC_SUIT, JACK_OF_ALL_TRADES, STEWARD, ELECTRONICS, GUN_COMBAT),
        skills(STREETWISE, MECHANICAL, ELECTRONICS, NAVIGATION, GUNNERY, MEDICAL),
        skills(MEDICAL, NAVIGATION, ENGINEERING, COMPUTER, PILOT, ADMIN)));
    values.put(Service.OTHER, table(
        skills(PLUS_STRENGTH, PLUS_DEXTERITY, PLUS_ENDURANCE, BLADE_COMBAT, BRAWLING, MINUS_SOCIAL),
        skills(VEHICLE, GAMBLING, BRAWLING, BRIBERY, BLADE_COMBAT, GUN_COMBAT),
        skills(STREETWISE, MECHANICAL, ELECTRONICS, GAMBLING, BRAWLING, FORGERY),
        skills(MEDICAL, FORGERY, ELECTRONICS, COMPUTER, STREETWISE, JACK_OF_ALL_TRADES)));
    tables = Collections.unmodifiableMap(values);
  }

  public SkillsForService get(Service service) {
    SkillsForService result = tables.get(service);
    if (result == null) {
      throw new IllegalArgumentException("No skill tables are defined for " + service);
    }
    return result;
  }

  public Map<Service, SkillsForService> getAll() {
    return tables;
  }

  private static SkillsForService table(RollableSkill[] personal, RollableSkill[] service,
      RollableSkill[] advanced, RollableSkill[] advancedEducation) {
    return new SkillsForService(personal, service, advanced, advancedEducation);
  }

  private static RollableSkill[] skills(RollableSkill... skills) {
    return skills;
  }
}

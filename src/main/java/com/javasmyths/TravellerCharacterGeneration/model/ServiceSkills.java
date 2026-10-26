/*
 * Copyright (C) 2018 JavaSmyths javasmyths@javasmyths.com
 */
package com.javasmyths.TravellerCharacterGeneration.model;
import java.util.HashMap;

/**
 *
 * @author Richard
 */
public class ServiceSkills {
  private HashMap<Service, SkillsForService> skillsForService;

  public ServiceSkills() {
    this.skillsForService = new HashMap<Service, SkillsForService> ();

    skillsForService.put(Service.Navy, new SkillsForService(
      new RolableSkills [] {RolableSkills.PLUS_STREN,RolableSkills.PLUS_DEXT,RolableSkills.PLUS_ENDUR,RolableSkills.PLUS_INTEL,RolableSkills.PLUS_EDUC,RolableSkills.PLUS_SOCIAL},
      new RolableSkills [] {RolableSkills.ShipsBoat,RolableSkills.VaccSuit,RolableSkills.FwdObsvr,RolableSkills.Gunnery,RolableSkills.BladeCbt,RolableSkills.GunCbt},
      new RolableSkills [] {RolableSkills.VaccSuit,RolableSkills.Mechanical,RolableSkills.Electronics,RolableSkills.Engineering,RolableSkills.Gunnery,RolableSkills.JackOT},
      new RolableSkills [] {RolableSkills.Medical,RolableSkills.Navigation,RolableSkills.Engineering,RolableSkills.Computer,RolableSkills.Pilot,RolableSkills.Admin}
    ));

    skillsForService.put(Service.Marines, new SkillsForService(
      new RolableSkills [] {RolableSkills.PLUS_STREN,RolableSkills.PLUS_DEXT,RolableSkills.PLUS_ENDUR,RolableSkills.Gambling,RolableSkills.Brawling,RolableSkills.BladeCbt},
      new RolableSkills [] {RolableSkills.Vehicle,RolableSkills.VaccSuit,RolableSkills.BladeCbt,RolableSkills.GunCbt,RolableSkills.BladeCbt,RolableSkills.GunCbt},
      new RolableSkills [] {RolableSkills.Vehicle,RolableSkills.Mechanical,RolableSkills.Electronics,RolableSkills.Tactics,RolableSkills.BladeCbt,RolableSkills.GunCbt},
      new RolableSkills [] {RolableSkills.Medical,RolableSkills.Tactics,RolableSkills.Tactics,RolableSkills.Computer,RolableSkills.Leader,RolableSkills.Admin}
    ));

    skillsForService.put(Service.Army, new SkillsForService(
      new RolableSkills [] {RolableSkills.PLUS_STREN,RolableSkills.PLUS_DEXT,RolableSkills.PLUS_ENDUR,RolableSkills.Gambling,RolableSkills.PLUS_EDUC,RolableSkills.Brawling},
      new RolableSkills [] {RolableSkills.Vehicle,RolableSkills.AirRaft,RolableSkills.GunCbt,RolableSkills.FwdObsvr,RolableSkills.BladeCbt,RolableSkills.GunCbt},
      new RolableSkills [] {RolableSkills.Vehicle,RolableSkills.Mechanical,RolableSkills.Electronics,RolableSkills.Tactics,RolableSkills.BladeCbt,RolableSkills.GunCbt},
      new RolableSkills [] {RolableSkills.Medical,RolableSkills.Tactics,RolableSkills.Tactics,RolableSkills.Computer,RolableSkills.Leader,RolableSkills.Admin}
    ));

    skillsForService.put(Service.Merchants, new SkillsForService(
      new RolableSkills [] {RolableSkills.PLUS_STREN,RolableSkills.PLUS_DEXT,RolableSkills.PLUS_ENDUR,RolableSkills.PLUS_STREN,RolableSkills.BladeCbt,RolableSkills.Bribery},
      new RolableSkills [] {RolableSkills.Vehicle,RolableSkills.VaccSuit,RolableSkills.JackOT,RolableSkills.Steward,RolableSkills.Electronics,RolableSkills.GunCbt},
      new RolableSkills [] {RolableSkills.Streetwise,RolableSkills.Mechanical,RolableSkills.Electronics,RolableSkills.Navigation,RolableSkills.Gunnery,RolableSkills.Medical},
      new RolableSkills [] {RolableSkills.Medical,RolableSkills.Navigation,RolableSkills.Engineering,RolableSkills.Computer,RolableSkills.Pilot,RolableSkills.Admin}
    ));

    skillsForService.put(Service.Scouts, new SkillsForService(
      new RolableSkills [] {RolableSkills.PLUS_STREN,RolableSkills.PLUS_DEXT,RolableSkills.PLUS_ENDUR,RolableSkills.PLUS_INTEL,RolableSkills.PLUS_EDUC,RolableSkills.GunCbt},
      new RolableSkills [] {RolableSkills.Vehicle,RolableSkills.VaccSuit,RolableSkills.Mechanical,RolableSkills.Navigation,RolableSkills.Electronics,RolableSkills.JackOT},
      new RolableSkills [] {RolableSkills.Vehicle,RolableSkills.Mechanical,RolableSkills.Electronics,RolableSkills.JackOT,RolableSkills.Gunnery,RolableSkills.Medical},
      new RolableSkills [] {RolableSkills.Medical,RolableSkills.Navigation,RolableSkills.Engineering,RolableSkills.Computer,RolableSkills.Pilot,RolableSkills.JackOT}
    ));

    skillsForService.put(Service.Other, new SkillsForService(
      new RolableSkills [] {RolableSkills.PLUS_STREN,RolableSkills.PLUS_DEXT,RolableSkills.PLUS_ENDUR,RolableSkills.BladeCbt,RolableSkills.Brawling,RolableSkills.MINUS_SOCIAL},
      new RolableSkills [] {RolableSkills.Vehicle,RolableSkills.Gambling,RolableSkills.Brawling,RolableSkills.Bribery,RolableSkills.BladeCbt,RolableSkills.GunCbt},
      new RolableSkills [] {RolableSkills.Streetwise,RolableSkills.Mechanical,RolableSkills.Electronics,RolableSkills.Gambling,RolableSkills.Brawling,RolableSkills.Forgery},
      new RolableSkills [] {RolableSkills.Medical,RolableSkills.Forgery,RolableSkills.Electronics,RolableSkills.Computer,RolableSkills.Streetwise,RolableSkills.JackOT}
    ));
  }

  public HashMap<Service, SkillsForService> getSkillsForService() {
    return skillsForService;
  }

  public void setSkillsForService(HashMap<Service, SkillsForService> skillsForService) {
    this.skillsForService = skillsForService;
  }
}

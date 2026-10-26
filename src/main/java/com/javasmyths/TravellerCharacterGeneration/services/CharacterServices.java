/*
 * Copyright (C) 2018 JavaSmyths javasmyths@javasmyths.com
 */
package com.javasmyths.TravellerCharacterGeneration.services;

import com.javasmyths.TravellerCharacterGeneration.model.Service;

/**
 *
 * @author Richard
 */
public class CharacterServices {
  public static Service draftService() {
    Dice dice = new Dice();

    switch (dice.roleDie()) {
      case 1:
        return Service.Navy;
      case 2:
        return Service.Marines;
      case 3:
        return Service.Army;
      case 4:
        return Service.Scouts;
      case 5:
        return Service.Merchants;
      case 6:
        return Service.Other;
      default:
        System.out.println("This should never happen.");
        return Service.Other;
    }
  }
}

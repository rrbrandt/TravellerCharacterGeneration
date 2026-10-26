/*
 * Copyright (C) 2018 JavaSmyths javasmyths@javasmyths.com
 */
package com.javasmyths.TravellerCharacterGeneration.services;

import java.util.Random;

/**
 * All dice are 6 sided.   Period.
 * @author Richard
 */
public class Dice {
  public static final int MAX_DIE = 6;
  Random random;

  public Dice () {
    random = new Random();
  }

  /**
   * @return random from 1..6
   */
  public int roleDie() {
    return random.nextInt(MAX_DIE) + 1;
  }

  /**
   * @return random from 2..12, as roleDie() + roleDie()
   */
  public int role2Dice() {
    return roleDie() + roleDie();
  }
}

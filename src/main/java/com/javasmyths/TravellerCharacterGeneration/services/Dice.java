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
  
  public int roleDie() {
    return random.nextInt(MAX_DIE) + 1;
  }
  
  public int role2Dice() {
    return roleDie() + roleDie();
  }
}

/*
 * Copyright (C) 2018 JavaSmyths javasmyths@javasmyths.com
 */
package com.javasmyths.traveller.services;

import java.util.Random;
import java.util.random.RandomGenerator;

/**
 * All dice are 6 sided.   Period.
 * @author Richard
 */
public final class Dice {
  public static final int MAX_DIE = 6;
  private final RandomGenerator random;
  
  public Dice() {
    this(new Random());
  }

  public Dice(RandomGenerator random) {
    if (random == null) {
      throw new IllegalArgumentException("random must not be null");
    }
    this.random = random;
  }
  
  public int rollDie() {
    return random.nextInt(MAX_DIE) + 1;
  }
  
  public int rollTwoDice() {
    return rollDie() + rollDie();
  }
}

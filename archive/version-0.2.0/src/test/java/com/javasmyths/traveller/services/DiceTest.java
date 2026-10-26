package com.javasmyths.traveller.services;

import java.util.Random;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiceTest {
  @Test
  void rollsStayWithinValidRanges() {
    Dice dice = new Dice(new Random(12345));
    for (int i = 0; i < 1_000; i++) {
      int oneDie = dice.rollDie();
      int twoDice = dice.rollTwoDice();
      assertTrue(oneDie >= 1 && oneDie <= 6);
      assertTrue(twoDice >= 2 && twoDice <= 12);
    }
  }

  @Test
  void seededDiceAreDeterministic() {
    Dice first = new Dice(new Random(42));
    Dice second = new Dice(new Random(42));
    for (int i = 0; i < 20; i++) {
      assertEquals(first.rollTwoDice(), second.rollTwoDice());
    }
  }
}

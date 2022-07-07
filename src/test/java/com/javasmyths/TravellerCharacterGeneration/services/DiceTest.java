/*
 * Copyright (C) 2018 JavaSmyths javasmyths@javasmyths.com
 */
package com.javasmyths.TravellerCharacterGeneration.services;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 *
 * @author Richard
 */
@SpringBootTest
public class DiceTest {
  
  public DiceTest() {
  }
 

  @Test
  public void testRoleDie() {
    System.out.println("roleDie");
//    Dice instance = new Dice();
//    int result = instance.roleDie();
//    assertTrue(result >= 1 && result <= 6);
  }

  @Test
  public void testRole2Dice() {
    System.out.println("role2Dice");
//    Dice instance = new Dice();
//    int result = instance.roleDie();
//    assertTrue(result >= 2 && result <= 12);
  }
  
}

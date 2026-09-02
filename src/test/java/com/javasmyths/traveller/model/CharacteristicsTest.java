package com.javasmyths.traveller.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CharacteristicsTest {
  @Test
  void intelligenceIsWritableAndUppUsesTravellerDigits() {
    Characteristics value = new Characteristics();
    value.setStrength(10);
    value.setDexterity(11);
    value.setEndurance(12);
    value.setIntelligence(9);
    value.setEducation(8);
    value.setSocialStanding(7);

    assertEquals(9, value.getIntelligence());
    assertEquals("ABC987", value.getUpp());
  }
}

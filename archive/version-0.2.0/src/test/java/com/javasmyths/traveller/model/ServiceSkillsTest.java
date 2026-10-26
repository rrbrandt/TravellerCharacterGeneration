package com.javasmyths.traveller.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ServiceSkillsTest {
  @Test
  void everyServiceHasFourCompleteSkillTables() {
    ServiceSkills skills = new ServiceSkills();
    assertEquals(Service.values().length, skills.getAll().size());
    for (Service service : Service.values()) {
      SkillsForService table = skills.get(service);
      assertNotNull(table);
      assertEquals(6, table.getPersonalDevelopment().length);
      assertEquals(6, table.getServiceSkills().length);
      assertEquals(6, table.getAdvancedEducation().length);
      assertEquals(6, table.getAdvancedEducationPlusEducation().length);
    }
  }
}

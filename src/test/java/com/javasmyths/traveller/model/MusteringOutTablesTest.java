package com.javasmyths.traveller.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MusteringOutTablesTest {
  @Test
  void everyServiceHasSevenMaterialAndCashResults() {
    MusteringOutTables tables = new MusteringOutTables();

    for (Service service : Service.values()) {
      assertEquals(7, tables.get(service).getMaterialBenefits().length);
      assertEquals(7, tables.get(service).getCashBenefits().length);
    }
  }

  @Test
  void classicHighAndLowCashResultsArePresent() {
    MusteringOutTables tables = new MusteringOutTables();

    assertEquals(1000, tables.get(Service.NAVY).cashFor(1));
    assertEquals(50000, tables.get(Service.NAVY).cashFor(7));
    assertEquals(20000, tables.get(Service.SCOUTS).cashFor(1));
    assertEquals(100000, tables.get(Service.OTHER).cashFor(7));
  }
}

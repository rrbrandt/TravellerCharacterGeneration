package com.javasmyths.traveller.model;

import java.util.EnumMap;
import java.util.Map;

import static com.javasmyths.traveller.model.MusterBenefit.*;

public final class MusteringOutTables {
  private final Map<Service, MusteringOutTable> tables = new EnumMap<>(Service.class);

  public MusteringOutTables() {
    tables.put(Service.NAVY, table(
        benefits(LOW_PASSAGE, PLUS_ONE_INTELLIGENCE, PLUS_TWO_EDUCATION, BLADE,
            TRAVELLERS_AID_SOCIETY, HIGH_PASSAGE, PLUS_TWO_SOCIAL),
        cash(1000, 5000, 5000, 10000, 20000, 50000, 50000)));
    tables.put(Service.MARINES, table(
        benefits(LOW_PASSAGE, PLUS_TWO_INTELLIGENCE, PLUS_ONE_EDUCATION, BLADE,
            TRAVELLERS_AID_SOCIETY, HIGH_PASSAGE, PLUS_TWO_SOCIAL),
        cash(2000, 5000, 5000, 10000, 20000, 30000, 40000)));
    tables.put(Service.ARMY, table(
        benefits(LOW_PASSAGE, PLUS_ONE_INTELLIGENCE, PLUS_TWO_EDUCATION, GUN,
            HIGH_PASSAGE, MIDDLE_PASSAGE, PLUS_ONE_SOCIAL),
        cash(2000, 5000, 10000, 10000, 10000, 20000, 30000)));
    tables.put(Service.SCOUTS, table(
        benefits(LOW_PASSAGE, PLUS_TWO_INTELLIGENCE, PLUS_TWO_EDUCATION, BLADE,
            GUN, SCOUT_SHIP, NONE),
        cash(20000, 20000, 30000, 30000, 50000, 50000, 50000)));
    tables.put(Service.MERCHANTS, table(
        benefits(LOW_PASSAGE, PLUS_ONE_INTELLIGENCE, PLUS_ONE_EDUCATION, GUN,
            BLADE, LOW_PASSAGE, FREE_TRADER),
        cash(1000, 5000, 10000, 20000, 20000, 40000, 40000)));
    tables.put(Service.OTHER, table(
        benefits(LOW_PASSAGE, PLUS_ONE_INTELLIGENCE, PLUS_ONE_EDUCATION, GUN,
            HIGH_PASSAGE, NONE, NONE),
        cash(1000, 5000, 10000, 10000, 10000, 50000, 100000)));
  }

  public MusteringOutTable get(Service service) {
    MusteringOutTable table = tables.get(service);
    if (table == null) {
      throw new IllegalArgumentException("No mustering-out table for " + service);
    }
    return table;
  }

  private static MusteringOutTable table(MusterBenefit[] benefits, int[] cash) {
    return new MusteringOutTable(benefits, cash);
  }

  private static MusterBenefit[] benefits(MusterBenefit... values) { return values; }
  private static int[] cash(int... values) { return values; }
}

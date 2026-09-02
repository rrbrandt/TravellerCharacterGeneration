package com.javasmyths.traveller.model;

public class MusteringOutTable {
  private final MusterBenefit[] materialBenefits;
  private final int[] cashBenefits;

  public MusteringOutTable(MusterBenefit[] materialBenefits, int[] cashBenefits) {
    if (materialBenefits == null || materialBenefits.length != 7
        || cashBenefits == null || cashBenefits.length != 7) {
      throw new IllegalArgumentException("Mustering-out tables must contain seven entries");
    }
    this.materialBenefits = materialBenefits.clone();
    this.cashBenefits = cashBenefits.clone();
  }

  public MusterBenefit[] getMaterialBenefits() {
    return materialBenefits.clone();
  }

  public int[] getCashBenefits() {
    return cashBenefits.clone();
  }

  public MusterBenefit materialFor(int result) {
    return materialBenefits[Math.max(1, Math.min(7, result)) - 1];
  }

  public int cashFor(int result) {
    return cashBenefits[Math.max(1, Math.min(7, result)) - 1];
  }
}

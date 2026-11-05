package com.javasmyths.shipbuilder;
import java.util.LinkedHashMap;
import java.util.Map;
public class ShipDesign {
 public String id, name="Untitled starship", hullType="standard", jump="A", maneuver="A", power="A", computer="1bis";
 public int hull=100, staterooms=4, lowBerths=0, emergencyBerths=0, highPassengers=0, middlePassengers=0,
     crew=1, hardpoints=1, singleTurrets=0, doubleTurrets=1, tripleTurrets=0, fireControlReserve=0,
     pulseLasers=0, beamLasers=0, missileRacks=0, sandcasters=0;
 public boolean streamlined=true, commercial=false, doubleOccupancy=false, dualRoles=false, gunners=true, standardDesign=false;
 public double fuel=-1, cargo=-1, engineering=0;
 public long extraSoftwareCredits=0;
 public Map<String,Integer> vehicles=new LinkedHashMap<>();
 public static ShipDesign scout() {
  ShipDesign d=new ShipDesign();d.name="Scout / Courier";d.standardDesign=true;d.vehicles.put("airRaft",1);return d;
 }
 public static ShipDesign trader() {
  ShipDesign d=new ShipDesign();d.name="Free Trader";d.hull=200;d.computer="1";d.staterooms=10;d.lowBerths=20;
  d.highPassengers=6;d.crew=4;d.hardpoints=2;d.doubleTurrets=0;d.fireControlReserve=2;d.commercial=true;d.standardDesign=true;return d;
 }
}

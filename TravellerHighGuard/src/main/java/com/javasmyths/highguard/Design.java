package com.javasmyths.highguard;
import java.util.*;
public class Design {
 public String id="",name="Wayfarer",shipClass="Patrol escort",type="EE",computer="4",spinal="None",purpose="Patrol and convoy escort",starport="A";
 public int hull=2000,tl=12,configuration=2,jump=2,maneuver=4,power=5,armor=0,damper=0,meson=0,globe=0,auxBridges=0;
 public int rooms=-1,lowBerths=0,emergencyBerths=0,passengers=0,lowPassengers=0,troops=0,extraCrew=0,launchTubes=0;
 public double fuel=-1,cargo=-1,extraCapacitors=0,otherTons=0,otherMcr=0,budget=0,dropTanks=0;
 public boolean scoops=true,purifier=true,military=true,frozenWatch=false,repeatClass=false,navalArchitect=false,artifactPermission=false;
 public List<Battery> batteries=new ArrayList<>();public List<Craft> craft=new ArrayList<>();
 public String sourceName="",sourceHash="",sourceJson="";public List<String> importNotes=new ArrayList<>();public boolean importReviewed=false;
 public static class Battery {public String weapon="beam";public int mounts=0,weaponsPerMount=3,mountsPerBattery=1;public Battery(){}public Battery(String w,int m,int n,int b){weapon=w;mounts=m;weaponsPerMount=n;mountsPerBattery=b;}}
 public static class Craft {public String name="Carried craft",kind="craft";public double tons=10,mcr=0;public int quantity=0,crew=1;public Craft(){}public Craft(String n,double t,double p,int q,int c){name=n;tons=t;mcr=p;quantity=q;crew=c;}}
 public Design(){batteries.add(new Battery("beam",4,3,2));batteries.add(new Battery("missile",2,3,1));batteries.add(new Battery("sand",2,3,1));}
 public static Design preset(String key){Design d=new Design();if(key.equals("courier")){d.name="Swift Courier";d.shipClass="Courier";d.type="XS";d.hull=400;d.tl=15;d.jump=6;d.maneuver=1;d.power=6;d.computer="6";d.batteries.clear();d.military=false;}else if(key.equals("cruiser")){d.name="Resolute";d.shipClass="Heavy cruiser";d.type="CH";d.hull=50000;d.tl=15;d.jump=4;d.maneuver=4;d.power=6;d.computer="7fib";d.armor=4;d.spinal="Meson-J";d.damper=7;d.meson=4;d.batteries.clear();d.batteries.add(new Battery("missile100",12,1,1));d.batteries.add(new Battery("beam",100,3,5));d.batteries.add(new Battery("sand",40,3,5));d.troops=150;}return d;}
}

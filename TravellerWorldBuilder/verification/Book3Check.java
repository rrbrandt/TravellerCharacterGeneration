package com.javasmyths.worldbuilder;
import java.util.*;
public class Book3Check {
 static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
 static class Fixed implements Book3Rules.Dice {int n;Fixed(int n){this.n=n;}public int d6(){return n;}}
 static class Sequence implements Book3Rules.Dice {int i;int[] n;Sequence(int...n){this.n=n;}public int d6(){if(i>=n.length)throw new AssertionError("Unexpected extra die");return n[i++];}}
 public static void main(String[] args){
  String[] ports={"A","A","A","B","B","C","C","D","E","E","X"};for(int i=2;i<=12;i++)check(Book3Rules.port(i).equals(ports[i-2]),"Starport boundary "+i);
  Atlas.World low=Book3Rules.world(new Fixed(1),"0101","Low");check(low.size==0&&low.atmosphere==0&&low.hydrographics==0,"Size-0 exceptions");check(low.population==0&&low.tech==11,"Preserve raw low-population tech; no modern reset");check(low.gasGiant&&!low.naval&&!low.scout,"Low base and gas rolls");
  Atlas.World high=Book3Rules.world(new Fixed(6),"0101","High");check(high.size==10&&high.atmosphere==15&&high.hydrographics==10,"Upper generated physical values");check(high.government==15&&high.law==20&&high.getUwp().charAt(6)=='L',"High law is not capped; extended hex skips I/O");check(!high.naval&&!high.scout&&!high.gasGiant,"X port bases and gas giant threshold");
  Atlas.World small=Book3Rules.world(new Sequence(3,4,3,4,3,4,1,2,6,6,6,6,3,4,3,4,3,4,3),"0101","Small");check(small.size==1&&small.atmosphere==6&&small.hydrographics==10,"1981 size-1 hydrographics uses atmosphere and is not automatically zero");
  Atlas.World regina=Book3Rules.decode("A788899-C","Regina","0101");check(regina.getDetails().get("Size").contains("7,000 miles"),"Book 3 sizes use thousands of miles");check(regina.getDetails().get("Atmosphere").contains("Dense"),"Decoded atmosphere");check(regina.getTradeCodes().equals(List.of("Ri")),"Rich world classification");
  Atlas.World ag=Book3Rules.decode("C666665-8","Agri","0202");check(ag.getTradeCodes().contains("Ag")&&ag.getTradeCodes().contains("Ni"),"Overlapping trade codes");
  try{Book3Rules.decode("A7888I9-C","Invalid","0101");throw new AssertionError("I accepted");}catch(IllegalArgumentException expected){}
  try{Book3Rules.decode("A788899-C","Invalid","9999");throw new AssertionError("Bad hex accepted");}catch(IllegalArgumentException expected){}
  Atlas same1=Book3Rules.generate("Test","repeat",4,false),same2=Book3Rules.generate("Test","repeat",4,false);check(same1.worlds.size()==same2.worlds.size(),"Deterministic count");for(int i=0;i<same1.worlds.size();i++)check(same1.worlds.get(i).getUwp().equals(same2.worlds.get(i).getUwp()),"Deterministic profiles");
  Atlas full=Book3Rules.generate("Full","repeat",1,false);check(full.worlds.size()==80,"Full subsector 80 hexes");Map<String,String> lookup=new HashMap<>();for(Atlas.World w:full.worlds)lookup.put(w.hex,w.getUwp());for(Atlas.World w:same1.worlds)check(lookup.get(w.hex).equals(w.getUwp()),"Density does not change a world's dice");
  int count=0;for(int seed=0;seed<100;seed++)for(Atlas.World w:Book3Rules.generate("Check","range-"+seed,4,false).worlds){Book3Rules.validate(w);check(!w.naval||"AB".contains(w.starport),"Naval port constraint");check(!w.scout||"ABCD".contains(w.starport),"Scout port constraint");check(w.size!=0||w.atmosphere==0&&w.hydrographics==0,"Belt exceptions");count++;}
  System.out.println("PASS: starport boundaries, physical exceptions, corrected hydrographics, extended codes, raw low-population results, decoded references, trade classifications, seed stability, density independence and "+count+" generated worlds.");
 }
}

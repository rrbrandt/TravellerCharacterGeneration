package com.javasmyths.mercenary;
import java.util.*;
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown=true)
public class Soldier {
 public String id=UUID.randomUUID().toString(),name="New recruit",service="Army",arm="Infantry",phase="RECRUIT",posting="",duty="",next="",special="",message="Roll your characteristics, then choose your service.";
 public int[] stats={7,7,7,7,7,7}; public int tl=12,years=0,rank=1,revision=0,benefits=0,cashRolls=0,credits=0; public long randomState=new java.security.SecureRandom().nextLong();
 public boolean rolled=false,officer=false,promoted=false,commando=false,aide=false,bladeExempt=false;
 public Map<String,Integer> skills=new TreeMap<>(),medals=new TreeMap<>();
 public List<String> log=new ArrayList<>(),equipment=new ArrayList<>(),schools=new ArrayList<>(),qualified=new ArrayList<>(),pending=new ArrayList<>();
 public int getAge(){return 18+years;} public int getTerm(){return years/4+1;} public int getCompletedTerms(){return years/4;}
 public String getUpp(){StringBuilder b=new StringBuilder();for(int x:stats)b.append("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".charAt(Math.max(0,Math.min(35,x))));return b.toString();}
 public String getRankName(){return (officer?"O":"E")+rank+" · "+(officer?new String[]{"","Second Lieutenant","First Lieutenant","Captain","Major","Lieutenant Colonel","Colonel","Brigadier General","Major General","Lieutenant General","General"}[rank]:new String[]{"","Private","Lance Corporal","Corporal","Lance Sergeant","Sergeant","Gunnery Sergeant","Leading Sergeant","First Sergeant","Sergeant Major"}[rank]);}
 public String getPendingSkill(){return pending.isEmpty()?"":pending.get(0);} public boolean isFinished(){return phase.equals("FINAL")||phase.equals("DEAD")||phase.equals("OTHER");}
}

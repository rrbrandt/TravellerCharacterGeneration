package com.javasmyths.mercenary;
import java.util.*;
public class Rules {
 public static final String[] ARMS={"Infantry","Cavalry","Artillery","Marine Infantry","Support","Commando"};
 public static final String[] MISSIONS={"Training","Internal security","Police action","Counterinsurgency","Raid","Garrison","Ship's troops"};
 public static final String[][] MOS={
 {"Gun Combat","Gun Combat","Heavy Weapons","Heavy Weapons","Vehicle","Recon","Vacc Suit"},
 {"Vehicle","Vehicle","Vehicle","Heavy Weapons","Heavy Weapons","Mechanical","Computer"},
 {"FA Gunnery","FA Gunnery","Vehicle","Mechanical","Forward Observer","Computer","Electronics"},
 {"Gun Combat","Gun Combat","Zero-G Combat","Zero-G Combat","Heavy Weapons","Forward Observer","Battle Dress"},
 {"Vehicle","Combat Engineering","Vehicle","Mechanical","Electronics","Medical","Computer"},
 {"Gun Combat","Gun Combat","Heavy Weapons","Demolitions","Wilderness Survival","Recon","Battle Dress"}};
 static final int[][] UNIT={{4,4,3,3,5,5,5,0,2,1,1},{4,0,3,2,5,5,5,0,2,1,2},{4,0,3,2,5,5,5,0,2,3,0},{4,4,3,1,6,5,6,0,3,2,2},{4,1,3,5,5,5,5,0,2,1,2},{4,4,3,2,1,5,0,0,3,4,4}};
 // survival, decoration, promotion (negative = enlisted only), skill. Zero = automatic/none.
 static final int[][] NORMAL={{0,0,-6,7},{4,12,-6,0},{5,9,8,7},{5,10,9,8},{6,6,6,5},{0,0,-7,0},{4,12,-6,6}};
 static final int[][] MARINE={{0,0,-6,7},{4,12,-6,0},{5,8,8,7},{5,9,9,8},{6,5,6,5},{0,0,-7,0},{4,12,-6,6}};
 static final int[][] SUPPORT={{0,0,-6,8},{4,0,-6,0},{4,10,9,7},{5,11,10,7},{6,7,7,6},{0,0,-7,0},{4,0,-6,0}};
 static final int[][] COMMANDO={{3,0,-8,6},{4,0,-7,0},{4,9,8,7},{5,8,7,6},{6,5,6,5},{0,0,-9,0},{4,0,-7,0}};
 public static final String[] ENLISTED={"Cross-training","Specialist school","Commando school","Protected forces","Recruiting","OCS","OCS"}, OFFICERS={"Intelligence school","Command college","Staff college","Commando school","Recruiting","Military attache / aide"};
 static final String[] ARMYLIFE={"Brawling","+STR","Gambling","+DEX","+END","+END","Pistol","+SOC","+SOC"},MARINELIFE={"Brawling","Gambling","+STR","+DEX","+END","Blade Combat","+EDU","+SOC","+SOC"},NCO={"Heavy Weapons","Mechanical","Tactics","Heavy Weapons","Mechanical","Tactics","Leadership","Leadership","Admin","Instruction","Admin"},COMMAND={"+END","Gun Combat","Vehicle","Heavy Weapons","Leadership","Tactics","Tactics","Leadership"},STAFF={"Mechanical","Forward Observer","Computer","Electronics","Medical","Instruction","Admin","Admin"},SHIP={"Forward Observer","Ship's Boat","Gunnery","Vacc Suit","Gunnery","Vacc Suit"};
 public static int arm(String a){return Arrays.asList(ARMS).indexOf(a);}
 public static int[] outcomes(Soldier s,int mission){return (s.arm.equals("Support")?SUPPORT:s.arm.equals("Commando")?COMMANDO:s.service.equals("Marines")?MARINE:NORMAL)[mission].clone();}
 public static String general(String arm,int roll){int command=arm.equals("Artillery")?2:arm.equals("Support")?1:arm.equals("Commando")?4:3;return roll>=6?"Special":roll<=command?"Command":"Staff";}
 public static int die(Soldier s){s.randomState+=0x9E3779B97F4A7C15L;long z=s.randomState;z=(z^(z>>>30))*0xBF58476D1CE4E5B9L;z=(z^(z>>>27))*0x94D049BB133111EBL;z^=z>>>31;return (int)Long.remainderUnsigned(z,6)+1;}
 public static int roll(Soldier s,String reason,int dice,int dm){int total=0;StringJoiner j=new StringJoiner(" + ");for(int i=0;i<dice;i++){int d=die(s);total+=d;j.add(""+d);}s.log.add(reason+": ["+j+"]"+(dm==0?"":(dm>0?" +":" ")+dm+" DM")+" = "+(total+dm));return total+dm;}
 public static void note(Soldier s,String text){s.log.add(text);s.message=text;}
 public static void medal(Soldier s,String key){s.medals.merge(key,1,Integer::sum);note(s,"Awarded "+key+".");}
 public static boolean matches(String actual,String generic){return actual.equals(generic)||actual.startsWith(generic+": ")||generic.equals("Gun Combat")&&Arrays.asList("Combat Rifleman","Pistol","Submachinegun","Laser Weapons","Zero-G Weapons","High Energy Weapons","Auto Weapons").contains(actual);}
 public static void award(Soldier s,String skill){
  if(skill.startsWith("+")){int i=Arrays.asList("STR","DEX","END","INT","EDU","SOC").indexOf(skill.substring(1));s.stats[i]=Math.min(35,s.stats[i]+1);note(s,skill+".");return;}
  if(skill.equals("Gun Combat")&&!s.skills.containsKey("Combat Rifleman")){skill="Combat Rifleman";}
  if(skill.equals("Blade Combat")&&s.service.equals("Marines")&&!s.bladeExempt){int cut=s.skills.getOrDefault("Blade Combat: Cutlass",0);if(roll(s,"Marine blade tradition — 9+ to choose another blade",2,cut>=2?-6:cut==1?-3:0)<9)skill="Blade Combat: Cutlass";}
  if(Arrays.asList("Gun Combat","Heavy Weapons","FA Gunnery","Vehicle","Blade Combat").contains(skill)){s.pending.add(skill);return;}
  s.skills.merge(skill,1,Integer::sum);note(s,"Gained "+skill+"-"+s.skills.get(skill)+".");
 }
 public static List<String> choices(Soldier s){String p=s.getPendingSkill();List<String> out=new ArrayList<>();switch(p){case "Gun Combat":out.addAll(Arrays.asList("Combat Rifleman","Pistol","Submachinegun","Auto Weapons"));if(s.tl>=8)out.add("Laser Weapons");if(s.tl>=12)out.add("High Energy Weapons");if(s.skills.containsKey("Zero-G Combat")&&s.tl>=8)out.add("Zero-G Weapons");break;case "Vehicle":out.addAll(Arrays.asList("Wheeled","Tracked"));if(s.tl>=8)out.add("Grav");break;case "Blade Combat":out.addAll(Arrays.asList("Dagger","Blade","Foil","Sword","Cutlass","Broadsword","Bayonet","Spear","Halberd","Pike"));break;case "Heavy Weapons":out.addAll(Arrays.asList("Machinegun","Autocannon","Mortar","Rocket launcher"));if(s.arm.equals("Cavalry"))out.replaceAll(x->"Vehicle-mounted "+x);if(s.tl>=8)out.add("Vehicle-mounted laser");break;case "FA Gunnery":out.addAll(Arrays.asList("Cannon","Howitzer"));if(s.tl>=6)out.add("Multiple rocket launcher");if(s.tl>=7)out.add("Missile artillery");break;}return out;}
 public static List<String> tables(Soldier s){List<String> r=new ArrayList<>(Arrays.asList("Life","MOS"));if(s.officer)r.add(s.duty.equals("Command")?"Command":"Staff");else if(s.rank>=3)r.add("NCO");if(s.service.equals("Marines")&&s.posting.equals("Ship's troops"))r.add("Shipboard");return r;}
 public static String[] table(Soldier s,String key){switch(key){case "MOS":return MOS[arm(s.arm)];case "NCO":return NCO;case "Command":return COMMAND;case "Staff":return STAFF;case "Shipboard":return SHIP;default:return s.service.equals("Marines")?MARINELIFE:ARMYLIFE;}}
 public static void skillRoll(Soldier s,String key){int dm=key.equals("MOS")?(s.tl>=12?1:0):key.equals("Life")?(s.officer?(s.rank>=7?3:s.rank>=4?2:1):0):key.equals("NCO")?Math.max(0,s.rank-4):(key.equals("Command")||key.equals("Staff"))?(s.officer?(s.rank>=7?2:s.rank>=4?1:0):0):0;String[] t=table(s,key);int r=roll(s,key+" skill table",1,dm);award(s,t[r-1]);}
 public static void advance(Soldier s){s.years++;if(s.years%4==0){aging(s);s.phase=s.phase.equals("DEAD")?"DEAD":"TERM";}else s.phase="ASSIGN";s.next=s.phase;if(!s.pending.isEmpty())s.phase="CHOICE";}
 public static void aging(Soldier s){int age=s.getAge();if(age<34)return;for(int i=0;i<(age>=66?4:3);i++){int target=age>=66?9:age>=50?(i==1?8:9):(i==1?7:8);int loss=age>=66?2:1;if(roll(s,"Age "+age+" — "+new String[]{"STR","DEX","END","INT"}[i]+" ageing, "+target+"+",2,0)<target){s.stats[i]-=loss;if(s.stats[i]<=0){if(roll(s,"Ageing crisis — 8+",2,0)<8){s.phase="DEAD";note(s,"Died following an ageing crisis.");return;}s.stats[i]=1;}note(s,"Ageing reduced characteristic "+(i+1)+" by "+loss+".");}}}
 public static void training(Soldier s){note(s,"Year 1 · Basic and advanced training · "+s.arm);award(s,"Gun Combat");skillRoll(s,"MOS");advance(s);}
 public static void assignment(Soldier s,boolean education,boolean buck,boolean specialDm,boolean survival,String forced){
  s.bladeExempt=false;note(s,"Year "+(s.years+1)+" · Term "+s.getTerm()+" · "+s.arm);
  if(s.aide&&!forced.isEmpty()){s.aide=false;if(forced.equals("Command")||forced.equals("Staff"))s.duty=forced;else{school(s,forced);advance(s);return;}}
  else{s.duty=general(s.arm,roll(s,"General assignment",1,(education&&s.stats[4]>=8?1:0)-(buck&&s.officer?1:0)));}
  if(s.duty.equals("Special")){int dm=!s.officer&&specialDm&&s.stats[s.service.equals("Army")?2:4]>=7?1:0;int r=roll(s,"Special assignment",1,dm);String school=(s.officer?OFFICERS:ENLISTED)[r-1];school(s,school);if(!s.phase.equals("CROSS"))advance(s);return;}
  int mission=UNIT[arm(s.arm)][roll(s,"Unit assignment",2,0)-2];s.posting=MISSIONS[mission];note(s,s.duty+" · "+s.posting);int[] o=outcomes(s,mission);boolean combat=mission>=2&&mission<=4;
  int dm=0;if(survival&&!s.arm.equals("Support")){for(Map.Entry<String,Integer> e:s.skills.entrySet())for(String mos:MOS[arm(s.arm)])if(e.getValue()>=2&&matches(e.getKey(),mos))dm=1;}
  if(o[0]>0){int r=roll(s,"Survival — "+o[0]+"+",2,dm);if(r<o[0]){s.years++;s.phase="DEAD";note(s,"Killed on "+s.posting+". Service record closed.");return;}if(r==o[0]){note(s,"Wounded in service.");if(combat)medal(s,"Purple Heart");}}
  if(combat){medal(s,"Combat service ribbon");if(s.officer&&s.duty.equals("Command"))medal(s,"Combat command ribbon");}
  if(o[1]>0){int r=roll(s,"Decoration — "+o[1]+"+",2,0);if(r>=o[1])medal(s,r>=o[1]+6?"SEH":r>=o[1]+3?"MCG":"MCUF");}
  if((!s.officer||o[2]>0&&!s.promoted)&&s.rank<(s.officer?10:9)){int pdm=s.arm.equals("Support")?(s.stats[3]>=8?1:0):s.arm.equals("Commando")?(s.stats[2]>=8?1:0):(s.stats[4]>=7?1:0);if(roll(s,"Promotion — "+Math.abs(o[2])+"+",2,pdm)>=Math.abs(o[2])){s.rank++;if(s.officer)s.promoted=true;note(s,"Promoted to "+s.getRankName()+".");}}
  if(o[3]>0&&roll(s,"Skill eligibility — "+o[3]+"+",2,0)>=o[3]){s.phase="SKILL";return;}advance(s);
 }
 static void school(Soldier s,String name){s.special=name;s.schools.add("Year "+(s.years+1)+": "+name);note(s,"Special assignment · "+name);
  if(name.equals("Cross-training")){s.phase="CROSS";return;}if(name.equals("Recruiting")){award(s,"Recruiting");return;}
  if(name.equals("OCS")){skillRoll(s,"Command");skillRoll(s,"Staff");skillRoll(s,"MOS");s.officer=true;s.rank=1;note(s,"Commissioned Second Lieutenant.");return;}
  if(name.equals("Military attache / aide")){award(s,"+SOC");if(roll(s,"Attache 1–4; aide 5–6",1,0)<=4){s.rank=Math.min(10,s.rank+1);s.promoted=true;note(s,"Attache posting: promotion to "+s.getRankName());}else{s.aide=true;note(s,"Aide posting: choose the next assignment.");}return;}
  String[] skills;int target;
  switch(name){case "Specialist school":skills=new String[]{"Admin","Medical","Commo","Computer","Mechanical","Electronics"};target=0;break;case "Commando school":s.commando=true;s.bladeExempt=true;skills=new String[]{"Brawling","Gun Combat","Demolitions","Wilderness Survival","Recon","Vacc Suit","Blade Combat","Instruction"};target=5;break;case "Protected forces":skills=new String[]{"Vacc Suit","Zero-G Combat"};target=3;break;case "Intelligence school":skills=new String[]{"Forgery","Bribery","Streetwise","Interrogation"};target=4;break;case "Command college":skills=new String[]{"Tactics","Leadership","Recon"};target=4;break;case "Staff college":skills=new String[]{"Admin","Combat Engineering","Computer"};target=4;break;default:throw new IllegalArgumentException("Unknown school");}
  boolean repeated=s.schools.stream().filter(x->x.endsWith(": "+name)).count()>1; if(repeated){for(String sk:skills)for(Map.Entry<String,Integer> e:s.skills.entrySet())if(matches(e.getKey(),sk)&&e.getValue()>=2){note(s,"School instructor assignment.");award(s,"Instruction");return;}}
  if(target==0)award(s,skills[roll(s,"Specialist school subject",1,0)-1]);else for(String sk:skills)if(roll(s,name+": "+sk+" — "+target+"+",1,0)>=target)award(s,sk);
 }
 public static List<String> transferArms(Soldier s){List<String> r=new ArrayList<>();r.add(s.arm);for(String a:ARMS)if(!r.contains(a)&&(!a.equals("Commando")||s.commando)&&(!a.equals("Marine Infantry")||s.service.equals("Marines"))&&(s.officer||s.qualified.contains(a)||a.equals("Commando")&&s.commando))r.add(a);return r;}
 public static int travellerRank(Soldier s){return !s.officer?0:s.rank<=2?1:s.rank<=6?s.rank-1:6;}
 public static void muster(Soldier s){int r=travellerRank(s);s.benefits=s.getCompletedTerms()+(r>=5?3:r>=3?2:r>=1?1:0);s.phase=s.benefits>0?"BENEFITS":"FINAL";note(s,"Mustered out. "+s.benefits+" benefit rolls; maximum three cash rolls.");}
 public static final int[][] CASH={{2000,5000,10000,10000,10000,20000,30000},{2000,5000,5000,10000,20000,30000,40000}};
 public static final String[][] MATERIAL={{"Low passage","+INT","+2EDU","Gun","High passage","Middle passage","+SOC"},{"Low passage","+2INT","+EDU","Blade","Travellers' Aid Society membership","High passage","+2SOC"}};
 public static void benefit(Soldier s,String table,boolean bonus){int dm=bonus&&(table.equals("Cash")?s.skills.containsKey("Gambling"):travellerRank(s)>=5)?1:0;int row=roll(s,table+" mustering-out benefit",1,dm)-1;int service=s.service.equals("Army")?0:1;if(table.equals("Cash")){if(s.cashRolls>=3)throw new IllegalArgumentException("Maximum three cash rolls.");s.cashRolls++;s.credits+=CASH[service][row];note(s,"Received Cr "+CASH[service][row]);}else{String item=MATERIAL[service][row];if(item.startsWith("+2")){award(s,"+"+item.substring(2));award(s,"+"+item.substring(2));}else if(item.startsWith("+"))award(s,item);else{if(!item.contains("Society")||!s.equipment.contains(item))s.equipment.add(item);note(s,"Received "+item);}}s.benefits--;if(s.benefits==0)s.phase="FINAL";}
}

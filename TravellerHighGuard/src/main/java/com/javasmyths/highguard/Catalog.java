package com.javasmyths.highguard;
import java.util.*;
public class Catalog {
 public static final String CODES="0123456789ABCDEFGHJKLMNPQRSTUVWXYZ";
 public static final String[] CONFIG={"","Needle / wedge","Cone","Cylinder","Close structure","Sphere","Flattened sphere","Dispersed structure","Planetoid","Buffered planetoid"};
 public static final double[] HULL_MULT={0,1.2,1.1,1,.6,.7,.8,.5,0,0};
 public static final int[] SIZE={0,100,200,300,400,500,600,700,800,900,1000,2000,3000,4000,5000,6000,7000,8000,9000,10000,20000,30000,40000,50000,75000,100000,200000,300000,400000,500000,700000,900000,1000000};
 public static String code(int value){return value>=0&&value<CODES.length()?""+CODES.charAt(value):"["+value+"]";}
 public static int sizeIndex(int tons){int i=0;while(i+1<SIZE.length&&tons>=SIZE[i+1])i++;return i;}
 public static String sizeCode(int tons){return code(sizeIndex(tons));}
 public static class Computer {public String name,usp;public int model,tl,ep;public double tons,mcr;Computer(String n,String u,int m,int t,int e,double w,double c){name=n;usp=u;model=m;tl=t;ep=e;tons=w;mcr=c;}}
 public static final Map<String,Computer> COMPUTERS=new LinkedHashMap<>();
 public static class Weapon {public String id,name,category;public int tl,maxPerMount;public double tons,mcr,ep;public int[] thresholds,bayFactors;public boolean bay;
  Weapon(String id,String n,String cat,int tl,int max,double tons,double mcr,double ep,int... thresholds){this.id=id;name=n;category=cat;this.tl=tl;maxPerMount=max;this.tons=tons;this.mcr=mcr;this.ep=ep;this.thresholds=thresholds;}
  public int factor(int weapons,int tech){if(bay){int r=0;for(int i=0;i<bayFactors.length&&i+7<=tech;i++)if(bayFactors[i]>0)r=bayFactors[i];return r;}int r=0;for(int i=0;i<thresholds.length;i++)if(thresholds[i]>0&&weapons>=thresholds[i])r=i+1;if(r==0)return 0;int bonus=id.equals("sand")?(tech>=10?2:tech>=8?1:0):id.equals("plasma")?(tech>=12?2:tech>=11?1:0):id.equals("fusion")?(tech>=14?1:0):Arrays.asList("missile","beam","pulse").contains(id)&&tech>=13?1:0;return Math.min(9,r+bonus);}
 }
 public static final Map<String,Weapon> WEAPONS=new LinkedHashMap<>();
 public static class Major {public String id,category;public int tl,ep;public double tons,mcr;Major(String id,String cat,int t,int e,double w,double c){this.id=id;category=cat;tl=t;ep=e;tons=w;mcr=c;}}
 public static final Map<String,Major> MAJORS=new LinkedHashMap<>();
 public static final int[] SCREEN_TL={0,12,13,13,14,14,14,15,15,15};
 public static final double[] DAMP_TONS={0,50,15,20,8,10,12,10,15,20},DAMP_COST={0,50,40,45,30,35,38,30,40,50},MESON_TONS={0,90,30,45,16,20,24,20,30,40},MESON_COST={0,80,50,55,40,45,50,40,50,60};
 static {
  String[] names={"1","1fib","1bis","2","2fib","2bis","3","3fib","4","4fib","5","5fib","6","6fib","7","7fib","8","8fib","9","9fib"};String[] usp={"1","A","R","2","B","S","3","C","4","D","5","E","6","F","7","G","8","H","9","J"};
  int[] models={1,1,1,2,2,2,3,3,4,4,5,5,6,6,7,7,8,8,9,9},tls={5,5,6,7,7,8,9,9,10,10,11,11,12,12,13,13,14,14,15,15},eps={0,0,0,0,0,0,1,1,2,2,3,3,5,5,7,7,9,9,12,12};double[] tons={1,2,1,2,4,2,3,6,4,8,5,10,7,14,9,18,11,22,13,26},cost={2,3,4,9,14,18,18,27,30,45,45,68,55,83,80,100,110,140,140,200};for(int i=0;i<names.length;i++)COMPUTERS.put(names[i],new Computer(names[i],usp[i],models[i],tls[i],eps[i],tons[i],cost[i]));
  weapon(new Weapon("missile","Missile racks","Missile",7,3,1,.75,0,1,3,6,12,18,30));weapon(new Weapon("beam","Beam lasers","Laser",7,3,1,1,1,1,2,3,6,10,15,21,30));weapon(new Weapon("pulse","Pulse lasers","Laser",7,3,1,.5,1,1,3,6,10,21,30));weapon(new Weapon("plasma","Plasma guns","Energy",10,2,2,1.5,1,1,4,10,16,20));weapon(new Weapon("fusion","Fusion guns","Energy",12,2,2,2,2,0,0,0,1,4,10,16,20));weapon(new Weapon("sand","Sandcasters","Sand",7,3,1,.25,0,1,3,6,8,10,20,30));weapon(new Weapon("particle","Particle accelerators","Particle",15,1,3,3,5,0,1,2,4,6,8,10));weapon(new Weapon("barbette","Particle barbettes","Particle",14,1,5,4,5,1,2,4,6,8,10));
  bay("meson100","Meson gun · 100t bay","Meson",100,70,200,0,0,0,0,0,0,3,5,9);bay("particle100","Particle accelerator · 100t bay","Particle",100,35,60,0,6,6,7,7,8,8,9,9);bay("repulsor100","Repulsor · 100t bay","Repulsor",100,10,10,0,0,0,2,4,6,7,8,9);bay("missile100","Missiles · 100t bay","Missile",100,20,0,7,7,7,8,8,9,9,0,0);
  bay("meson50","Meson gun · 50t bay","Meson",50,50,100,0,0,0,0,0,0,0,0,4);bay("particle50","Particle accelerator · 50t bay","Particle",50,20,30,0,0,0,3,3,4,4,5,5);bay("repulsor50","Repulsor · 50t bay","Repulsor",50,6,5,0,0,0,0,0,0,0,3,5);bay("missile50","Missiles · 50t bay","Missile",50,12,0,0,0,7,7,7,8,8,9,9);bay("plasma50","Plasma gun · 50t bay","Energy",50,5,10,0,0,0,4,5,6,0,0,0);bay("fusion50","Fusion gun · 50t bay","Energy",50,8,20,0,0,0,0,0,7,8,9,0);bay("empty50","Empty 50t bay","Empty",50,0,0,1,1,1,1,1,1,1,1,1);bay("empty100","Empty 100t bay","Empty",100,0,0,1,1,1,1,1,1,1,1,1);
  int[] paT={8,9,10,11,12,13,14,15,10,11,12,13,14,15,12,13,14,15},paE={500,500,500,600,600,600,700,700,800,800,800,900,900,900,1000,1000,1000,1000},paW={5500,5000,4500,4000,3500,3000,2500,2500,5000,4500,4000,3500,3000,2500,4500,4000,3500,3000},paC={3500,3000,2400,1500,1200,1200,800,500,3000,2000,1600,1200,1000,800,2000,1500,1200,1000};
  int[] meT={11,11,12,12,13,13,14,14,15,12,13,14,15,13,14,15,14,15},meE={500,600,600,700,700,800,800,900,900,1000,1000,1000,1000,1100,1100,1100,1200,1200},meW={5000,8000,2000,5000,1000,2000,1000,2000,1000,8000,5000,4000,2000,8000,7000,5000,8000,7000},meC={10000,12000,3000,5000,800,1000,400,600,400,10000,3000,800,600,5000,1000,800,2000,1000};
  for(int i=0;i<18;i++){String letter="ABCDEFGHJKLMNPQRST".substring(i,i+1);major(new Major("Particle-"+letter,"Particle",paT[i],paE[i],paW[i],paC[i]));major(new Major("Meson-"+letter,"Meson",meT[i],meE[i],meW[i],meC[i]));}
 }
 static void weapon(Weapon w){WEAPONS.put(w.id,w);}static void major(Major m){MAJORS.put(m.id,m);}static void bay(String id,String n,String cat,double tons,double mcr,double ep,int... factors){int tl=7;while(tl<16&&factors[tl-7]==0)tl++;Weapon w=new Weapon(id,n,cat,tl,1,tons,mcr,ep);w.bay=true;w.bayFactors=factors;weapon(w);}
}

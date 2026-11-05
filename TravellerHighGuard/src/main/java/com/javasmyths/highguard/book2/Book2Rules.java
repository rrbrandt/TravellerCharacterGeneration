package com.javasmyths.highguard.book2;
import java.util.*;

/** Classic Traveller Book 2, revised 1981: numeric construction data, pp.12–23. */
public class Book2Rules {
 public static final String LETTERS="ABCDEFGHJKLMNPQRSTUVWXYZ";
 public static final int[] HULLS={100,200,400,600,800,1000,2000,3000,4000,5000};
 private static final int[] STANDARD={100,200,400,600,800,1000}, ENGINEERING={15,15,50,85,165,165}, HULL_PRICE={2,8,16,48,80,100}, STANDARD_MONTHS={9,11,14,22,25,27}, CUSTOM_MONTHS={10,12,16,24,28,30,32,34,35,36};
 private static final int[][] POTENTIAL={
  {2,1,0,0,0,0,0,0,0,0},{4,2,1,0,0,0,0,0,0,0},{6,3,1,1,0,0,0,0,0,0},
  {0,4,2,1,1,0,0,0,0,0},{0,5,2,1,1,1,0,0,0,0},{0,6,3,2,1,1,0,0,0,0},
  {0,0,3,2,1,1,0,0,0,0},{0,0,4,2,2,1,0,0,0,0},{0,0,4,3,2,1,1,0,0,0},
  {0,0,5,3,2,2,1,0,0,0},{0,0,5,3,2,2,1,0,0,0},{0,0,6,4,3,2,1,0,0,0},
  {0,0,6,4,3,2,1,0,0,0},{0,0,0,4,3,2,1,0,0,0},{0,0,0,5,3,3,1,1,0,0},
  {0,0,0,5,4,3,1,1,0,0},{0,0,0,5,4,3,1,1,0,0},{0,0,0,6,4,3,1,1,0,0},
  {0,0,0,6,4,3,1,1,0,0},{0,0,0,6,5,4,2,1,1,0},{0,0,0,0,5,5,2,1,1,1},
  {0,0,0,0,6,6,3,2,1,1},{0,0,0,0,6,6,4,2,2,1},{0,0,0,0,6,6,6,4,3,2}};
 public static final Map<String,double[]> COMPUTERS=new LinkedHashMap<>();
 public static final Map<String,Vehicle> VEHICLES=new LinkedHashMap<>();
 public static class Vehicle {
  public String name;public double tons;public long credits;
  public boolean smallCraft;public int people,maneuver;public double fuel,excessSpace;
  public int getListedPassengers(){return smallCraft?Math.max(0,people-1):0;}
  public int getSeatingOnlyPassengers(){return smallCraft?getListedPassengers()+(int)Math.floor(excessSpace/.5+1e-8):0;}
  Vehicle(String n,double t,long c){name=n;tons=t;credits=c;}
 }
 static {
  String[] names={"1","1bis","2","2bis","3","4","5","6","7"};
  double[] tons={1,1,2,2,3,4,5,7,9},price={2,4,9,18,18,30,45,55,80};int[] jump={1,2,2,3,3,4,5,6,6},tl={5,6,7,8,9,10,11,12,13};
  for(int i=0;i<names.length;i++)COMPUTERS.put(names[i],new double[]{tons[i],price[i]*1e6,jump[i],tl[i]});
  vehicle("airRaft","Air/raft",4,600000);vehicle("atvWheeled","Wheeled ATV",10,30000);vehicle("atvTracked","Tracked ATV",10,30000);
  vehicle("speeder","Speeder",6,1000000);vehicle("gCarrier","GCarrier",8,1000000);vehicle("launch","20-ton launch",20,14000000);
  vehicle("boat","Ship's boat",30,16000000);vehicle("pinnace","Pinnace",40,20000000);vehicle("cutter","Modular cutter (without modules)",50,28000000);
  vehicle("fuelModule","Cutter fuel module",30,1000000);vehicle("atvModule","Cutter ATV module",30,1800000);vehicle("openModule","Cutter open module",30,2000000);
  vehicle("slowBoat","Slow boat",30,15000000);vehicle("slowPinnace","Slow pinnace",40,18000000);vehicle("shuttle","Shuttle",95,33000000);vehicle("fighter","Fighter",10,18000000);
  craft("launch",2,1,1,13);craft("boat",2,6,1.8,13.7);craft("slowBoat",2,3,1,19.9);
  craft("pinnace",2,5,2,22.4);craft("slowPinnace",2,2,1,31.6);craft("cutter",2,4,2,2.5);
  craft("shuttle",2,3,2.85,71);craft("fighter",1,6,1,1);
 }
 private static void craft(String id,int people,int maneuver,double fuel,double excessSpace){Vehicle v=VEHICLES.get(id);v.smallCraft=true;v.people=people;v.maneuver=maneuver;v.fuel=fuel;v.excessSpace=excessSpace;}
 private static void vehicle(String id,String name,double tons,long credits){VEHICLES.put(id,new Vehicle(name,tons,credits));}
 public static int driveIndex(String letter){return letter!=null && letter.length()==1 ? LETTERS.indexOf(letter)+1:0;}
 public static int hullColumn(int hull){for(int i=0;i<HULLS.length;i++)if(hull<=HULLS[i])return i;return -1;}
 public static int rating(String letter,int hull){int i=driveIndex(letter),h=hullColumn(hull);return i>0&&h>=0?POTENTIAL[i-1][h]:0;}
 public static class Line {public String name;public double tons;public long credits;public String area;Line(String n,double t,long c,String a){name=n;tons=t;credits=c;area=a;}}
 public static class Report {
  public List<String> errors=new ArrayList<>(),notes=new ArrayList<>();public List<Line> lines=new ArrayList<>();public Map<String,Integer> positions=new LinkedHashMap<>();
  public boolean valid;public int jump,maneuver,power,months,fullCrew,minimumCrew,computerTL;
  public double driveTons,engineeringCapacity,mainCapacity,mainUsed,minimumFuel,fuel,cargo,unusedMain,totalUsed;
  public long listPrice,shipPrice,architectFee,totalPrice,downPayment,monthlyPayment,annualMaintenance;
  void add(String name,double tons,long credits,String area){lines.add(new Line(name,tons,credits,area));listPrice+=credits;if("main".equals(area))mainUsed+=tons;}
 }
 private static void error(Report r,boolean condition,String message){if(condition)r.errors.add(message);}
 private static int count(Report r,int n,String name){error(r,n<0||n>10000,name+" must be between 0 and 10,000.");return Math.max(0,Math.min(n,10000));}
 private static boolean finite(double n){return Double.isFinite(n);}
 public static Report calculate(ShipDesign d){
  Report r=new Report();error(r,d.hull<100||d.hull>5000,"Book 2 hulls must be 100–5,000 displacement tons; small craft use published designs.");
  error(r,d.name==null||d.name.trim().isEmpty()||d.name.length()>120,"Enter a ship name of 1–120 characters.");
  error(r,!"standard".equals(d.hullType)&&!"custom".equals(d.hullType),"Choose a standard or custom hull.");
  error(r,!finite(d.fuel)||d.fuel< -1||d.fuel>100000,"Fuel must be -1 (automatic) or a nonnegative tonnage.");
  error(r,!finite(d.cargo)||d.cargo< -1||d.cargo>100000,"Cargo must be -1 (remaining space) or a nonnegative tonnage.");
  error(r,!finite(d.engineering)||d.engineering<0||d.engineering>5000,"Engineering allocation must be nonnegative and within the hull.");
  error(r,d.extraSoftwareCredits<0||d.extraSoftwareCredits>10000000000L,"Additional software cost is out of range.");
  if(!r.errors.isEmpty())return r;
  int standard=-1;for(int i=0;i<STANDARD.length;i++)if(d.hull==STANDARD[i])standard=i;
  boolean std="standard".equals(d.hullType);error(r,std&&standard<0,"No standard hull exists at this tonnage. Choose Custom hull.");
  long hullPrice=std&&standard>=0?HULL_PRICE[standard]*1000000L:Math.max(20000000L,d.hull*100000L);
  r.add("Hull",0,hullPrice,"hull");r.months=std&&standard>=0?STANDARD_MONTHS[standard]:CUSTOM_MONTHS[hullColumn(d.hull)];
  int j=driveIndex(d.jump),m=driveIndex(d.maneuver),p=driveIndex(d.power);
  error(r,j==0&&!"None".equals(d.jump),"Choose a valid jump drive or None.");error(r,m==0&&!"None".equals(d.maneuver),"Choose a valid maneuver drive or None.");error(r,p==0,"A valid power plant is required.");
  error(r,j==0&&m==0,"A non-starship requires a maneuver drive.");error(r,p<Math.max(j,m),"Power plant letter must equal or exceed both drive letters.");
  r.jump=rating(d.jump,d.hull);r.maneuver=rating(d.maneuver,d.hull);r.power=rating(d.power,d.hull);
  error(r,j>0&&r.jump==0,"Jump drive is unavailable for this hull size.");error(r,m>0&&r.maneuver==0,"Maneuver drive is unavailable for this hull size.");error(r,p>0&&r.power==0,"Power plant is unavailable for this hull size.");
  if(j>0){double tons=5*j+5;r.driveTons+=tons;r.add("Jump drive "+d.jump,tons,j*10000000L,"engineering");}
  if(m>0){double tons=2*m-1;r.driveTons+=tons;r.add("Maneuver drive "+d.maneuver,tons,m*4000000L,"engineering");}
  if(p>0){double tons=3*p+1;r.driveTons+=tons;r.add("Power plant "+d.power,tons,p*8000000L,"engineering");}
  r.engineeringCapacity=std&&standard>=0?ENGINEERING[standard]:d.engineering==0?r.driveTons:d.engineering;
  r.mainCapacity=d.hull-r.engineeringCapacity;error(r,r.driveTons>r.engineeringCapacity,"Drives exceed the engineering section.");error(r,r.engineeringCapacity>d.hull,"Engineering exceeds the hull.");
  r.minimumFuel=d.hull*.1*r.jump+10*r.power;r.fuel=d.fuel==-1?r.minimumFuel:d.fuel;
  error(r,r.fuel+1e-8<r.minimumFuel,"Fuel is below the one-jump plus four-week power-plant minimum.");
  r.add("Fuel tankage",r.fuel,0,"main");r.add("Bridge",Math.max(20,.02*d.hull),d.hull*5000L,"main");
  double[] computer=COMPUTERS.get(d.computer);error(r,computer==null,"Choose a valid computer model.");
  if(computer!=null){r.add("Computer Model/"+d.computer,computer[0],(long)computer[1],"main");r.computerTL=(int)computer[3];error(r,r.jump>computer[2],"Computer model cannot support the selected jump number.");}
  int rooms=count(r,d.staterooms,"Staterooms"),low=count(r,d.lowBerths,"Low berths"),emergency=count(r,d.emergencyBerths,"Emergency berths");
  int high=count(r,d.highPassengers,"High passengers"),middle=count(r,d.middlePassengers,"Middle passengers"),crew=count(r,d.crew,"Crew");
  r.add("Staterooms × "+rooms,4.0*rooms,500000L*rooms,"main");r.add("Low berths × "+low,.5*low,50000L*low,"main");r.add("Emergency berths × "+emergency,emergency,100000L*emergency,"main");
  int hp=count(r,d.hardpoints,"Hardpoints"),single=count(r,d.singleTurrets,"Single turrets"),dual=count(r,d.doubleTurrets,"Double turrets"),triple=count(r,d.tripleTurrets,"Triple turrets"),reserve=count(r,d.fireControlReserve,"Reserved fire control");
  int turrets=single+dual+triple;error(r,hp>d.hull/100,"At most one hardpoint per 100 hull tons.");error(r,turrets>hp,"Turrets exceed designated hardpoints.");error(r,turrets+reserve>hp,"Installed and reserved fire control exceed hardpoints.");
  r.add("Hardpoints × "+hp,0,hp*100000L,"main");r.add("Turrets and fire control",turrets,single*200000L+dual*500000L+triple*1000000L,"main");r.add("Future fire-control reserve",reserve,0,"main");
  int pulse=count(r,d.pulseLasers,"Pulse lasers"),beam=count(r,d.beamLasers,"Beam lasers"),missiles=count(r,d.missileRacks,"Missile racks"),sand=count(r,d.sandcasters,"Sandcasters");
  int weapons=pulse+beam+missiles+sand;error(r,weapons>single+2*dual+3*triple,"Weapons exceed turret mount capacity.");
  r.add("Turret weapons",0,pulse*500000L+beam*1000000L+missiles*750000L+sand*250000L,"main");
  if(d.vehicles==null)d.vehicles=new LinkedHashMap<>();for(Map.Entry<String,Integer> e:d.vehicles.entrySet()){
   Vehicle v=VEHICLES.get(e.getKey());if(v==null){r.errors.add("Unknown vehicle type.");continue;}int n=count(r,e.getValue()==null?-1:e.getValue(),"Vehicle quantity");if(n>0)r.add(v.name+" × "+n,v.tons*n,v.credits*n,"main");
  }
  if(d.streamlined)r.add("Streamlining",0,d.hull*10000L,"hull");
  if(d.extraSoftwareCredits>0)r.add("Additional purchased software",0,d.extraSoftwareCredits,"main");
  r.positions.put("Pilot",1);r.positions.put("Navigator",j>0&&d.hull>200?1:0);r.positions.put("Engineers",d.hull>=200?(int)Math.ceil(r.driveTons/35):0);
  r.positions.put("Stewards",(high+7)/8);r.positions.put("Medics",j>0?Math.max(d.hull>=200?1:0,(high+middle+low+119)/120):0);
  r.positions.put("Gunners (optional)",d.gunners?Math.min(turrets,weapons):0);r.fullCrew=r.positions.values().stream().mapToInt(Integer::intValue).sum();r.minimumCrew=d.dualRoles?(r.fullCrew+1)/2:r.fullCrew;
  error(r,crew<r.minimumCrew,"Assigned crew is below required positions"+(d.dualRoles?" after dual-role pairing.":"."));
  error(r,d.commercial&&d.doubleOccupancy,"Commercial ships require single occupancy; disable double occupancy.");
  error(r,crew+high+middle>rooms*(d.doubleOccupancy?2:1),"Not enough stateroom places for crew and awake passengers.");
  r.cargo=d.cargo==-1?Math.max(0,r.mainCapacity-r.mainUsed):d.cargo;r.add("Cargo hold",r.cargo,0,"main");r.unusedMain=r.mainCapacity-r.mainUsed;
  error(r,r.unusedMain< -1e-8,"Main-compartment fittings exceed available space by "+String.format(Locale.ROOT,"%.2f",-r.unusedMain)+" tons.");
  r.totalUsed=r.engineeringCapacity+r.mainUsed;
  r.shipPrice=d.standardDesign?Math.round(r.listPrice*.9):r.listPrice;r.architectFee=d.standardDesign?0:Math.round(r.shipPrice*.01);r.totalPrice=r.shipPrice+r.architectFee;
  r.downPayment=Math.round(r.shipPrice*.2);r.monthlyPayment=Math.round(r.shipPrice/240.0);r.annualMaintenance=Math.round(r.shipPrice*.001);
  r.notes.add("Fuel covers one maximum-range jump and four weeks of power-plant operations. Extra tankage is optional.");
  if(d.standardDesign)r.notes.add("10% standard-design discount assumes an established, available design plan; custom plans do not qualify.");
  if(d.dualRoles)r.notes.add("Two roles per crew member require the appropriate skills (normally level 2 in both; steward 1). Pairing is subject to referee approval.");
  r.notes.add("Bridge includes basic sensors and communications. Computer jump support is checked; choose flight/combat software separately using Book 2's software allowances.");
  r.notes.add("Carried craft are charged at their full listed displacement; modules are allocated separately. This is a construction worksheet, not a deck-plan or combat simulator.");
  r.valid=r.errors.isEmpty();return r;
 }
}

package com.javasmyths.worldbuilder;
import java.util.*;
import static com.javasmyths.worldbuilder.Atlas.*;

/** Book 3 revised 1981 mainworld generation; hydrographics uses atmosphere, per errata. */
public class Book3Rules {
 public static final String DIGITS="0123456789ABCDEFGHJKLMNPQRSTUVWXYZ";
 public static String digit(int n){if(n<0||n>=DIGITS.length())throw new IllegalArgumentException("Code outside display range");return DIGITS.substring(n,n+1);}
 public interface Dice {int d6();}
 public static class RandomDice implements Dice {private final Random random;public RandomDice(String seed){random=new Random(seedHash(seed));}public int d6(){return random.nextInt(6)+1;}}
 public static long seedHash(String s){long h=0xcbf29ce484222325L;for(char c:s.toCharArray()){h^=c;h*=0x100000001b3L;}return h;}
 static int clamp(int n,int max){return Math.max(0,Math.min(n,max));}
 static int[] roll(Dice d,int n){int[] a=new int[n];for(int i=0;i<n;i++){a[i]=d.d6();if(a[i]<1||a[i]>6)throw new IllegalArgumentException("Invalid die");}return a;}
 static int total(int[] a){return Arrays.stream(a).sum();}
 static String dice(int[] a){return Arrays.toString(a)+" = "+total(a);}
 public static String port(int roll){if(roll<=4)return "A";if(roll<=6)return "B";if(roll<=8)return "C";if(roll==9)return "D";if(roll<=11)return "E";return "X";}
 public static int techDm(World w){return portDm(w.starport)+sizeDm(w.size)+atmoDm(w.atmosphere)+hydroDm(w.hydrographics)+popDm(w.population)+govDm(w.government);}
 static int portDm(String p){switch(p){case "A":return 6;case "B":return 4;case "C":return 2;case "X":return -4;default:return 0;}}
 static int sizeDm(int n){return n<=1?2:n<=4?1:0;}
 static int atmoDm(int n){return n<=3||n>=10?1:0;}
 static int hydroDm(int n){return n==9?1:n==10?2:0;}
 static int popDm(int n){return n>=1&&n<=5?1:n==9?2:n==10?4:0;}
 static int govDm(int n){return n==0||n==5?1:n==13?-2:0;}
 static int numeric(World w,String label,Dice d,int dm,int max,String formula){int[] a=roll(d,2);int raw=total(a)+dm,n=clamp(raw,max);w.rolls.add(new Roll(label,dice(a),formula+(raw!=n?"; clamp "+raw+" to "+n:""),String.valueOf(n)));return n;}
 public static World world(Dice d,String hex,String name){
  World w=new World();w.hex=hex;w.name=name;int[] a=roll(d,2);w.starport=port(total(a));w.rolls.add(new Roll("Starport",dice(a),"2D → starport table",w.starport));
  if(w.starport.equals("A")||w.starport.equals("B")){a=roll(d,2);w.naval=total(a)>=8;w.rolls.add(new Roll("Naval base",dice(a),"Class A/B: 8+",w.naval?"Present":"None"));}else w.rolls.add(new Roll("Naval base","—","Only at class A/B ports","None"));
  if("ABCD".contains(w.starport)){int dm=w.starport.equals("A")?-3:w.starport.equals("B")?-2:w.starport.equals("C")?-1:0;a=roll(d,2);w.scout=total(a)+dm>=7;w.rolls.add(new Roll("Scout base",dice(a),"2D "+dm+"; 7+",w.scout?"Present":"None"));}else w.rolls.add(new Roll("Scout base","—","Not at class E/X ports","None"));
  a=roll(d,2);w.gasGiant=total(a)<=9;w.rolls.add(new Roll("Gas giant",dice(a),"2D: 9 or less",w.gasGiant?"Present":"None"));
  w.size=numeric(w,"Size",d,-2,10,"2D − 2");
  if(w.size==0){w.atmosphere=0;w.hydrographics=0;w.rolls.add(new Roll("Atmosphere","—","Size 0 → atmosphere 0","0"));w.rolls.add(new Roll("Hydrographics","—","Size 0 → hydrographics 0","0"));}
  else{w.atmosphere=numeric(w,"Atmosphere",d,w.size-7,15,"2D − 7 + size "+w.size);int dm=w.atmosphere<=1||w.atmosphere>=10?-4:0;w.hydrographics=numeric(w,"Hydrographics",d,w.atmosphere-7+dm,10,"2D − 7 + atmosphere "+w.atmosphere+(dm!=0?" − 4 (extreme atmosphere)":""));}
  w.population=numeric(w,"Population",d,-2,10,"2D − 2");
  w.government=numeric(w,"Government",d,w.population-7,15,"2D − 7 + population "+w.population);
  w.law=numeric(w,"Law level",d,w.government-7,33,"2D − 7 + government "+w.government);
  a=roll(d,1);int raw=total(a)+techDm(w);w.tech=Math.max(0,raw);w.rolls.add(new Roll("Tech level",dice(a),"1D + port "+portDm(w.starport)+" + size "+sizeDm(w.size)+" + atmosphere "+atmoDm(w.atmosphere)+" + hydrographics "+hydroDm(w.hydrographics)+" + population "+popDm(w.population)+" + government "+govDm(w.government)+(raw<0?"; minimum 0":""),String.valueOf(w.tech)));
  return w;
 }
 public static Atlas generate(String name,String seed,int density,boolean single){
  if(density<1||density>6)throw new IllegalArgumentException("Density must be a 1D threshold from 1 to 6");
  Atlas a=new Atlas();a.name=clean(name,80);if(a.name.isEmpty())a.name="The Far Shore";a.seed=clean(seed,80);if(a.seed.isEmpty())a.seed=UUID.randomUUID().toString().substring(0,8);a.density=density;a.kind=single?"world":"subsector";
  if(single){a.worlds.add(world(new RandomDice(a.seed+":world"),"0101",a.name));return a;}
  Dice presence=new RandomDice(a.seed+":presence");
  for(int c=1;c<=8;c++)for(int r=1;r<=10;r++){String hex=String.format(Locale.ROOT,"%02d%02d",c,r);int roll=presence.d6();if(roll>=density){World w=world(new RandomDice(a.seed+":"+hex),hex,worldName(a.seed,hex));w.rolls.add(0,new Roll("World presence",String.valueOf(roll),"1D ≥ "+density,"Present"));a.worlds.add(w);}}
  return a;
 }
 static String worldName(String seed,String hex){String[] p={"Ar","Bel","Cor","Dar","El","Far","Gal","Hel","Is","Jan","Kel","Lor","Mor","Nor","Or","Pel","Quel","Rin","Sol","Tor","Ul","Val","Wen","Xan","Yor","Zel"},s={"adon","ara","ea","eron","ia","ion","is","ora","os","une","us","yn"};Random r=new Random(seedHash(seed+":name:"+hex));return p[r.nextInt(p.length)]+s[r.nextInt(s.length)]+" "+hex;}
 static String clean(String s,int length){if(s==null)return "";s=s.trim();if(s.length()>length)throw new IllegalArgumentException("Text exceeds "+length+" characters");return s;}
 public static World decode(String profile,String name,String hex){
  String p=profile==null?"":profile.trim().toUpperCase(Locale.ROOT);if(!p.matches("[ABCDEX][0-9A-HJ-NP-Z]{6}-[0-9A-HJ-NP-Z]"))throw new IllegalArgumentException("Use a profile such as A788899-C. I and O are not valid codes.");
  World w=new World();w.name=clean(name,80);w.hex=hex;w.starport=p.substring(0,1);w.size=DIGITS.indexOf(p.charAt(1));w.atmosphere=DIGITS.indexOf(p.charAt(2));w.hydrographics=DIGITS.indexOf(p.charAt(3));w.population=DIGITS.indexOf(p.charAt(4));w.government=DIGITS.indexOf(p.charAt(5));w.law=DIGITS.indexOf(p.charAt(6));w.tech=DIGITS.indexOf(p.charAt(8));validate(w);w.rolls.add(new Roll("Manual profile","—","Referee-entered UWP; no dice claimed",w.getUwp()));return w;
 }
 public static void validate(World w){
  if(w==null||w.starport==null||!w.starport.matches("[ABCDEX]")||w.hex==null||!w.hex.matches("0[1-8](0[1-9]|10)"))throw new IllegalArgumentException("Invalid starport or subsector hex");
  int[] n={w.size,w.atmosphere,w.hydrographics,w.population,w.government,w.law,w.tech},max={10,15,10,10,15,33,33};for(int i=0;i<n.length;i++)if(n[i]<0||n[i]>max[i])throw new IllegalArgumentException("UWP value outside supported Book 3 range");
  w.name=clean(w.name,80);w.notes=clean(w.notes,4000);if(w.name.isEmpty())w.name="Unnamed";
  if(w.rolls==null||w.rolls.size()>20)throw new IllegalArgumentException("Invalid roll history");
  for(Roll r:w.rolls){if(r==null)throw new IllegalArgumentException("Invalid roll history");r.label=clean(r.label,80);r.dice=clean(r.dice,160);r.formula=clean(r.formula,500);r.result=clean(r.result,80);}
 }
 public static List<String> trade(World w){List<String> t=new ArrayList<>();if(w.atmosphere>=4&&w.atmosphere<=9&&w.hydrographics>=4&&w.hydrographics<=8&&w.population>=5&&w.population<=7)t.add("Ag");if(w.atmosphere<=3&&w.hydrographics<=3&&w.population>=6)t.add("Na");if(Arrays.asList(0,1,2,4,7,9).contains(w.atmosphere)&&w.population>=9)t.add("In");if(w.population<=6)t.add("Ni");if((w.atmosphere==6||w.atmosphere==8)&&w.population>=6&&w.population<=8&&w.government>=4&&w.government<=9)t.add("Ri");if(w.atmosphere>=2&&w.atmosphere<=5&&w.hydrographics<=3)t.add("Po");return t;}
 public static Map<String,String> describe(World w){
  Map<String,String> d=new LinkedHashMap<>();
  String[] atmosphere={"Vacuum; vacc suit required","Trace atmosphere; vacc suit required","Very thin and tainted; respirator and filter","Very thin; respirator","Thin and tainted; filter mask","Thin, breathable","Standard, breathable","Standard and tainted; filter mask","Dense, breathable","Dense and tainted; filter mask","Exotic; independent air supply","Corrosive; protective suit","Insidious; specialised protection","Above the basic atmosphere descriptions; referee interpretation","Above the basic atmosphere descriptions; referee interpretation","Above the basic atmosphere descriptions; referee interpretation"};
  String[] governments={"No central government","Company or corporation rule","Participating democracy","Self-perpetuating oligarchy","Representative democracy","Feudal technocracy","Captive government or colony","Balkanized rival governments","Civil-service bureaucracy","Impersonal bureaucracy","Charismatic dictator","Non-charismatic dictator","Charismatic oligarchy","Religious dictatorship","Above the basic government descriptions; referee interpretation","Above the basic government descriptions; referee interpretation"};
  String[] technology={"Stone-age tools","Bronze / iron working","Early industrial machinery","Industrial manufacturing","Early electrical technology","Radio and early aviation","Nuclear-era industry","Computers and orbital flight","Fusion and early grav vehicles","Early interstellar technology","Improved interstellar systems","Advanced industrial systems","Mature interstellar technology","Advanced gravitic systems","Highly advanced interstellar technology","Leading-edge interstellar technology"};
  switch(w.starport){case "A":d.put("Starport","A · Excellent; refined fuel, overhaul and starship construction");break;case "B":d.put("Starport","B · Good; refined fuel, overhaul and non-starship construction");break;case "C":d.put("Starport","C · Routine; unrefined fuel and limited repair");break;case "D":d.put("Starport","D · Poor; unrefined fuel, minimal facilities");break;case "E":d.put("Starport","E · Frontier landing area; no assured fuel or repair");break;default:d.put("Starport","X · No starport facilities");}
  d.put("Size",w.size==0?"0 · Asteroid or planetoid belt":digit(w.size)+" · About "+String.format(Locale.ROOT,"%,d",w.size*1000)+" miles diameter ("+String.format(Locale.ROOT,"%,d",Math.round(w.size*1609.344))+" km)");
  d.put("Atmosphere",digit(w.atmosphere)+" · "+atmosphere[w.atmosphere]);
  d.put("Hydrographics",digit(w.hydrographics)+" · Approximately "+w.hydrographics*10+"% surface liquid coverage");
  d.put("Population",w.population==0?"0 · Fewer than 10 inhabitants":digit(w.population)+" · Order of "+String.format(Locale.ROOT,"%,d",(long)Math.pow(10,w.population))+" inhabitants");
  d.put("Government",digit(w.government)+" · "+governments[w.government]);
  String[] laws={"No weapon restrictions","Body pistols, explosives and poison gas barred","Portable energy weapons barred","Military weapons barred","Light assault weapons barred","Concealable weapons barred","Most firearms barred","Shotguns also barred","Bladed weapons also restricted","Weapons outside the home prohibited"};
  d.put("Law level",digit(w.law)+" · "+laws[Math.min(w.law,9)]+"; enforcement throw 2D ≥ "+w.law);
  d.put("Tech level",digit(w.tech)+" · "+(w.tech<technology.length?technology[w.tech]:"Beyond the basic technology chart"));return d;
 }
}

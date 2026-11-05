import com.javasmyths.shipbuilder.*;
public class Book2Check {
 static void check(boolean b,String message){if(!b)throw new AssertionError(message);}
 static void equals(double a,double b,String message){check(Math.abs(a-b)<1e-6,message+": "+a+" != "+b);}
 public static void main(String[]args){
  Book2Rules.Report scout=Book2Rules.calculate(ShipDesign.scout());check(scout.valid,scout.errors.toString());equals(scout.cargo,3,"Scout cargo");equals(scout.fuel,40,"Scout fuel");equals(scout.totalPrice,29430000,"Scout cost");check(scout.jump==2&&scout.maneuver==2&&scout.months==9,"Scout performance/time");
  ShipDesign trader=ShipDesign.trader();Book2Rules.Report free=Book2Rules.calculate(trader);check(free.valid,free.errors.toString());equals(free.cargo,82,"Trader cargo");equals(free.totalPrice,37080000,"Trader price");check(free.fullCrew==4,"Trader crew");
  trader.jump="B";check(!Book2Rules.calculate(trader).valid,"Plant letter failure");trader.power="B";trader.hullType="custom";check(!Book2Rules.calculate(trader).valid,"Computer jump failure");trader.computer="1bis";check(Book2Rules.calculate(trader).valid,"Bis jump support");
  trader.fuel=1;check(!Book2Rules.calculate(trader).valid,"Fuel too small");trader.fuel=-1;trader.tripleTurrets=1;trader.hardpoints=0;check(!Book2Rules.calculate(trader).valid,"Hardpoint validation");
  ShipDesign custom=new ShipDesign();custom.hullType="custom";custom.hull=300;custom.jump="B";custom.maneuver="B";custom.power="B";custom.staterooms=4;custom.crew=4;custom.computer="1";custom.doubleTurrets=0;custom.hardpoints=0;
  Book2Rules.Report c=Book2Rules.calculate(custom);check(c.valid,c.errors.toString());check(c.jump==1,"Intermediate hull rounds up");check(c.months==16,"Custom construction time");
  custom.hull=99;check(!Book2Rules.calculate(custom).valid,"Small craft excluded");custom.hull=5001;check(!Book2Rules.calculate(custom).valid,"Maximum hull");custom.hull=300;custom.name="<script>test</script>";custom.vehicles.put("unknown",1);check(!Book2Rules.calculate(custom).valid,"Unknown vehicle rejected");
  ShipDesign nonstar=new ShipDesign();nonstar.jump="None";nonstar.maneuver="A";nonstar.power="A";nonstar.doubleTurrets=0;Book2Rules.Report n=Book2Rules.calculate(nonstar);check(n.valid,n.errors.toString());equals(n.fuel,20,"Nonstar plant fuel");
  check(Book2Rules.rating("Z",5000)==2&&Book2Rules.rating("W",1000)==5,"High-letter table values");
  nonstar.hullType="custom";nonstar.hull=300;nonstar.crew=2;check(Book2Rules.calculate(nonstar).positions.get("Navigator")==0,"Non-starship needs no jump navigator");
  ShipDesign overflow=ShipDesign.scout();overflow.staterooms=99;check(!Book2Rules.calculate(overflow).valid,"Compartment overflow");
  ShipDesign weapons=ShipDesign.scout();weapons.beamLasers=3;check(!Book2Rules.calculate(weapons).valid,"Weapon mount overflow");
  ShipDesign occupancy=ShipDesign.trader();occupancy.staterooms=1;check(!Book2Rules.calculate(occupancy).valid,"Accommodation shortage");
  custom.hull=300;custom.name="Finite check";custom.fuel=Double.NaN;check(!Book2Rules.calculate(custom).valid,"Nonfinite rejected");
  System.out.println("PASS: Book2 1981 Scout and Free Trader benchmarks; drives, fuel, bis computer, hull rounding, crew, hardpoints, unknown vehicles, non-starships, limits, finite values.");
 }
}

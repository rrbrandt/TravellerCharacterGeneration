import com.javasmyths.TravellerCharacterGeneration.model.*;
public class CareerProgressCheck {
  static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
  static TravellerCharacter character(){TravellerCharacter c=new TravellerCharacter();c.setAge(18);return c;}
  static void finish(CareerProgress p,TravellerCharacter c){while(p.isAdvancementPending())p.advance("skip",2,c);while(p.getRollsRemaining()>0)p.recordRoll(1,RolableSkills.Admin,c);}
  public static void main(String[]args){
    int[] targets={6,6,7,3,4,5};
    Service[] services={Service.Navy,Service.Marines,Service.Army,Service.Scouts,Service.Merchants,Service.Other};
    for(int i=0;i<services.length;i++){
      CareerProgress p=new CareerProgress();p.start(services[i]);TravellerCharacter c=character();
      check(p.getRollAllowance()==2,"first term allowance");check(p.getReenlistTarget()==targets[i],"target");
      p.decide(true,12);check(p.getTerm()==1,"early decision");
      finish(p,c);check(c.getAge()==22&&p.getCompletedTerms()==1,"age and term");
      try{p.recordRoll(1,RolableSkills.Admin,c);throw new AssertionError("extra roll accepted");}catch(IllegalStateException expected){}
      p.decide(true,targets[i]);check(p.getTerm()==2&&!p.isMusteredOut(),"reenlist success");
      check(p.getRollAllowance()==(services[i]==Service.Scouts?2:1),"later term allowance");
      finish(p,c);p.decide(false,12);check(p.getTerm()==3&&!p.isMusteredOut(),"mandatory reenlist");
      finish(p,c);p.decide(true,2);check(p.isMusteredOut(),"failed reenlist");
      CareerProgress retired=new CareerProgress();retired.start(services[i]);TravellerCharacter rc=character();
      for(int term=1;term<7;term++){finish(retired,rc);retired.decide(true,12);}
      finish(retired,rc);retired.decide(true,11);check(retired.isMusteredOut(),"seven term retirement");
    }
    CareerProgress p=new CareerProgress();p.start(Service.Navy);finish(p,character());p.decide(false,6);check(p.isMusteredOut(),"voluntary muster");
    System.out.println("PASS: all service allowances, targets, exhausted rolls, age, early actions, failed/mandatory reenlistment, seven-term retirement, muster out.");
  }
}
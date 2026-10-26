import com.javasmyths.TravellerCharacterGeneration.model.*;
public class BenefitProgressCheck {
 static void check(boolean b,String m){if(!b)throw new AssertionError(m);}
 static void finish(CareerProgress p,TravellerCharacter c){while(p.isAdvancementPending())p.advance("skip",2,c);while(p.getRollsRemaining()>0)p.recordRoll(1,RolableSkills.Gambling,c);}
 public static void main(String[]a){for(Service s:Service.values()){
  CareerProgress p=new CareerProgress();p.start(s);TravellerCharacter c=new TravellerCharacter(18);c.setCharacteristics(new Characteristics());
  p.rollBenefit("cash",1,false,c);check(p.getBenefitRollsUsed()==0,"locked before muster");
  for(int i=0;i<3;i++){finish(p,c);p.decide(true,12);}finish(p,c);p.decide(false,2);
  check(p.getBenefitRollsRemaining()==4,"allowance");
  for(int i=0;i<3;i++)p.rollBenefit("cash",6,true,c);
  check(p.getCredits()>0&&p.getCashRollsUsed()==3,"cash");p.rollBenefit("cash",1,false,c);check(p.getBenefitRollsUsed()==3,"cash cap");
  int before=c.getCharacteristics().getIntelligence();p.rollBenefit("material",2,false,c);check(c.getCharacteristics().getIntelligence()>before,"stat benefit");
  check(p.getBenefitRollsRemaining()==0,"exhausted");p.rollBenefit("material",1,false,c);check(p.getBenefits().size()==4,"extra benefit rejected");
 }System.out.println("PASS: all service benefits, eligibility, allowance, cash cap, Gambling modifier, stat awards, exhaustion.");}
}
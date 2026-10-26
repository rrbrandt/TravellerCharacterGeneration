import com.javasmyths.TravellerCharacterGeneration.model.*;
public class AdvancementCheck {
 static void ok(boolean v,String m){if(!v)throw new AssertionError(m);}
 static TravellerCharacter character(){TravellerCharacter c=new TravellerCharacter(18);Characteristics s=new Characteristics();s.setEducation(9);s.setEndurance(8);s.setIntelligence(10);s.setSocialStanding(9);c.setCharacteristics(s);return c;}
 static void finish(CareerProgress p,TravellerCharacter c){while(p.isAdvancementPending())p.advance("skip",2,c);while(p.getRollsRemaining()>0)p.recordRoll(1,RolableSkills.Admin,c);}
 public static void main(String[]a){
 Service[] services={Service.Navy,Service.Marines,Service.Army,Service.Merchants};int[] commission={10,9,5,4},promotion={8,9,6,10};
 for(int i=0;i<services.length;i++){
  CareerProgress p=new CareerProgress();TravellerCharacter c=character();p.enlist(services[i],false);p.serviceAwards();
  ok(p.getCommissionTarget()==commission[i]&&p.getPromotionTarget()==promotion[i],"targets");
  ok(p.commissionModifier(c.getCharacteristics())==1&&p.promotionModifier(c.getCharacteristics())==1,"modifiers");
  try{p.recordRoll(1,RolableSkills.Admin,c);throw new AssertionError("skills before advancement");}catch(IllegalStateException expected){}
  p.advance("commission",commission[i]-1,c);ok(p.getRank()==1&&p.getRollAllowance()==3,"commission bonus");
  p.advance("commission",12,c);ok(p.getRank()==1,"duplicate commission");
  p.advance("promotion",promotion[i]-1,c);ok(p.getRank()==2&&p.getRollAllowance()==4,"promotion bonus");
  p.advance("promotion",12,c);ok(p.getRank()==2,"duplicate promotion");
  finish(p,c);ok(c.getAge()==22&&p.getCompletedTerms()==1,"completion once");
  while(p.getRank()<p.getMaxRank()){
   p.decide(true,12);ok(p.getBonusSkillRolls()==0,"bonus reset");p.advance("promotion",12,c);finish(p,c);
  }
  ok(!p.isCanPromote(),"rank cap");p.decide(false,2);ok(p.getRankBenefitBonus()==3&&p.getBenefitRollAllowance()==p.getCompletedTerms()+3,"benefit bonus");
  int social=c.getCharacteristics().getSocialStanding();String result=p.rollBenefit("material",6,false,true,c);
  ok(result.contains(services[i]==Service.Merchants?"Free Trader":"Social Standing"),"seventh material row");
  if(services[i]!=Service.Merchants)ok(c.getCharacteristics().getSocialStanding()>social,"social award");
  CareerProgress d=new CareerProgress();TravellerCharacter dc=character();d.enlist(services[i],true);ok(!d.isCanCommission(),"draft first term");finish(d,dc);d.decide(true,12);ok(d.isCanCommission(),"draft next term");
  CareerProgress f=new CareerProgress();TravellerCharacter fc=character();f.start(services[i]);f.advance("commission",2,fc);ok(f.getRank()==0&&!f.isCanCommission()&&f.getRollAllowance()==2,"failed commission");f.advance("commission",12,fc);ok(f.getRank()==0,"failed commission retry");
 }
 for(Service s:new Service[]{Service.Scouts,Service.Other}){CareerProgress p=new CareerProgress();p.start(s);ok(!p.isAdvancementPending(),"unranked eligibility");p.advance("commission",12,character());ok(p.getRank()==0,"unranked rank");}
 CareerProgress p=new CareerProgress();TravellerCharacter c=character();p.start(Service.Army);p.advance("commission",12,c);p.advance("promotion",2,c);ok(p.getRank()==1&&!p.isCanPromote()&&p.getBonusSkillRolls()==1,"failed promotion");
 c.getCharacteristics().setEducation(6);c.getCharacteristics().setEndurance(6);c.getCharacteristics().setIntelligence(5);c.getCharacteristics().setSocialStanding(7);
 for(Service s:services){CareerProgress q=new CareerProgress();q.start(s);ok(q.commissionModifier(c.getCharacteristics())==0&&q.promotionModifier(c.getCharacteristics())==0,"modifier threshold");}
 System.out.println("PASS: all targets/modifiers, commission and promotion, failure, draft restriction, one attempt/term, skill bonuses, rank caps, rank benefits and seventh-row awards.");
 }
}
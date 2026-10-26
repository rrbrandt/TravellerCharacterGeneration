import com.javasmyths.TravellerCharacterGeneration.model.*;
public class FinalRecordCheck {
 static void ok(boolean b,String m){if(!b)throw new AssertionError(m);}
 static TravellerCharacter character(){TravellerCharacter c=new TravellerCharacter(18);c.setCharacteristics(new Characteristics());return c;}
 public static void main(String[]args){
  CareerProgress p=new CareerProgress();TravellerCharacter c=character();p.enlist(Service.Army,false);p.serviceAwards();p.serviceAwards();ok(p.getSkillLevels().get("Rifle")==1,"automatic awards duplicate");
  p.advance("skip",2,c);p.recordRoll(1,RolableSkills.Admin,c);p.recordRoll(1,RolableSkills.Admin,c);ok(p.getSkillLevels().get("Administration")==2,"skill totals");ok(p.getCompletedTerms()==1&&c.getAge()==22,"term summary");
  p.decide(false,2);ok(!p.isGenerationComplete(),"early completion");p.rollBenefit("cash",1,false,c);ok(p.isGenerationComplete()&&p.getCredits()==2000,"complete reward summary");
  ok(p.getFullHistory().size()==p.getHistory().size()+p.getBenefits().size(),"full history");ok(p.getFullHistory().get(0).contains("Enlisted"),"enlist history");ok(p.getAdvancementHistory().size()==1,"advancement history");
  int size=p.getFullHistory().size();p.getFullHistory();ok(p.getFullHistory().size()==size,"read-only history");
  CareerProgress s=new CareerProgress();TravellerCharacter sc=character();s.start(Service.Scouts);int before=sc.getCharacteristics().getStrength();s.recordRoll(1,RolableSkills.PLUS_STREN,sc);ok(sc.getCharacteristics().getStrength()==before+1,"characteristic awards");ok(!s.getSkillLevels().containsKey("PLUS_STREN"),"stats misclassified as skills");
  CareerProgress m=new CareerProgress();TravellerCharacter mc=character();m.start(Service.Marines);m.serviceAwards();m.advance("commission",12,mc);m.advance("promotion",12,mc);ok(m.getSkillLevels().get("Cutlass")==1&&m.getSkillLevels().get("Revolver")==1,"rank/service skills");ok(m.getAdvancementHistory().size()==2,"promotion summary");
  System.out.println("PASS: skill aggregation, automatic skills, characteristic awards, complete history, promotion records, benefit summary and completion state.");
 }
}
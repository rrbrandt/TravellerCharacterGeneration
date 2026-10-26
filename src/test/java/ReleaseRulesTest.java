import org.junit.jupiter.api.Test;
public class ReleaseRulesTest {
  @Test void careerRules() { CareerProgressCheck.main(new String[0]); }
  @Test void advancementRules() { AdvancementCheck.main(new String[0]); }
  @Test void benefitRules() { BenefitProgressCheck.main(new String[0]); }
  @Test void finalRecord() { FinalRecordCheck.main(new String[0]); }
}

package com.javasmyths.traveller.handlingformsubmission;

import com.javasmyths.traveller.model.Characteristics;
import com.javasmyths.traveller.model.Service;
import com.javasmyths.traveller.model.ServiceSkills;
import com.javasmyths.traveller.model.SkillTable;
import com.javasmyths.traveller.model.RollableSkill;
import com.javasmyths.traveller.model.SkillsForService;
import com.javasmyths.traveller.model.TravellerCharacter;
import com.javasmyths.traveller.model.MusterBenefit;
import com.javasmyths.traveller.model.MusteringOutTable;
import com.javasmyths.traveller.model.MusteringOutTables;
import com.javasmyths.traveller.services.Dice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@SessionAttributes("character")
public class CharacterController {
  private static final Logger LOGGER = LoggerFactory.getLogger(CharacterController.class);

  private final Dice dice;
  private final ServiceSkills serviceSkills;
  private final MusteringOutTables musteringOutTables;

  public CharacterController() {
    this(new Dice(), new ServiceSkills());
  }

  CharacterController(Dice dice, ServiceSkills serviceSkills) {
    this.dice = dice;
    this.serviceSkills = serviceSkills;
    this.musteringOutTables = new MusteringOutTables();
  }

  @ModelAttribute("character")
  public TravellerCharacter character() {
    return new TravellerCharacter();
  }

  @GetMapping({"/", "/newcharacter"})
  public String newCharacter(Model model) {
    model.addAttribute("character", new TravellerCharacter());
    return "newcharacter";
  }

  @PostMapping("/newcharacter")
  public String createCharacter(@ModelAttribute("character") TravellerCharacter character,
      BindingResult bindingResult) {
    validateIdentity(character, bindingResult);
    if (bindingResult.hasErrors()) {
      return "newcharacter";
    }
    character.setCharacteristics(new Characteristics(dice));
    character.resetCareer();
    LOGGER.info("Started character generation");
    return "characteristics";
  }

  @GetMapping("/characteristics")
  public String characteristics(@ModelAttribute("character") TravellerCharacter character) {
    if (character.getCharacteristics() == null) {
      character.setCharacteristics(new Characteristics(dice));
    }
    return "characteristics";
  }

  @PostMapping(value = "/characteristics", params = "action=reroll")
  public String reroll(@ModelAttribute("character") TravellerCharacter character) {
    character.setCharacteristics(new Characteristics(dice));
    return "characteristics";
  }

  @PostMapping(value = "/characteristics", params = "action=continue")
  public String acceptCharacteristics(@ModelAttribute("character") TravellerCharacter character,
      BindingResult bindingResult, Model model) {
    validateIdentity(character, bindingResult);
    validateCharacteristics(character.getCharacteristics(), bindingResult);
    if (bindingResult.hasErrors()) {
      return "characteristics";
    }
    addServices(model);
    return "selectservice";
  }

  @GetMapping("/service")
  public String service(@ModelAttribute("character") TravellerCharacter character, Model model) {
    if (character.getCharacteristics() == null) {
      return "redirect:/characteristics";
    }
    addServices(model);
    return "selectservice";
  }

  @PostMapping("/service")
  public String selectService(@ModelAttribute("character") TravellerCharacter character,
      @RequestParam String service, @RequestParam String action, Model model) {
    Service requested;
    try {
      requested = Service.valueOf(service);
    } catch (IllegalArgumentException exception) {
      model.addAttribute("serviceError", "Choose a valid service.");
      addServices(model);
      return "selectservice";
    }

    if ("accept".equals(action)) {
      character.setService(requested);
      character.setEnlistmentResult("You joined the " + requested.getDisplayName() + ".");
    } else if ("attempt".equals(action)) {
      attemptEnlistment(character, requested);
    } else {
      model.addAttribute("serviceError", "Choose either Join or Attempt enlistment.");
      addServices(model);
      return "selectservice";
    }

    character.prepareCareer();
    resolveTerm(character);

    addSkillTables(character, model);
    LOGGER.info("Character generation completed with service {}", character.getService());
    return "result";
  }

  @GetMapping("/result")
  public String result(@ModelAttribute("character") TravellerCharacter character, Model model) {
    if (character.getService() == null) {
      return "redirect:/service";
    }
    addSkillTables(character, model);
    return "result";
  }

  @GetMapping("/backstory")
  public String backstory(@ModelAttribute("character") TravellerCharacter character) {
    if (!isCareerComplete(character)) {
      return "redirect:/result";
    }
    return "backstory";
  }

  @PostMapping("/backstory")
  public String saveBackstory(@ModelAttribute("character") TravellerCharacter character,
      BindingResult bindingResult) {
    if (!isCareerComplete(character)) {
      return "redirect:/result";
    }
    if (character.getBackstory() != null && character.getBackstory().length() > 2000) {
      bindingResult.rejectValue("backstory", "backstory.length",
          "Keep the backstory to 2,000 characters or fewer.");
      return "backstory";
    }
    return "charactersheet";
  }

  @GetMapping("/charactersheet")
  public String characterSheet(@ModelAttribute("character") TravellerCharacter character) {
    if (!isCareerComplete(character)) {
      return "redirect:/result";
    }
    return "charactersheet";
  }

  @PostMapping("/skill")
  public String rollSkill(@ModelAttribute("character") TravellerCharacter character,
      @RequestParam String table, Model model) {
    if (character.getService() == null || character.getCharacteristics() == null) {
      return "redirect:/service";
    }

    if (!character.isCareerActive() || character.getSkillRollsRemaining() <= 0) {
      model.addAttribute("skillError", "No skill rolls remain for this term.");
      addSkillTables(character, model);
      return "result";
    }

    SkillTable selectedTable;
    try {
      selectedTable = SkillTable.valueOf(table);
    } catch (IllegalArgumentException exception) {
      model.addAttribute("skillError", "Choose a valid skill table.");
      addSkillTables(character, model);
      return "result";
    }

    if (selectedTable.isRequiresEducationEight()
        && character.getCharacteristics().getEducation() < 8) {
      model.addAttribute("skillError", "Advanced education requires Education 8 or higher.");
      addSkillTables(character, model);
      return "result";
    }

    int roll = dice.rollDie();
    SkillsForService tables = serviceSkills.get(character.getService());
    RollableSkill result = selectedTable.skillsFrom(tables)[roll - 1];
    String award = applySkillResult(character, result);
    character.useSkillRoll();
    character.setLastSkillResult("Rolled " + roll + " on " + selectedTable.getDisplayName()
        + ": " + award + " " + character.getSkillRollsRemaining() + " skill roll(s) remain.");
    addSkillTables(character, model);
    return "result";
  }

  @PostMapping("/career/reenlist")
  public String reenlist(@ModelAttribute("character") TravellerCharacter character, Model model) {
    if (!canMakeCareerDecision(character)) {
      model.addAttribute("careerError", "Finish the current term's skill rolls first.");
      addSkillTables(character, model);
      return "result";
    }
    int roll = dice.rollTwoDice();
    int target = character.getService().getReenlistmentTarget();
    if (roll < target) {
      character.setCareerActive(false);
      character.setLastTermResult("Reenlistment failed: rolled " + roll + " against " + target
          + "+. You mustered out after " + character.getTermsServed() + " term(s).");
      beginMusteringOut(character);
    } else {
      resolveTerm(character);
    }
    addSkillTables(character, model);
    return "result";
  }

  @PostMapping("/career/leave")
  public String leaveService(@ModelAttribute("character") TravellerCharacter character, Model model) {
    if (!canMakeCareerDecision(character)) {
      model.addAttribute("careerError", "Finish the current term's skill rolls first.");
    } else {
      character.setCareerActive(false);
      character.setLastTermResult("You left the " + character.getService().getDisplayName()
          + " after " + character.getTermsServed() + " term(s) of service.");
      beginMusteringOut(character);
    }
    addSkillTables(character, model);
    return "result";
  }

  @PostMapping("/muster")
  public String rollMusterBenefit(@ModelAttribute("character") TravellerCharacter character,
      @RequestParam String type, Model model) {
    if (!character.isMusteringOut()) {
      model.addAttribute("musterError", "No mustering-out rolls are available.");
      addSkillTables(character, model);
      return "result";
    }

    MusteringOutTable table = musteringOutTables.get(character.getService());
    int die = dice.rollDie();
    if ("cash".equals(type)) {
      if (character.getCashRollsTaken() >= 3) {
        model.addAttribute("musterError", "Only three rolls may be made on the cash table.");
      } else {
        int modifier = character.getSkills().containsKey(RollableSkill.GAMBLING) ? 1 : 0;
        int result = Math.min(7, die + modifier);
        int cash = table.cashFor(result);
        character.addCashBenefit(cash);
        character.setLastMusterResult("Cash roll: " + die + formatModifier(modifier)
            + " = " + result + ", received Cr" + String.format("%,d", cash) + ".");
      }
    } else if ("material".equals(type)) {
      int modifier = character.getRank() >= 5 ? 1 : 0;
      int result = Math.min(7, die + modifier);
      MusterBenefit benefit = table.materialFor(result);
      String award = applyMusterBenefit(character, benefit);
      character.setLastMusterResult("Material roll: " + die + formatModifier(modifier)
          + " = " + result + ", " + award);
    } else {
      model.addAttribute("musterError", "Choose cash or a material benefit.");
    }
    addSkillTables(character, model);
    return "result";
  }

  private void beginMusteringOut(TravellerCharacter character) {
    int rankBonus = character.getRank() <= 0 ? 0
        : character.getRank() <= 2 ? 1
        : character.getRank() <= 4 ? 2 : 3;
    int rolls = character.getTermsServed() + rankBonus;
    int pension = character.getTermsServed() < 5
        ? 0 : 4000 + ((character.getTermsServed() - 5) * 2000);
    character.startMusteringOut(rolls, pension);
  }

  private String applyMusterBenefit(TravellerCharacter character, MusterBenefit benefit) {
    Characteristics values = character.getCharacteristics();
    String description = benefit.getDisplayName();
    switch (benefit) {
      case PLUS_ONE_INTELLIGENCE -> values.setIntelligence(Math.min(15,
          values.getIntelligence() + 1));
      case PLUS_TWO_INTELLIGENCE -> values.setIntelligence(Math.min(15,
          values.getIntelligence() + 2));
      case PLUS_ONE_EDUCATION -> values.setEducation(Math.min(15, values.getEducation() + 1));
      case PLUS_TWO_EDUCATION -> values.setEducation(Math.min(15, values.getEducation() + 2));
      case PLUS_ONE_SOCIAL -> values.setSocialStanding(Math.min(15,
          values.getSocialStanding() + 1));
      case PLUS_TWO_SOCIAL -> values.setSocialStanding(Math.min(15,
          values.getSocialStanding() + 2));
      case BLADE -> {
        if (character.getMusterBenefits().contains("Blade")) {
          int level = character.addSkill(RollableSkill.BLADE_COMBAT);
          description = "Blade Combat increased to level " + level;
        }
      }
      case GUN -> {
        if (character.getMusterBenefits().contains("Gun")) {
          int level = character.addSkill(RollableSkill.GUN_COMBAT);
          description = "Gun Combat increased to level " + level;
        }
      }
      default -> { }
    }
    character.addMaterialBenefit(description);
    return description + ".";
  }

  private String formatModifier(int modifier) {
    return modifier == 0 ? "" : " + " + modifier + " DM";
  }

  private boolean canMakeCareerDecision(TravellerCharacter character) {
    return character.getService() != null && character.isAlive()
        && character.isCareerActive() && character.getSkillRollsRemaining() == 0;
  }

  private boolean isCareerComplete(TravellerCharacter character) {
    return character.getService() != null && !character.isCareerActive()
        && (!character.isAlive() || character.getMusterRollsRemaining() == 0);
  }

  private void resolveTerm(TravellerCharacter character) {
    Service service = character.getService();
    Characteristics values = character.getCharacteristics();
    int term = character.getTermsServed() + 1;
    int survivalRoll = dice.rollTwoDice();
    int survivalModifier = service.survivalModifier(values);
    int survivalTotal = survivalRoll + survivalModifier;
    character.setTermsServed(term);
    character.setAge(character.getAge() + 4);

    if (survivalTotal < service.getSurvivalTarget()) {
      character.setAlive(false);
      character.setCareerActive(false);
      character.setSkillRollsRemaining(0);
      character.addTermResult("Term " + term + ": survival failed with " + survivalRoll
          + " + " + survivalModifier + " DM = " + survivalTotal + " against "
          + service.getSurvivalTarget() + "+. The character died in service.");
      return;
    }

    int skillRolls = term == 1 ? 2 : 1;
    StringBuilder result = new StringBuilder("Term ").append(term)
        .append(": survived with ").append(survivalRoll).append(" + ")
        .append(survivalModifier).append(" DM = ").append(survivalTotal).append(".");

    if (!character.isCommissioned() && service.getCommissionTarget() > 0) {
      int commissionRoll = dice.rollTwoDice();
      int modifier = service.commissionModifier(values);
      if (commissionRoll + modifier >= service.getCommissionTarget()) {
        character.setCommissioned(true);
        character.setRank(1);
        skillRolls++;
        result.append(" Commission earned (+1 skill roll). ");
      } else {
        result.append(" Commission not earned. ");
      }
    }

    if (character.isCommissioned() && service.getPromotionTarget() > 0) {
      int promotionRoll = dice.rollTwoDice();
      int modifier = service.promotionModifier(values);
      if (promotionRoll + modifier >= service.getPromotionTarget()) {
        character.setRank(character.getRank() + 1);
        skillRolls++;
        result.append(" Promoted to officer rank ").append(character.getRank())
            .append(" (+1 skill roll). ");
      } else {
        result.append(" No promotion. ");
      }
    }

    result.append(skillRolls).append(" skill roll(s) available.");
    character.setCareerActive(true);
    character.setSkillRollsRemaining(skillRolls);
    character.setLastSkillResult(null);
    character.addTermResult(result.toString());
  }

  private String applySkillResult(TravellerCharacter character, RollableSkill result) {
    Characteristics values = character.getCharacteristics();
    switch (result) {
      case PLUS_STRENGTH -> values.setStrength(values.getStrength() + 1);
      case PLUS_DEXTERITY -> values.setDexterity(values.getDexterity() + 1);
      case PLUS_ENDURANCE -> values.setEndurance(values.getEndurance() + 1);
      case PLUS_INTELLIGENCE -> values.setIntelligence(values.getIntelligence() + 1);
      case PLUS_EDUCATION -> values.setEducation(values.getEducation() + 1);
      case PLUS_SOCIAL -> values.setSocialStanding(values.getSocialStanding() + 1);
      case MINUS_SOCIAL -> values.setSocialStanding(Math.max(1, values.getSocialStanding() - 1));
      default -> {
        int level = character.addSkill(result);
        return result.getDisplayName() + " gained at level " + level + ".";
      }
    }
    return result.getDisplayName() + " applied. Your UPP is now " + values.getUpp() + ".";
  }

  private void addSkillTables(TravellerCharacter character, Model model) {
    model.addAttribute("skillTables", serviceSkills.get(character.getService()));
    model.addAttribute("skillTableOptions", SkillTable.values());
    model.addAttribute("musterTable", musteringOutTables.get(character.getService()));
  }

  private void attemptEnlistment(TravellerCharacter character, Service requested) {
    int roll = dice.rollTwoDice();
    int modifier = requested.enlistmentModifier(character.getCharacteristics());
    int total = roll + modifier;
    if (total >= requested.getEnlistmentTarget()) {
      character.setService(requested);
      character.setEnlistmentResult("Enlistment succeeded: rolled " + roll + " + " + modifier
          + " DM = " + total + " against " + requested.getEnlistmentTarget() + "+.");
      return;
    }

    Service drafted = Service.values()[dice.rollDie() - 1];
    character.setService(drafted);
    character.setEnlistmentResult("Enlistment in the " + requested.getDisplayName()
        + " failed: rolled " + roll + " + " + modifier + " DM = " + total
        + ". The draft assigned you to the " + drafted.getDisplayName() + ".");
  }

  private void addServices(Model model) {
    model.addAttribute("services", Service.values());
  }

  private void validateIdentity(TravellerCharacter character, BindingResult errors) {
    if (character.getName() == null || character.getName().isBlank()) {
      errors.rejectValue("name", "name.required", "Enter a character name.");
    } else if (character.getName().length() > 80) {
      errors.rejectValue("name", "name.length", "Name must be 80 characters or fewer.");
    }
    if (character.getAge() < 18 || character.getAge() > 100) {
      errors.rejectValue("age", "age.range", "Age must be between 18 and 100.");
    }
  }

  private void validateCharacteristics(Characteristics value, BindingResult errors) {
    if (value == null) {
      errors.reject("characteristics.required", "Roll characteristics before continuing.");
      return;
    }
    validateRange("characteristics.strength", value.getStrength(), errors);
    validateRange("characteristics.dexterity", value.getDexterity(), errors);
    validateRange("characteristics.endurance", value.getEndurance(), errors);
    validateRange("characteristics.intelligence", value.getIntelligence(), errors);
    validateRange("characteristics.education", value.getEducation(), errors);
    validateRange("characteristics.socialStanding", value.getSocialStanding(), errors);
  }

  private void validateRange(String field, int value, BindingResult errors) {
    if (value < 2 || value > 12) {
      errors.rejectValue(field, "characteristic.range", "Characteristics must be between 2 and 12.");
    }
  }
}

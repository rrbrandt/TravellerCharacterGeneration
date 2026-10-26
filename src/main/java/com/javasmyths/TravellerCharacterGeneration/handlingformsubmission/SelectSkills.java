package com.javasmyths.TravellerCharacterGeneration.handlingformsubmission;

import com.javasmyths.TravellerCharacterGeneration.model.Service;
import com.javasmyths.TravellerCharacterGeneration.services.CharacterServices;
import com.javasmyths.TravellerCharacterGeneration.services.Dice;
import com.javasmyths.TravellerCharacterGeneration.model.Characteristics;
import com.javasmyths.TravellerCharacterGeneration.model.TravellerCharacter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SelectSkills {

  Dice dice = new Dice();

  @GetMapping("/selectskills")
  public String greetingForm(@ModelAttribute("character") TravellerCharacter character, Model model) {
    return greetingSubmit(character, model);
  }

  @PostMapping("/selectskills")
  public String greetingSubmit(@ModelAttribute("character") TravellerCharacter character, Model model) {
    model.addAttribute("character", character);
    if (character.getService() == null) return "skills-pending";
    return new SelectService().greetingSubmit(character, model);
  }
  @RequestMapping(value = "/selectskills", method = RequestMethod.POST, params = "attempt")
  public String reroll(@ModelAttribute("character") TravellerCharacter character, Model model,
          @RequestParam(value = "selectSkillTable", required = false) String table) {
    if (character.getService() == null) return "skills-pending";
    String view = new SelectService().greetingSubmit(character, model);
    com.javasmyths.TravellerCharacterGeneration.model.CareerProgress career =
        (com.javasmyths.TravellerCharacterGeneration.model.CareerProgress) model.getAttribute("career");
    if (career.isAdvancementPending()) {
      model.addAttribute("result", "Roll or skip your commission/promotion attempt before choosing skills.");
      return view;
    }
    if (career.isMusteredOut() || career.isTermComplete()) {
      model.addAttribute("result", "No skill rolls remain. Choose Re-enlist or Muster out.");
      return view;
    }
    com.javasmyths.TravellerCharacterGeneration.model.SkillsForService skills =
        new com.javasmyths.TravellerCharacterGeneration.model.ServiceSkills().getSkillsForService().get(character.getService());
    com.javasmyths.TravellerCharacterGeneration.model.RolableSkills[] choices;
    if (table == null) {
      model.addAttribute("result", "Choose a skill category before rolling.");
      return view;
    }
    switch (table) {
      case "PersonalDevelopment": choices = skills.getPersonalDevelopment(); break;
      case "ServiceSkills": choices = skills.getServiceSkills(); break;
      case "AdvanceEducation": choices = skills.getAdvancedEducation(); break;
      case "AdvanceEducationPlus":
        if (character.getCharacteristics().getEducation() < 8) {
          model.addAttribute("result", "Education 8 or higher is required for this category.");
          return view;
        }
        choices = skills.getAdvancedEducationPlusEduc(); break;
      default:
        model.addAttribute("result", "Choose a valid skill category before rolling.");
        return view;
    }
    int roll = dice.roleDie();
    career.recordRoll(roll, choices[roll - 1], character);
    model.addAttribute("result", career.getMessage());
    return view;
  }
  @PostMapping(value = "/selectskills", params = "careerAction")
  public String careerAction(@ModelAttribute("character") TravellerCharacter character, Model model,
      @RequestParam String careerAction) {
    if (character.getService() == null) return "skills-pending";
    String view = new SelectService().greetingSubmit(character, model);
    com.javasmyths.TravellerCharacterGeneration.model.CareerProgress career =
        (com.javasmyths.TravellerCharacterGeneration.model.CareerProgress) model.getAttribute("career");
    if (!"reenlist".equals(careerAction) && !"muster".equals(careerAction)) {
      model.addAttribute("result", "Choose Re-enlist or Muster out.");
      return view;
    }
    if (career.isMusteredOut()) return view;
    career.decide("reenlist".equals(careerAction), dice.role2Dice());
    model.addAttribute("result", career.getMessage());
    if (career.isMusteredOut()) return "redirect:/musterout";
    return view;
  }
  @PostMapping(value = "/selectskills", params = "advancementAction")
  public String advancement(@ModelAttribute("character") TravellerCharacter character, Model model,
      @RequestParam String advancementAction) {
    if (character.getService() == null) return "skills-pending";
    String view = new SelectService().greetingSubmit(character, model);
    com.javasmyths.TravellerCharacterGeneration.model.CareerProgress career =
        (com.javasmyths.TravellerCharacterGeneration.model.CareerProgress) model.getAttribute("career");
    model.addAttribute("result", career.advance(advancementAction, dice.role2Dice(), character));
    return view;
  }
}
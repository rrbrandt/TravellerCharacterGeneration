package com.javasmyths.TravellerCharacterGeneration.handlingformsubmission;

import com.javasmyths.TravellerCharacterGeneration.model.Service;
import com.javasmyths.TravellerCharacterGeneration.model.ServiceSkills;
import com.javasmyths.TravellerCharacterGeneration.model.Skills;
import com.javasmyths.TravellerCharacterGeneration.model.SkillsForService;
import com.javasmyths.TravellerCharacterGeneration.services.CharacterServices;
import com.javasmyths.TravellerCharacterGeneration.services.Dice;
import com.javasmyths.TravellerCharacterGeneration.model.TravellerCharacter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SelectService {

  Dice dice = new Dice();

  @GetMapping("/selectservice")
  public String greetingForm(@ModelAttribute("character") TravellerCharacter character, Model model) {
    System.out.println("****************************************");
    System.out.println("selectservice - get");
    System.out.println("****************************************");
    model.addAttribute("character", character);
    System.out.println("Character: " + model.getAttribute("character"));
    return "selectservice";
  }

  @PostMapping("/selectservice")
  public String greetingSubmit(@ModelAttribute("character") TravellerCharacter character, Model model) {
    System.out.println("****************************************");
    System.out.println("selectservice - post");
    System.out.println("****************************************");
    model.addAttribute("character", character);
    System.out.println("Character: " + character);
    com.javasmyths.TravellerCharacterGeneration.model.CareerProgress career =
        (com.javasmyths.TravellerCharacterGeneration.model.CareerProgress) model.getAttribute("career");
    if (career != null) {
      career.start(character.getService());
      career.serviceAwards();
      if (career.getService() != null) character.setService(career.getService());
    }
    List<Skills> skills = new ArrayList();

    ServiceSkills serviceSkills = new ServiceSkills();
    SkillsForService skillsForService = serviceSkills.getSkillsForService().get(character.getService());

    for (int i = 0; i < 6; i++) {
      skills.add(new Skills(String.valueOf(i + 1),
              (skillsForService.getPersonalDevelopment())[i].toString(),
              (skillsForService.getServiceSkills())[i].toString(),
              (skillsForService.getAdvancedEducation())[i].toString(),
              (skillsForService.getAdvancedEducationPlusEduc())[i].toString())
      );

      System.out.println("Skills[" + i + "] = " + skills.get(i));
    }
    model.addAttribute("skills", skills);
    model.addAttribute("acceptButton", "hidden");
    model.addAttribute("attemptButton", "display");
    model.addAttribute("continueButton", "hidden");
    model.addAttribute("character", character);
    return "selectskills";
  }

  @RequestMapping(value = "/selectservice", method = RequestMethod.POST, params = "attempt")
  public String reroll(@ModelAttribute("character") TravellerCharacter character, Model model,
          @RequestParam("selectService") String selectService) {
    model.addAttribute("character", character);
    System.out.println("****************************************");
    System.out.println("selectservice - attempt   selectService = " + selectService);
    System.out.println("****************************************");
    System.out.println("Character: " + character);
    Service service = Service.valueOf(selectService);

    System.out.println("Attemting " + service);
    int success = 0;
    switch (service) {
      case Army:
        success = 5;
        success -= (character.getCharacteristics().getDexterity() >= 6) ? 1 : 0;
        success -= (character.getCharacteristics().getEndurance() >= 5) ? 2 : 0;
        break;
      case Marines:
        success = 9;
        success -= (character.getCharacteristics().getIntelligence() >= 8) ? 1 : 0;
        success -= (character.getCharacteristics().getStrength() >= 8) ? 2 : 0;
        break;
      case Merchants:
        success = 7;
        success -= (character.getCharacteristics().getStrength() >= 7) ? 1 : 0;
        success -= (character.getCharacteristics().getIntelligence() >= 8) ? 2 : 0;
        break;
      case Navy:
        success = 8;
        success -= (character.getCharacteristics().getIntelligence() >= 8) ? 1 : 0;
        success -= (character.getCharacteristics().getEducation() >= 9) ? 2 : 0;
        break;
      case Other:
        success = 3;
        break;
      case Scouts:
        success = 7;
        success -= (character.getCharacteristics().getIntelligence() >= 6) ? 1 : 0;
        success -= (character.getCharacteristics().getStrength() >= 8) ? 2 : 0;
        break;
    }

    StringBuilder sb = new StringBuilder("Your chance to enter the " + service + " is " + success + " on 2D6 \n <br />");
    int diceRole = dice.role2Dice();

    if (diceRole >= success) {
      sb.append("Your rolled a ").append(diceRole).append(".  You made it! you are now in the ").append(service);
    } else {
      service = CharacterServices.draftService();
      sb.append("Your rolled a ").append(diceRole).append(".  that was a fail.  You have been drafted into the ").append(service).append(".");
      model.addAttribute("result", "Your rolled a " + diceRole + ".  that was a fail.  You have been drafted into the " + service + ".");
    }

    model.addAttribute("result", sb.toString());
    System.out.println(sb);
    com.javasmyths.TravellerCharacterGeneration.model.CareerProgress career =
        (com.javasmyths.TravellerCharacterGeneration.model.CareerProgress) model.getAttribute("career");
    if (career != null) {
      career.enlist(service, diceRole < success);
      service = career.getService();
    }
    character.setService(service);

    model.addAttribute("acceptButton", "hidden");
    model.addAttribute("attemptButton", "hidden");
    model.addAttribute("continueButton", "display");

    model.addAttribute("character", character);
    System.out.println("Character: " + character);
    return "selectservice";
  }

}

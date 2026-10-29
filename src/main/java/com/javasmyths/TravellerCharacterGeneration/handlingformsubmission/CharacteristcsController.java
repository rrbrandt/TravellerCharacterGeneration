package com.javasmyths.TravellerCharacterGeneration.handlingformsubmission;

import com.javasmyths.TravellerCharacterGeneration.model.Characteristics;
import com.javasmyths.TravellerCharacterGeneration.model.TravellerCharacter;
import javax.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class CharacteristcsController {

  @GetMapping("/characteristics")
  public String greetingForm(@ModelAttribute("character") TravellerCharacter character, Model model) {
    System.out.println("****************************************");
    System.out.println("CharacteristcsController - get");
    System.out.println("****************************************");
    System.out.println("Character: " + character);
    model.addAttribute("character", character);
    return "characteristics";
  }

  @PostMapping("/characteristics")
  public String greetingSubmit(@ModelAttribute("character") TravellerCharacter character, Model model) {
    System.out.println("****************************************");
    System.out.println("CharacteristcsController - post");
    System.out.println("****************************************");
    model.addAttribute("character", character);
    System.out.println("Character: " + character);
    model.addAttribute("acceptButton", "display");
    model.addAttribute("attemptButton", "display");
    model.addAttribute("continueButton", "hidden");
    return "selectservice";
  }

  @RequestMapping(value = "/characteristics", method = RequestMethod.POST, params = "ReRoll")
  public String reroll(@ModelAttribute("character") TravellerCharacter character, Model model) {
    System.out.println("****************************************");
    System.out.println("CharacteristcsController - ReRoll");
    System.out.println("****************************************");
    model.addAttribute("character", character);
    Characteristics characteristics = new Characteristics();
    character.setCharacteristics(characteristics);
    System.out.println("Character: " + character);
    return "characteristics";
  }

}

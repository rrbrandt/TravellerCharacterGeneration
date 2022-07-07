package com.javasmyths.TravellerCharacterGeneration.handlingformsubmission;

import com.javasmyths.travellercharactergeneration.model.Characteristics;
import com.javasmyths.travellercharactergeneration.model.TravellerCharacter;
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
  public String greetingForm(@ModelAttribute TravellerCharacter character, Model model) {
    System.out.println("****************************************");
    System.out.println("characteristics - get");
    System.out.println("****************************************");
    character.setCharacteristics(new Characteristics());
    System.out.println("Character: " + model.getAttribute("character"));
    return "characteristics";
  }

  @PostMapping("/characteristics")
  public String greetingSubmit(@ModelAttribute TravellerCharacter character, Model model) {
    System.out.println("****************************************");
    System.out.println("characteristics - post");
    System.out.println("****************************************");
    model.addAttribute("character", character);
    System.out.println("Character: " + character);
    return "selectservice";
  }

  @RequestMapping(value = "/characteristics", method = RequestMethod.POST, params = "ReRoll")
  public String reroll(@ModelAttribute TravellerCharacter character, Model model) {
    System.out.println("****************************************");
    System.out.println("characteristics - ReRoll");
    System.out.println("****************************************");
    Characteristics characteristics = new Characteristics();
    model.addAttribute("character", character);
    character.setCharacteristics(characteristics);
    System.out.println("Character: " + character);
    return "characteristics";
  }

}
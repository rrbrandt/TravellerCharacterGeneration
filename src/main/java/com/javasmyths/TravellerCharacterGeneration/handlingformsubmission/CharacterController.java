package com.javasmyths.TravellerCharacterGeneration.handlingformsubmission;

import com.javasmyths.travellercharactergeneration.model.Characteristics;
import com.javasmyths.travellercharactergeneration.model.TravellerCharacter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class CharacterController {

  @GetMapping("/newcharacter")
  public String greetingForm(Model model) {
    System.out.println("****************************************");
    System.out.println("newcharacter - get");
    System.out.println("****************************************");
    model.addAttribute("character", new TravellerCharacter(18));
    System.out.println("Character: " + model.getAttribute("character"));
    return "newcharacter";
  }

  @PostMapping("/newcharacter")
  public String greetingSubmit(@ModelAttribute TravellerCharacter character, Model model) {
    System.out.println("****************************************");
    System.out.println("newcharacter - post");
    System.out.println("****************************************");
    model.addAttribute("character", character);
    Characteristics characteristic = new Characteristics();
    character.setCharacteristics(characteristic);
    model.addAttribute("characteristics", new Characteristics());
    System.out.println("Character: " + character);
    return "characteristics";
  }

}
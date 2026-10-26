package com.javasmyths.TravellerCharacterGeneration.handlingformsubmission;

import com.javasmyths.TravellerCharacterGeneration.model.Characteristics;
import com.javasmyths.TravellerCharacterGeneration.model.TravellerCharacter;
import javax.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class CharacterController {
  @GetMapping("/")
  public String home() { return "redirect:/newcharacter"; }

  @GetMapping("/newcharacter")
  public String greetingForm(Model model, javax.servlet.http.HttpSession session) {
    TravellerCharacter character = new TravellerCharacter();
    character.setAge(18);
    character.setCharacteristics(new Characteristics());
    session.setAttribute("travellerCharacter", character);
    session.removeAttribute("careerProgress");
    model.addAttribute("character", character);
    return "newcharacter";
  }
  @PostMapping("/newcharacter")
  public String greetingSubmit(@ModelAttribute("character") TravellerCharacter character, Model model) {
    System.out.println("****************************************");
    System.out.println("CharacterController - post");
    System.out.println("****************************************");

    model.addAttribute("character", character);
    Characteristics characteristic = new Characteristics();
    character.setCharacteristics(characteristic);
    model.addAttribute("characteristics", new Characteristics());
    System.out.println("Character: " + character);
    return "characteristics";
  }

}

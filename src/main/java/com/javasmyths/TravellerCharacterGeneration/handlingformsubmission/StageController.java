package com.javasmyths.TravellerCharacterGeneration.handlingformsubmission;

import com.javasmyths.TravellerCharacterGeneration.model.Characteristics;
import com.javasmyths.TravellerCharacterGeneration.model.TravellerCharacter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class StageController {
  @PostMapping("/stage")
  public String stage(@RequestParam int stage, @ModelAttribute("character") TravellerCharacter character, Model model) {
    if (character.getCharacteristics() == null) character.setCharacteristics(new Characteristics());
    if (stage == 0) return "newcharacter";
    if (stage == 1) return "characteristics";
    if (stage == 4) return "musterout";
    if (stage == 5) return "finalcharacter";
    if (stage == 3 && character.getService() != null) return new SelectService().greetingSubmit(character, model);
    if (stage == 3) return "skills-pending";
    model.addAttribute("acceptButton", "hidden");
    model.addAttribute("attemptButton", character.getService() == null ? "display" : "hidden");
    model.addAttribute("continueButton", character.getService() == null ? "hidden" : "display");
    return "selectservice";
  }
}

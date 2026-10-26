package com.javasmyths.TravellerCharacterGeneration.handlingformsubmission;

import com.javasmyths.TravellerCharacterGeneration.model.CareerProgress;
import com.javasmyths.TravellerCharacterGeneration.model.TravellerCharacter;
import com.javasmyths.TravellerCharacterGeneration.services.Dice;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class MusterOut {
  @GetMapping("/musterout")
  public String show() { return "musterout"; }

  @PostMapping("/musterout")
  public String roll(@ModelAttribute("character") TravellerCharacter character, Model model,
      @RequestParam(required = false) String benefitTable,
      @RequestParam(defaultValue = "false") boolean gamblingBonus,
      @RequestParam(defaultValue = "false") boolean rankBonus) {
    CareerProgress career = (CareerProgress) model.getAttribute("career");
    model.addAttribute("result", career.rollBenefit(benefitTable, new Dice().roleDie(), gamblingBonus, rankBonus, character));
    return "musterout";
  }
}

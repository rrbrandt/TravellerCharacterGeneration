package com.javasmyths.TravellerCharacterGeneration.handlingformsubmission;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FinalCharacter {
  @GetMapping("/character")
  public String show() { return "finalcharacter"; }
}

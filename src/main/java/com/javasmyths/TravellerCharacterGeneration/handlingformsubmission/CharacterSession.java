package com.javasmyths.TravellerCharacterGeneration.handlingformsubmission;

import javax.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import com.javasmyths.TravellerCharacterGeneration.model.TravellerCharacter;
import com.javasmyths.TravellerCharacterGeneration.model.Characteristics;

@ControllerAdvice
public class CharacterSession {
  @ModelAttribute("career")
  public com.javasmyths.TravellerCharacterGeneration.model.CareerProgress career(HttpSession session) {
    com.javasmyths.TravellerCharacterGeneration.model.CareerProgress career =
        (com.javasmyths.TravellerCharacterGeneration.model.CareerProgress) session.getAttribute("careerProgress");
    if (career == null) {
      career = new com.javasmyths.TravellerCharacterGeneration.model.CareerProgress();
      session.setAttribute("careerProgress", career);
    }
    return career;
  }
  @ModelAttribute("character")
  public TravellerCharacter character(HttpSession session) {
    TravellerCharacter character = (TravellerCharacter) session.getAttribute("travellerCharacter");
    if (character == null) {
      character = new TravellerCharacter();
      character.setAge(18);
      character.setCharacteristics(new Characteristics());
      session.setAttribute("travellerCharacter", character);
    }
    return character;
  }
}
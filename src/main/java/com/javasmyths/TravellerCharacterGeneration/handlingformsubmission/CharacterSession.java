package com.javasmyths.TravellerCharacterGeneration.handlingformsubmission;

import javax.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import com.javasmyths.TravellerCharacterGeneration.model.TravellerCharacter;
import com.javasmyths.TravellerCharacterGeneration.model.Characteristics;

@ControllerAdvice
public class CharacterSession {
  @org.springframework.web.bind.annotation.InitBinder("character")
  public void protectCharacter(org.springframework.web.bind.WebDataBinder binder,
      javax.servlet.http.HttpServletRequest request) {
    int active = StageLocks.active(request.getSession());
    String path = request.getServletPath();
    boolean navigation = "/stage".equals(path) || "/characters/save".equals(path);
    boolean ownsStage = "POST".equals(request.getMethod()) && (!navigation || String.valueOf(active).equals(request.getParameter("fromStage")));
    if (ownsStage && active == 0 && (navigation || "/newcharacter".equals(path))) {
      binder.setAllowedFields("name", "age");
    } else if (ownsStage && active == 1 && (navigation || "/characteristics".equals(path))) {
      binder.setAllowedFields("characteristics.strength", "characteristics.dexterity", "characteristics.endurance",
          "characteristics.intelligence", "characteristics.education", "characteristics.socialStanding");
    } else {
      // An empty allowed-fields list means allow everything in Spring, so use an impossible field.
      binder.setAllowedFields("__no_character_fields__");
    }
  }
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

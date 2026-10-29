package com.javasmyths.TravellerCharacterGeneration.handlingformsubmission;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.javasmyths.TravellerCharacterGeneration.model.*;

@Controller
public class CharacterLibrary {
  private final CharacterStore store;
  public CharacterLibrary(CharacterStore store) { this.store = store; }
  @GetMapping("/characters")
  public String list(Model model) throws IOException {
    model.addAttribute("saves", store.list()); return "savedcharacters";
  }
  @PostMapping("/characters/save")
  public String save(@ModelAttribute("character") TravellerCharacter character, HttpSession session) throws IOException {
    synchronized (session) {
      CharacterStore.Saved saved = store.save((String)session.getAttribute("savedCharacterId"),
          (TravellerCharacter)session.getAttribute("travellerCharacter"),
          (CareerProgress)session.getAttribute("careerProgress"), StageLocks.active(session));
      session.setAttribute("savedCharacterId", saved.id);
    }
    return "redirect:/characters?saved";
  }
  @PostMapping("/characters/{id}/open")
  public String open(@PathVariable String id, HttpSession session) throws IOException {
    CharacterStore.Saved saved;
    try { saved = store.load(id); }
    catch (NoSuchFileException | IllegalArgumentException ex) { throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Saved character not found"); }
    synchronized (session) {
      session.setAttribute("travellerCharacter", saved.character);
      session.setAttribute("careerProgress", saved.career);
      session.setAttribute("activeStage", saved.activeStage);
      session.setAttribute("savedCharacterId", saved.id);
    }
    return "redirect:/stage?stage=" + saved.activeStage;
  }
  @ExceptionHandler(IOException.class)
  @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
  public String storageError() { return "characterstorageerror"; }
}

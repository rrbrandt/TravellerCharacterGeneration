package com.javasmyths.TravellerCharacterGeneration.handlingformsubmission;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class GreetingController {

  @GetMapping("/greeting")
  public String greetingForm(Model model) {
    System.out.println("****************************************");
    System.out.println("greeting - get");
    System.out.println("****************************************");
    model.addAttribute("greeting", new Greeting());
    return "greeting";
  }

  @PostMapping("/greeting")
  public String greetingSubmit(@ModelAttribute Greeting greeting, Model model) {
    System.out.println("****************************************");
    System.out.println("greeting - post");
    System.out.println("****************************************");
    model.addAttribute("greeting", greeting);
    return "result";
  }

}
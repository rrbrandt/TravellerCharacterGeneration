package com.javasmyths.traveller.handlingformsubmission;

import com.javasmyths.traveller.model.ServiceSkills;
import com.javasmyths.traveller.model.TravellerCharacter;
import com.javasmyths.traveller.services.Dice;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class CharacterControllerTest {
  private MockMvc mvc;

  @BeforeEach
  void setUp() {
    CharacterController controller = new CharacterController(
        new Dice(new Random(12345)), new ServiceSkills());
    InternalResourceViewResolver views = new InternalResourceViewResolver();
    views.setPrefix("/templates/");
    views.setSuffix(".html");
    mvc = standaloneSetup(controller).setViewResolvers(views).build();
  }

  @Test
  void completeCharacterWorkflowReachesResult() throws Exception {
    MvcResult created = mvc.perform(post("/newcharacter")
            .param("name", "Alexis")
            .param("age", "18"))
        .andExpect(status().isOk())
        .andExpect(view().name("characteristics"))
        .andExpect(model().attribute("character", hasProperty("characteristics", notNullValue())))
        .andReturn();

    TravellerCharacter character = (TravellerCharacter) created.getModelAndView()
        .getModel().get("character");

    MvcResult characteristics = mvc.perform(post("/characteristics")
            .sessionAttr("character", character)
            .param("name", "Alexis")
            .param("age", "18")
            .param("characteristics.strength", "8")
            .param("characteristics.dexterity", "7")
            .param("characteristics.endurance", "9")
            .param("characteristics.intelligence", "10")
            .param("characteristics.education", "8")
            .param("characteristics.socialStanding", "6")
            .param("action", "continue"))
        .andExpect(status().isOk())
        .andExpect(view().name("selectservice"))
        .andExpect(model().attributeExists("services"))
        .andReturn();

    TravellerCharacter updated = (TravellerCharacter) characteristics.getModelAndView()
        .getModel().get("character");

    MvcResult enlisted = mvc.perform(post("/service")
            .sessionAttr("character", updated)
            .param("service", "NAVY")
            .param("action", "accept"))
        .andExpect(status().isOk())
        .andExpect(view().name("result"))
        .andExpect(model().attributeExists("skillTables"))
        .andExpect(model().attribute("character", hasProperty("service", notNullValue())))
        .andReturn();

    TravellerCharacter enlistedCharacter = (TravellerCharacter) enlisted.getModelAndView()
        .getModel().get("character");

    org.junit.jupiter.api.Assertions.assertEquals(22, enlistedCharacter.getAge());
    org.junit.jupiter.api.Assertions.assertEquals(1, enlistedCharacter.getTermsServed());
    org.junit.jupiter.api.Assertions.assertTrue(enlistedCharacter.getSkillRollsRemaining() > 0);
    int rollsBefore = enlistedCharacter.getSkillRollsRemaining();

    mvc.perform(post("/skill")
            .sessionAttr("character", enlistedCharacter)
            .param("table", "SERVICE_SKILLS"))
        .andExpect(status().isOk())
        .andExpect(view().name("result"))
        .andExpect(model().attributeExists("skillTables", "skillTableOptions"))
        .andExpect(model().attribute("character", hasProperty("lastSkillResult", notNullValue())));

    org.junit.jupiter.api.Assertions.assertEquals(
        rollsBefore - 1, enlistedCharacter.getSkillRollsRemaining());
  }

  @Test
  void cannotRollSkillsWhenTermAllowanceIsExhausted() throws Exception {
    TravellerCharacter character = careerCharacter();
    character.setSkillRollsRemaining(0);

    mvc.perform(post("/skill")
            .sessionAttr("character", character)
            .param("table", "SERVICE_SKILLS"))
        .andExpect(status().isOk())
        .andExpect(view().name("result"))
        .andExpect(model().attributeExists("skillError"));
  }

  @Test
  void characterCanLeaveAfterUsingAllTermSkills() throws Exception {
    TravellerCharacter character = careerCharacter();
    character.setTermsServed(1);
    character.setSkillRollsRemaining(0);

    mvc.perform(post("/career/leave").sessionAttr("character", character))
        .andExpect(status().isOk())
        .andExpect(view().name("result"));

    org.junit.jupiter.api.Assertions.assertFalse(character.isCareerActive());
    org.junit.jupiter.api.Assertions.assertEquals(1, character.getMusterRollsRemaining());

    mvc.perform(post("/muster")
            .sessionAttr("character", character)
            .param("type", "cash"))
        .andExpect(status().isOk())
        .andExpect(view().name("result"));

    org.junit.jupiter.api.Assertions.assertEquals(0, character.getMusterRollsRemaining());
    org.junit.jupiter.api.Assertions.assertTrue(character.getCredits() > 0);
  }

  @Test
  void limitsCashBenefitsToThreeRolls() throws Exception {
    TravellerCharacter character = careerCharacter();
    character.setCareerActive(false);
    character.startMusteringOut(4, 0);

    for (int roll = 0; roll < 3; roll++) {
      mvc.perform(post("/muster")
              .sessionAttr("character", character)
              .param("type", "cash"))
          .andExpect(status().isOk());
    }

    mvc.perform(post("/muster")
            .sessionAttr("character", character)
            .param("type", "cash"))
        .andExpect(status().isOk())
        .andExpect(model().attributeExists("musterError"));

    org.junit.jupiter.api.Assertions.assertEquals(3, character.getCashRollsTaken());
    org.junit.jupiter.api.Assertions.assertEquals(1, character.getMusterRollsRemaining());
  }

  @Test
  void educationEightTableRejectsAnIneligibleCharacter() throws Exception {
    TravellerCharacter character = new TravellerCharacter();
    character.setCharacteristics(new com.javasmyths.traveller.model.Characteristics(
        new Dice(new Random(7))));
    character.getCharacteristics().setEducation(7);
    character.setService(com.javasmyths.traveller.model.Service.NAVY);
    character.prepareCareer();
    character.setSkillRollsRemaining(1);

    mvc.perform(post("/skill")
            .sessionAttr("character", character)
            .param("table", "ADVANCED_EDUCATION_8_PLUS"))
        .andExpect(status().isOk())
        .andExpect(view().name("result"))
        .andExpect(model().attributeExists("skillError"));
  }

  @Test
  void rejectsInvalidIdentity() throws Exception {
    mvc.perform(post("/newcharacter").param("name", " ").param("age", "12"))
        .andExpect(status().isOk())
        .andExpect(view().name("newcharacter"))
        .andExpect(model().attributeHasFieldErrors("character", "name", "age"));
  }

  @Test
  void directCharacteristicsPageHasAValidCharacterModel() throws Exception {
    mvc.perform(get("/characteristics"))
        .andExpect(status().isOk())
        .andExpect(view().name("characteristics"))
        .andExpect(model().attribute("character", hasProperty("characteristics", notNullValue())));
  }

  @Test
  void completedCharacterCanAddBackstoryAndReachCharacterSheet() throws Exception {
    TravellerCharacter character = careerCharacter();
    character.setCareerActive(false);
    character.setTermsServed(1);
    character.startMusteringOut(0, 0);

    mvc.perform(get("/backstory").sessionAttr("character", character))
        .andExpect(status().isOk())
        .andExpect(view().name("backstory"));

    mvc.perform(post("/backstory")
            .sessionAttr("character", character)
            .param("backstory", "A missing patron left behind an impossible star chart."))
        .andExpect(status().isOk())
        .andExpect(view().name("charactersheet"));

    org.junit.jupiter.api.Assertions.assertEquals(
        "A missing patron left behind an impossible star chart.", character.getBackstory());
  }

  @Test
  void activeCareerCannotSkipToFinalSteps() throws Exception {
    TravellerCharacter character = careerCharacter();

    mvc.perform(get("/backstory").sessionAttr("character", character))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/result"));

    mvc.perform(get("/charactersheet").sessionAttr("character", character))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/result"));
  }

  private TravellerCharacter careerCharacter() {
    TravellerCharacter character = new TravellerCharacter();
    character.setCharacteristics(new com.javasmyths.traveller.model.Characteristics(
        new Dice(new Random(7))));
    character.setService(com.javasmyths.traveller.model.Service.NAVY);
    character.prepareCareer();
    return character;
  }
}

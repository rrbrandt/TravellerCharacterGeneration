package com.javasmyths.shipbuilder;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
@Controller
public class ShipController {
 private final ShipStore store;public ShipController(ShipStore store){this.store=store;}
 @InitBinder("ship") public void bind(WebDataBinder binder){binder.initDirectFieldAccess();binder.setAutoGrowCollectionLimit(64);binder.setAllowedFields("id","name","hullType","hull","jump","maneuver","power","computer","staterooms","lowBerths","emergencyBerths","highPassengers","middlePassengers","crew","hardpoints","singleTurrets","doubleTurrets","tripleTurrets","fireControlReserve","pulseLasers","beamLasers","missileRacks","sandcasters","streamlined","commercial","doubleOccupancy","dualRoles","gunners","standardDesign","fuel","cargo","engineering","extraSoftwareCredits","vehicles*");}
 private Book2Rules.Report report(ShipDesign ship,BindingResult binding){Book2Rules.Report r=Book2Rules.calculate(ship);if(binding!=null&&binding.hasErrors()){r.valid=false;r.errors.add("Some values are not valid numbers. Check the marked inputs.");}return r;}
 private String page(ShipDesign ship,Book2Rules.Report report,Model model){model.addAttribute("ship",ship);model.addAttribute("report",report);model.addAttribute("letters",Book2Rules.LETTERS.split(""));model.addAttribute("computers",Book2Rules.COMPUTERS);model.addAttribute("vehicles",Book2Rules.VEHICLES);return "builder";}
 @GetMapping("/") public String home(@RequestParam(required=false)String id,@RequestParam(required=false)String preset,Model model)throws IOException{
  ShipDesign d=id!=null?store.read(id).design:"trader".equals(preset)?ShipDesign.trader():"scout".equals(preset)?ShipDesign.scout():new ShipDesign();return page(d,report(d,null),model);
 }
 @PostMapping("/") public String calculate(@ModelAttribute("ship")ShipDesign d,BindingResult binding,Model model){return page(d,report(d,binding),model);}
 @PostMapping("/calculate") @ResponseBody public Book2Rules.Report calculateJson(@ModelAttribute("ship")ShipDesign d,BindingResult binding){return report(d,binding);}
 @PostMapping("/save") public String save(@ModelAttribute("ship")ShipDesign d,BindingResult binding,Model model)throws IOException{
  Book2Rules.Report r=report(d,binding);if(!r.valid)return page(d,r,model);ShipStore.Saved saved=store.save(d);return "redirect:/?id="+saved.design.id+"&saved";
 }
 @GetMapping("/library") public String library(Model model)throws IOException{model.addAttribute("ships",store.list());return "library";}
 @GetMapping("/ships/{id}.json") @ResponseBody public ResponseEntity<byte[]> download(@PathVariable String id)throws IOException{return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=ship-"+id+".json").body(store.export(id));}
 @GetMapping("/health") @ResponseBody public String health(){return "{\"ok\":true,\"app\":\"ship-builder\",\"rules\":\"Book 2 (1981)\"}";}
 @ExceptionHandler({NoSuchFileException.class,IllegalArgumentException.class}) @ResponseStatus(HttpStatus.NOT_FOUND) public String missing(Model model){model.addAttribute("message","That saved ship could not be found.");return "error";}
 @ExceptionHandler(IOException.class) @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE) public String storage(Model model){model.addAttribute("message","Ship storage is unavailable. The save or open operation did not complete; please return to your design and try again.");return "error";}
}

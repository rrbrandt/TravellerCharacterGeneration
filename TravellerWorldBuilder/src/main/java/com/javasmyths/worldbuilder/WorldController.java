package com.javasmyths.worldbuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Value;
import javax.servlet.http.*;
import java.util.*;
import java.io.*;
@Controller
public class WorldController {
 private final AtlasStore store;
 private final boolean publicHosting;
 public WorldController(AtlasStore store,@Value("${worldbuilder.public-hosting:false}")boolean publicHosting){this.store=store;this.publicHosting=publicHosting;}
 String csrf(HttpServletRequest r){HttpSession s=r.getSession();if(s.getAttribute("csrf")==null)s.setAttribute("csrf",UUID.randomUUID().toString());return s.getAttribute("csrf").toString();}
 void authorize(HttpServletRequest r,String value){HttpSession s=r.getSession(false);if(s==null||value==null||!value.equals(s.getAttribute("csrf")))throw new SecurityException("This form expired. Refresh and try again.");}
 String render(Atlas a,HttpServletRequest r,Model m)throws IOException{
  m.addAttribute("publicHosting",publicHosting);m.addAttribute("atlas",a);m.addAttribute("csrf",csrf(r));m.addAttribute("atlasJson",store.mapper.writeValueAsString(a).replace("<","\\u003c").replace(">","\\u003e").replace("&","\\u0026"));return "atlas";
 }
 @GetMapping("/") public String home(@RequestParam(required=false)String id,HttpServletRequest r,Model m)throws IOException{return render(id==null?Book3Rules.generate("The Far Shore","1981",4,false):store.load(id),r,m);}
 @PostMapping("/generate") public String generate(@RequestParam String name,@RequestParam(defaultValue="")String seed,@RequestParam(defaultValue="4")int density,@RequestParam(defaultValue="subsector")String kind,@RequestParam String csrf,HttpServletRequest r,Model m)throws IOException{authorize(r,csrf);return render(Book3Rules.generate(name,seed,density,kind.equals("world")),r,m);}
 @PostMapping("/decode") @ResponseBody public Atlas.World decode(@RequestBody Map<String,String> input,@RequestHeader(value="X-CSRF-Token",required=false)String csrf,HttpServletRequest r){authorize(r,csrf);return Book3Rules.decode(input.get("uwp"),input.get("name"),input.get("hex"));}
 @PostMapping("/save") @ResponseBody public Atlas save(@RequestBody Atlas a,@RequestHeader(value="X-CSRF-Token",required=false)String csrf,HttpServletRequest r)throws IOException{authorize(r,csrf);return store.save(a);}
 @GetMapping("/library") public String library(Model m)throws IOException{m.addAttribute("publicHosting",publicHosting);m.addAttribute("items",store.list());return "library";}
 @GetMapping("/atlases/{id}.json") public ResponseEntity<Atlas> json(@PathVariable String id)throws IOException{return ResponseEntity.ok().header("Content-Disposition","attachment; filename=atlas-"+id+".json").body(store.load(id));}
 @GetMapping(value="/atlases/{id}.txt",produces="text/plain;charset=UTF-8") public ResponseEntity<String> text(@PathVariable String id)throws IOException{Atlas a=store.load(id);StringBuilder b=new StringBuilder(a.name+"\nBook 3 (1981) mainworlds · Seed: "+a.seed+"\n\nHex  Name                            UWP       Bases       Gas   Trade\n");for(Atlas.World w:a.worlds)b.append(String.format(Locale.ROOT,"%s %-31s %s %-11s %-5s %s%n",w.hex,w.name,w.getUwp(),w.getBases(),w.gasGiant?"Yes":"No",String.join(" ",w.getTradeCodes())));return ResponseEntity.ok().header("Content-Disposition","attachment; filename=atlas-"+id+".txt").body(b.toString());}
 @GetMapping("/health") @ResponseBody public Map<String,Object> health(){return Map.of("ok",true,"app","world-builder","rules","Book 3 revised 1981");}
 @ExceptionHandler(SecurityException.class) public ResponseEntity<Map<String,String>> forbidden(SecurityException ex){return ResponseEntity.status(403).body(Map.of("error",ex.getMessage()));}
 @ExceptionHandler({IllegalArgumentException.class,IOException.class}) public ResponseEntity<Map<String,String>> bad(Exception ex){return ResponseEntity.badRequest().body(Map.of("error",ex instanceof IOException?"The requested atlas could not be read or written.":ex.getMessage()));}
}

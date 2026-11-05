package com.javasmyths.worldbuilder;
import java.util.*;
public class Atlas {
 public String id="",name="The Far Shore",kind="subsector",seed="1981",notes="";
 public int density=4;
 public List<World> worlds=new ArrayList<>();
 public static class World {
  public String hex="0101",name="Unnamed",starport="C",notes="";
  public int size,atmosphere,hydrographics,population,government,law,tech;
  public boolean naval,scout,gasGiant;
  public List<Roll> rolls=new ArrayList<>();
  public String getUwp(){return starport+Book3Rules.digit(size)+Book3Rules.digit(atmosphere)+Book3Rules.digit(hydrographics)+Book3Rules.digit(population)+Book3Rules.digit(government)+Book3Rules.digit(law)+"-"+Book3Rules.digit(tech);}
  public Map<String,String> getDetails(){return Book3Rules.describe(this);}
  public List<String> getTradeCodes(){return Book3Rules.trade(this);}
  public String getBases(){return naval&&scout?"Naval + Scout":naval?"Naval":scout?"Scout":"None";}
 }
 public static class Roll {
  public String label,dice,formula,result;
  public Roll(){}
  public Roll(String label,String dice,String formula,String result){this.label=label;this.dice=dice;this.formula=formula;this.result=result;}
 }
}

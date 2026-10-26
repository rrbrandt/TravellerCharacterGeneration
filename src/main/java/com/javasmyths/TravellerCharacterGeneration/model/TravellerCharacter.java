/*
 * Copyright (C) 2018 JavaSmyths javasmyths@javasmyths.com
 */
package com.javasmyths.TravellerCharacterGeneration.model;

import com.javasmyths.TravellerCharacterGeneration.model.Service;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

/**
 *
 * @author Richard
 */
public class TravellerCharacter {

  private String Name;
  private Characteristics characteristics;
  private int age;
  private Service service;
  private int rank;

  public TravellerCharacter() {
  }

  public TravellerCharacter(int age) {
    this.age = age;
  }

  public int getAge() {
    return age;
  }

  public void setAge(int age) {
    this.age = age;
  }

  public Characteristics getCharacteristics() {
    return characteristics;
  }

  public void setCharacteristics(Characteristics characteristics) {
    this.characteristics = characteristics;
  }

  public String getName() {
    return Name;
  }

  public void setName(String Name) {
    this.Name = Name;
  }

  public Service getService() {
    return service;
  }

  public void setService(Service service) {
    this.service = service;
  }

  @Override
  public String toString() {
    return "TravellerCharacter{" + "Name=" + Name + ", characteristics=" + characteristics + ", age=" + age + ", service=" + service + '}';
  }


}

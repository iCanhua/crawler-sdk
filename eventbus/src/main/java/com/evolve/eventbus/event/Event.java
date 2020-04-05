package com.evolve.eventbus.event;

public interface Event {

  String getId();

  Object getSource();

  String getDescription();

}

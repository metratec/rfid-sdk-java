package com.metratec.lib.rfidreader.event;

import com.metratec.lib.tag.UhfTag;

public class UhfTagLost extends UhfTagEvent {

  private static final long serialVersionUID = 7278059847916249298L;

  public UhfTagLost(String identifier, UhfTag tag, Long timestamp) {
    super(identifier, tag, timestamp);
  }

  public UhfTagLost(RfidTagLost<UhfTag> event) {
    super(event.getIdentifier(), event.getTag(), event.getTimestamp());
  }

  
  
}

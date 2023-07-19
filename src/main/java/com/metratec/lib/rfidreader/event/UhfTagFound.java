package com.metratec.lib.rfidreader.event;

import com.metratec.lib.tag.UhfTag;

public class UhfTagFound extends UhfTagEvent {

  private static final long serialVersionUID = -3146078949023706787L;

  public UhfTagFound(String identifier, UhfTag tag, Long timestamp) {
    super(identifier, tag, timestamp);
  }

  public UhfTagFound(RfidTagFound<UhfTag> event) {
    super(event.getIdentifier(), event.getTag(), event.getTimestamp());
  }
 
}

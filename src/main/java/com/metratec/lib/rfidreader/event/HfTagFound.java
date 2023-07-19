package com.metratec.lib.rfidreader.event;

import com.metratec.lib.tag.HfTag;

public class HfTagFound extends HfTagEvent {

  private static final long serialVersionUID = 6078620849415075887L;

  public HfTagFound(String identifier, HfTag tag, Long timestamp) {
    super(identifier, tag, timestamp);
  }

  public HfTagFound(RfidTagFound<HfTag> event) {
    super(event.getIdentifier(), event.getTag(), event.getTimestamp());
  }
}

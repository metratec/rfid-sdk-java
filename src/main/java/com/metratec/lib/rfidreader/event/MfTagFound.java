package com.metratec.lib.rfidreader.event;

import com.metratec.lib.tag.MfTag;

public class MfTagFound extends MfTagEvent {

  private static final long serialVersionUID = 983614753433053502L;

  public MfTagFound(String identifier, MfTag tag, Long timestamp) {
    super(identifier, tag, timestamp);
  }

  public MfTagFound(RfidTagFound<MfTag> event) {
    super(event.getIdentifier(), event.getTag(), event.getTimestamp());
  }
}

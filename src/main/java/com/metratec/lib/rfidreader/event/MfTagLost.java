package com.metratec.lib.rfidreader.event;

import com.metratec.lib.tag.MfTag;

public class MfTagLost extends MfTagEvent {

  private static final long serialVersionUID = 4477252752848578861L;

  public MfTagLost(String identifier, MfTag tag, Long timestamp) {
    super(identifier, tag, timestamp);
  }

  public MfTagLost(RfidTagLost<MfTag> event) {
    super(event.getIdentifier(), event.getTag(), event.getTimestamp());
  }
}

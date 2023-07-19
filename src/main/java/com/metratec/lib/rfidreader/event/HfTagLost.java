package com.metratec.lib.rfidreader.event;

import com.metratec.lib.tag.HfTag;

public class HfTagLost extends RfidTagLost<HfTag> {

  private static final long serialVersionUID = -5297586422554972508L;

  public HfTagLost(String identifier, HfTag tag, Long timestamp) {
    super(identifier, tag, timestamp);
  }

  public HfTagLost(RfidTagLost<HfTag> event) {
    super(event.getIdentifier(), event.getTag(), event.getTimestamp());
  }

}

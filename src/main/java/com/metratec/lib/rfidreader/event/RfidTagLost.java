package com.metratec.lib.rfidreader.event;


public class RfidTagLost<T> extends RfidTagEvent<T> {

  private static final long serialVersionUID = -8962542078797483491L;

  public RfidTagLost(String identifier, T tag, Long timestamp) {
    super(identifier, tag, timestamp);
  }

}

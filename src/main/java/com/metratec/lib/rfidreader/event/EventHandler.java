/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader.event;

import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.metratec.lib.rfidreader.MetratecReader;

/**
 * @author man
 *
 */
public class EventHandler<T> implements Runnable, RfidReaderEventListener, RfidTagEventListener<T> {
  private LinkedBlockingQueue<RfidEvent> eventBuffer;
  private final static int MAX_SIZE = 1024;
  private static final Logger LOGGER = LoggerFactory.getLogger(MetratecReader.class);

  private RfidReaderEventListener readerListener = null;
  private RfidTagEventListener<T> tagListener = null;
  private Thread internalThread;
  private String threadName;
  /**
   * @param identifier instance name Create a new instance
   */
  public EventHandler(String identifier) {
    eventBuffer = new LinkedBlockingQueue<>(MAX_SIZE);
    this.threadName = "EH-" + identifier;
  }

  public void start() {
    if(null != internalThread && internalThread.isAlive()) {
      return;
    }
    internalThread = new Thread(this, threadName);
    internalThread.setDaemon(true);
    internalThread.start();
  }

  public void stop() {
    // Poisson pill shutdown
    addEvent(new RfidEvent(null, 0l));
  }

  @Override
  @SuppressWarnings({"PMD.EmptyCatchBlock", "unchecked"})
  public void run() {
    while (true) {
      try {
        RfidEvent event = eventBuffer.take();
        if (null == event.getIdentifier()) {
          break;
        }
        if (RfidTagEvent.class.isInstance(event)) {
          if (null == tagListener) {
            continue;
          }
          if(RfidTagFound.class.isInstance(event)){
            tagListener.tagFound((RfidTagFound<T>) event);
          } else if(RfidTagLost.class.isInstance(event)){
            tagListener.tagLost((RfidTagLost<T>) event);
          }
        } else if (RfidReaderInputChange.class.isInstance(event)) {
          if (null != readerListener) {
            readerListener.inputChange((RfidReaderInputChange) event);
          }
        } else if (RfidReaderConnectionState.class.isInstance(event) && null != readerListener) {
          readerListener.connectionState((RfidReaderConnectionState) event);
        }
      } catch (Exception e) {
        LOGGER.warn(e.getClass().getSimpleName() + " " + e.getMessage(), e);
      }
    }
  }

  /**
   * Add a event to the internal buffer
   * 
   * @param event the {@link RfidEvent}
   */
  public void addEvent(RfidEvent event) {
    if (eventBuffer.size() > 128 && LOGGER.isDebugEnabled()) {
      LOGGER.debug("Size " + eventBuffer.size());
    }
    if (eventBuffer.size() >= MAX_SIZE) {
      LOGGER.debug("remove Event " + eventBuffer.size());
      eventBuffer.poll();
    }
    eventBuffer.offer(event);
  }

//  /**
//   * @return the listener
//   */
//  public RfidEventListener getListener() {
//    return listener;
//  }

  /**
   * @param listener the listener to set
   */
  public void setReaderListener(RfidReaderEventListener listener) {
    this.readerListener = listener;
  }

  /**
   * @param listener the listener to set
   */
  public void setTagListener(RfidTagEventListener<T> listener) {
    this.tagListener = listener;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.metratec.lib.rfidreader.event.RfidEventListener#inputChange(com.metratec.lib.rfidreader.
   * event.RfidReaderInputChange)
   */
  @Override
  public void inputChange(RfidReaderInputChange event) {
    addEvent(event);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.metratec.lib.rfidreader.event.RfidEventListener#connectionState(com.metratec.lib.rfidreader
   * .event.RfidReaderConnectionState)
   */
  @Override
  public void connectionState(RfidReaderConnectionState event) {
    addEvent(event);
  }


  public boolean isAlive() {
    return null != internalThread && internalThread.isAlive();
  }

  @Override
  public void tagFound(RfidTagFound<T> tagEvent) {
    addEvent(tagEvent);
  }

  @Override
  public void tagLost(RfidTagLost<T> tagEvent) {
    addEvent(tagEvent);
  }
}

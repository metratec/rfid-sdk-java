/*******************************************************************************
 * Copyright (c) 2026 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader.event;

import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.metratec.lib.rfidreader.MetratecReader;

/**
 * Event handler that manages and dispatches RFID reader and tag events.
 * This class runs in its own thread and processes events from a queue,
 * forwarding them to registered listeners. It acts as a bridge between
 * the reader's event generation and the application's event handling.
 * 
 * @param <T> the type of RFID tag handled by this event handler
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
   * Create a new event handler instance.
   * 
   * @param identifier instance name used for thread naming and identification
   */
  public EventHandler(String identifier) {
    eventBuffer = new LinkedBlockingQueue<>(MAX_SIZE);
    this.threadName = "EH-" + identifier;
  }

  /**
   * Start the event handler thread.
   * Creates and starts a new daemon thread to process events from the queue.
   */
  public void start() {
    if(null != internalThread && internalThread.isAlive()) {
      return;
    }
    internalThread = new Thread(this, threadName);
    internalThread.setDaemon(true);
    internalThread.start();
  }

  /**
   * Stop the event handler thread.
   * Uses a poison pill approach to gracefully shutdown the event processing thread.
   */
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
   * Add an event to the internal buffer for processing.
   * If the buffer is full, the oldest event will be removed to make space.
   * 
   * @param event the {@link RfidEvent} to be processed
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
   * Set the reader event listener for handling reader-specific events.
   * 
   * @param listener the {@link RfidReaderEventListener} to receive reader events
   */
  public void setReaderListener(RfidReaderEventListener listener) {
    this.readerListener = listener;
  }

  /**
   * Set the tag event listener for handling tag-specific events.
   * 
   * @param listener the {@link RfidTagEventListener} to receive tag events
   */
  public void setTagListener(RfidTagEventListener<T> listener) {
    this.tagListener = listener;
  }

  /**
   * Handles input change events from the RFID reader.
   * Forwards the event to the internal event queue for processing.
   * 
   * @param event the input change event to process
   */
  @Override
  public void inputChange(RfidReaderInputChange event) {
    addEvent(event);
  }

  /**
   * Handles connection state change events from the RFID reader.
   * Forwards the event to the internal event queue for processing.
   * 
   * @param event the connection state change event to process
   */
  @Override
  public void connectionState(RfidReaderConnectionState event) {
    addEvent(event);
  }

  /**
   * Tests if the event handler thread is alive and running.
   *
   * @return <code>true</code> if the event handler thread is alive and running;
   *         <code>false</code> otherwise
   */
  public boolean isAlive() {
    return null != internalThread && internalThread.isAlive();
  }

  /**
   * Handles tag found events.
   * Forwards the event to the internal event queue for processing.
   * 
   * @param tagEvent the tag found event to process
   */
  @Override
  public void tagFound(RfidTagFound<T> tagEvent) {
    addEvent(tagEvent);
  }

  /**
   * Handles tag lost events.
   * Forwards the event to the internal event queue for processing.
   * 
   * @param tagEvent the tag lost event to process
   */
  @Override
  public void tagLost(RfidTagLost<T> tagEvent) {
    addEvent(tagEvent);
  }
}

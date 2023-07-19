/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.metratec.lib.connection.CommConnectionException;
import com.metratec.lib.connection.ICommConnection;
import com.metratec.lib.inventory.Inventory;
import com.metratec.lib.inventory.event.InventoryChangedEvent;
import com.metratec.lib.inventory.event.InventoryListener;
import com.metratec.lib.inventory.event.TagArrivedEvent;
import com.metratec.lib.inventory.event.TagDepartedEvent;
import com.metratec.lib.rfidreader.event.EventHandler;
import com.metratec.lib.rfidreader.event.RfidReaderConnectionState;
import com.metratec.lib.rfidreader.event.RfidReaderEventListener;
import com.metratec.lib.rfidreader.event.RfidReaderInputChange;
import com.metratec.lib.rfidreader.event.RfidTagEventListener;
import com.metratec.lib.rfidreader.event.RfidTagFound;
import com.metratec.lib.rfidreader.event.RfidTagLost;
import com.metratec.lib.tag.RfidTag;

/**
 * @author jannis becke, matthias neumann
 *
 */
public abstract class MetratecReader<T extends RfidTag> {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  protected final static int DEFAULT_RECEIVE_TIMEOUT = 10000;
  private int receiveTimeout = DEFAULT_RECEIVE_TIMEOUT;
  private static final long DEFAULT_TAG_LOST_TIME = 5000;
  private final Queue<String> responseBuffer = new ConcurrentLinkedQueue<>();

  private Lock lockInput0 = new ReentrantLock();
  private Lock lockInput1 = new ReentrantLock();
  private boolean input0 = false;
  private boolean input1 = false;
  private Thread input0Thread;
  private Thread input1Thread;
  private boolean input0Working = false;
  private boolean input1Working = false;
  /**
   * minimum input debounce time 50ms
   */
  private static final int MIN_INPUT_DEBOUNCE_TIME = 50;
  private int inputDebounceTime = MIN_INPUT_DEBOUNCE_TIME;

  private EventHandler<T> eventHandler;
  private Inventory<T> inventory;

  /**
   * the receiver handler
   */
  protected RFIDDataHandler receiveHandler;

  /**
   * communication lock for send and receive synchron
   */
  protected ReentrantLock communicateLock = new ReentrantLock(true);

  /**
   * Heart beat interval
   */
  protected int heartBeatInterval = 5;

  private String identifier = "unknown";

  /** the master is connecting */
  public static final int STATE_CONNECTING = 0;
  /** the master is running */
  public static final int STATE_RUNNING = 1;
  /** the master is stopped */
  public static final int STATE_STOPPED = 2;
  /** the master is waiting during reconnect */
  public static final int STATE_WAITING_FOR_RECONNECT = 0xF;
  /** the master is being configured */
  public static final int STATE_CONFIGURING = 3;

  // private ICommConnection connection;

  /**
   * Construct a new StandardReader instance with the specified connection
   * 
   * @param identifier reader identifier
   * @param connection connection
   */
  public MetratecReader(String identifier, ICommConnection connection) {
    if (null != connection) {
      this.identifier = identifier;
      try {
        receiveHandler = new RFIDDataHandler(getIdentifier(), connection, data -> {
          String tmp = data.trim();
          if (tmp.isEmpty()) {
            return;
          }
          if (handleResponse(tmp)) {
            responseBuffer.add(tmp);
          }
        }, this);
      } catch (CommConnectionException e) {
        e.printStackTrace();
      }
    } else {
      throw new NullPointerException();
    }

    inventory = new Inventory<>(getIdentifier(), new InventoryListener<T>() {

      @Override
      public void tagArrive(TagArrivedEvent<T> tagArrived) {
        eventHandler.tagFound(new RfidTagFound<T>(getIdentifier(), tagArrived.getTag(), tagArrived.getTimestamp()));
      }

      @Override
      public void inventoryChanged(InventoryChangedEvent<T> readPointInventoryChanged) {

      }

      @Override
      public void tagDeparted(TagDepartedEvent<T> tagDeparted) {
        eventHandler.tagLost(new RfidTagLost<T>(getIdentifier(), tagDeparted.getTag(), tagDeparted.getTimestamp()));
      }

    });
  }

  protected Logger getLogger() {
    return logger;
  }

  /**
   * set the rfid event listener
   * 
   * @param listener {@link RfidReaderEventListener}
   */
  public void setReaderEventListener(RfidReaderEventListener listener) {
    updateListener(listener, null);
  }

  /**
   * set the rfid event listener
   * 
   * @param listener {@link RfidReaderEventListener}
   */
  public void setTagEventListener(RfidTagEventListener<T> listener) {
    updateListener(null, listener);
  }

  protected EventHandler<T> getEventHandler() {
    return eventHandler;
  }

  private void updateListener(RfidReaderEventListener readerListener, RfidTagEventListener<T> tagListener) {
    if (null != readerListener || null != tagListener) {
      if (null == eventHandler) {
        eventHandler = new EventHandler<T>(getIdentifier());
        receiveHandler.setEventHandler(eventHandler);
        if (isConnected()) {
          startHandler();
          eventHandler.connectionState(
              new RfidReaderConnectionState(getIdentifier(), isConnected(), receiveHandler.getStateMessage()));
        }
      }
      if (null != readerListener) {
        eventHandler.setReaderListener(readerListener);
      }
      if (null != tagListener) {
        eventHandler.setTagListener(tagListener);
      }
    } else {
      stopHandler();
    }
  }

  private void startHandler() {
    if (null != eventHandler && !eventHandler.isAlive()) {
      eventHandler.start();
      while (!eventHandler.isAlive()) {
        try {
          Thread.sleep(1);
        } catch (InterruptedException e) {
        }
      }
    }
  }

  private void stopHandler() {
    if (null != eventHandler) {
      eventHandler.setReaderListener(null);
      eventHandler.stop();
      while (eventHandler.isAlive()) {
        try {
          Thread.sleep(50);
        } catch (InterruptedException e) {
        }
      }
    }
  }

  /**
   * <b>For internal use!</b><br>
   * get the reader answer
   * 
   * @return the reader answer as a string array
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException throw {@link RFIDErrorCodes#SRT} if the reader has been reseted
   */
  protected String receiveData() throws CommConnectionException, RFIDReaderException {
    long endtime = System.currentTimeMillis() + receiveTimeout;
    while (responseBuffer.isEmpty()) {
      if (System.currentTimeMillis() > endtime) {
        // checkHardReset();
        throw new CommConnectionException(ICommConnection.RECV_TIMEOUT, "Reader did not respond");
      }
      try {
        Thread.sleep(1);
      } catch (InterruptedException e) {
      }
    }
    return responseBuffer.poll();
  }

  /**
   * clear the response buffer
   */
  protected void clearResponseBuffer() {
    if (!responseBuffer.isEmpty()) {
      if (logger.isDebugEnabled()) {
        StringBuilder buf = new StringBuilder();
        while (!responseBuffer.isEmpty()) {
          buf.append(responseBuffer.poll());
        }
        logger.debug("clearResponseBuffer " + buf.toString().replaceAll("\r", "<CR>").replaceAll("\n", "<LF>"));
      } else {
        responseBuffer.clear();
      }
    }
  }

  // /**
  // * Prepare the reader, stop the cnr mode if active, enable End Of Frame mode, enable check crc
  // * mode
  // *
  // * @throws CommConnectionException if an communication exception occurs
  // * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
  // * range, ..)
  // */
  // public abstract void prepareReader() throws CommConnectionException, RFIDReaderException;
  /**
   * @return the identifier
   */
  public String getIdentifier() {
    return identifier;
  }

  /**
   * @param identifier the identifier to set
   */
  public void setIdentifier(String identifier) {
    this.identifier = identifier;
    receiveHandler.setIdentifier(identifier);
  }

  /**
   * @param command command
   * @param parameters parameters
   * @return the answer
   */
  protected abstract String prepareCommand(String command, Object... parameters);

  /**
   * @param data data
   * @return answer array
   * @throws RFIDReaderException if an reader exception occurs
   */
  protected abstract String[] checkData(String data) throws RFIDReaderException;

  /**
   * @return teh RFIDResponseChecker
   */
  protected abstract boolean handleResponse(String response);

  /**
   * Start the reader
   */
  public void start() {
    startHandler();
    if (receiveHandler.isAlive()) {
      return;
    }
    receiveHandler.start();
  }

  /**
   * Start the reader
   * 
   * @param heartBeatInterval heart beat interval in seconds
   */
  public void start(int heartBeatInterval) {
    this.heartBeatInterval = heartBeatInterval;
    start();
  }

  /**
   * start the reader but also wait until the reader is connected and initialized
   * 
   * @throws CommConnectionException if an error occurs
   * @throws RFIDReaderException if an error occurs
   */
  public void startAndWait() throws CommConnectionException, RFIDReaderException {
    startAndWait(20000);
  }

  /**
   * connect the reader and wait until the reader is connected
   * 
   * @param timeout max time to try connect and configure the reader
   * 
   * @throws CommConnectionException if an error occurs
   * @throws RFIDReaderException if an error occurs
   * 
   */
  public void startAndWait(long timeout) throws CommConnectionException, RFIDReaderException {
    start();
    long timeoutTime = System.currentTimeMillis() + timeout;
    while (!isConnected() && System.currentTimeMillis() < timeoutTime) {
      if (receiveHandler.getHandlerState() == MetratecReader.STATE_WAITING_FOR_RECONNECT) {
        Exception e = receiveHandler.getLastException();
        if (CommConnectionException.class.isInstance(e)) {
          throw (CommConnectionException) e;
        } else if (RFIDReaderException.class.isInstance(e)) {
          throw (RFIDReaderException) e;
        } else {
          throw new CommConnectionException(ICommConnection.UNHANDLED_ERROR, receiveHandler.getStateMessage());
        }
      }
      try {
        Thread.sleep(20);
      } catch (InterruptedException e) {
      }
    }
    if (!isConnected()) {
      throw new CommConnectionException(receiveHandler.getStateMessage());
    }
  }

  /**
   * start the reader but also wait until the reader is connected and initialized
   * 
   * @deprecated use {@link #startAndWait()} instead
   * @throws CommConnectionException if an error occurs
   * @throws RFIDReaderException if an error occurs
   */
  @Deprecated
  public void connect() throws CommConnectionException, RFIDReaderException {
    startAndWait(20000);
  }

  /**
   * connect the reader and wait until the reader is connected
   * 
   * @deprecated use {@link #startAndWait(long timeout)} instead
   * @param timeout max time to try connect and configure the reader
   * 
   * @throws CommConnectionException if an error occurs
   * @throws RFIDReaderException if an error occurs
   * 
   */
  @Deprecated
  public void connect(long timeout) throws CommConnectionException, RFIDReaderException {
    startAndWait(timeout);
  }

  /**
   * stop the reader
   * 
   * @throws CommConnectionException possible ICommConnection Error codes:
   *         <ul>
   *         <li>UNHANDLED_ERROR</li>
   *         </ul>
   */
  public void stop() throws CommConnectionException {
    stopHandler();
    if (isConnected()) {
      try {
        stopInventory();
      } catch (RFIDReaderException e) {
        getLogger().debug("Error stopping inventory - {}", e.getMessage());
      }
    }
    closeConnection();
  }

  /**
   * close the connection
   * 
   * @deprecated use {@link #stop()} method instead
   * @throws CommConnectionException possible ICommConnection Error codes:
   *         <ul>
   *         <li>UNHANDLED_ERROR</li>
   *         </ul>
   */
  @Deprecated
  public void disconnect() throws CommConnectionException {
    stop();
  }

  /**
   * Close the communication
   *
   * @throws CommConnectionException possible ICommConnection Error codes:
   *         <ul>
   *         <li>UNHANDLED_ERROR</li>
   *         </ul>
   */
  protected void closeConnection() throws CommConnectionException {
    if (null != receiveHandler) {
      receiveHandler.stop();
    }
  }

  // /**
  // * <b>For internal use!</b><br>
  // *
  // * @return the Connection Object
  // */
  // public ICommConnection getConnection() {
  // return connection;
  // }


  /**
   * @return the connection state
   */
  public boolean isConnected() {
    return receiveHandler.isWorking();
  }

  /**
   * <b>For internal use!</b><br>
   * get the reader answer
   * 
   * @return the reader answer as a string array
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  protected String[] receive() throws CommConnectionException, RFIDReaderException {
    if (!(receiveHandler.isConnected() && receiveHandler.isAlive())) {
      throw new CommConnectionException(ICommConnection.CONNECTION_LOST, "not connected");
    }
    communicateLock.lock();
    try {
      return checkData(receiveData());
    } finally {
      communicateLock.unlock();
    }

  }

  /**
   * @return the next raw answer, in this case the answer is not checked
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an reader exception occurs
   */
  protected String receiveRaw() throws CommConnectionException, RFIDReaderException {
    if (!(receiveHandler.isConnected() && receiveHandler.isAlive())) {
      throw new CommConnectionException(ICommConnection.CONNECTION_LOST, "not connected");
    }
    communicateLock.lock();
    try {
      return receiveData().toString();
    } finally {
      communicateLock.unlock();
    }
  }

  /**
   * <b>For internal use!</b><br>
   * Sends an command to the connected reader (appends crc and command end sign automatically)
   * 
   * @param command command
   * @param parameters command parameters
   * @throws CommConnectionException possible ICommConnection Error codes:
   *         <ul>
   *         <li>NOT_INITIALISE</li>
   *         <li>CONNECTION_LOST</li>
   *         <li>RECV_TIMEOUT</li>
   *         <li>UNHANDLED_ERROR</li>
   *         </ul>
   */
  protected void send(String command, Object... parameters) throws CommConnectionException {
    if (!(receiveHandler.isConnected() && receiveHandler.isAlive())) {
      throw new CommConnectionException(ICommConnection.CONNECTION_LOST, "not connected");
    }
    communicateLock.lock();
    try {
      receiveHandler.sendCommand(prepareCommand(command, parameters));
    } finally {
      communicateLock.unlock();
    }
  }

  /**
   * Set the minimum time before attempting to reconnect. Default value 21600000ms (6h).
   * 
   * @param timeInMilliseconds maximum reconnect wait time in milliseconds
   */
  public void setMaxReconnectWaitTime(long timeInMilliseconds) {
    receiveHandler.setMaxReconnectWaitTime(timeInMilliseconds);
  }

  /**
   * Set the minimum time before attempting to reconnect. Default value 2000ms.
   * 
   * @param timeInMilliseconds minimum reconnect wait time in milliseconds
   */
  public void setMinReconnectWaitTime(long timeInMilliseconds) {
    receiveHandler.setMinReconnectWaitTime(timeInMilliseconds);
  }

  /**
   * @return the receiveTimeout
   */
  public int getReceiveTimeout() {
    return receiveTimeout;
  }

  /**
   * @param receiveTimeout the receiveTimeout to set
   */
  public void setReceiveTimeout(int receiveTimeout) {
    this.receiveTimeout = 0 < receiveTimeout ? receiveTimeout : DEFAULT_RECEIVE_TIMEOUT;
  }

  /**
   * method are called after the device is connected
   * 
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   */
  protected abstract String prepareDevice() throws CommConnectionException, RFIDReaderException;


  /**
   * @return the inputDebounceTime
   */
  public int getInputDebounceTime() {
    return inputDebounceTime;
  }

  /**
   * @param inputDebounceTime the inputDebounceTime to set (minimum 50ms)
   */
  public void setInputDebounceTime(int inputDebounceTime) {
    this.inputDebounceTime = inputDebounceTime > MIN_INPUT_DEBOUNCE_TIME ? inputDebounceTime : MIN_INPUT_DEBOUNCE_TIME;
  }

  /**
   * @return the input0
   */
  protected boolean isInput0() {
    return input0;
  }

  /**
   * @param input0 the input0 to set
   */
  protected void setInput0(boolean input0) {
    this.input0 = input0;
  }

  /**
   * @return the input1
   */
  protected boolean isInput1() {
    return input1;
  }

  /**
   * @param input1 the input1 to set
   */
  protected void setInput1(boolean input1) {
    this.input1 = input1;
  }

  protected void startNewInputThread(int input) {
    if (input == 0) {
      if (input0Working) {
        return;
      }
      input0Working = true;
      input0Thread = new Thread() {
        @Override
        public void run() {
          long debounceEnd = System.currentTimeMillis() + inputDebounceTime;
          lockInput0.lock();
          try {
            boolean lastState = input0;
            while (input0Working) {
              try {
                Thread.sleep(10);
              } catch (InterruptedException e) {
              }
              try {
                boolean nextState = getInput(0);
                if (lastState != nextState) {
                  debounceEnd = System.currentTimeMillis() + inputDebounceTime;
                  lastState = nextState;
                }
              } catch (RFIDReaderException | CommConnectionException e) {
                if (getLogger().isTraceEnabled()) {
                  getLogger().trace(e.getMessage(), e);
                }
              }
              input0Working = System.currentTimeMillis() < debounceEnd;
            }
            if (lastState != input0) {
              input0 = lastState;
              RfidReaderInputChange event =
                  new RfidReaderInputChange(getIdentifier(), System.currentTimeMillis(), 0, input0);
              eventHandler.inputChange(event);
            }
          } finally {
            lockInput0.unlock();
          }
        }
      };
      input0Thread.start();
    } else {
      if (input1Working) {
        return;
      }
      input1Working = true;
      input1Thread = new Thread() {
        @Override
        public void run() {
          long debounceEnd = System.currentTimeMillis() + inputDebounceTime;
          lockInput1.lock();
          try {
            boolean lastState = input1;
            while (input1Working) {
              try {
                Thread.sleep(10);
              } catch (InterruptedException e) {
              }
              try {
                boolean nextState = getInput(1);
                if (lastState != nextState) {
                  debounceEnd = System.currentTimeMillis() + inputDebounceTime;
                  lastState = nextState;
                }
              } catch (RFIDReaderException | CommConnectionException e) {
                if (getLogger().isTraceEnabled()) {
                  getLogger().trace(e.getMessage(), e);
                }
              }
              input1Working = System.currentTimeMillis() < debounceEnd;
            }
            if (lastState != input1) {
              input1 = lastState;
              RfidReaderInputChange event =
                  new RfidReaderInputChange(getIdentifier(), System.currentTimeMillis(), 1, input1);
              eventHandler.inputChange(event);
            }
          } finally {
            lockInput1.unlock();
          }
        }

      };
      input1Thread.start();
    }
  }

  /**
   * gets a byte array from a hex string
   * 
   * @param str hex string
   * @return byte array
   * @throws RFIDReaderException possible RFIDErrorCodes:
   *         <ul>
   *         <li>WPA, data are not hex data</li>
   *         <li>WDL, hex data have a wrong lenght</li>
   *         </ul>
   */
  protected byte[] getByteFromHexString(String str) throws RFIDReaderException {
    if (1 == str.length() % 2)
      throw new RFIDReaderException(RFIDErrorCodes.WDL, "Wrong hex data Length");
    if (!str.matches("\\p{XDigit}*"))
      throw new RFIDReaderException(RFIDErrorCodes.WPA, "No Hex String");
    byte[] data = new byte[str.length() / 2];
    for (int i = 0; i < data.length; i++) {
      data[i] = (byte) Integer.parseInt(str.substring(i * 2, i * 2 + 2), 16);
    }
    return data;
  }

  /**
   * Reads the current state of an input pin
   * 
   * @param pin input pin to read
   * @return true if input is high, false if input is low
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public abstract boolean getInput(int pin) throws RFIDReaderException, CommConnectionException;

  /**
   * Sets the state of an output pin
   * 
   * @param pin output pin
   * @param state true for high state, false for low state
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public abstract void setOutput(int pin, boolean state) throws RFIDReaderException, CommConnectionException;


  protected Inventory<T> getInternalInventory() {
    return inventory;
  }

  /**
   * Parse the Firmware Name and return the {@link ReaderType}
   * 
   * @return the {@link ReaderType}
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public abstract ReaderType getReaderType() throws RFIDReaderException, CommConnectionException;

  /**
   * get the Firmware Revision
   * 
   * @return the Firmware Revision as String
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public abstract String getFirmwareRevision() throws RFIDReaderException, CommConnectionException;

  /**
   * Gets the serial number
   * 
   * @return the serial number of the reader. The serial number is an ASCII string of 16 characters.
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public abstract String getSerialNumber() throws RFIDReaderException, CommConnectionException;

  /**
   * Gets the hardware revision
   * 
   * @return the hardware revision of the reader which corresponds to the PCB layout version printed on the board. The
   *         number is an ASCII string of four characters.
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public abstract String getHardwareRevision() throws RFIDReaderException, CommConnectionException;

  /**
   * Set the heartbeat interval
   * 
   * @param interval heartbeat interval
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  protected abstract void setHeartbeatInterval(int interval) throws RFIDReaderException, CommConnectionException;

  /**
   * Set the antenna port.
   * 
   * @param port antenna port for the connected multiplexer [0..15]
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public abstract void setAntennaPort(int port) throws CommConnectionException, RFIDReaderException;

  /**
   * In case you want to automatically switch between multiple antennas (e.g. trying to find all tags in a search area
   * that can only be searched using multiple antennas) you can use this automatic switching mode.<br>
   * 
   * Switching always starts with the lowest antenna port (0). Switching to the next antenna port oc- curs automatically
   * with the start of every tag manipulation command. No pin state is changed until the first tag manipulation
   * command.<br>
   * 
   * @param numberOfAntennas number of antennas [1..16], 0 for disable; Please note that for this parameter the number
   *        given is the counted number of participating antennas, not the antenna port numbers, thus stating a number
   *        "X" would stand for "X antennas participating".
   * 
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public abstract void setMultiplexAntennas(int numberOfAntennas) throws CommConnectionException, RFIDReaderException;

  /**
   * Looks for all tags in range of the reader and get all tags as a number of strings back<br>
   * 
   * @return List with the founded tags
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public abstract List<T> getInventory() throws RFIDReaderException, CommConnectionException;

  /**
   * Looks for all tags in range of the reader and call events with founded tags.
   * 
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public void startInventory() throws CommConnectionException, RFIDReaderException {
    startInventory(DEFAULT_TAG_LOST_TIME);
  }

  /**
   * Looks for all tags in range of the reader and call events with founded tags.
   * 
   * @param tagLostTime timeout in milliseconds for tag lost event
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public abstract void startInventory(long tagLostTime) throws CommConnectionException, RFIDReaderException;

  /**
   * Stops the current continues inventory
   * 
   * @return the current inventory
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs
   */
  public abstract List<T> stopInventory() throws CommConnectionException, RFIDReaderException;
}

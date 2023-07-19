/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH All rights reserved.
 *******************************************************************************/
/**
 * 
 */
package com.metratec.lib.rfidreader;


import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.metratec.lib.connection.CommConnectionException;
import com.metratec.lib.connection.ICommConnection;
import com.metratec.lib.rfidreader.event.EventHandler;
import com.metratec.lib.rfidreader.event.RfidReaderConnectionState;
import com.metratec.lib.rfidreader.event.RfidReaderEventListener;

/**
 * @author jbecke, man
 *
 */
class RFIDDataHandler {

  // private final Queue<String> responseBuffer = new ConcurrentLinkedQueue<>();
  private final Queue<String> sendBuffer = new ConcurrentLinkedQueue<>();

  private ReceiveListener receiveListener;
  private MetratecReader<?> device;

  private boolean isEOF = false;

  private int connectionReceiveTimeout = 10;
  /**
   * Default reader receive timeout
   */
  // protected final static int DEFAULT_RECEIVE_TIMEOUT = 10000;
  // private int receiveTimeout = DEFAULT_RECEIVE_TIMEOUT;

  private String identifier = "unknown";
  private int handlerState = 0;
  private final ICommConnection connection;
  private long minReconnectWaitTime = 2 * 1000;
  private long maxReconnectWaitTime = 60 * 1000; // 60 s
  private int tryReconnectCount = 0;
  private final Logger logger;
  private String stateMessage = "";
  private Exception lastException = null;
  private int heartBeat = -1;
  private boolean checkHeartbeat = false;
  private long lastReceiveTime = 0;
  private boolean isRunning = false;
  // private static final int checkReachableTimeout = 3000;
  // private boolean restartTUC = true;
  private RfidReaderEventListener eventListener;
  /** Carriage return sign */
  private static final int CARRIAGE_RETURN = 13;
  /** Line feed sign */
  private static final int LINE_FEED = 10;
  private boolean isConnectingForTheFirstTime = true;

  private StringBuilder recvBuf = new StringBuilder();
  private Thread internalThread;

  /**
   * Construct a new receive handler
   * 
   * @param identifier instance name
   * @param connection connection to the metraZ master
   * @param responseChecker {@link RFIDResponseChecker}
   * @param device {@link MetratecReader}
   * @throws CommConnectionException if connection failed
   */
  protected RFIDDataHandler(String identifier, ICommConnection connection,
      ReceiveListener receiveListener, MetratecReader<?> device) throws CommConnectionException {
    logger = LoggerFactory.getLogger(device.getClass());
    if (null == connection) {
      throw new CommConnectionException(ICommConnection.NOT_INITIALISED, "The connection is NULL!");
    }
    this.identifier = identifier;
    this.connection = connection;
    this.receiveListener = receiveListener;
    this.device = device;
  }

  /**
   * @param identifier the identifier to set
   */
  protected void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  /**
   * @param handler the {@link EventHandler}
   */
  protected void setEventHandler(EventHandler<?> handler) {
    this.eventListener = handler;
  }

  /**
   * @param event event
   */
  protected void connectionStateChanged(RfidReaderConnectionState event) {
    if (null != eventListener) {
      eventListener.connectionState(event);
    }
  }

  private void work() {
    if (logger.isDebugEnabled()) {
      logger.debug(identifier + " started");
    }

    isRunning = true;
    boolean reprint = false;
    int c;
    recvBuf.setLength(0);
    isConnectingForTheFirstTime = true;
    handlerState = MetratecReader.STATE_CONNECTING;
    while (isRunning) {
      try {
        switch (handlerState) {
          case MetratecReader.STATE_WAITING_FOR_RECONNECT:
            tryReconnectCount++;
            long waitTime;
            if (tryReconnectCount <= 10) {
              waitTime = minReconnectWaitTime * tryReconnectCount;
            } else {
              waitTime = minReconnectWaitTime * tryReconnectCount ^ 3;
              if (waitTime > maxReconnectWaitTime)
                waitTime = maxReconnectWaitTime;
            }
            long waitUntil = System.currentTimeMillis() + waitTime;
            while (waitUntil > System.currentTimeMillis() && isRunning) {
              try {
                TimeUnit.MILLISECONDS.sleep(100);
              } catch (InterruptedException e1) {
              }
            }
            handlerState = MetratecReader.STATE_CONNECTING;
            if (logger.isDebugEnabled()) {
              logger.debug(identifier + " StandardReader.STATE_CONNECTING");
            }
            continue;
          case MetratecReader.STATE_CONNECTING: // check connection
            try {
              if (!isConnected()) {
                connection.connect();
                connection.setRecvTimeout(connectionReceiveTimeout);
                /*
                 * If another device is already connected, we are able to establish a connection,
                 * but cannot receive data. Here we have to check whether we can receive or we have
                 * to reconnect. In case we destroy a message the corrupt message will be handled in
                 * the reader preparation. PLZ DON'T DELETE THIS LINE!
                 */
                connection.recv();
              }
              Thread thread = new Thread() {
                @Override
                public void run() {
                  try {
                    checkHeartbeat = false; // disable if it was enable
                    String message = device.prepareDevice();
                    handlerState = MetratecReader.STATE_RUNNING;
                    isConnectingForTheFirstTime = false;
                    if (logger.isDebugEnabled()) {
                      logger.debug(identifier + " StandardReader.STATE_RUNNING");
                    }
                    stateMessage = "running";
                    lastException = null;
                    connectionStateChanged(
                        new RfidReaderConnectionState(identifier, true, message));
                    tryReconnectCount = 0;
                  } catch (CommConnectionException | RFIDReaderException e) {
                    stateMessage = e.getMessage();
                    lastException = e;
                    handlerState = MetratecReader.STATE_WAITING_FOR_RECONNECT;
                    connectionStateChanged(
                        new RfidReaderConnectionState(identifier, false, stateMessage));
                    if (logger.isDebugEnabled()) {
                      logger.debug(
                          identifier + " " + e.getLocalizedMessage() + " WAITING_FOR_RECONNECT");
                    }
                    try {
                      connection.disconnect();
                    } catch (CommConnectionException e1) {
                      if (logger.isDebugEnabled()) {
                        logger.debug(identifier + " " + e1.toString());
                      }
                    }
                  }
                }
              };
              recvBuf.setLength(0);
              thread.start();
              handlerState = MetratecReader.STATE_CONFIGURING;
              if (logger.isDebugEnabled()) {
                logger.debug(identifier + " StandardReader.STATE_CONFIGURING");
              }
              stateMessage = "configuring";
            } catch (CommConnectionException e) {
              try {
                connection.disconnect();
              } catch (CommConnectionException e2) {
                if (logger.isDebugEnabled()) {
                  logger.debug(identifier + " error disconnect reader " + e2.getLocalizedMessage());
                }
              }
              switch (e.getErrorCode()) {
                case ICommConnection.RECV_TIMEOUT:
                  stateMessage = "receive timeout";
                  break;
                case ICommConnection.NO_DEVICES_FOUND:
                case ICommConnection.NOT_INITIALISED:
                case ICommConnection.UNHANDLED_ERROR:
                case ICommConnection.CONNECTION_LOST:
                case ICommConnection.ETHERNET_TIMEOUT:
                default:
                  stateMessage = e.getMessage();
                  break;
              }
              lastException = e;
              handlerState = MetratecReader.STATE_WAITING_FOR_RECONNECT;
              if (logger.isDebugEnabled()) {
                logger.debug(identifier + " StandardReader.STATE_WAITING_FOR_RECONNECT");
              }
              if (isConnectingForTheFirstTime) {
                connectionStateChanged(
                    new RfidReaderConnectionState(identifier, connection.isConnected(),
                        null != e.getMessage() ? e.getMessage() : e.getErrorDescription()));
                isConnectingForTheFirstTime = false;
              }
            }
            continue;
          case MetratecReader.STATE_CONFIGURING: // configuring - falls through
          case MetratecReader.STATE_RUNNING: // running
            try {
              if (!sendBuffer.isEmpty()) {
                String command = sendBuffer.poll();
                if (logger.isTraceEnabled()) {
                  logger.trace(identifier + " send: "
                      + command.replaceAll("\r", "<CR>").replaceAll("\n", "<LF>"));
                }
                connection.send(command);
              }
              while (-1 == (c = connection.recv()) && isRunning && sendBuffer.isEmpty()) {
                try {
                  Thread.sleep(10);
                } catch (InterruptedException e) {
                }
                checkHeartBeat();
              }
              if (2048 < connection.dataAvailable()) {
                if (logger.isDebugEnabled()) {
                  logger.debug(identifier + " data overflow " + connection.dataAvailable());
                }
                reprint = true;
              }
              if (0 < c) { // if 0 < c the while loop above was broken because new data are
                // available
                do {
                  recvBuf.append((char) c);
                  if (isEOF && LINE_FEED == c || !isEOF && CARRIAGE_RETURN == c) {
                    handleReceivedData(recvBuf);
                    recvBuf.setLength(0);
                    if (!sendBuffer.isEmpty()) {
                      break;
                    }
                  }
                } while (0 < (c = connection.recv()));
              }
              if (reprint) {
                reprint = false;
                if (logger.isDebugEnabled()) {
                  logger.debug(identifier + " data overflow " + connection.dataAvailable());
                }
              }
            } catch (CommConnectionException e) {
              if (ICommConnection.RECV_TIMEOUT == e.getErrorCode()) {
                if (logger.isDebugEnabled()) {
                  logger.debug(identifier + " Receive Timeout");
                }
              } else if (handlerState != MetratecReader.STATE_WAITING_FOR_RECONNECT) {
                stateMessage = e.getMessage();
                lastException = e;
                handlerState = MetratecReader.STATE_CONNECTING;
                if (logger.isTraceEnabled()) {
                  logger.trace(identifier + " StandardReader.STATE_CONNECTING");
                }
                try {
                  connection.disconnect();
                } catch (CommConnectionException e1) {
                  if (logger.isDebugEnabled()) {
                    logger.debug(identifier + " " + e1.toString());
                  }
                }
                connectionStateChanged(
                    new RfidReaderConnectionState(identifier, connection.isConnected(),
                        null != e.getMessage() ? e.getMessage() : e.getErrorDescription()));
              }
            }
            continue;
          case MetratecReader.STATE_STOPPED: // falls through
          default:
            // unknown or stopped state...stop the loop
            break;
        }
        break;
      } catch (Exception e) {
        if (logger.isErrorEnabled()) {
          logger.error(identifier + " " + e.getMessage());
        }
        if (logger.isTraceEnabled()) {
          logger.trace(identifier + " " + e.getMessage(), e);
        }
        lastException = e;
        stateMessage = "Error " + e.getMessage();
      }
    }

    try {
      connection.disconnect();
    } catch (CommConnectionException e) {
      logger.warn(e.toString());
    }

    handlerState = MetratecReader.STATE_STOPPED;
    if (!isRunning) {
      stateMessage = "stopped";
      lastException = null;
    }
    connectionStateChanged(
        new RfidReaderConnectionState(identifier, connection.isConnected(), stateMessage));
    if (logger.isDebugEnabled()) {
      logger.debug(identifier + " stopped");
    }
  }

  private void checkHeartBeat() throws CommConnectionException {
    if (!checkHeartbeat) {
      return;
    }
    if (lastReceiveTime + 3 * heartBeat < System.currentTimeMillis()) {
      throw new CommConnectionException(ICommConnection.CONNECTION_LOST,
          "Connection lost (no heart beat)");
    }
  }

  private void handleReceivedData(StringBuilder data) {
    if (logger.isTraceEnabled()) {
      logger.trace("{}  resp: {}", identifier,
          data.toString().replaceAll("\r", "<CR>").replaceAll("\n", "<LF>"));
    }
    lastReceiveTime = System.currentTimeMillis();
    receiveListener.dataReceived(data.toString());
  }

  /**
   * @param interval heart beat interval in seconds
   */
  protected void setHeartBeatInterval(int interval) {
    this.heartBeat = interval * 1000;
    lastReceiveTime = System.currentTimeMillis();
    checkHeartbeat = 0 < heartBeat;
  }

  /**
   * send command
   * 
   * @param command command
   * @throws CommConnectionException if the reader is not connected
   */
  protected void sendCommand(String command) throws CommConnectionException {
    if (!isConnected()) {
      throw new CommConnectionException(ICommConnection.NOT_INITIALISED, "not connected");
    }
    sendBuffer.offer(command);
  }

  /**
   * stop the handler
   */
  protected void stop() {
    if (isAlive()) {
      isRunning = false;
      // internalThread.interrupt();
      if (logger.isDebugEnabled()) {
        logger.debug(identifier + " StandardReader.STATE_STOPPED");
      }
      while (isAlive()) {
        try {
          Thread.sleep(10);
        } catch (InterruptedException e) {
          if (logger.isTraceEnabled()) {
            logger.trace("Sleep interrupted");
          }
        }
      }
    }
  }

  /**
   * @return true if connected
   */
  protected boolean isConnected() {
    if (null == connection) {
      return false;
    }
    if (!connection.isConnected()) {
      return false;
    }
    return handlerState == MetratecReader.STATE_RUNNING
        || handlerState == MetratecReader.STATE_CONFIGURING;
  }

  /**
   * @return true if is working
   */
  protected boolean isWorking() {
    return isConnected() && handlerState == MetratecReader.STATE_RUNNING;
  }

  /**
   * @return the handler state
   */
  protected int getHandlerState() {
    return handlerState;
  }

  /**
   * @return the state message
   */
  protected String getStateMessage() {
    return stateMessage;
  }

  /**
   * @return the lastException
   */
  public Exception getLastException() {
    return lastException;
  }

  /**
   * @return the isEOF
   */
  protected boolean isEOF() {
    return isEOF;
  }

  /**
   * @param isEOF the isEOF to set
   */
  protected void setEOF(boolean isEOF) {
    this.isEOF = isEOF;
  }

  /**
   * @return true if this handler is alive; false otherwise.
   */
  protected boolean isAlive() {
    return null != internalThread && internalThread.isAlive();
  }

  /**
   * 
   */
  protected void start() {
    if (!isAlive()) {

      internalThread = new Thread(new Runnable() {

        @Override
        public void run() {
          work();
        }
      }, "DH-" + identifier);
      internalThread.start();
    }

  }

  /**
   * Set the minimum time before attempting to reconnect. Default value 21600000ms (6h).
   * 
   * @param timeInMilliseconds maximum reconnect wait time in milliseconds
   */
  protected void setMaxReconnectWaitTime(long timeInMilliseconds) {
    this.maxReconnectWaitTime = timeInMilliseconds;
  }

  /**
   * Set the minimum time before attempting to reconnect. Default value 2000ms.
   * 
   * @param timeInMilliseconds minimum reconnect wait time in milliseconds
   */
  protected void setMinReconnectWaitTime(long timeInMilliseconds) {
    this.minReconnectWaitTime = timeInMilliseconds;
  }

  /**
   * Inform the data handler about the reader standby state
   * 
   * @param isStandby reader stand by state
   */
  protected void setReaderStandby(boolean isStandby) {
    if (isStandby) {
      checkHeartbeat = false;
    } else {
      checkHeartbeat = 0 < heartBeat;
    }
  }

}

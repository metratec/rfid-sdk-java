package com.metratec.lib.rfidreader.uhf;
/**
 * transponder response on the auth request
 */
public class UhfTagAuth {
  private String epc;
  private String shortTID;
  private String response;
  private String challenge;
  private boolean hasError;
  private String message;

  /**
   * The transponder returns an error on the auth request
   * 
   * @param epc
   * @param message
   */
  public UhfTagAuth(String epc, String message) {
    this.epc = epc;
    this.message = message;
    this.hasError = true;
  }

  /**
   * The transponder returns a valid response
   * 
   * @param epc
   * @param shortTID
   * @param response
   * @param challenge
   */
  public UhfTagAuth(String epc, String shortTID, String response, String challenge) {
    this.epc = epc;
    this.shortTID = shortTID;
    this.response = response;
    this.challenge = challenge;
    this.hasError = false;
  }

  /**
   * @return the transponder epc
   */
  public String getEpc() {
    return epc;
  }

  /**
   * @param epc the transponder epc to set
   */
  protected void setEpc(String epc) {
    this.epc = epc;
  }

  /**
   * @return the short TID
   */
  public String getShortTID() {
    return shortTID;
  }

  /**
   * @param shortTID the short TID
   */
  protected void setShortTID(String shortTID) {
    this.shortTID = shortTID;
  }

  /**
   * @return the transponder response
   */
  public String getResponse() {
    return response;
  }

  /**
   * @param response the transponder response to set
   */
  protected void setResponse(String response) {
    this.response = response;
  }

  /**
   * @return the transponder challenge
   */
  public String getChallenge() {
    return challenge;
  }

  /**
   * @param challenge the transponder challenge to set
   */
  protected void setChallenge(String challenge) {
    this.challenge = challenge;
  }

  /**
   * @return true, if the transponder report an error
   */
  public boolean hasError() {
    return hasError;
  }

  /**
   * @param hasError if the transponder response an error
   */
  protected void setHasError(boolean hasError) {
    this.hasError = hasError;
  }

  /**
   * @return the error message
   */
  public String getMessage() {
    return message;
  }

  /**
   * @param message the error message to set
   */
  protected void setMessage(String message) {
    this.message = message;
  }


}

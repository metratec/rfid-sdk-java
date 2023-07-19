/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader;

/**
 * Error codes for the metraTec RFID reader exceptions
 * 
 * @author Matthias Neumann (neumann@metratec.com)
 *
 */
public class RFIDErrorCodes {
  /** Not expected response */
  public static final int NER = 0x00000001;
  /** Null pointer */
  public static final int NUL = 0x00000002;
  /** Wrong Parameter */
  public static final int WPA = 0x00000003;
  /** internal Error code */
  public static final int IEC = 0x00000004;
  /** User Error code 1 */
  public static final int UE1 = 0x00000005;
  /** User Error code 2 */
  public static final int UE2 = 0x00000006;
  /** User Error code 3 */
  public static final int UE3 = 0x00000007;
  /** Wrong Reader type */
  public static final int WRT = 0x00000008;
  /** Reserved */
  public static final int RES = 0x00000009;
  /** wrong minimal Firmware revision */
  public static final int WFR = 0x0000000A;
  /** Reader is busy */
  public static final int BSY = 0x0000000B;
  /** Tag Listener missing */
  public static final int TLM = 0x0000000C;


  // HF
  /** REQ - Collision detect */
  public static final int CLD = 0x00000030;
  /**
   * REQ - Tag Error Code<br>
   * [2Byte Error Flag][2Byte ErrorCode]<br>
   * Error codes:
   * <ul>
   * <li>01 - The command is not supported, i.e. the request code is not recognized.</li>
   * <li>02 - The command is not recognized, for example: a format error occurred.</li>
   * <li>03 - The option is not supported.</li>
   * <li>0F - Unknown error.</li>
   * <li>10 - The specified block is not available (doesn't exist).</li>
   * <li>11 - The specified block is already -locked and thus cannot be locked again</li>
   * <li>12 - The specified block is locked and its content cannot be changed.</li>
   * <li>13 - The specified block was not successfully programmed.</li>
   * <li>14 - The specified block was not successfully locked.</li>
   * <li>A0-DF - Custom command error codes</li>
   * <li>others - RFU</li>
   * </ul>
   */
  public static final int TEC = 0x00000031;

  // UHF
  /** Access Error */
  public static final int ACE = 0x00000040;
  /** CRC error, CRC16 from the Tag is wrong */
  public static final int CER = 0x00000041;
  /** FIFO Length Error */
  public static final int FLE = 0x00000042;
  /** Headerbit Error */
  public static final int HBE_XX = 0x00000043;
  /** Preamble Detect Error */
  public static final int PDE = 0x00000044;
  /** Read Data to Long */
  public static final int RDL = 0x00000045;
  /** Response Count Expected Error */
  public static final int RXE = 0x00000046;
  /** Tag Communication Error */
  public static final int TCE = 0x00000047;
  /** Too Many Tags */
  public static final int TMT = 0x00000048;
  /** TimeOut Error */
  public static final int TOE = 0x00000049;
  /** Tag Out of Range */
  public static final int TOR = 0x0000004A;

  // Mifare
  /** Tag Not Responding */
  public static final int TNR = 0x00000050;
  /** Card is not selected */
  public static final int CNS = 0x00000051;
  /** Block Is too high (i.e. bigger than 63 at MiFare 1k) */
  public static final int BIH = 0x00000052;
  /** Authentication Error (i.e. wrong key) */
  public static final int ATE = 0x00000053;
  /** No Key Selected, select a temporary or a static key */
  public static final int NKS = 0x00000054;
  /** Input and Outputblock are not in the same Sector */
  public static final int IOS = 0x00000055;
  /** No MiFare classic chip Authenticated */
  public static final int NMA = 0x00000056;
  /** No Data Block */
  public static final int NDB = 0x00000057;
  /** Key B is Readable */
  public static final int KBR = 0x00000058;
  /** Operation Not Executed */
  public static final int ONE = 0x00000059;
  /** Block Mode Error, not 0 or 3 (not writeable with value block function) */
  public static final int BME = 0x0000005A;
  /** Block Not Writable */
  public static final int BNW = 0x0000005B;
  /** Block Access Error */
  public static final int BAE = 0x0000005C;
  /** Block Not Authenticated */
  public static final int BNA = 0x0000005D;
  /** Access bits or Keys not Writable */
  public static final int AKW = 0x0000005E;
  /** Use Key B for authentication */
  public static final int UKB = 0x0000005F;
  /** Use Key A for authentication */
  public static final int UKA = 0x00000060;
  /** Key(s) not changeable */
  public static final int KNC = 0x00000061;
  /** Number of Blocks to Read is 0 */
  public static final int NB0 = 0x00000062;
  /** No Tag Inventoried */
  public static final int NTI = 0x00000063;
  /** To Many Data (i.e. Uart input buffer overflow) */
  public static final int TMD = 0x00000064;

  // Transceiver Errors
  /** Antenna Reflectivity High */
  public static final int ARH = 0x00000010;
  /** BrownOut detected */
  public static final int BOD = 0x00000011;
  /** Buffer Overflow */
  public static final int BOF = 0x00000012;
  /** Communication CRC Error */
  public static final int CCE = 0x00000013;
  /** Command Received Timeout */
  public static final int CRT = 0x00000014;
  /** Did not Sleep */
  public static final int DNS = 0x00000015;
  /** Error decimal expected */
  public static final int EDX = 0x00000016;
  /** Error Hardware Failure */
  public static final int EHF = 0x00000017;
  /** Error hex expected */
  public static final int EHX = 0x00000018;
  /** Not in CNR Mode */
  public static final int NCM = 0x00000019;
  /** Number Out Of Range */
  public static final int NOR = 0x0000001A;
  /** Not supported */
  public static final int NOS = 0x0000001B;
  /** No RF-Field active */
  public static final int NRF = 0x0000001C;
  /** No Standard Selected */
  public static final int NSS = 0x0000001D;
  /** PLL Error */
  public static final int PLE = 0x0000001E;
  /** Hardware Reset */
  public static final int SRT = 0x0000001F;
  /** Unknown command */
  public static final int UCO = 0x00000020;
  /** Unknown Error */
  public static final int UER = 0x00000021;
  /** Unknown parameter */
  public static final int UPA = 0x00000022;
  /** UART Receive Error */
  public static final int URE = 0x00000023;
  /** Wrong Data Length */
  public static final int WDL = 0x00000024;

  // SHL2100 Errorcodes
  /** Error */
  public static final int SYN = 0x00000100;
  /** Check Sum error */
  public static final int CSE = 0x00000101;
  /** Global Error */
  public static final int NAK = 0x00000102;
  /** (receive) unknow control byte */
  public static final int UCB = 0x00000103;
  /** wrong baudrate */
  public static final int WBR = 0x00000104;

}

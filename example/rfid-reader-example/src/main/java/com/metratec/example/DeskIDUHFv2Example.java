package com.metratec.example;

import java.io.IOException;
import java.util.List;

import com.metratec.lib.connection.CommConnectionException;
import com.metratec.lib.rfidreader.RFIDReaderException;
import com.metratec.lib.rfidreader.event.RfidReaderConnectionState;
import com.metratec.lib.rfidreader.event.RfidReaderEventListener;
import com.metratec.lib.rfidreader.event.RfidTagEventListener;
import com.metratec.lib.rfidreader.event.RfidTagFound;
import com.metratec.lib.rfidreader.event.RfidTagLost;
import com.metratec.lib.rfidreader.uhf.DeskID_UHF_v2;
import com.metratec.lib.rfidreader.uhf.UHFReaderAT.MEMBANK;
import com.metratec.lib.tag.UhfTag;

/**
 * Example class demonstrating DeskID UHF v2 reader functionality.
 * 
 * This class provides automated demos for: - Serial and USB connection support (/dev/ttyUSB0 default) - UHF EPC Gen 2
 * transponder inventory operations with RSSI/antenna info - Memory bank operations (EPC, TID, USR) with EPC mask
 * targeting - Real-time tag detection with comprehensive event listeners - Advanced features: EPC/hex masking, single
 * slot inventory, inventory reports - Tag-specific operations using EPC mask for precise targeting
 */
public class DeskIDUHFv2Example {

    /**
     * Demonstrate UHF inventory operations
     */
    public void performInventoryExample() {
        // Create the reader instance using serial communication
        // Note: Update "/dev/ttyACM0" to match your actual device path (Linux/Mac) or "COM#" for Windows
        String serialPort = "/dev/ttyACM0";
        DeskID_UHF_v2 reader = new DeskID_UHF_v2("DeskID-UHF-v2", serialPort, 115200, 8, 1, 0, 0);
        System.out.println("\n--- UHF Inventory Example ---");

        // Add a listener to the reader status events
        reader.setReaderEventListener(new RfidReaderEventListener() {
            @Override
            public void connectionState(RfidReaderConnectionState event) {
                System.out.println("Reader Status: " + event.getIdentifier() + " - " + event.getMessage());
            }
        });

        // Add a listener to the tag detection events
        reader.setTagEventListener(new RfidTagEventListener<UhfTag>() {
            @Override
            public void tagFound(RfidTagFound<UhfTag> event) {
                UhfTag tag = event.getTag();
                System.out.println("UHF Tag FOUND: " + tag.getEpc()
                        + (tag.getTid() != null ? " [TID: " + tag.getTid() + "]" : ""));

                // Show additional tag information if available
                if (tag.getData() != null && !tag.getData().isEmpty()) {
                    System.out.println("  Tag Data: " + tag.getData());
                }
                if (tag.getRssi() != null) {
                    System.out.println("  RSSI: " + tag.getRssi() + " dBm");
                }
                if (tag.getAntenna() != null) {
                    System.out.println("  Antenna: " + tag.getAntenna());
                }
            }

            @Override
            public void tagLost(RfidTagLost<UhfTag> event) {
                System.out.println("UHF Tag LOST: " + event.getTag().getEpc());
            }
        });

        try {
            // Connect to reader
            System.out.println("Connecting to DeskID UHF v2 reader...");
            reader.startAndWait(5000);
            System.out.println("Successfully connected!");

            // Get reader information
            try {
                String firmware = reader.getFirmwareRevision();
                System.out.println("Reader firmware: " + firmware);
            } catch (Exception e) {
                System.out.println("Could not read firmware version: " + e.getMessage());
            }

            // Single inventory scan
            System.out.println("\n--- Single Inventory Scan ---");
            List<UhfTag> tags = reader.getInventory();
            System.out.println("Found " + tags.size() + " UHF tag(s):");

            for (UhfTag tag : tags) {
                System.out.println("  - EPC: " + tag.getEpc());
                System.out.println("    ID: " + tag.getId());
                if (tag.getTid() != null && !tag.getTid().isEmpty()) {
                    System.out.println("    TID: " + tag.getTid());
                }
                if (tag.getData() != null && !tag.getData().isEmpty()) {
                    System.out.println("    Data: " + tag.getData());
                }
                if (tag.getRssi() != null) {
                    System.out.println("    RSSI: " + tag.getRssi() + " dBm");
                }
                System.out.println();
            }

            // Continuous inventory if tags found
            if (!tags.isEmpty()) {
                System.out.println("\n--- Starting Continuous Inventory with tag lost time 2000ms---");
                System.out.println("--- Running continuous inventory until Enter is pressing ---");
                reader.startInventory(2000);

                long nextReport = 0L;
                try {
                    while (0 == System.in.available()) {
                        if (nextReport <= System.currentTimeMillis()) {
                            // get current inventory
                            System.out.println(reader.getInventory());
                            nextReport = System.currentTimeMillis() + 2500;
                        }
                    }
                } catch (IOException e) {
                    // Stopping
                }
                reader.stopInventory();
                System.out.println("Inventory stopped.");
            } else {
                System.out
                        .println("No UHF tags found. Please place EPC Gen 2 tags near the reader for continuous demo.");
            }

            // Demonstrate inventory report (single shot with duration)
            System.out.println("\n--- Inventory Report (250 milliseconds scan) ---");
            List<UhfTag> reportTags = reader.getInventoryReport(250); // 250 milliseconds scan
            System.out.println("Inventory report found " + reportTags.size() + " tag(s)");
            for (UhfTag tag : reportTags) {
                System.out.println("  Report tag: " + tag.getEpc() + " (seen " + tag.getSeenCount() + " times)");
            }

        } catch (CommConnectionException e) {
            System.err.println("Connection error: " + e.getLocalizedMessage());
        } catch (RFIDReaderException e) {
            System.err.println("Reader error: " + e.getLocalizedMessage());
        } finally {
            try {
                reader.stop();
                System.out.println("Disconnected from DeskID UHF v2 reader.");
            } catch (Exception e) {
                System.err.println("Error disconnecting: " + e.getLocalizedMessage());
            }
        }
    }

    /**
     * Demonstrate UHF read/write operations on different memory banks using EPC targeting
     */
    public void performReadWriteExample() {
        // Create the reader instance using serial communication
        // Note: Update "/dev/ttyACM0" to match your actual device path (Linux/Mac) or "COM#" for Windows
        String serialPort = "/dev/ttyACM0";
        DeskID_UHF_v2 reader = new DeskID_UHF_v2("DeskID-UHF-v2", serialPort, 115200, 8, 1, 0, 0);
        System.out.println("\n--- UHF Read/Write Example ---");

        try {
            // Connect to reader
            System.out.println("Connecting to DeskID UHF v2 reader...");
            reader.startAndWait(5000);
            System.out.println("Successfully connected!");

            // Find UHF tags
            System.out.println("\n--- Finding UHF Tags ---");
            List<UhfTag> tags = reader.getInventory();
            if (tags.isEmpty()) {
                System.out.println("No UHF tags found! Please place an EPC Gen 2 tag near the reader.");
                return;
            }

            UhfTag firstTag = tags.get(0);
            System.out.println("Working with UHF tag EPC: " + firstTag.getEpc());

            // Read TID memory bank
            System.out.println("\n--- Reading TID Memory Bank ---");
            try {
                List<UhfTag> tidTags = reader.getTagData(MEMBANK.TID, 0, 8, firstTag.getEpc());
                if (tidTags.isEmpty()) {
                    System.out.println("Could not read TID data - Tag not found");
                } else if (!tidTags.get(0).hasError()) {
                    String tid = tidTags.get(0).getTid();
                    System.out.println("TID data: " + tid);
                } else {
                    System.out.println("Could not read TID data - " + tidTags.get(0).getMessage());
                }
            } catch (Exception e) {
                System.out.println("TID read failed: " + e.getMessage());
            }

            // Read EPC memory bank
            System.out.println("\n--- Reading EPC Memory Bank ---");
            try {
                List<UhfTag> epcTags = reader.getTagData(MEMBANK.EPC, 0, 24, firstTag.getEpc());
                if (epcTags.isEmpty()) {
                    System.out.println("Could not read EPC data - Tag not found");
                } else if (!epcTags.get(0).hasError()) {
                    String epcData = epcTags.get(0).getData();
                    System.out.println("EPC memory data: " + epcData);
                } else {
                    System.out.println("Could not read EPC data - " + epcTags.get(0).getMessage());
                }
            } catch (Exception e) {
                System.out.println("EPC read failed: " + e.getMessage());
            }

            // Test User memory bank read/write
            System.out.println("\n--- User Memory Bank Operations ---");
            try {
                // Try to read user memory first
                List<UhfTag> userReadTags = reader.getTagData(MEMBANK.USR, 0, 16, firstTag.getEpc()); // Read 4 words (16
                                                                                                     // bytes)
                if (!userReadTags.isEmpty()) {
                    String currentUserData = userReadTags.get(0).getData();
                    System.out.println("Current user memory: "
                            + (currentUserData != null && !currentUserData.isEmpty() ? currentUserData
                                    : "(empty/zeros)"));

                    // Write test data to user memory
                    String testData =
                            String.format("%016X%016X", System.currentTimeMillis(), System.currentTimeMillis());
                    System.out.println("Writing test data to user memory: " + testData);

                    List<UhfTag> writeResult = reader.setTagData(MEMBANK.USR, 0, testData, firstTag.getEpc());
                    if (!writeResult.isEmpty() && !writeResult.get(0).hasError()) {
                        System.out.println("Write operation completed successfully");

                        // Verify the write
                        List<UhfTag> verifyTags = reader.getTagData(MEMBANK.USR, 0, 16, firstTag.getEpc());
                        if (!verifyTags.isEmpty() && verifyTags.get(0).getData() != null) {
                            String readBackData = verifyTags.get(0).getData();
                            System.out.println("Read back data: " + readBackData);

                            if (readBackData.toUpperCase().startsWith(testData.toUpperCase())) {
                                System.out.println("Write verification SUCCESSFUL!");
                            } else {
                                System.out.println("Write verification FAILED - data mismatch");
                            }
                        } else {
                            System.out.println("Verification read failed");
                        }
                    } else {
                        System.out.println("Write operation failed - "
                                + (writeResult.isEmpty() ? "tag not found" : writeResult.get(0).getMessage()));
                    }
                }
            } catch (Exception e) {
                System.out.println("User memory operations not supported or failed: " + e.getMessage());
                System.out.println("This is normal for tags without user memory or with locked memory");
            }

            // Demonstrate EPC writing
            System.out.println("\n--- EPC Writing Example ---");
            try {
                String currentEPC = firstTag.getEpc();
                System.out.println("Current EPC: " + currentEPC);

                // Create a new EPC (modify last 4 characters)
                String newEPC = currentEPC;
                if (currentEPC.length() >= 4) {
                    String prefix = currentEPC.substring(0, currentEPC.length() - 4);
                    String suffix = String.format("%04X", (int) (System.currentTimeMillis() % 65536));
                    newEPC = prefix + suffix;
                } else {
                    newEPC = String.format("%024X", System.currentTimeMillis()); // 12-byte EPC
                }

                System.out.println("Writing new EPC: " + newEPC);

                // Set mask to target only this tag
                reader.setMask(MEMBANK.EPC, 0, currentEPC);

                List<UhfTag> epcWriteResult = reader.setTagEpc(0, newEPC);
                if (!epcWriteResult.isEmpty() && !epcWriteResult.get(0).hasError()) {
                    System.out.println("EPC write operation completed");

                    // Clear mask for verification
                    reader.resetMask();
                    // Verify by reading inventory
                    List<UhfTag> verifyTags = reader.getInventory();
                    boolean found = false;
                    for (UhfTag tag : verifyTags) {
                        if (newEPC.equalsIgnoreCase(tag.getEpc())) {
                            System.out.println("EPC write verification SUCCESSFUL!");
                            System.out.println("New tag EPC: " + tag.getEpc());
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        System.out.println("EPC write verification FAILED - new EPC not found");
                    }
                } else {
                    System.out.println("EPC write failed - "
                                + (epcWriteResult.isEmpty() ? "tag not found" : epcWriteResult.get(0).getMessage()));
                }
                // Clear any masks
                reader.resetMask();

            } catch (Exception e) {
                System.out.println("EPC write failed: " + e.getMessage());
                System.out.println("This could be due to locked EPC memory or write protection");
            }

        } catch (CommConnectionException e) {
            System.err.println("Connection error: " + e.getLocalizedMessage());
        } catch (RFIDReaderException e) {
            System.err.println("Reader error: " + e.getLocalizedMessage());
        } finally {
            try {
                reader.stop();
                System.out.println("Disconnected from DeskID UHF v2 reader.");
            } catch (Exception e) {
                System.err.println("Error disconnecting: " + e.getLocalizedMessage());
            }
        }
    }

    /**
     * Main method to run this example independently
     */
    public static void main(String[] args) {
        System.out.println("=== DeskID UHF v2 Example ===");
        DeskIDUHFv2Example example = new DeskIDUHFv2Example();
        example.performInventoryExample();
        example.performReadWriteExample();
    }

}

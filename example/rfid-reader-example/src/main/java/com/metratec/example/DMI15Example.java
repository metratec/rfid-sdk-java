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
import com.metratec.lib.rfidreader.iso.DMI15;
import com.metratec.lib.tag.HfTag;

/**
 * Example class demonstrating DMI15 Industrial HF RFID reader functionality.
 * 
 * The DMI15 is an industrial short range RFID reader for 13.56 MHz with an integrated antenna and power supply via
 * Ethernet (PoE). It features PLC compatible inputs/outputs.
 * 
 * This class provides automated demos for: - TCP/Ethernet connection (default) - Inventory operations for ISO 15693
 * tags - Block-based read/write operations - Industrial-grade tag processing
 */
public class DMI15Example {

    /**
     * Demonstrate DMI15 inventory operations
     */
    public void performInventoryExample() {
        // Create the reader instance using Ethernet communication
        // Note: Update IP address and port to match your DMI15 network configuration
        // DMI15 typically uses PoE and standard port 10001
        String hostname = "192.168.2.239";
        DMI15 reader = new DMI15("DMI15", hostname, 10001);
        System.out.println("\n--- HF Inventory Example ---");

        // add a listener to the reader status events
        reader.setReaderEventListener(new RfidReaderEventListener() {
            @Override
            public void connectionState(RfidReaderConnectionState event) {
                System.out.println("Reader Status: " + event.getIdentifier() + " - " + event.getMessage());
            }
        });

        // add a listener to the tag detection events
        reader.setTagEventListener(new RfidTagEventListener<HfTag>() {
            @Override
            public void tagFound(RfidTagFound<HfTag> event) {
                HfTag tag = event.getTag();
                System.out.println(
                        "HF Tag FOUND: " + tag.getId() + " [TID: " + tag.getTid() + ", Type: " + tag.getType() + "]");
            }

            @Override
            public void tagLost(RfidTagLost<HfTag> event) {
                System.out.println("HF Tag LOST: " + event.getTag().getId());
            }
        });

        try {
            System.out.println("Connecting to DMI15 reader at " + hostname + "...");
            reader.startAndWait(5000);
            System.out.println("Successfully connected!");

            // Single inventory scan
            System.out.println("\n--- Single Inventory Scan ---");
            List<HfTag> tags = reader.getInventory();
            System.out.println("Found " + tags.size() + " HF tag(s):");

            for (HfTag tag : tags) {
                System.out.println("  - Tag ID: " + tag.getId());
            }

            // Continuous inventory if tags found
            if (!tags.isEmpty()) {
                System.out.println("\n--- Starting Continuous Inventory with tag lost time 2000ms ---");
                System.out.println("--- Running continuous inventory until Enter is pressed ---");
                reader.startInventory(2000);

                long nextReport = 0L;
                try {
                    while (0 == System.in.available()) {
                        if (nextReport <= System.currentTimeMillis()) {
                            List<HfTag> currentInventory = reader.getInventory();
                            System.out.println("Current inventory: " + currentInventory);
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
                        .println("No HF tags found. Please place ISO 15693 tags near the reader for continuous demo.");
            }

        } catch (CommConnectionException e) {
            System.err.println("Connection error: " + e.getLocalizedMessage());
        } catch (RFIDReaderException e) {
            System.err.println("Reader error: " + e.getLocalizedMessage());
        } finally {
            try {
                reader.stop();
                System.out.println("Disconnected from DMI15 reader.");
            } catch (Exception e) {
                System.err.println("Error disconnecting: " + e.getLocalizedMessage());
            }
        }
    }

    /**
     * Demonstrate DMI15 read/write operations
     */
    public void performReadWriteExample() {
        // Create the reader instance using Ethernet communication
        // Note: Update IP address and port to match your DMI15 network configuration
        // DMI15 typically uses PoE and standard port 10001
        String hostname = "192.168.2.239";
        DMI15 reader = new DMI15("DMI15", hostname, 10001);
        System.out.println("\n--- HF Read/Write Example ---");

        try {
            // Connect
            System.out.println("Connecting to reader...");
            reader.startAndWait(5000);
            System.out.println("Successfully connected!");

            // Find tags
            System.out.println("\n--- Finding HF Tags ---");
            List<HfTag> tags = reader.getInventory();
            if (tags.isEmpty()) {
                System.out.println("No HF tags found! Please place an ISO 15693 tag near the reader.");
                return;
            }

            HfTag firstTag = tags.get(0);
            System.out.println("Working with tag: " + firstTag.getId());
            System.out.println("Tag TID: " + firstTag.getTid());

            // Read blocks
            System.out.println("\n--- Reading Tag Blocks ---");

            // Read block 0
            System.out.println("Reading block 0...");
            HfTag block0Result = reader.getTagData(0, firstTag.getTid());
            if (block0Result != null && !block0Result.hasError()) {
                System.out.println("Block 0: " + block0Result.getData());
            } else {
                System.out.println("Failed to read block 0: "
                        + (block0Result != null ? block0Result.getMessage() : "No response"));
            }

            // Read multiple blocks
            System.out.println("Reading block 0 to 2 (3 blocks)");
            HfTag block1Result = reader.getTagData(0, 2, firstTag.getTid());
            if (block1Result != null && !block1Result.hasError()) {
                String currentData = block1Result.getData();
                System.out.println("Block 1 current data: "
                        + (currentData == null || currentData.isEmpty() ? "(empty)" : currentData));
            } else {
                System.out.println("Failed to read block 1: "
                        + (block1Result != null ? block1Result.getMessage() : "No response"));
            }

            // Write data to the tag starting with block 0
            System.out.println("\n--- Writing data ---");
            String testData = String.format("%08X", (int) (System.currentTimeMillis() / 1000));
            System.out.println("Writing test data: " + testData);

            HfTag writeResult = reader.setTagData(0, testData, firstTag.getTid());
            if (writeResult != null && !writeResult.hasError()) {
                System.out.println("Write successful!");

                // Verify write
                HfTag verifyResult = reader.getTagData(0, firstTag.getTid());
                if (verifyResult != null && !verifyResult.hasError()) {
                    String readBackData = verifyResult.getData();
                    System.out.println("Verified data: " + readBackData);

                    if (readBackData != null && readBackData.toUpperCase().equals(testData.toUpperCase())) {
                        System.out.println("Write verification SUCCESSFUL!");
                    } else {
                        System.out.println("Write verification FAILED - data mismatch");
                    }
                } else {
                    System.out.println("Verification read failed");
                }
            } else {
                System.out.println("Write failed: " + (writeResult != null ? writeResult.getMessage() : "No response"));
            }

        } catch (CommConnectionException e) {
            System.err.println("Connection error: " + e.getLocalizedMessage());
        } catch (RFIDReaderException e) {
            System.err.println("Reader error: " + e.getLocalizedMessage());
        } finally {
            try {
                reader.stop();
                System.out.println("Disconnected from DMI15 reader.");
            } catch (Exception e) {
                System.err.println("Error disconnecting: " + e.getLocalizedMessage());
            }
        }
    }

    /**
     * Main method to run this example independently
     */
    public static void main(String[] args) {
        System.out.println("=== DMI15 Example ===");
        DMI15Example example = new DMI15Example();
        example.performInventoryExample();
        example.performReadWriteExample();
    }

}

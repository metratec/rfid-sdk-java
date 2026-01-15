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
import com.metratec.lib.rfidreader.nfc.DeskID_NFC;
import com.metratec.lib.rfidreader.nfc.NFCReaderAT.NFCReaderMode;
import com.metratec.lib.rfidreader.nfc.NFCReaderAT.KeyType;
import com.metratec.lib.tag.HfTag;

/**
 * Example class demonstrating DeskID NFC reader functionality.
 * 
 * This class provides automated demos for: - Serial and USB connection support (/dev/ttyACM0 default) - AUTO mode
 * inventory for ISO 15693 (NFC Type 5) and ISO 14443 (Mifare) tags - Tag type detection with detailed tag information -
 * Mifare Classic read/write operations with authentication - ISO 15693 read/write operations with block-based memory
 * access - Real-time continuous inventory with interactive control - Comprehensive tag data display (ID, Type, RSSI,
 * Data)
 */
public class DeskIDNFCExample {

    /**
     * Demonstrate NFC inventory operations in AUTO mode
     */
    public void performInventoryExample() {
        // Create the reader instance using serial communication
        // Note: Update "/dev/ttyACM0" to match your actual device path (Linux/Mac) or "COM#" for Windows
        String serialPort = "/dev/ttyACM0";
        DeskID_NFC reader = new DeskID_NFC("DeskID-NFC-USB", serialPort, 115200, 8, 1, 0, 0);
        System.out.println("\n--- NFC Inventory Example (AUTO Mode) ---");

        // Add a listener to the reader status events
        reader.setReaderEventListener(new RfidReaderEventListener() {
            @Override
            public void connectionState(RfidReaderConnectionState event) {
                System.out.println("Reader Status: " + event.getIdentifier() + " - " + event.getMessage());
            }
        });

        // Add a listener to the tag detection events
        reader.setTagEventListener(new RfidTagEventListener<HfTag>() {
            @Override
            public void tagFound(RfidTagFound<HfTag> event) {
                HfTag tag = event.getTag();
                System.out.println("NFC Tag FOUND: " + tag.getId() + " [TID: " + tag.getTid() + ", Type: "
                        + (tag.getType() != null ? tag.getType() : "Unknown") + "]");

                // Show additional tag information if available
                if (tag.getData() != null && !tag.getData().isEmpty()) {
                    System.out.println("  Tag Data: " + tag.getData());
                }
                if (tag.getRssi() != null) {
                    System.out.println("  RSSI: " + tag.getRssi() + " dBm");
                }
            }

            @Override
            public void tagLost(RfidTagLost<HfTag> event) {
                System.out.println("NFC Tag LOST: " + event.getTag().getId());
            }
        });

        try {
            // Connect to reader
            System.out.println("Connecting to DeskID NFC reader...");
            reader.startAndWait(5000);
            System.out.println("Successfully connected!");

            // Set reader to AUTO mode to detect both ISO 15693 and ISO 14443 tags
            // To detect only ISO 15693 tags, set the mode to NFCReaderMode.ISO15
            // To detect only ISO 14443 tags, set the mode to NFCReaderMode.ISO14A
            reader.setReaderMode(NFCReaderMode.AUTO);
            System.out.println("Reader mode set to AUTO");

            // Single inventory scan
            System.out.println("\n--- Single Inventory Scan ---");
            List<HfTag> tags = reader.getInventory();
            System.out.println("Found " + tags.size() + " NFC tag(s):");
            for (HfTag tag : tags) {
                System.out.println("  - Tag ID: " + tag.getId());
                System.out.println("    Type: " + (tag.getType() != null ? tag.getType() : "Unknown"));
                if (tag.getData() != null && !tag.getData().isEmpty()) {
                    System.out.println("    Data: " + tag.getData());
                }
                if (tag.getRssi() != null) {
                    System.out.println("    RSSI: " + tag.getRssi() + " dBm");
                }
                System.out.println();
            }

            // Single inventory scan
            System.out.println("\n--- Detect Tag Types Scan ---");
            List<HfTag> detectedTags = reader.detectTagTypes();
            System.out.println("Found " + detectedTags.size() + " NFC tag(s):");
            for (HfTag tag : detectedTags) {
                System.out.println("  - Tag ID: " + tag.getId());
                System.out.println("    Type: " + (tag.getType() != null ? tag.getType() : "Unknown"));
                if (tag.getData() != null && !tag.getData().isEmpty()) {
                    System.out.println("    Data: " + tag.getData());
                }
                if (tag.getRssi() != null) {
                    System.out.println("    RSSI: " + tag.getRssi() + " dBm");
                }
                System.out.println();
            }

            // Continuous inventory if tags found
            if (!tags.isEmpty() || !detectedTags.isEmpty()) {
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
                System.out.println("No NFC tags found.");
            }

        } catch (CommConnectionException e) {
            System.err.println("Connection error: " + e.getLocalizedMessage());
        } catch (RFIDReaderException e) {
            System.err.println("Reader error: " + e.getLocalizedMessage());
        } finally {
            try {
                reader.stop();
                System.out.println("Disconnected from DeskID NFC reader.");
            } catch (Exception e) {
                System.err.println("Error disconnecting: " + e.getLocalizedMessage());
            }
        }
    }

    /**
     * Demonstrate Mifare Classic read/write operations
     */
    public void performMifareReadWriteExample() {
        // Create the reader instance using serial communication
        // Note: Update "/dev/ttyACM0" to match your actual device path (Linux/Mac) or "COM#" for Windows
        String serialPort = "/dev/ttyACM0";

        DeskID_NFC reader = new DeskID_NFC("DeskID-NFC-USB", serialPort, 115200, 8, 1, 0, 0);
        System.out.println("\n--- Mifare Classic Read/Write Example ---");

        try {
            // Connect to reader
            System.out.println("Connecting to DeskID NFC reader...");
            reader.startAndWait(5000);
            System.out.println("Successfully connected!");

            // Set reader to ISO14A mode for Mifare operations
            System.out.println("Setting reader to ISO14A mode for Mifare operations...");
            reader.setReaderMode(NFCReaderMode.ISO14A);

            // Find Mifare tags
            System.out.println("\n--- Finding Mifare Tags ---");
            List<HfTag> tags = reader.getInventory();
            if (tags.isEmpty()) {
                System.out.println("No Mifare tags found! Please place a Mifare Classic tag near the reader.");
                return;
            }

            HfTag firstTag = tags.get(0);
            System.out.println("Working with Mifare tag: " + firstTag.getId());
            System.out.println("Tag TID: " + firstTag.getTid());

            // Select the tag for operations
            System.out.println("Selecting tag for operations...");
            reader.selectTag(firstTag.getTid());
            System.out.println("Tag selected successfully");

            // Mifare Classic operations require authentication
            System.out.println("\n--- Mifare Classic Authentication ---");

            // Default Mifare Classic key (factory default)
            String defaultKey = "FFFFFFFFFFFF";
            int blockToAuthenticate = 1; // Block 1 (data block in sector 0)

            System.out.println("Attempting to authenticate block " + blockToAuthenticate + " with default key...");
            try {
                reader.authenticateMifareClassicBlock(blockToAuthenticate, defaultKey, KeyType.A);
                System.out.println("Authentication successful!");

                // Read block
                System.out.println("\n--- Reading Mifare Block ---");
                System.out.println("Reading block " + blockToAuthenticate + "...");
                String currentData = reader.readBlock(blockToAuthenticate);
                System.out.println("Current data in block " + blockToAuthenticate + ": "
                        + (currentData == null || currentData.isEmpty() ? "(empty/zeros)" : currentData));

                // Write to block
                System.out.println("\n--- Writing to Mifare Block ---");
                String testData = String.format("%032X", System.currentTimeMillis()); // 16 bytes hex
                if (testData.length() > 32) {
                    testData = testData.substring(0, 32); // Ensure exactly 16 bytes (32 hex chars)
                } else {
                    testData = String.format("%-32s", testData).replace(' ', '0'); // Pad with zeros
                }

                System.out.println("Writing test data to block " + blockToAuthenticate + ": " + testData);
                reader.writeBlock(blockToAuthenticate, testData);
                System.out.println("Write operation completed!");

                // Verify write
                System.out.println("Verifying written data...");
                String readBackData = reader.readBlock(blockToAuthenticate);
                System.out.println("Read back data: " + readBackData);

                if (readBackData != null && readBackData.toUpperCase().equals(testData.toUpperCase())) {
                    System.out.println("Write verification SUCCESSFUL!");
                } else {
                    System.out.println("Write verification FAILED - data mismatch");
                }

            } catch (RFIDReaderException e) {
                System.out.println("Authentication failed: " + e.getMessage());
            }

            // Deselect tag
            System.out.println("\n--- Deselecting Tag ---");
            reader.deselectTag();
            System.out.println("Tag deselected");

        } catch (CommConnectionException e) {
            System.err.println("Connection error: " + e.getLocalizedMessage());
        } catch (RFIDReaderException e) {
            System.err.println("Reader error: " + e.getLocalizedMessage());
        } finally {
            try {
                reader.stop();
                System.out.println("Disconnected from DeskID NFC reader.");
            } catch (Exception e) {
                System.err.println("Error disconnecting: " + e.getLocalizedMessage());
            }
        }
    }

    /**
     * Demonstrate ISO 15693 read/write operations
     */
    public void performISO15ReadWriteExample() {
        // Create the reader instance using serial communication
        // Note: Update "/dev/ttyACM0" to match your actual device path (Linux/Mac) or "COM#" for Windows
        String serialPort = "/dev/ttyACM0";

        DeskID_NFC reader = new DeskID_NFC("DeskID-NFC-USB", serialPort, 115200, 8, 1, 0, 0);
        System.out.println("\n--- ISO 15693 Read/Write Example ---");

        try {
            // Connect to reader
            System.out.println("Connecting to DeskID NFC reader...");
            reader.startAndWait(5000);
            System.out.println("Successfully connected!");

            // Set reader to ISO15 mode for ISO 15693 operations
            System.out.println("Setting reader to ISO15 mode for ISO 15693 operations...");
            reader.setReaderMode(NFCReaderMode.ISO15);

            // Find ISO 15693 tags
            System.out.println("\n--- Finding ISO 15693 Tags ---");
            List<HfTag> tags = reader.getInventory();
            if (tags.isEmpty()) {
                System.out.println("No ISO 15693 tags found! Please place an ISO 15693 tag near the reader.");
                return;
            }

            HfTag firstTag = tags.get(0);
            System.out.println("Working with ISO 15693 tag: " + firstTag.getId());
            System.out.println("Tag TID: " + firstTag.getTid());

            // ISO 15693 operations don't typically require authentication like Mifare
            System.out.println("\n--- ISO 15693 Block Operations ---");

            int blockToRead = 0;

            // Read current block content
            System.out.println("Reading block " + blockToRead + "...");
            try {
                String currentData = reader.readBlock(blockToRead);
                System.out.println("Current data in block " + blockToRead + ": "
                        + (currentData == null || currentData.isEmpty() ? "(empty/zeros)" : currentData));

                // Write to block
                System.out.println("\n--- Writing to ISO 15693 Block ---");
                String testData = String.format("%08X", System.currentTimeMillis() & 0xFFFFFFFF); // 4 bytes hex
                if (testData.length() > 8) {
                    testData = testData.substring(0, 8); // Ensure exactly 4 bytes (8 hex chars)
                } else {
                    testData = String.format("%-8s", testData).replace(' ', '0'); // Pad with zeros
                }

                System.out.println("Writing test data to block " + blockToRead + ": " + testData);
                reader.writeBlock(blockToRead, testData);
                System.out.println("Write operation completed!");

                // Verify write
                System.out.println("Verifying written data...");
                String readBackData = reader.readBlock(blockToRead);
                System.out.println("Read back data: " + readBackData);

                if (readBackData != null && readBackData.toUpperCase().equals(testData.toUpperCase())) {
                    System.out.println("Write verification SUCCESSFUL!");
                } else {
                    System.out.println("Write verification FAILED - data mismatch");
                }

                // Try to read multiple blocks
                System.out.println("\n--- Reading/Writing Multiple Blocks ---");
                int blocksToRead = 2;
                try {
                    String multiBlockData = reader.readMultipleBlocks(0, blocksToRead); // Read blocks 0, 1
                    System.out.println("Multiple blocks data (0-2): " + multiBlockData);
                    testData = String.format("%016X", System.currentTimeMillis()); // 4 bytes hex
                    System.out.println("Writing test data to the first blocks: " + testData);
                    reader.writeMultipleBlocks(0, testData);
                    System.out.println("Write operation completed!");

                    // Verify write
                    System.out.println("Verifying written data...");
                    readBackData = reader.readMultipleBlocks(0, 2); // Read blocks 0, 1
                    System.out.println("Read back data: " + readBackData);
                    if (readBackData != null && readBackData.toUpperCase().equals(testData.toUpperCase())) {
                        System.out.println("Write verification SUCCESSFUL!");
                    } else {
                        System.out.println("Write verification FAILED - data mismatch");
                    }
                } catch (Exception e) {
                    System.out.println("Could not read multiple blocks: " + e.getMessage());
                }

            } catch (RFIDReaderException e) {
                System.out.println("Block operation failed: " + e.getMessage());
            }

        } catch (CommConnectionException e) {
            System.err.println("Connection error: " + e.getLocalizedMessage());
        } catch (RFIDReaderException e) {
            System.err.println("Reader error: " + e.getLocalizedMessage());
        } finally {
            try {
                reader.stop();
                System.out.println("Disconnected from DeskID NFC reader.");
            } catch (Exception e) {
                System.err.println("Error disconnecting: " + e.getLocalizedMessage());
            }
        }
    }

    /**
     * Main method to run this example independently
     */
    public static void main(String[] args) {
        System.out.println("=== DeskID NFC Example ===");
        DeskIDNFCExample example = new DeskIDNFCExample();
        example.performInventoryExample();
        example.performMifareReadWriteExample();
        example.performISO15ReadWriteExample();
    }

}

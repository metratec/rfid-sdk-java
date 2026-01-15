package com.metratec.example;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.metratec.lib.connection.CommConnectionException;
import com.metratec.lib.rfidreader.RFIDReaderException;
import com.metratec.lib.rfidreader.event.RfidReaderConnectionState;
import com.metratec.lib.rfidreader.event.RfidReaderEventListener;
import com.metratec.lib.rfidreader.event.RfidTagEventListener;
import com.metratec.lib.rfidreader.event.RfidTagFound;
import com.metratec.lib.rfidreader.event.RfidTagLost;
import com.metratec.lib.rfidreader.uhf.PulsarLR;
import com.metratec.lib.rfidreader.uhf.UHFReaderAT;
import com.metratec.lib.tag.UhfTag;

/**
 * Example class demonstrating PulsarLR UHF RFID reader functionality.
 * 
 * This class provides automated demos for: - TCP/Ethernet connection (default) - Inventory operations (single scan and
 * continuous) - Read/Write operations on UHF tag memory banks - Real-time event handling
 */
public class PulsarLRExample {

    /**
     * Demonstrate UHF inventory operations
     */
    public void performSingleInventoryExample() {
        // Create the reader instance using tcp communication
        String hostname = "plr-000143.metratec.com";
        PulsarLR reader = new PulsarLR("PulsarLR-TCP", hostname, 10001);
        System.out.println("\n--- UHF Single Inventory Example ---");

        // add a listener to the reader status events
        reader.setReaderEventListener(new RfidReaderEventListener() {
            @Override
            public void connectionState(RfidReaderConnectionState event) {
                System.out.println("Reader Status: " + event.getIdentifier() + " - " + event.getMessage());
            }
        });

        // add a listener to the tag detection events
        reader.setTagEventListener(new RfidTagEventListener<UhfTag>() {
            @Override
            public void tagFound(RfidTagFound<UhfTag> event) {
                UhfTag tag = event.getTag();
                System.out.println("UHF Tag FOUND: " + tag.getId() + " [Antenna: " + tag.getAntenna() + ", RSSI: "
                        + tag.getRssi() + " dBm]");
            }

            @Override
            public void tagLost(RfidTagLost<UhfTag> event) {
                System.out.println("UHF Tag LOST: " + event.getTag().getId());
            }
        });

        try {
            // Connect and configure
            System.out.println("Connecting to PulsarLR reader...");
            reader.startAndWait(5000);
            System.out.println("Successfully connected!");

            reader.setAntennaPort(1);
            reader.setAntennaPower(1, 12);
            System.out.println("Reader configured: Power=12dBm, Antenna=1");

            // Single inventory scan
            System.out.println("\n--- Single Inventory Scan ---");
            List<UhfTag> tags = reader.getSingleInventory();
            System.out.println("Found " + tags.size() + " UHF tag(s):");
            for (UhfTag tag : tags) {
                System.out.println("  - Tag ID: " + tag.getId() + " [Antenna: " + tag.getAntenna() + ", RSSI: "
                        + tag.getRssi() + " dBm]");
            }

            // Continuous inventory if tags found
            if (!tags.isEmpty()) {
                System.out.println("\n--- Starting Continuous Inventory with tag lost time 2000ms---");
                System.out.println("--- Running continuous inventory until Enter is pressing ---");
                reader.startSingleInventory(2000);

                long nextReport = 0L;
                try {
                    while (0 == System.in.available()) {
                        if (nextReport <= System.currentTimeMillis()) {
                            // get current inventory
                            System.out.println(reader.getSingleInventory());
                            nextReport = System.currentTimeMillis() + 2500;
                        }
                    }
                } catch (IOException e) {
                    // Stopping
                }
                reader.stopSingleInventory();
                System.out.println("Inventory stopped.");
            } else {
                System.out.println("No UHF tags found. Please place UHF tags near the reader for continuous demo.");
            }

        } catch (CommConnectionException e) {
            System.err.println("Connection error: " + e.getLocalizedMessage());
        } catch (RFIDReaderException e) {
            System.err.println("Reader error: " + e.getLocalizedMessage());
        } finally {
            try {
                reader.stop();
                System.out.println("Disconnected from PulsarLR reader.");
            } catch (Exception e) {
                System.err.println("Error disconnecting: " + e.getLocalizedMessage());
            }
        }
    }

    /**
     * Demonstrate UHF inventory operations
     */
    public void performMultiplexInventoryExample() {
        // Create the reader instance using tcp communication
        String hostname = "plr-000143.metratec.com";
        PulsarLR reader = new PulsarLR("PulsarLR-TCP", hostname, 10001);
        System.out.println("\n--- UHF Multiplex Inventory Example ---");

        // add a listener to the reader status events
        reader.setReaderEventListener(new RfidReaderEventListener() {
            @Override
            public void connectionState(RfidReaderConnectionState event) {
                System.out.println("Reader Status: " + event.getIdentifier() + " - " + event.getMessage());
            }
        });

        // add a listener to the tag detection events
        reader.setTagEventListener(new RfidTagEventListener<UhfTag>() {
            @Override
            public void tagFound(RfidTagFound<UhfTag> event) {
                UhfTag tag = event.getTag();
                System.out.println("UHF Tag FOUND: " + tag.getId() + " [Antenna: " + tag.getAntenna() + ", RSSI: "
                        + tag.getRssi() + " dBm]");
            }

            @Override
            public void tagLost(RfidTagLost<UhfTag> event) {
                System.out.println("UHF Tag LOST: " + event.getTag().getId());
            }
        });

        try {
            // Connect and configure
            System.out.println("Connecting to PulsarLR reader...");
            reader.startAndWait(5000);
            System.out.println("Successfully connected!");

            reader.setMultiplexAntennas(Arrays.asList(1, 2));
            reader.setAntennaPower(1, 12);
            reader.setAntennaPower(2, 12);
            System.out.println("Reader configured: Power=12dBm, Antenna=1, 2");

            // Single inventory scan
            System.out.println("\n--- Multiplex Inventory Scan ---");
            List<UhfTag> tags = reader.getMultiplexInventory();
            System.out.println("Found " + tags.size() + " UHF tag(s):");
            for (UhfTag tag : tags) {
                System.out.println("  - Tag ID: " + tag.getId() + " [Antenna: " + tag.getAntenna() + ", RSSI: "
                        + tag.getRssi() + " dBm]");
            }

            // Continuous inventory if tags found
            if (!tags.isEmpty()) {
                System.out.println("\n--- Starting Continuous Inventory with tag lost time 2000ms---");
                System.out.println("--- Running continuous inventory until Enter is pressing ---");
                reader.startMultiplexInventory(2000);

                long nextReport = 0L;
                try {
                    while (0 == System.in.available()) {
                        if (nextReport <= System.currentTimeMillis()) {
                            // get current inventory
                            System.out.println(reader.getMultiplexInventory());
                            nextReport = System.currentTimeMillis() + 2500;
                        }
                    }
                } catch (IOException e) {
                    // Stopping
                }
                reader.stopMultiplexInventory();
                System.out.println("Inventory stopped.");
            } else {
                System.out.println("No UHF tags found. Please place UHF tags near the reader for continuous demo.");
            }

        } catch (CommConnectionException e) {
            System.err.println("Connection error: " + e.getLocalizedMessage());
        } catch (RFIDReaderException e) {
            System.err.println("Reader error: " + e.getLocalizedMessage());
        } finally {
            try {
                reader.stop();
                System.out.println("Disconnected from PulsarLR reader.");
            } catch (Exception e) {
                System.err.println("Error disconnecting: " + e.getLocalizedMessage());
            }
        }
    }

    /**
     * Demonstrate UHF read/write operations
     */
    public void performReadWriteExample() {

        // Create the reader instance using tcp communication
        String hostname = "plr-000143.local";
        PulsarLR reader = new PulsarLR("PulsarLR-TCP", hostname, 10001);
        System.out.println("\n--- UHF Read/Write Example ---");

        try {
            // Connect and configure
            System.out.println("Connecting to reader...");
            reader.startAndWait(5000);
            System.out.println("Successfully connected!");

            reader.setAntennaPort(1);
            reader.setAntennaPower(1, 16);

            // Find tags
            System.out.println("\n--- Finding UHF Tags ---");
            List<UhfTag> tags = reader.getInventory();
            if (tags.isEmpty()) {
                System.out.println("No UHF tags found! Please place a UHF tag near the reader.");
                return;
            }

            UhfTag firstTag = tags.get(0);
            System.out.println("Working with tag: " + firstTag.getId());

            // Read TID
            System.out.println("\n--- Reading TID Memory Bank ---");
            List<UhfTag> tidResult = reader.getTagData(UHFReaderAT.MEMBANK.TID, 0, 8, firstTag.getEpc());
            if (!tidResult.isEmpty() && !tidResult.get(0).hasError()) {
                System.out.println("TID: " + tidResult.get(0).getTid());
            } else {
                System.out.println(
                        "Failed to read TID: " + (tidResult.isEmpty() ? "No response" : tidResult.get(0).getMessage()));
            }

            // Read/Write User memory
            System.out.println("\n--- User Memory Operations ---");

            // Read current data
            List<UhfTag> userReadResult = reader.getTagData(UHFReaderAT.MEMBANK.USR, 0, 4, firstTag.getEpc());
            if (!userReadResult.isEmpty() && !userReadResult.get(0).hasError()) {
                String currentData = userReadResult.get(0).getData();
                System.out.println("Current User Data: "
                        + (currentData == null || currentData.isEmpty() ? "(empty)" : currentData));
            } else {
                System.out.println("Failed to read User memory");
            }

            // Write new data
            String testData = String.format("%08X", (int) (System.currentTimeMillis() / 1000));
            System.out.println("Writing test data: " + testData);

            List<UhfTag> writeResult = reader.setTagData(UHFReaderAT.MEMBANK.USR, 0, testData, firstTag.getEpc());
            if (!writeResult.isEmpty() && !writeResult.get(0).hasError()) {
                System.out.println("Write successful!");

                // Verify write
                List<UhfTag> verifyResult = reader.getTagData(UHFReaderAT.MEMBANK.USR, 0, 4, firstTag.getEpc());
                if (!verifyResult.isEmpty() && !verifyResult.get(0).hasError()) {
                    String readBackData = verifyResult.get(0).getData();
                    System.out.println("Verified data: " + readBackData);

                    if (readBackData != null && readBackData.toUpperCase().startsWith(testData.toUpperCase())) {
                        System.out.println("Write verification SUCCESSFUL!");
                    } else {
                        System.out.println("Write verification FAILED - data mismatch");
                    }
                }
            } else {
                System.out.println(
                        "Write failed: " + (writeResult.isEmpty() ? "No response" : writeResult.get(0).getMessage()));
            }

        } catch (CommConnectionException e) {
            System.err.println("Connection error: " + e.getLocalizedMessage());
        } catch (RFIDReaderException e) {
            System.err.println("Reader error: " + e.getLocalizedMessage());
        } finally {
            try {
                reader.stop();
                System.out.println("Disconnected from PulsarLR reader.");
            } catch (Exception e) {
                System.err.println("Error disconnecting: " + e.getLocalizedMessage());
            }
        }
    }

    /**
     * Main method to run this example independently
     */
    public static void main(String[] args) {
        System.out.println("=== PulsarLR Example ===");
        PulsarLRExample example = new PulsarLRExample();
        example.performSingleInventoryExample();
        example.performMultiplexInventoryExample();
        example.performReadWriteExample();
    }

}

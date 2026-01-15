package com.metratec.example;

/**
 * Main application class for demonstrating Metratec RFID readers.
 * 
 * This application demonstrates various Metratec RFID reader types: - UHF readers: * Desktop: DeskID UHF v2 (EPC Gen 2)
 * * Industrial: PulsarLR - HF/ISO readers: * Desktop: DeskID ISO * Industrial: QuasarLR (Long Range), QuasarMX (High
 * Speed), DMI15 (PoE) * Compact/Module: QR15 (Plug-In), Dwarf15 (SMD) - NFC readers: * Desktop: DeskID NFC
 * (Multi-protocol: ISO15693, ISO14443, Mifare)
 * 
 * Examples can be run individually by uncommenting the desired demonstrations.
 */
public class RFIDReaderExample {

    public static void main(String[] args) {
        System.out.println("=== Metratec RFID Reader Library Examples ===");

        /* UHF Reader Examples */
        /* Desktop UHF Readers */
        /* Run DeskID UHF v2 desktop reader examples (EPC Gen 2) */

        DeskIDUHFv2Example deskidUHFDemo = new DeskIDUHFv2Example();
        deskidUHFDemo.performInventoryExample();
        // deskidUHFDemo.performReadWriteExample();

        /* Industrial UHF Readers */

        /* Run PulsarLR UHF reader examples */
        // PulsarLRExample pulsarDemo = new PulsarLRExample();
        // pulsarDemo.performSingleInventoryExample();
        // pulsarDemo.performMultiplexInventoryExample();
        // pulsarDemo.performReadWriteExample();


        /* NFC Reader Examples */

        /* Run DeskID NFC multi-protocol reader examples (supports ISO15693, ISO14443, Mifare) */
        // DeskIDNFCExample deskidNFCDemo = new DeskIDNFCExample();
        // deskidNFCDemo.performInventoryExample(); // AUTO mode + tag detection
        // deskidNFCDemo.performMifareReadWriteExample(); // Mifare Classic operations
        // deskidNFCDemo.performISO15ReadWriteExample(); // ISO 15 operations


        /* HF Reader Examples */

        /* Desktop HF Readers */

        /* Run DeskID ISO HF reader examples (Desktop reader) */
        // DeskIDISOExample deskidDemo = new DeskIDISOExample();
        // deskidDemo.performInventoryExample();
        // deskidDemo.performReadWriteExample();

        /* Industrial HF Readers */

        /* Run QuasarLR Long Range Industrial HF reader examples */
        // QuasarLRExample quasarLRDemo = new QuasarLRExample();
        // quasarLRDemo.performInventoryExample();
        // quasarLRDemo.performReadWriteExample();

        /* Run QuasarMX High-Speed Industrial HF reader examples */
        // QuasarMXExample quasarMXDemo = new QuasarMXExample();
        // quasarMXDemo.performInventoryExample();
        // quasarMXDemo.performReadWriteExample();

        /* Run DMI15 Industrial HF reader examples */
        // DMI15Example dmi15Demo = new DMI15Example();
        // dmi15Demo.performInventoryExample();
        // dmi15Demo.performReadWriteExample();

        /* Compact/Module HF Readers */

        /* Run QR15 Compact Plug-In Module HF reader examples */
        // QR15Example qr15Demo = new QR15Example();
        // qr15Demo.performInventoryExample();
        // qr15Demo.performReadWriteExample();

        /* Run Dwarf15 SMD Module HF reader examples */
        // Dwarf15Example dwarf15Demo = new Dwarf15Example();
        // dwarf15Demo.performInventoryExample();
        // dwarf15Demo.performReadWriteExample();

    }
}

# Metratec RFID Reader Example

This project demonstrates usage of the Metratec RFID Reader Library with various reader types including UHF, HF/ISO, NFC, and Mifare readers.

## Prerequisites

- Java 8 or higher
- Maven 3.6+ (for Maven-based execution)
- Metratec RFID Reader Library installed in local Maven repository

## Installation

First, build and install the main RFID reader library:

```bash
cd ../../
mvn clean install
cd example/rfid-reader-example/
```

## Running with Maven

### Method 1: Maven Exec Plugin (Recommended)
```bash
mvn exec:java
```

### Method 2: Build and Run Executable JAR
```bash
# Build the executable JAR with all dependencies
mvn clean package

# Run the executable JAR
java -jar target/rfid-reader-example-1.0.0.jar
```

### Method 3: Run Specific Example Classes
```bash
# Run individual example classes
mvn exec:java -Dexec.mainClass="com.metratec.example.DeskIDUHFv2Example"
mvn exec:java -Dexec.mainClass="com.metratec.example.DeskIDISOExample"
mvn exec:java -Dexec.mainClass="com.metratec.example.PulsarLRExample"
mvn exec:java -Dexec.mainClass="com.metratec.example.QuasarLRExample"
```

### Method 4: Compile and Run with Maven
```bash
# Compile the project
mvn clean compile

# Run with Maven using the compiled classes
mvn exec:java -Dexec.mainClass="com.metratec.example.RFIDReaderExample"
```

## Available Examples

The project includes examples for various reader types:

### UHF Readers (EPC Gen2)
- **DeskIDUHFv2Example**: Desktop UHF reader with USB/Serial connection
- **PulsarLRExample**: Industrial UHF reader with TCP/Ethernet connection

### HF/ISO Readers (13.56 MHz, ISO 15693)
- **DeskIDISOExample**: Desktop HF reader with USB connection
- **QuasarLRExample**: Long-range industrial HF reader
- **QuasarMXExample**: High-speed industrial HF reader
- **DMI15Example**: PoE-powered industrial HF reader
- **Dwarf15Example**: SMD compact HF reader
- **QR15Example**: Plug-in compact HF reader

### NFC Readers
- **DeskIDNFCExample**: Desktop NFC reader with multi-protocol support

## Configuration

### Modifying Examples

To run specific examples, edit `RFIDReaderExample.java` and uncomment the desired demonstration code blocks.

## Logging

The application creates detailed logs in the `../logs/` directory:
- `logging.log`: Main application logs

Log levels can be configured in `src/main/resources/log4j2.xml`.

## Troubleshooting

### Build Issues
- Ensure the main RFID library is installed: `cd ../../ && mvn clean install`
- Verify Java 8+ is installed: `java -version`
- Check Maven version: `mvn -version`

### Runtime Issues
- **Connection errors**: Check hardware connections and device permissions
- **USB permission issues (Linux)**: Add user to dialout group or run with sudo
- **Missing dependencies**: Ensure all JAR files are in classpath

### Hardware Requirements
- Compatible Metratec RFID readers
- Appropriate RFID tags (UHF: EPC Gen2, HF: ISO 15693, NFC: NTag compatible)
- Proper drivers installed for USB connections

## Example Output

When running successfully, you'll see output similar to:
```
=== Metratec RFID Reader Library Examples ===
Connecting to DeskID UHF v2...
Reader connected successfully
Performing single inventory...
Found tag: EPC=...
Performing continuous inventory...
...
```

## Project Structure

```
rfid-reader-example/
├── README.md                  # This file
├── pom.xml                    # Maven configuration
├── src/main/
│   ├── java/com/metratec/example/  # Example source code
│   │   ├── RFIDReaderExample.java  # Main class
│   │   ├── DeskIDUHFv2Example.java # UHF examples
│   │   ├── DeskIDISOExample.java   # HF examples
│   │   └── ...                     # Other reader examples
│   └── resources/
│       └── log4j2.xml         # Logging configuration
├── target/                    # Build output
└── dependency-reduced-pom.xml # Generated after shading
```

## License

This example project uses the Metratec RFID Reader Library. Check the main library documentation for license information.
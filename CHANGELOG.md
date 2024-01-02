# RFID Reader Library

Java library for controlling the metratec rfid readers

## Version 4.0

### 4.1

* HF/ISO Reader: Request commands added to send manufacturer specific commands to the transponders

### 4.0

* event handler can be subscribe (tag found, tag lost, reader status)
* inventories return List with Transponder objects
* reader of the generation 2 added (PulsarLR)

## Version 3.5

### 3.5.13

* hf reader DMI15 added

### 3.5.12

* uhf reader DwarfG2 Mini added

### 3.5.11

* use connection library 1.22.3

### 3.5.10

* ISO Reader - use WRQ command for writing

### 3.5.9

* PulsarLR added
* UHF Reader - kill/lock tag fixed
* ISO Reader - get transponder data method corrected (transponder crc removed)

### 3.5.8

* do not trust the input event values of the reader, minimum debounce time of 50 ms

### 3.5.7

* quality assurance (e.g. remove duplicate code)

### 3.5.6

* inventory checking thread is stopped immediately

### 3.5.5

* throw input change event only when the state is changed

### 3.5.4

* MF Reader, added methods for enable and disable of the RF field
* now the events are also thrown when the reader was stopped and started again (bug)

### 3.5.3

* securing the verification of inputs with debouncing

### 3.5.2

* fix crc calculation for long values

### 3.5.1

* fix reset method
* fair synchronisation

### 3.5

* add classes for every reader
* !!! Change some packages !!!
* add inventory classes
* throw error if the device is no metraTec reader
* UHFReader - remove save power mode by default

## Version 3.4

### 3.4.1

* catch reader reset by firmware

### 3.4

* split classes
* add license information

## Version 3.3

* event based

## Version 3.2

### 3.2.6

* enable rf field on start up

### 3.2.5

* use latest connection library

### 3.2.4

* uhf reader - add set communication channel method
* connection library 1.17

### 3.2.3

* iso reader - retry 5 times to get the tag data. After that throw a error, also if a part has been read
  
### 3.2.2

* update documentation
* fix get input bug
* fix tag information

### 3.2.1

* updated dependent libraries

### 3.2

* UHF Reader for reader firmware 3.2+
* use connection library 1.10

## Version 3.1

* use connection library 1.9

## Version 3.0

* create from RFIDReaderJavaSDK with maven

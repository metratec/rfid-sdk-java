import java.io.IOException;
import java.util.List;
import com.metratec.lib.connection.CommConnectionException;
import com.metratec.lib.rfidreader.RFIDReaderException;
import com.metratec.lib.rfidreader.event.RfidReaderConnectionState;
import com.metratec.lib.rfidreader.event.RfidReaderEventListener;
import com.metratec.lib.rfidreader.event.RfidTagEventListener;
import com.metratec.lib.rfidreader.event.RfidTagFound;
import com.metratec.lib.rfidreader.event.RfidTagLost;
import com.metratec.lib.rfidreader.iso.DeskID_ISO;
import com.metratec.lib.tag.HfTag;

public class DeskidIsoExample {
  public static void main(String[] args) {
    InventoryExample();
    // ReadWriteExample();
  }

  private static void InventoryExample() {
    // Create the reader instance
    DeskID_ISO reader = new DeskID_ISO("DeskID", "COM1", 115200, 8, 2, 0, 0);
    // add a reader status listener
    reader.setReaderEventListener(new RfidReaderEventListener() {

      @Override
      public void connectionState(RfidReaderConnectionState event) {
        System.out.println(String.format("%s %s", event.getIdentifier(), event.getMessage()));
      }

    });
    reader.setTagEventListener(new RfidTagEventListener<HfTag>() {

      @Override
      public void tagFound(RfidTagFound<HfTag> event) {
        System.out.println(String.format("Tag found: %s", event.getTag().getId()));
      }

      @Override
      public void tagLost(RfidTagLost<HfTag> event) {
        System.out.println(String.format("Tag lost: %s", event.getTag().getId()));
      }

    });
    try {

      // connect the reader with timeout
      reader.startAndWait(2000);

      // fetches the current inventory - if an inventory listener exists, this method also triggers the listener
      List<HfTag> tags = reader.getInventory();
      System.out.println(String.format("Current inventory: %s Tag(s) found", tags.size()));
      for (HfTag tag : tags) {
        System.out.println(String.format(" %s", tag.getId()));
      }

      reader.startInventory(2000);
      System.out.println("Continuous inventory scan started - Press any key to stop");
      try {
        System.in.read();
      } catch (IOException e) {
      }
      reader.stopInventory();

    } catch (CommConnectionException e) {
      System.out.println("An connection error occurs: " + e.getLocalizedMessage());
    } catch (RFIDReaderException e) {
      System.out.println("An reader error occurs: " + e.getLocalizedMessage());
    } finally {
      // Disconnect reader
      try {
        reader.stop();
      } catch (CommConnectionException e) {
        System.out.println("Error disconnect reader: " + e.getLocalizedMessage());
      }
    }
  }

  private static void ReadWriteExample() {
    // Create the reader instance
    DeskID_ISO reader = new DeskID_ISO("DeskID", "COM1", 115200, 8, 2, 0, 0);
    // add a reader status listener
    reader.setReaderEventListener(new RfidReaderEventListener() {

      @Override
      public void connectionState(RfidReaderConnectionState event) {
        System.out.println(String.format("%s %s", event.getIdentifier(), event.getMessage()));
      }

    });
    try {
      // connect the reader with timeout
      reader.startAndWait(2000);

      // fetches the current inventory - if an inventory listener exists, this method also triggers the listener
      List<HfTag> tags = reader.getInventory();
      while (tags.size() == 0) {
        System.out.println("Please put a tag on the reader and press any key...");
        try {
          System.in.read();
        } catch (IOException e) {
        }
        tags = reader.getInventory();
      }
      HfTag tag = tags.get(0);
      System.out.println("Try to read tag bock 0...");
      HfTag resp = reader.getTagData(0, tag.getTid());
      if (resp.hasError()) {
        System.out.println(String.format("Can not read the transponder block 0 %s", resp.getMessage()));
      } else {
        System.out.println(String.format("Transponder Block 0: %s", resp.getData()));
      }
      System.out.println("Try to write tag bock 0...");
      resp = reader.setTagData(0, "01020304", tag.getTid());
      if (resp.hasError()) {
        System.out.println(String.format("Can not write the transponder block 0 %s", resp.getMessage()));
      } else {
        System.out.println(String.format("Transponder Block 0 written: %s", resp.getData()));
      }
    } catch (CommConnectionException e) {
      System.out.println("An connection error occurs: " + e.getLocalizedMessage());
    } catch (RFIDReaderException e) {
      System.out.println("An reader error occurs: " + e.getLocalizedMessage());
    } finally {
      // Disconnect reader
      try {
        reader.stop();
      } catch (CommConnectionException e) {
        System.out.println("Error disconnect reader: " + e.getLocalizedMessage());
      }
    }
  }
}

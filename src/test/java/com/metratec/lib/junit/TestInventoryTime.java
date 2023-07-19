/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH All rights reserved.
 *******************************************************************************/
package com.metratec.lib.junit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.metratec.lib.inventory.InventoryWithTimestamp;
import com.metratec.lib.tag.UhfTag;

/**
 * Test if the license files are generated and stored in the jar file
 * 
 * @author mn
 *
 */
public class TestInventoryTime {
  private Logger logger = LoggerFactory.getLogger(TestInventoryTime.class);
  private InventoryWithTimestamp<UhfTag> inventory;

  /**
   * called before every test
   */
  @Before
  public void setUp() {}

  /**
   * called after every test
   */
  @After
  public void tearDown() {

  }

  /**
   * adding tag
   * 
   * @throws Exception if an error occurs
   */
  @Test
  public void testAddTags() throws Exception {
    logger.info("addTags");
    inventory = new InventoryWithTimestamp<>("test");

    String tagA = "AAAA";
    Long lastTimer = inventory.addTag(new UhfTag(tagA));
    Assert.assertNull(lastTimer);
    Assert.assertEquals(1, inventory.getInventory().size());
    Assert.assertEquals(tagA, inventory.getInventory().get(0).getId());
    lastTimer = inventory.addTag(new UhfTag(tagA));
    Assert.assertNotNull(lastTimer);

    String tagB = "BBBB";
    List<UhfTag> addedTags = inventory.addTags(Arrays.asList(new UhfTag(tagA), new UhfTag(tagB)));
    Assert.assertEquals(1, addedTags.size());
    Assert.assertEquals(tagB, addedTags.get(0).getId());
    Assert.assertEquals(2, inventory.getInventory().size());

    String tagC = "CCCC";
    addedTags =
        inventory.addTags(Arrays.asList(new UhfTag(tagA), new UhfTag(tagB), new UhfTag(tagC)));
    Assert.assertEquals(1, addedTags.size());
    Assert.assertEquals(tagC, addedTags.get(0).getId());
    Assert.assertEquals(3, inventory.getInventory().size());

    addedTags =
        inventory.addTags(Arrays.asList(new UhfTag(tagA), new UhfTag(tagB), new UhfTag(tagC)));
    Assert.assertEquals(0, addedTags.size());

    List<UhfTag> removedTags = inventory.checkInventory();
    Assert.assertEquals(0, removedTags.size());

  }

  /**
   * adding tag
   * 
   * @throws Exception if an error occurs
   */
  @Test
  public void testRemoveTags() throws Exception {
    logger.info("removeTagsWithNewTagList");
    inventory = new InventoryWithTimestamp<>("test", 500l);

    String tagA = "AAAA";
    List<UhfTag> addedTags = inventory.addTags(Arrays.asList(new UhfTag(tagA)));
    // Check arrive event
    Assert.assertEquals(1, addedTags.size());
    Assert.assertEquals(tagA, addedTags.get(0).getId());


    String tagB = "BBBB";
    addedTags = inventory.addTags(Arrays.asList(new UhfTag(tagA), new UhfTag(tagB)));
    Assert.assertEquals(1, addedTags.size());
    Assert.assertEquals(tagB, addedTags.get(0).getId());


    // Sleep tag keep time second
    Thread.sleep(600);
    addedTags = inventory.addTags(Arrays.asList(new UhfTag(tagB)));
    // Check depart event
    Assert.assertEquals(0, addedTags.size());

    List<UhfTag> removedTags = inventory.checkInventory();
    Assert.assertEquals(1, removedTags.size());
    Assert.assertEquals(tagA, removedTags.get(0).getId());

    Long lastTimestamp = inventory.removeTag(tagB);
    Assert.assertNotNull(lastTimestamp);
    Assert.assertTrue(inventory.getInventory().isEmpty());
    Assert.assertNull(inventory.removeTag(tagB));
  }



  // @Test
  // public void speedTest() {
  // for (int i = 0; i < 10; i++) {
  // long start = System.nanoTime();
  // InventoryWithTimestamp inventoryTime = new InventoryWithTimestamp("test");
  // List<String> tags = new ArrayList<>();
  // for (int n = 0; n < 10000; n++) {
  // inventoryTime.addTags(tags, System.currentTimeMillis());
  // inventoryTime.checkInventory();
  // }
  // long end = System.nanoTime();
  // System.out.println("SimpleInventory: " + (end - start));
  // }
  // for (int i = 0; i < 10; i++) {
  //
  // long start = System.nanoTime();
  // SimpleInventory simpleInventory = new SimpleInventory();
  // List<String> tags = new ArrayList<>();
  // for (int n = 0; n < 10000; n++) {
  // simpleInventory.updateInventory(tags);
  // }
  // long end = System.nanoTime();
  // System.out.println("Inventory: " + (end - start));
  // }
  //
  // }


  /**
   * Test the implementation with continuous tag updates
   * 
   * @throws Exception if an error occurs
   */
  @Test
  public void stressTest() throws Exception {
    logger.info("testInventoryContinuous");
    inventory = new InventoryWithTimestamp<>("test", 200l);
    // inventory.start();

    List<String> tags = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      tags.add(String.format("AAAA%04d", i));
    }

    List<AddingTagToInventory> addTags = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      addTags.add(new AddingTagToInventory(tags));
      Timer addTagsTimer = new Timer(true);
      addTagsTimer.schedule(addTags.get(i), 0, 1);
    }

    List<RemovingTagsFromInventory> removeTags = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      removeTags.add(new RemovingTagsFromInventory(tags));
      Timer removeTagsTimer = new Timer(true);
      removeTagsTimer.schedule(removeTags.get(i), 0, 1);
    }

    long endTime = System.currentTimeMillis() + 1000;
    // int count = 0;
    while (System.currentTimeMillis() < endTime) {
      @SuppressWarnings("unused")
      List<UhfTag> inv = inventory.getInventory();
      // if (inv.size() > 0 && inv.size() < 100) {
      // System.out.println("inv: " + inv.size());
      // }
      // count++;
      Thread.sleep(1);
    }
    // int addCount = 0;
    // for (AddingTagToInventory add : addTags) {
    // add.cancel();
    // addCount += add.getCount();
    // }
    // int removeCount = 0;
    // for (RemovingTagsFromInventory remove : removeTags) {
    // remove.cancel();
    // removeCount += remove.getCount();
    // }
    // System.out.println("getInventory count: " + count);
    // System.out.println("addTags count: " + addCount);
    // System.out.println("removeTags count: " + removeCount);

  }

  /**
   * @author mn
   *
   */
  class AddingTagToInventory extends TimerTask {
    private int count = 0;
    private List<String> tagIds;

    /**
     * @param tagIds tag ids
     * 
     */
    public AddingTagToInventory(List<String> tagIds) {
      this.tagIds = tagIds;
    }

    @Override
    public void run() {
      List<UhfTag> tags = new ArrayList<>();
      for (String id : tagIds) {
        tags.add(new UhfTag(id));
      }
      @SuppressWarnings("unused")
      List<UhfTag> addedTags = inventory.addTags(tags);
      // if (addedTags.size() > 0 && addedTags.size() < 100) {
      // System.out.println("add: " + addedTags.size());
      // }
      count++;
    }

    /**
     * @return the count
     */
    public int getCount() {
      return count;
    }

  }
  /**
   * @author mn
   *
   */
  class RemovingTagsFromInventory extends TimerTask {
    private int count = 0;
    private List<String> tagIds;

    /**
     * @param tagIds tag ids
     * 
     */
    public RemovingTagsFromInventory(List<String> tagIds) {
      this.tagIds = tagIds;
    }

    @Override
    public void run() {
      @SuppressWarnings("unused")
      List<String> removed = inventory.removeTags(tagIds);
      // if (removed.size() > 0 && removed.size() < 100) {
      // System.out.println("removes: " + removed.size());
      // }
      count++;
    }

    /**
     * @return the count
     */
    public int getCount() {
      return count;
    }
  }

}

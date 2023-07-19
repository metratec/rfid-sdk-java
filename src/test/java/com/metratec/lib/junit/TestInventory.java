/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH All rights reserved.
 *******************************************************************************/
package com.metratec.lib.junit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.metratec.lib.inventory.Inventory;
import com.metratec.lib.inventory.event.InventoryChangedEvent;
import com.metratec.lib.inventory.event.InventoryListener;
import com.metratec.lib.inventory.event.TagArrivedEvent;
import com.metratec.lib.inventory.event.TagDepartedEvent;
import com.metratec.lib.tag.UhfTag;

/**
 * Test if the license files are generated and stored in the jar file
 * 
 * @author mn
 *
 */
public class TestInventory {
  private Logger logger = LoggerFactory.getLogger(TestInventory.class);
  private LinkedBlockingQueue<TagArrivedEvent<UhfTag>> arrivedTags = new LinkedBlockingQueue<>();
  private LinkedBlockingQueue<TagDepartedEvent<UhfTag>> departedTags = new LinkedBlockingQueue<>();
  private LinkedBlockingQueue<InventoryChangedEvent<UhfTag>> inventoryChanges =
      new LinkedBlockingQueue<>();
  private Inventory<UhfTag> inventory;
  private InventoryListener<UhfTag> listener = new InventoryListener<UhfTag>() {

    @Override
    public void tagDeparted(TagDepartedEvent<UhfTag> tagDeparted) {
      logger.debug("Tag " + tagDeparted.getTag().getId() + " departed");
      departedTags.add(tagDeparted);
    }

    @Override
    public void tagArrive(TagArrivedEvent<UhfTag> tagArrived) {
      logger.debug("Tag " + tagArrived.getTag().getId() + " added");
      arrivedTags.add(tagArrived);
    }

    @Override
    public void inventoryChanged(InventoryChangedEvent<UhfTag> inventoryChanged) {
      logger.debug("inventory changes " + " Size " + inventoryChanged.getTags().size() + " New: "
          + inventoryChanged.getNewTags() + " Lost: " + inventoryChanged.getLostTags());
      inventoryChanges.add(inventoryChanged);

    }
  };

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
    if (null != inventory) {
      inventory.stop();
    }
  }

  /**
   * adding tag
   * 
   * @throws Exception if an error occurs
   */
  @Test
  public void addTags() throws Exception {
    logger.info("addTags");
    inventory = new Inventory<>("test", listener);

    String tagA = "AAAA";
    inventory.updateInventory(Arrays.asList(new UhfTag(tagA)));
    TagArrivedEvent<UhfTag> tagArrived = arrivedTags.poll();
    Assert.assertFalse(null == tagArrived);
    Assert.assertEquals(tagA, tagArrived.getTag().getId());
    Assert.assertEquals("test", tagArrived.getIdentifier());
    InventoryChangedEvent<UhfTag> invChange = inventoryChanges.poll();
    Assert.assertFalse(null == invChange);
    Assert.assertEquals(1, invChange.getTags().size());
    Assert.assertEquals(1, invChange.getNewTags().size());
    Assert.assertEquals(0, invChange.getLostTags().size());
    Assert.assertEquals(tagA, invChange.getNewTags().get(0).getId());


    String tagB = "BBBB";
    inventory.updateInventory(Arrays.asList(new UhfTag(tagA), new UhfTag(tagB)));
    tagArrived = arrivedTags.poll();
    Assert.assertFalse(null == tagArrived);
    Assert.assertEquals(tagB, tagArrived.getTag().getId());
    invChange = inventoryChanges.poll();
    Assert.assertFalse(null == invChange);
    Assert.assertEquals(2, invChange.getTags().size());
    Assert.assertEquals(1, invChange.getNewTags().size());
    Assert.assertEquals(0, invChange.getLostTags().size());
    Assert.assertEquals(tagB, invChange.getNewTags().get(0).getId());

    String tagC = "CCCC";
    inventory.updateInventory(Arrays.asList(new UhfTag(tagA), new UhfTag(tagB), new UhfTag(tagC)));
    tagArrived = arrivedTags.poll();
    Assert.assertFalse(null == tagArrived);
    Assert.assertEquals(tagC, tagArrived.getTag().getId());
    invChange = inventoryChanges.poll();
    Assert.assertFalse(null == invChange);
    Assert.assertEquals(3, invChange.getTags().size());
    Assert.assertEquals(1, invChange.getNewTags().size());
    Assert.assertEquals(0, invChange.getLostTags().size());
    Assert.assertEquals(tagC, invChange.getNewTags().get(0).getId());

    Assert.assertTrue(arrivedTags.isEmpty());
    Assert.assertTrue(departedTags.isEmpty());
    Assert.assertTrue(inventoryChanges.isEmpty());

    inventory.clear();
  }

  /**
   * adding tag
   * 
   * @throws Exception if an error occurs
   */
  @Test
  public void removeTagsWithNewTagList() throws Exception {
    logger.info("removeTagsWithNewTagList");
    inventory = new Inventory<>("test", listener, 500);

    String tagA = "AAAA";
    inventory.updateInventory(Arrays.asList(new UhfTag(tagA)));
    // Check arrive event
    TagArrivedEvent<UhfTag> tagArrived = arrivedTags.poll();
    Assert.assertFalse(null == tagArrived);
    Assert.assertEquals(tagA, tagArrived.getTag().getId());
    InventoryChangedEvent<UhfTag> invChange = inventoryChanges.poll();
    Assert.assertFalse(null == invChange);
    Assert.assertEquals(1, invChange.getTags().size());
    Assert.assertEquals(1, invChange.getNewTags().size());
    Assert.assertEquals(0, invChange.getLostTags().size());
    Assert.assertEquals(tagA, invChange.getNewTags().get(0).getId());

    String tagB = "BBBB";
    inventory.updateInventory(Arrays.asList(new UhfTag(tagA), new UhfTag(tagB)));
    tagArrived = arrivedTags.poll();
    Assert.assertFalse(null == tagArrived);
    Assert.assertEquals(tagB, tagArrived.getTag().getId());
    invChange = inventoryChanges.poll();
    Assert.assertFalse(null == invChange);
    Assert.assertEquals(2, invChange.getTags().size());
    Assert.assertEquals(1, invChange.getNewTags().size());
    Assert.assertEquals(0, invChange.getLostTags().size());
    Assert.assertEquals(tagB, invChange.getNewTags().get(0).getId());

    // Sleep tag keep time second
    Thread.sleep(600);
    inventory.updateInventory(Arrays.asList(new UhfTag(tagB)));
    // Check depart event
    TagDepartedEvent<UhfTag> tagDeparted = departedTags.poll();
    Assert.assertTrue(null != tagDeparted);
    Assert.assertEquals(tagA, tagDeparted.getTag().getId());
    invChange = inventoryChanges.poll();
    Assert.assertFalse(null == invChange);
    Assert.assertEquals(1, invChange.getTags().size());
    Assert.assertEquals(0, invChange.getNewTags().size());
    Assert.assertEquals(1, invChange.getLostTags().size());
    Assert.assertEquals(tagA, invChange.getLostTags().get(0).getId());


    Assert.assertTrue(arrivedTags.isEmpty());
    Assert.assertTrue(departedTags.isEmpty());
    Assert.assertTrue(inventoryChanges.isEmpty());

    inventory.clear();
  }

  /**
   * check if the tags are automatically removed
   * 
   * @throws Exception if an error occurs
   */
  @Test
  public void removeTagsWithoutUpdateTagList() throws Exception {
    logger.info("removeTagsWithoutUpdateTagList");
    inventory = new Inventory<>("test", listener, 500);
    inventory.start();

    String tagA = "AAAA";
    inventory.updateInventory(Arrays.asList(new UhfTag(tagA)));
    // Check arrive event
    TagArrivedEvent<UhfTag> tagArrived = arrivedTags.poll();
    Assert.assertFalse(null == tagArrived);
    Assert.assertEquals(tagA, tagArrived.getTag().getId());
    InventoryChangedEvent<UhfTag> invChange = inventoryChanges.poll();
    Assert.assertFalse(null == invChange);
    Assert.assertEquals(1, invChange.getTags().size());
    Assert.assertEquals(1, invChange.getNewTags().size());
    Assert.assertEquals(0, invChange.getLostTags().size());
    Assert.assertEquals(tagA, invChange.getNewTags().get(0).getId());

    // Check depart event
    TagDepartedEvent<UhfTag> tagDeparted = departedTags.poll(800, TimeUnit.MILLISECONDS);
    Assert.assertFalse(null == tagDeparted);
    Assert.assertEquals(tagA, tagDeparted.getTag().getId());
    invChange = inventoryChanges.poll(100, TimeUnit.MILLISECONDS);
    Assert.assertFalse(null == invChange);
    Assert.assertEquals(0, invChange.getTags().size());
    Assert.assertEquals(0, invChange.getNewTags().size());
    Assert.assertEquals(1, invChange.getLostTags().size());
    Assert.assertEquals(tagA, invChange.getLostTags().get(0).getId());

    Assert.assertTrue(arrivedTags.isEmpty());
    Assert.assertTrue(departedTags.isEmpty());
    Assert.assertTrue(inventoryChanges.isEmpty());

    inventory.clear();
  }

  /**
   * Test the implementation with continuous tag updates
   * 
   * @throws Exception if an error occurs
   */
  @Test
  public void testInventoryContinuous() throws Exception {
    logger.info("testInventoryContinuous");
    inventory = new Inventory<>("test", listener, 200);
    // inventory.start();

    String tagA = "AAAA";
    Timer addTagATimer = new Timer(true);
    addTagATimer.schedule(new AddingTagToInventory(Arrays.asList(tagA)), 0, 150);

    String tagB = "BBBB";
    String tagC = "CCCC";
    String tagD = "DDDD";
    Timer addTagBTimer = new Timer(true);
    addTagBTimer.schedule(new AddingTagToInventory(Arrays.asList(tagB, tagC, tagD)), 0, 150);

    // wait
    Thread.sleep(1000);
    // check arrive Events
    Assert.assertEquals(4, arrivedTags.size());
    arrivedTags.clear();

    Assert.assertEquals(2, inventoryChanges.size());
    int tagSize = inventoryChanges.poll().getNewTags().size();
    Assert.assertTrue(1 == tagSize || 3 == tagSize);
    tagSize = inventoryChanges.poll().getNewTags().size();
    Assert.assertTrue(1 == tagSize || 3 == tagSize);


    // stop adding tag b
    addTagATimer.cancel();
    // check tag depart event
    TagDepartedEvent<UhfTag> tagDeparted = departedTags.poll(600, TimeUnit.MILLISECONDS);
    Assert.assertFalse(null == tagDeparted);
    Assert.assertEquals(tagA, tagDeparted.getTag().getId());
    InventoryChangedEvent<UhfTag> invChange = inventoryChanges.poll(100, TimeUnit.MILLISECONDS);
    Assert.assertFalse(null == invChange);
    Assert.assertEquals(3, invChange.getTags().size());
    Assert.assertEquals(0, invChange.getNewTags().size());
    Assert.assertEquals(1, invChange.getLostTags().size());
    Assert.assertEquals(tagA, invChange.getLostTags().get(0).getId());

    addTagBTimer.cancel();
    Assert.assertTrue(arrivedTags.isEmpty());
    Assert.assertTrue(departedTags.isEmpty());
    Assert.assertTrue(inventoryChanges.isEmpty());

    inventory.clear();
  }

  /**
   * @author mn
   *
   */
  class AddingTagToInventory extends TimerTask {
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
      inventory.updateInventory(tags);
    }
  }


}

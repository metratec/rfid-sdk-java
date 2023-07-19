/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH All rights reserved.
 *******************************************************************************/
package com.metratec.lib.junit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.metratec.lib.inventory.SimpleInventory;
import com.metratec.lib.tag.UhfTag;

/**
 * Test if the license files are generated and stored in the jar file
 * 
 * @author mn
 *
 */
public class TestSimpleInventory {
  private Logger logger = LoggerFactory.getLogger(TestSimpleInventory.class);

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
    SimpleInventory<UhfTag> inv = new SimpleInventory<>();
    logger.info(this.getClass().getSimpleName() + " testAddTags");
    inv = new SimpleInventory<>();

    UhfTag tagA = new UhfTag("AAAA", System.currentTimeMillis());
    inv.updateInventory(Arrays.asList(tagA));
    Assert.assertEquals(1, inv.getInventory().size());
    Assert.assertEquals(tagA, inv.getInventory().get(0));
    Assert.assertEquals(1, inv.getAddedTags().size());
    inv.updateInventory(Arrays.asList(tagA));
    Assert.assertEquals(1, inv.getInventory().size());
    Assert.assertEquals(tagA, inv.getInventory().get(0));
    Assert.assertEquals(0, inv.getAddedTags().size());

    UhfTag tagB = new UhfTag("BBBB", System.currentTimeMillis());
    inv.updateInventory(Arrays.asList(tagA, tagB));
    Assert.assertEquals(2, inv.getInventory().size());
    Assert.assertEquals(1, inv.getAddedTags().size());
    Assert.assertEquals(tagB, inv.getAddedTags().get(0));
    inv.updateInventory(Arrays.asList(tagA, tagB));
    Assert.assertEquals(2, inv.getInventory().size());
    Assert.assertEquals(0, inv.getAddedTags().size());


    UhfTag tagC = new UhfTag("CCCC", System.currentTimeMillis());
    inv.updateInventory(Arrays.asList(tagA, tagB, tagC));
    Assert.assertEquals(3, inv.getInventory().size());
    Assert.assertEquals(1, inv.getAddedTags().size());
    Assert.assertEquals(tagC, inv.getAddedTags().get(0));
    inv.updateInventory(Arrays.asList(tagA, tagB, tagC));
    Assert.assertEquals(3, inv.getInventory().size());
    Assert.assertEquals(0, inv.getAddedTags().size());

  }

  /**
   * adding tag
   *
   * @throws Exception if an error occurs
   */
  @Test
  public void testRemoveTags() throws Exception {
    SimpleInventory<UhfTag> inv = new SimpleInventory<>();
    logger.info(this.getClass().getSimpleName() + " testRemoveTags");
    inv = new SimpleInventory<>();

    UhfTag tagA = new UhfTag("AAAA", System.currentTimeMillis());
    // tagA is founde
    inv.updateInventory(Arrays.asList(tagA));
    Assert.assertEquals(1, inv.getAddedTags().size());
    Assert.assertEquals(tagA, inv.getAddedTags().get(0));


    UhfTag tagB = new UhfTag("BBBB", System.currentTimeMillis());
    // tagA and tagB is found
    inv.updateInventory(Arrays.asList(tagA, tagB));
    Assert.assertEquals(1, inv.getAddedTags().size());
    Assert.assertEquals(tagB, inv.getAddedTags().get(0));

    // now only tagB is found
    inv.updateInventory(Arrays.asList(tagB));
    Assert.assertEquals(0, inv.getAddedTags().size());
    Assert.assertEquals(1, inv.getInventory().size());
    Assert.assertEquals(tagB, inv.getInventory().get(0));
    Assert.assertEquals(1, inv.getRemovedTags().size());
    Assert.assertEquals(tagA, inv.getRemovedTags().get(0));

  }

  /**
   * Test with many tags
   *
   * @throws Exception if an error occurs
   */
  @Test
  public void testManipulatedArray() throws Exception {
    logger.info(this.getClass().getSimpleName() + " bigTest");
    SimpleInventory<UhfTag> inv = new SimpleInventory<>();
    int count = 10000;
    List<UhfTag> tags = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      tags.add(new UhfTag("AAAA" + i, System.currentTimeMillis()));
    }
    // long start = System.currentTimeMillis();
    inv.updateInventory(tags);
    // long end = System.currentTimeMillis();
    // System.out.println(end - start);
    Assert.assertEquals(count, inv.getAddedTags().size());
    Assert.assertEquals(count, inv.getInventory().size());

    count /= 2;
    // now clear the tag list - check if this inventory list is used in the SimpleInventory
    // implementation
    tags.clear();
    for (int i = 0; i < count; i++) {
      tags.add(new UhfTag("AAAA" + i, System.currentTimeMillis()));
    }
    // start = System.currentTimeMillis();
    inv.updateInventory(tags);
    // end = System.currentTimeMillis();
    // System.out.println(end - start);
    Assert.assertEquals(0, inv.getAddedTags().size());
    Assert.assertEquals(count, inv.getInventory().size());
    Assert.assertEquals(count, inv.getRemovedTags().size());
  }

  /**
   * check the correct incrementation of the seen count and the timestamps of the tags
   */
  @Test
  public void testTagCount() {
    SimpleInventory<UhfTag> inv = new SimpleInventory<>();
    logger.info(this.getClass().getSimpleName() + " testTagCount");
    inv = new SimpleInventory<>();

    String tagA = "AAAA";
    String tagB = "BBBB";

    long timestampA = System.currentTimeMillis();

    inv.updateInventory(
        Arrays.asList(new UhfTag(tagA, timestampA), new UhfTag(tagB, System.currentTimeMillis())));
    Assert.assertEquals(2, inv.getAddedTags().size());
    Assert.assertEquals(0, inv.getRemovedTags().size());
    Assert.assertEquals(2, inv.getInventory().size());
    Assert.assertNotNull(getTag(tagA, inv.getInventory()));
    Assert.assertNotNull(getTag(tagB, inv.getInventory()));

    inv.updateInventory(Arrays.asList(new UhfTag("AAAA", System.currentTimeMillis())));
    Assert.assertEquals(0, inv.getAddedTags().size());
    Assert.assertEquals(1, inv.getInventory().size());
    Assert.assertNotNull(getTag(tagA, inv.getInventory()));
    Assert.assertEquals(2, getTag(tagA, inv.getInventory()).getSeenCount().intValue());
    Assert.assertEquals(1, inv.getRemovedTags().size());
    Assert.assertNotNull(getTag(tagB, inv.getRemovedTags()));

    long timestampB = System.currentTimeMillis();
    inv.updateInventory(Arrays.asList(new UhfTag("AAAA", System.currentTimeMillis()),
        new UhfTag(tagB, timestampB)));
    Assert.assertEquals(1, inv.getAddedTags().size());
    Assert.assertNotNull(getTag(tagB, inv.getAddedTags()));
    Assert.assertEquals(2, inv.getInventory().size());
    Assert.assertNotNull(getTag(tagA, inv.getInventory()));
    Assert.assertNotNull(getTag(tagB, inv.getInventory()));
    Assert.assertEquals(3, getTag(tagA, inv.getInventory()).getSeenCount().intValue());
    Assert.assertEquals(1, getTag(tagB, inv.getInventory()).getSeenCount().intValue());
    Assert.assertEquals(0, inv.getRemovedTags().size());

    long timestampLast = System.currentTimeMillis();
    inv.updateInventory(
        Arrays.asList(new UhfTag("AAAA", timestampLast), new UhfTag(tagB, timestampLast)));
    Assert.assertEquals(0, inv.getAddedTags().size());
    Assert.assertEquals(2, inv.getInventory().size());
    Assert.assertNotNull(getTag(tagA, inv.getInventory()));
    Assert.assertNotNull(getTag(tagB, inv.getInventory()));
    Assert.assertEquals(4, getTag(tagA, inv.getInventory()).getSeenCount().intValue());
    Assert.assertEquals(2, getTag(tagB, inv.getInventory()).getSeenCount().intValue());
    Assert.assertEquals(timestampA,
        getTag(tagA, inv.getInventory()).getFirstSeenTimestamp().longValue());
    Assert.assertEquals(timestampLast,
        getTag(tagA, inv.getInventory()).getLastSeenTimestamp().longValue());
    Assert.assertEquals(timestampB,
        getTag(tagB, inv.getInventory()).getFirstSeenTimestamp().longValue());
    Assert.assertEquals(timestampLast,
        getTag(tagB, inv.getInventory()).getLastSeenTimestamp().longValue());

    Assert.assertEquals(0, inv.getRemovedTags().size());

  }

  private UhfTag getTag(String tag, List<UhfTag> tags) {
    for (UhfTag entry : tags) {
      if (entry.getId().equals(tag)) {
        return entry;
      }
    }
    return null;
  }
}

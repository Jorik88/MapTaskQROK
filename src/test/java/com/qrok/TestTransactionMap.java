package com.qrok;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasKey;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TestTransactionMap {

  private TransactionMap<String, String> transactionMap;
  private String firstKey = "FirstKey";
  private String secondKey = "SecondKey";
  private String thirdKey = "ThirdKey";
  private String firstThreadKey = "FirstThreadKey";
  private String secondThreadKey = "SecondThreadKey";

  @Rule
  public ExpectedException testRuleException = ExpectedException.none();

  @Before
  public void init() {
    transactionMap = new TransactionMapImpl<>();
  }

  /**
   * This method check put() and get() methods without transaction.
   */
  @Test
  public void testPutAndGetWithoutTransaction() {
    transactionMap.put(firstKey, "FirstKey");
    transactionMap.put(secondKey, "SecondKey");

    Assert.assertEquals("Actual transactionMap size must be as expected", 2, transactionMap.size());
    Assert.assertThat("transactionMap must contain keys",
        transactionMap, allOf(hasKey(firstKey), hasKey(secondKey)));
  }

  /**
   * This test check that, all new Thread get copy of exist collection.
   */
  @Test
  public void testStartTransaction() {
    transactionMap.put(firstKey, "FirstValue");
    Runnable firstThread = new Runnable() {
      @Override
      public void run() {
        transactionMap.startTransaction();
        Assert.assertEquals("Actual transactionMap size must be 1", 1, transactionMap.size());
        Assert.assertEquals("Actual object must be FirstValue", transactionMap.get(firstKey), "FirstValue");
      }
    };
    new Thread(firstThread).start();
  }

  /**
   * This method check how work startTransaction() and commit() method.
   */
  @Test
  public void testStartTransactionAndCommit() {
    transactionMap.put(firstKey, "FirstValue");
    transactionMap.put(secondKey, "SecondValue");

    transactionMap.startTransaction();
    transactionMap.put(thirdKey, "ThirdValue");
    Assert.assertEquals("Actual transactionMap size must be 2", 2, transactionMap.size());

    transactionMap.commit();
    Assert.assertEquals("Actual transactionMap size must be 3", 3, transactionMap.size());
    Assert.assertThat("transactionMap must contain keys",
        transactionMap, allOf(hasKey(firstKey), hasKey(secondKey), hasKey(thirdKey)));
  }

  /**
   * This method check rollBack() method.
   */
  @Test
  public void testRollbackInTransaction() throws InterruptedException {
    transactionMap.put(firstKey, "FirstValue");
    Runnable firstThread = new Runnable() {
      @Override
      public void run() {
        transactionMap.startTransaction();
        transactionMap.put(firstThreadKey, "FirstThreadValue");
        transactionMap.put(secondKey, "SecondValue");
        transactionMap.rollback();
        Assert.assertEquals("Actual transactionMap size must be 1", 1, transactionMap.size());
        Assert.assertThat("transactionMap must contain keys", transactionMap, hasKey(firstKey));
      }
    };
    new Thread(firstThread).start();
    Thread.sleep(1000);
    Assert.assertEquals("Actual transactionMap size must be 1", 1, transactionMap.size());
    Assert.assertThat("transactionMap must contain keys", transactionMap, hasKey(firstKey));
  }

  /**
   *  This test check remove Object by key in transaction.
   */
  @Test
  public void testRemoveObjectInTransaction() {
    transactionMap.put(firstKey, "FirstValue");
    transactionMap.put(secondKey, "SecondValue");

    transactionMap.startTransaction();
    transactionMap.remove(firstKey);
    transactionMap.commit();

    Assert.assertEquals("Actual transactionMap size must be as expected", 1, transactionMap.size());
    Assert.assertThat("transactionMap must contain key",
        transactionMap, hasKey(secondKey));
  }

  /**
   *  This test check remove Object by key in new Thread with transaction.
   */
  @Test
  public void testRemoveObjectInTransactionInNewThread() throws InterruptedException {
    transactionMap.put(firstKey, "FirstValue");
    transactionMap.put(secondKey, "SecondValue");

    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        transactionMap.startTransaction();
        transactionMap.remove(firstKey);
        transactionMap.commit();
        Assert.assertEquals("Actual transactionMap size must be 1", 1, transactionMap.size());
        Assert.assertThat("transactionMap must contain key",
            transactionMap, hasKey(secondKey));
      }
    };
    new Thread(runnable).start();
    Thread.sleep(1000);

    Assert.assertEquals("Actual transactionMap size must be as expected", 1, transactionMap.size());
    Assert.assertThat("transactionMap must contain key", transactionMap, hasKey(secondKey));
  }

  /**
   * This method check remove and rollback method in transaction.
   */
  @Test
  public void testRemoveAndRollbackMethodInTransaction() {
    transactionMap.put(firstKey, "FirstValue");

    transactionMap.startTransaction();
    transactionMap.remove(firstKey);
    transactionMap.rollback();

    Assert.assertEquals("Actual transactionMap size must be as expected", 1, transactionMap.size());
    Assert.assertThat("transactionMap must contain key", transactionMap, hasKey(firstKey));
  }

  /**
   *  This test check commit() method, when transaction is not open.
   */
  @Test
  public void testCommitWithoutStartTransaction() {
    this.testRuleException.expect(IllegalStateException.class);
    this.testRuleException.expectMessage("Transaction is not open");
    transactionMap.commit();
  }

  /**
   *  This test check rollback() method, when transaction is not open.
   */
  @Test
  public void testRollbackWithoutStartTransaction() {
    this.testRuleException.expect(IllegalStateException.class);
    this.testRuleException.expectMessage("Transaction is not open");
    transactionMap.rollback();
  }

  /**
   * This method check how will be changes collection in two threads.
   *
   * @throws InterruptedException  @see java.lang.InterruptedException
   */
  @Test
  public void testCaseChangeMapInTwoThread() throws InterruptedException {
    transactionMap.put(firstKey, "FirstValue");
    Runnable firstThread = new Runnable() {
      @Override
      public void run() {
        transactionMap.startTransaction();
        transactionMap.put(firstThreadKey, "FirstThreadValue");
      }
    };

    Runnable secondThread = new Runnable() {
      @Override
      public void run() {
        transactionMap.startTransaction();
        transactionMap.put(secondThreadKey, "SecondThreadValue");
        transactionMap.commit();
      }
    };

    new Thread(firstThread).start();
    new Thread(secondThread).start();
    Thread.sleep(1000);

    Assert.assertEquals("Actual transaction mup must be as expected", 2, transactionMap.size());
    Assert.assertThat("transactionMap must contain keys",
        transactionMap, allOf(hasKey(firstKey), hasKey(secondThreadKey)));
  }

  /**
   * This test check remove Object by key in two thread.
   */
  @Test
  public void testRemoveObjectByKeyInTwoThread() throws InterruptedException {
    transactionMap.put(firstKey, "FirstValue");
    final Runnable firstThread = new Runnable() {
      @Override
      public void run() {
        transactionMap.startTransaction();
        transactionMap.put(firstThreadKey, "FirstThreadValue");
      }
    };

    Runnable secondThread = new Runnable() {
      @Override
      public void run() {
        transactionMap.startTransaction();
        transactionMap.put(secondThreadKey, "SecondThreadValue");
        transactionMap.put(thirdKey, "ThirdValue");
        transactionMap.remove(thirdKey);
        transactionMap.commit();
      }
    };

    new Thread(firstThread).start();
    new Thread(secondThread).start();
    Thread.sleep(1000);

    Assert.assertEquals("Actual transaction mup must be as expected", 2, transactionMap.size());
    Assert.assertThat("transactionMap must contain keys",
        transactionMap, allOf(hasKey(firstKey), hasKey(secondThreadKey)));
  }

  @Test
  public void testFork() {

  }

  @Test
  public void emptyTest() {



  }
}

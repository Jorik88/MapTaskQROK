package com.qrok;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasKey;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestTransactionMap {

  private TransactionMap<String, String> transactionMap;
  private String firstKey = "FirstKey";
  private String secondKey = "SecondKey";
  private String thirdKey = "ThirdKey";
  private String firstThreadKey = "FirstThreadKey";
  private String secondThreadKey = "SecondThreadKey";

  @Before
  public void init() {
    transactionMap = new TransactionMapImpl<>();
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
   * This method check how work rollBack() method.
   */
  @Test
  public void testRollbackTransaction() {
    transactionMap.put(firstKey, "FirstValue");
    transactionMap.startTransaction();
    transactionMap.put(secondKey, "SecondValue");
    transactionMap.rollback();
    Assert.assertEquals("Actual transactionMap size must be 1", 1, transactionMap.size());
    Assert.assertThat("transactionMap must contain keys", transactionMap, hasKey(firstKey));
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

    Assert.assertEquals("Actual transaction mup must be 2", 2, transactionMap.size());
    Assert.assertThat("transactionMap must contain keys",
        transactionMap, allOf(hasKey(firstKey), hasKey(secondThreadKey)));
  }
}

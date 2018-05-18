package com.qrok;

import java.util.Map;

/**
 * This implementation provides ability to change data in transaction scope.
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public interface TransactionMap<K,V> extends Map<K,V> {

  /**
   * Invoke this to start new transaction.
   */
  void startTransaction();

  /**
   *  Commit all changes in the transaction.
   */
  void commit();

  /**
   * Cancel all changes in the transaction.
   */
  void rollback();
}

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
   *  @throws IllegalStateException if the {@code startTransaction} method has not yet been called,
   *  or the {@code rollback} method has already been called
   */
  void commit();

  /**
   * Cancel all changes in the transaction.
   * @throws IllegalStateException if the {@code startTransaction} method has not yet been called,
   * or the {@code commit} method has already been called
   */
  void rollback();
}

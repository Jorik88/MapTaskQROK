package com.qrok;

import java.util.HashMap;
import java.util.Map;

/**
 * This implementation provides ability to change data in transaction scope.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */

public class TransactionMapImpl<K, V> extends HashMap<K, V> implements TransactionMap<K, V> {

  private ThreadLocal<Map<K, V>> threadLocal = new ThreadLocal<>();

  /**
   * This method is start a new transaction.
   */
  public void startTransaction() {
    threadLocal.set(new HashMap<>(this));
  }

  /**
   * Saves the all changes made in the scope of this transaction.
   */
  public void commit() {
    if (threadLocal.get() == null) {
      return;
    }
    putAll(threadLocal.get());
    threadLocal.remove();
  }

  /**
   * Cancels all changes made in the scope of this transaction.
   */
  public void rollback() {
    threadLocal.remove();
  }

  /**
   * @param key key with which the specified value is to be associated
   * @param value value to be associated with the specified ke
   * @see #put(Object, Object)
   */
  @Override
  public V put(K key, V value) {
    if (threadLocal.get() != null) {
      return threadLocal.get().put(key, value);
    }
    return super.put(key, value);
  }

  /**
   * Returns the value to which the specified key is mapped,
   * or {@code null} if this map contains no mapping for the key.
   */

  @Override
  public V get(Object key) {
    if (threadLocal.get() != null) {
      return threadLocal.get().get(key);
    }
    return super.get(key);
  }
}

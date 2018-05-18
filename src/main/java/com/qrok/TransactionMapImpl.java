package com.qrok;

import java.util.HashMap;
import java.util.Map;

public class TransactionMapImpl<K, V> extends HashMap<K, V> implements TransactionMap<K, V> {

  private ThreadLocal<Map<K, V>> threadLocal = new ThreadLocal<>();

  public void startTransaction() {
    threadLocal.set(new HashMap<>(this));
  }

  public void commit() {
    if (threadLocal.get() == null) {
      return;
    }
    putAll(threadLocal.get());
    threadLocal.remove();
  }

  public void rollback() {
    threadLocal.remove();
  }

  /**
   * If transaction is not open data save in into storage.
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

  /**
   * Removes the mapping for the specified key from this map if present.
   * @param key key whose mapping is to be removed from the map
   * @return @see #remove(Object key)
   */
  @Override
  public V remove(Object key) {
    if (threadLocal != null) {
      return threadLocal.get().remove(key);
    }
    return super.remove(key);
  }
}

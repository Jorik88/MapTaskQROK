package com.qrok;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TransactionMapImpl<K, V> extends HashMap<K, V> implements TransactionMap<K, V> {

  private ThreadLocal<Map<K, V>> transactionStorage = new ThreadLocal<>();
  private ThreadLocal<Set<K>> removedKeys = new ThreadLocal<>();

  public void startTransaction() {
    transactionStorage.set(new HashMap<>(this));
    removedKeys.set(new HashSet<>());
  }

  public void commit() {
    if (transactionStorage.get() == null) {
      throw new IllegalStateException("Transaction is not open");
    }
    removedKeys.get().forEach(super::remove);
    putAll(transactionStorage.get());
    transactionStorage.remove();
    removedKeys.remove();
  }

  public void rollback() {
    if (transactionStorage.get() == null){
      throw new IllegalStateException("Transaction is not open");
    }
    transactionStorage.remove();
    removedKeys.remove();
  }

  /**
   * If transaction is not open data save in into storage.
   * @param key key with which the specified value is to be associated
   * @param value value to be associated with the specified ke
   * @see #put(Object, Object)
   */
  @Override
  public V put(K key, V value) {
    if (transactionStorage.get() != null) {
      return transactionStorage.get().put(key, value);
    }
    return super.put(key, value);
  }

  /**
   * Returns the value to which the specified key is mapped,
   * or {@code null} if this map contains no mapping for the key.
   */
  @Override
  public V get(Object key) {
    if (transactionStorage.get() != null) {
      return transactionStorage.get().get(key);
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
    if (transactionStorage.get() != null) {
      removedKeys.get().add((K) key);
      return transactionStorage.get().remove(key);
    }
    return super.remove(key);
  }
}

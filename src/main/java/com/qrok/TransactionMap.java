package com.qrok;

import java.util.Map;

public interface TransactionMap<K,V> extends Map<K,V> {

  void startTransaction();

  void commit();

  void rollback();
}

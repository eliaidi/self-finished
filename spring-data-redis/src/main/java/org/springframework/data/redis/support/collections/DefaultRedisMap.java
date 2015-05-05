/*
 * Copyright 2011-2013 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.redis.support.collections;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.SessionCallback;

/**
 * Default implementation for {@link RedisMap}. Note that the current implementation doesn't provide the same locking
 * semantics across all methods. In highly concurrent environments, race conditions might appear.
 * 
 * @author Costin Leau
 */
public class DefaultRedisMap<K, V> implements RedisMap<K, V> {

	private final BoundHashOperations<String, K, V> hashOps;

	private class DefaultRedisMapEntry implements Map.Entry<K, V> {

		private K key;
		private V value;

		public DefaultRedisMapEntry(K key, V value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public K getKey() {
			return key;
		}

		@Override
		public V getValue() {
			return value;
		}

		@Override
		public V setValue(V value) {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Constructs a new <code>DefaultRedisMap</code> instance.
	 * 
	 * @param key
	 * @param operations
	 */
	public DefaultRedisMap(String key, RedisOperations<String, ?> operations) {
		this.hashOps = operations.boundHashOps(key);
	}

	/**
	 * Constructs a new <code>DefaultRedisMap</code> instance.
	 * 
	 * @param boundOps
	 */
	public DefaultRedisMap(BoundHashOperations<String, K, V> boundOps) {
		this.hashOps = boundOps;
	}

	@Override
	public Long increment(K key, long delta) {
		return hashOps.increment(key, delta);
	}

	@Override
	public Double increment(K key, double delta) {
		return hashOps.increment(key, delta);
	}

	@Override
	public RedisOperations<String, ?> getOperations() {
		return hashOps.getOperations();
	}

	@Override
	public void clear() {
		getOperations().delete(Collections.singleton(getKey()));
	}

	@Override
	public boolean containsKey(Object key) {
		Boolean result = hashOps.hasKey(key);
		checkResult(result);
		return result;
	}

	@Override
	public boolean containsValue(Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		Set<K> keySet = keySet();
		checkResult(keySet);
		Collection<V> multiGet = hashOps.multiGet(keySet);

		Iterator<K> keys = keySet.iterator();
		Iterator<V> values = multiGet.iterator();

		Set<Map.Entry<K, V>> entries = new LinkedHashSet<Entry<K, V>>();
		while (keys.hasNext()) {
			entries.add(new DefaultRedisMapEntry(keys.next(), values.next()));
		}

		return entries;
	}

	@Override
	public V get(Object key) {
		return hashOps.get(key);
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public Set<K> keySet() {
		return hashOps.keys();
	}

	@Override
	public V put(K key, V value) {
		V oldV = get(key);
		hashOps.put(key, value);
		return oldV;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		hashOps.putAll(m);
	}

	@Override
	public V remove(Object key) {
		V v = get(key);
		hashOps.delete(key);
		return v;
	}

	@Override
	public int size() {
		Long size = hashOps.size();
		checkResult(size);
		return size.intValue();
	}

	@Override
	public Collection<V> values() {
		return hashOps.values();
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;

		if (o instanceof RedisMap) {
			return o.hashCode() == hashCode();
		}
		return false;
	}

	@Override
	public int hashCode() {
		int result = 17 + getClass().hashCode();
		result = result * 31 + getKey().hashCode();
		return result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("RedisStore for key:");
		sb.append(getKey());
		return sb.toString();
	}

	@Override
	public V putIfAbsent(K key, V value) {
		return (hashOps.putIfAbsent(key, value) ? null : get(key));
	}

	@Override
	public boolean remove(final Object key, final Object value) {
		if (value == null) {
			throw new NullPointerException();
		}
		return hashOps.getOperations().execute(new SessionCallback<Boolean>() {
			@Override
			@SuppressWarnings({ "unchecked", "rawtypes" })
			public Boolean execute(RedisOperations ops) {
				for (;;) {
					ops.watch(Collections.singleton(getKey()));
					V v = get(key);
					if (value.equals(v)) {
						ops.multi();
						remove(key);
						if (ops.exec(ops.getHashValueSerializer()) != null) {
							return true;
						}
					} else {
						return false;
					}
				}
			}
		});
	}

	@Override
	public boolean replace(final K key, final V oldValue, final V newValue) {
		if (oldValue == null || newValue == null) {
			throw new NullPointerException();
		}
		return hashOps.getOperations().execute(new SessionCallback<Boolean>() {
			@Override
			@SuppressWarnings({ "unchecked", "rawtypes" })
			public Boolean execute(RedisOperations ops) {
				for (;;) {
					ops.watch(Collections.singleton(getKey()));
					V v = get(key);
					if (oldValue.equals(v)) {
						ops.multi();
						put(key, newValue);
						if (ops.exec(ops.getHashValueSerializer()) != null) {
							return true;
						}
					} else {
						return false;
					}
				}
			}
		});
	}

	@Override
	public V replace(final K key, final V value) {
		if (value == null) {
			throw new NullPointerException();
		}
		return hashOps.getOperations().execute(new SessionCallback<V>() {
			@Override
			@SuppressWarnings({ "unchecked", "rawtypes" })
			public V execute(RedisOperations ops) {
				for (;;) {
					ops.watch(Collections.singleton(getKey()));
					V v = get(key);
					if (v != null) {
						ops.multi();
						put(key, value);
						if (ops.exec(ops.getHashValueSerializer()) != null) {
							return v;
						}
					} else {
						return null;
					}
				}
			}
		});
	}

	@Override
	public Boolean expire(long timeout, TimeUnit unit) {
		return hashOps.expire(timeout, unit);
	}

	@Override
	public Boolean expireAt(Date date) {
		return hashOps.expireAt(date);
	}

	@Override
	public Long getExpire() {
		return hashOps.getExpire();
	}

	@Override
	public Boolean persist() {
		return hashOps.persist();
	}

	@Override
	public String getKey() {
		return hashOps.getKey();
	}

	@Override
	public void rename(String newKey) {
		hashOps.rename(newKey);
	}

	@Override
	public DataType getType() {
		return hashOps.getType();
	}

	private void checkResult(Object obj) {
		if (obj == null) {
			throw new IllegalStateException("Cannot read collection with Redis connection in pipeline/multi-exec mode");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.support.collections.RedisMap#scan()
	 */
	@Override
	public Cursor<java.util.Map.Entry<K, V>> scan() {
		return scan(ScanOptions.NONE);
	}

	/**
	 * @since 1.4
	 * @param options
	 * @return
	 */
	private Cursor<java.util.Map.Entry<K, V>> scan(ScanOptions options) {
		return hashOps.scan(options);
	}
}

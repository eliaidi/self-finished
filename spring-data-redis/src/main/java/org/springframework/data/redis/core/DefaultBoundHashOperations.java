/*
 * Copyright 2011-2014 the original author or authors.
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
package org.springframework.data.redis.core;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.data.redis.connection.DataType;

/**
 * Default implementation for {@link HashOperations}.
 * 
 * @author Costin Leau
 * @author Christoph Strobl
 */
class DefaultBoundHashOperations<H, HK, HV> extends DefaultBoundKeyOperations<H> implements
		BoundHashOperations<H, HK, HV> {

	private final HashOperations<H, HK, HV> ops;

	/**
	 * Constructs a new <code>DefaultBoundHashOperations</code> instance.
	 * 
	 * @param key
	 * @param template
	 */
	public DefaultBoundHashOperations(H key, RedisOperations<H, ?> operations) {
		super(key, operations);
		this.ops = operations.opsForHash();
	}

	@Override
	public void delete(Object... keys) {
		ops.delete(getKey(), keys);
	}

	@Override
	public HV get(Object key) {
		return ops.get(getKey(), key);
	}

	@Override
	public List<HV> multiGet(Collection<HK> hashKeys) {
		return ops.multiGet(getKey(), hashKeys);
	}

	@Override
	public RedisOperations<H, ?> getOperations() {
		return ops.getOperations();
	}

	@Override
	public Boolean hasKey(Object key) {
		return ops.hasKey(getKey(), key);
	}

	@Override
	public Long increment(HK key, long delta) {
		return ops.increment(getKey(), key, delta);
	}

	@Override
	public Double increment(HK key, double delta) {
		return ops.increment(getKey(), key, delta);
	}

	@Override
	public Set<HK> keys() {
		return ops.keys(getKey());
	}

	@Override
	public Long size() {
		return ops.size(getKey());
	}

	@Override
	public void putAll(Map<? extends HK, ? extends HV> m) {
		ops.putAll(getKey(), m);
	}

	@Override
	public void put(HK key, HV value) {
		ops.put(getKey(), key, value);
	}

	@Override
	public Boolean putIfAbsent(HK key, HV value) {
		return ops.putIfAbsent(getKey(), key, value);
	}

	@Override
	public List<HV> values() {
		return ops.values(getKey());
	}

	@Override
	public Map<HK, HV> entries() {
		return ops.entries(getKey());
	}

	@Override
	public DataType getType() {
		return DataType.HASH;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.core.BoundHashOperations#hscan(java.lang.Object)
	 */
	@Override
	public Cursor<Entry<HK, HV>> scan(ScanOptions options) {
		return ops.scan(getKey(), options);
	}
}

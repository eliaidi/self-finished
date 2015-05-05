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
import java.util.Set;

import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;

/**
 * Default implementation for {@link BoundZSetOperations}.
 * 
 * @author Costin Leau
 * @author Christoph Strobl
 */
class DefaultBoundZSetOperations<K, V> extends DefaultBoundKeyOperations<K> implements BoundZSetOperations<K, V> {

	private final ZSetOperations<K, V> ops;

	/**
	 * Constructs a new <code>DefaultBoundZSetOperations</code> instance.
	 * 
	 * @param key
	 * @param oeprations
	 */
	public DefaultBoundZSetOperations(K key, RedisOperations<K, V> operations) {
		super(key, operations);
		this.ops = operations.opsForZSet();
	}

	@Override
	public Boolean add(V value, double score) {
		return ops.add(getKey(), value, score);
	}

	@Override
	public Long add(Set<TypedTuple<V>> tuples) {
		return ops.add(getKey(), tuples);
	}

	@Override
	public Double incrementScore(V value, double delta) {
		return ops.incrementScore(getKey(), value, delta);
	}

	@Override
	public RedisOperations<K, V> getOperations() {
		return ops.getOperations();
	}

	@Override
	public void intersectAndStore(K otherKey, K destKey) {
		ops.intersectAndStore(getKey(), otherKey, destKey);
	}

	@Override
	public void intersectAndStore(Collection<K> otherKeys, K destKey) {
		ops.intersectAndStore(getKey(), otherKeys, destKey);
	}

	@Override
	public Set<V> range(long start, long end) {
		return ops.range(getKey(), start, end);
	}

	@Override
	public Set<V> rangeByScore(double min, double max) {
		return ops.rangeByScore(getKey(), min, max);
	}

	@Override
	public Set<TypedTuple<V>> rangeByScoreWithScores(double min, double max) {
		return ops.rangeByScoreWithScores(getKey(), min, max);
	}

	@Override
	public Set<TypedTuple<V>> rangeWithScores(long start, long end) {
		return ops.rangeWithScores(getKey(), start, end);
	}

	@Override
	public Set<V> reverseRangeByScore(double min, double max) {
		return ops.reverseRangeByScore(getKey(), min, max);
	}

	@Override
	public Set<TypedTuple<V>> reverseRangeByScoreWithScores(double min, double max) {
		return ops.reverseRangeByScoreWithScores(getKey(), min, max);
	}

	@Override
	public Set<TypedTuple<V>> reverseRangeWithScores(long start, long end) {
		return ops.reverseRangeWithScores(getKey(), start, end);
	}

	@Override
	public Long rank(Object o) {
		return ops.rank(getKey(), o);
	}

	@Override
	public Long reverseRank(Object o) {
		return ops.reverseRank(getKey(), o);
	}

	@Override
	public Double score(Object o) {
		return ops.score(getKey(), o);
	}

	@Override
	public Long remove(Object... values) {
		return ops.remove(getKey(), values);
	}

	@Override
	public void removeRange(long start, long end) {
		ops.removeRange(getKey(), start, end);
	}

	@Override
	public void removeRangeByScore(double min, double max) {
		ops.removeRangeByScore(getKey(), min, max);
	}

	@Override
	public Set<V> reverseRange(long start, long end) {
		return ops.reverseRange(getKey(), start, end);
	}

	@Override
	public Long count(double min, double max) {
		return ops.count(getKey(), min, max);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.core.BoundZSetOperations#size()
	 */
	@Override
	public Long size() {
		return zCard();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.core.BoundZSetOperations#zCard()
	 */
	@Override
	public Long zCard() {
		return ops.zCard(getKey());
	}

	@Override
	public void unionAndStore(K otherKey, K destKey) {
		ops.unionAndStore(getKey(), otherKey, destKey);
	}

	@Override
	public void unionAndStore(Collection<K> otherKeys, K destKey) {
		ops.unionAndStore(getKey(), otherKeys, destKey);
	}

	@Override
	public DataType getType() {
		return DataType.ZSET;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.core.BoundZSetOperations#scan(org.springframework.data.redis.core.ScanOptions)
	 */
	@Override
	public Cursor<TypedTuple<V>> scan(ScanOptions options) {
		return ops.scan(getKey(), options);
	}
}

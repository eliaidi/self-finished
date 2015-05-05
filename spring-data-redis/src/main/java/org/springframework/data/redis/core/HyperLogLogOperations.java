/*
 * Copyright 2014 the original author or authors.
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

/**
 * @author Christoph Strobl
 * @since 1.5
 */
public interface HyperLogLogOperations<K, V> {

	/**
	 * Adds the given {@literal values} to the {@literal key}.
	 * 
	 * @param key must not be {@literal null}.
	 * @param values must not be {@literal null}.
	 * @return 1 of at least one of the values was added to the key; 0 otherwise.
	 */
	@SuppressWarnings("unchecked")
	Long add(K key, V... values);

	/**
	 * Gets the current number of elements within the {@literal key}.
	 * 
	 * @param keys must not be {@literal null} or {@literal empty}.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	Long size(K... keys);

	/**
	 * Merges all values of given {@literal sourceKeys} into {@literal destination} key.
	 * 
	 * @param destination key of HyperLogLog to move source keys into.
	 * @param sourceKeys must not be {@literal null} or {@literal empty}.
	 */
	@SuppressWarnings("unchecked")
	Long union(K destination, K... sourceKeys);

	/**
	 * Removes the given {@literal key}.
	 * 
	 * @param key must not be {@literal null}.
	 */
	void delete(K key);

}

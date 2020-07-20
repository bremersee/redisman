/*
 * Copyright 2020 the original author or authors.
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

package org.bremersee.redisman.service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.bremersee.redisman.model.RedisEntry;
import org.bremersee.redisman.model.RedisEntry.RedisEntryBuilder;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * The redis service implementation.
 *
 * @author Christian Bremer
 */
@Component
public class RedisServiceImpl implements RedisService {

  private final ReactiveStringRedisTemplate redisTemplate;

  /**
   * Instantiates a new redis service.
   *
   * @param redisTemplate the redis template
   */
  public RedisServiceImpl(ReactiveStringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @Override
  public Flux<RedisEntry> getEntries(String pattern) {
    return redisTemplate.scan(ScanOptions.scanOptions().match(pattern).build())
        .map(key -> RedisEntry.builder().key(key))
        .flatMap(entry -> redisTemplate.opsForValue().get(key(entry))
            .map(entry::value)
            .defaultIfEmpty(entry))
        .flatMap(entry -> redisTemplate.getExpire(key(entry))
            .filter(duration -> !duration.isZero())
            .map(duration -> OffsetDateTime.now(ZoneOffset.UTC).plus(duration))
            .map(entry::expiration)
            .defaultIfEmpty(entry))
        .map(RedisEntryBuilder::build);
  }

  private String key(RedisEntryBuilder entryBuilder) {
    return entryBuilder.build().getKey();
  }

}

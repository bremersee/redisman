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

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Duration;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import reactor.test.StepVerifier;

/**
 * The redis service test.
 *
 * @author Christian Bremer
 */
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@TestInstance(Lifecycle.PER_CLASS)
class RedisServiceTest {

  @Autowired
  private ReactiveStringRedisTemplate redisTemplate;

  @Autowired
  private RedisService redisService;

  private ZonedDateTime until;

  /**
   * Sets up.
   */
  @BeforeAll
  void setUp() {
    StepVerifier.create(redisTemplate.opsForValue().set("aaa", "ValueAAA")
        .flatMap(result -> redisTemplate.expire("aaa", Duration.ofDays(1L))))
        .assertNext(Assertions::assertTrue)
        .verifyComplete();
    StepVerifier.create(redisTemplate.opsForValue().set("aab", "ValueAAB"))
        .assertNext(Assertions::assertTrue)
        .verifyComplete();
    StepVerifier.create(redisTemplate.opsForValue().set("bbb", "ValueBBB"))
        .assertNext(Assertions::assertTrue)
        .verifyComplete();
    until = ZonedDateTime.now().plus(Duration.ofSeconds(3L));
    StepVerifier.create(redisTemplate.opsForValue().set("ccc", "ValueCCC")
        .flatMap(result -> redisTemplate.expireAt("ccc", until.toInstant())))
        .assertNext(Assertions::assertTrue)
        .verifyComplete();
  }

  /**
   * Gets entries.
   */
  @Test
  void getEntries() {
    StepVerifier.create(redisService.getEntries("aa*"))
        .assertNext(redisEntry -> {
          assertNotNull(redisEntry);
          System.out.println(redisEntry);
        })
        .assertNext(redisEntry -> {
          assertNotNull(redisEntry);
          System.out.println(redisEntry);
        })
        .verifyComplete();
  }

  /**
   * Expired.
   */
  @Test
  void expired() {
    await().until(() -> ZonedDateTime.now().isAfter(until));
    StepVerifier.create(redisTemplate.opsForValue().get("ccc"))
        .verifyComplete();
  }

}
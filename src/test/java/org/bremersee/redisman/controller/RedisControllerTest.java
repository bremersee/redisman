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

package org.bremersee.redisman.controller;

import static org.bremersee.security.core.AuthorityConstants.ADMIN_ROLE_NAME;

import java.time.Duration;
import org.bremersee.redisman.model.RedisEntry;
import org.bremersee.test.security.authentication.WithJwtAuthenticationToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

/**
 * The redis controller test.
 *
 * @author Christian Bremer
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"in-memory"})
@TestInstance(Lifecycle.PER_CLASS)
class RedisControllerTest {

  @Autowired
  private ReactiveStringRedisTemplate redisTemplate;

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  private WebTestClient webTestClient;

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
  }

  /**
   * Gets entries.
   */
  @WithJwtAuthenticationToken(roles = {ADMIN_ROLE_NAME})
  @Test
  void getEntries() {
    webTestClient
        .get()
        .uri("/api/entries")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
        .expectBodyList(RedisEntry.class)
        .contains(RedisEntry.builder()
            .key("aab")
            .value("ValueAAB")
            .build());
  }

}
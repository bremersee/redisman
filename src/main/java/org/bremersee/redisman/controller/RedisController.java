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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.bremersee.redisman.model.RedisEntry;
import org.bremersee.redisman.service.RedisService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * The redis controller.
 *
 * @author Christian Bremer
 */
@RestController
public class RedisController {

  private final RedisService redisService;

  /**
   * Instantiates a new redis controller.
   *
   * @param redisService the redis service
   */
  public RedisController(RedisService redisService) {
    this.redisService = redisService;
  }

  /**
   * Gets entries.
   *
   * @param pattern the pattern
   * @return the entries
   */
  @Operation(
      summary = "Get entries.",
      operationId = "getEntries",
      tags = {"link-controller"})
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "The entries.",
          content = @Content(
              array = @ArraySchema(
                  schema = @Schema(implementation = RedisEntry.class)))),
      @ApiResponse(
          responseCode = "403",
          description = "Forbidden")
  })
  @GetMapping(path = "/api/entries", produces = MediaType.APPLICATION_JSON_VALUE)
  public Flux<RedisEntry> getEntries(
      @Parameter(name = "pattern", description = "The key pattern.")
      @RequestParam(name = "pattern", required = false) String pattern) {
    return redisService.getEntries(pattern);
  }
}

/*
 * Copyright 2022 Keisuke.K <ewigkeit1204@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.ewigkeit.spacenow.service;

import java.net.URI;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import reactor.core.publisher.Mono;
import twitter4j.JSONObject;
import twitter4j.SpacesResponse;
import twitter4j.UsersResponse;

/**
 * @author Keisuke.K <ewigkeit1204@gmail.com>
 */
@Service
public class TwitterService {

    @Autowired
    private WebClient webClient;

    public Mono<UsersResponse> lookupUsersByUsernames(String... usernames) {
        return getUsersResponse(uriBuilder -> uriBuilder.path("/2/users/by")
                .queryParam("usernames", String.join(",", usernames)).build());
    }

    public Mono<UsersResponse> lookupUsers(long... ids) {
        return getUsersResponse(uriBuilder -> uriBuilder.path("/2/users")
                .queryParam("ids", LongStream.of(ids).mapToObj(String::valueOf).collect(Collectors.joining(",")))
                .build());
    }

    public Mono<SpacesResponse> lookupSpacesByCreatorIds(long... userIds) {
        return getSpacesResponse(uriBuilder -> uriBuilder.path("/2/spaces/by/creator_ids")
                .queryParam("user_ids",
                        LongStream.of(userIds).mapToObj(String::valueOf).collect(Collectors.joining(",")))
                .queryParam("expansions", "creator_id").build());
    }

    public Mono<SpacesResponse> lookupSpaces(String... ids) {
        return getSpacesResponse(
                uriBuilder -> uriBuilder.path("/2/spaces").queryParam("ids", String.join(",", ids)).build());
    }

    Mono<UsersResponse> getUsersResponse(Function<UriBuilder, URI> uriFunction) {
        return webClient.get().uri(uriFunction).retrieve().bodyToMono(String.class).map(JSONObject::new)
                .map(json -> new UsersResponse(json, false));
    }

    Mono<SpacesResponse> getSpacesResponse(Function<UriBuilder, URI> uriFunction) {
        return webClient.get().uri(uriFunction).retrieve().bodyToMono(String.class).map(JSONObject::new)
                .map(json -> new SpacesResponse(json, false));
    }

}

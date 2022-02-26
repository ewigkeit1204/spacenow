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
package jp.ewigkeit.spacenow.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import jp.ewigkeit.spacenow.entity.SpaceInfo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Keisuke.K <ewigkeit1204@gmail.com>
 */
public interface SpaceInfoRepository extends ReactiveCrudRepository<SpaceInfo, String> {

    Flux<SpaceInfo> findByCreatorId(long creatorId);

    Mono<SpaceInfo> findBySpaceId(String spaceId);

}

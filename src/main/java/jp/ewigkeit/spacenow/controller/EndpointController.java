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
package jp.ewigkeit.spacenow.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.ewigkeit.spacenow.entity.Endpoint;
import jp.ewigkeit.spacenow.model.EndpointModel;
import jp.ewigkeit.spacenow.repository.EndpointRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Keisuke.K <ewigkeit1204@gmail.com>
 */
@RestController
@RequestMapping("/endpoint")
public class EndpointController {

    @Autowired
    private EndpointRepository repository;

    @GetMapping
    public Flux<Endpoint> getEndpoints() {
        return Flux.fromIterable(repository.findAll());
    }

    @PostMapping
    public Mono<Endpoint> createEndpoint(@RequestBody @Validated EndpointModel model) {
        Endpoint endpoint = new Endpoint();
        endpoint.setWebhookUrl(model.getWebhookUrl());
        endpoint.setUsernames(model.getCreatorUsernames());

        return Mono.just(repository.save(endpoint));
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteEndpoint(@PathVariable String id) {
        repository.deleteById(id);

        return Mono.empty();
    }

}

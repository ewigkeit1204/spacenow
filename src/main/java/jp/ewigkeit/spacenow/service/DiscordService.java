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

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author Keisuke.K <ewigkeit1204@gmail.com>
 */
@Service
public class DiscordService {

    @Autowired
    private WebClient webClient;

    public void fireWebhook(String endpoint, String content) {
        Map<String, String> payload = Map.of("content", content);

        webClient.post().uri(endpoint).contentType(MediaType.APPLICATION_JSON).bodyValue(payload).retrieve()
                .toBodilessEntity().block();
    }

}

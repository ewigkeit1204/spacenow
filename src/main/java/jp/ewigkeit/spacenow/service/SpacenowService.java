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

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jp.ewigkeit.spacenow.entity.Endpoint;
import jp.ewigkeit.spacenow.entity.SpaceInfo;
import jp.ewigkeit.spacenow.repository.EndpointRepository;
import jp.ewigkeit.spacenow.repository.SpaceInfoRepository;
import lombok.extern.slf4j.Slf4j;
import twitter4j.Space;
import twitter4j.SpacesResponse;
import twitter4j.TwitterException;
import twitter4j.User2;

/**
 * @author Keisuke.K <ewigkeit1204@gmail.com>
 */
@Service
@Slf4j
public class SpacenowService {

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private EndpointRepository endpointRepository;

    @Autowired
    private SpaceInfoRepository spaceInfoRepository;

    @Autowired
    private TwitterService twitterService;

    @Autowired
    private DiscordService discordService;

    @Scheduled(fixedDelay = 60, timeUnit = TimeUnit.SECONDS)
    public void doSomething() {
        Set<SpaceInfo> wastedInfos = new HashSet<>();

        endpointRepository.findAll().forEach(endpoint -> {
            getSpaces(endpoint).ifPresent(response -> response.getSpaces().forEach(space -> {
                spaceInfoRepository.findBySpaceId(space.getId()).ifPresentOrElse(spaceInfo -> {
                    if (space.getState() == Space.State.Ended) {
                        spaceInfoRepository.delete(spaceInfo);
                    }
                }, () -> {
                    if (space.getState() == Space.State.Live) {
                        SpaceInfo info = new SpaceInfo();
                        info.setSpaceId(space.getId());
                        info.setCreatorId(space.getCreatorId());
                        spaceInfoRepository.save(info);

                        User2 creator = response.getUsersMap().get(space.getCreatorId());
                        discordService.fireWebhook(endpoint.getWebhookUrl(),
                                messageSource.getMessage("spacenow.startSpace",
                                        new String[] { creator.getName(), creator.getUsername() },
                                        Locale.getDefault()));

                        StreamSupport
                                .stream(spaceInfoRepository.findByCreatorId(space.getCreatorId()).spliterator(), false)
                                .filter(i -> !space.getId().equals(i.getSpaceId())).forEach(wastedInfos::add);
                    }
                });
            }));
        });

        spaceInfoRepository.deleteAll(wastedInfos);
    }

    private Optional<SpacesResponse> getSpaces(Endpoint endpoint) {
        try {
            List<User2> userIds = twitterService.getUserIdFromUsernames(endpoint.getUsernames().toArray(new String[0]))
                    .getUsers();

            return Optional
                    .of(twitterService.getSpacesByCreatorIds(userIds.stream().mapToLong(User2::getId).toArray()));
        } catch (TwitterException e) {
            log.error("error occurred", e);
            return Optional.empty();
        }
    }

}

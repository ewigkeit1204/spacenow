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

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.MessageCreateSpec;
import jp.ewigkeit.spacenow.SpacenowUtils;
import jp.ewigkeit.spacenow.entity.SpaceInfo;
import jp.ewigkeit.spacenow.entity.SubscribeInfo;
import jp.ewigkeit.spacenow.repository.SpaceInfoRepository;
import jp.ewigkeit.spacenow.repository.SubscribeInfoRepository;
import lombok.extern.slf4j.Slf4j;
import twitter4j.Space;
import twitter4j.Space.State;
import twitter4j.SpacesResponse;
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
    private SubscribeInfoRepository subscribeInfoRepository;

    @Autowired
    private SpaceInfoRepository spaceInfoRepository;

    @Autowired
    private GatewayDiscordClient gatewayDiscordClient;

    @Autowired
    private TwitterService twitterService;

    @Scheduled(fixedDelay = 60, timeUnit = TimeUnit.SECONDS)
    public void monitorSpace() {
        long[] ids = subscribeInfoRepository.findAll().map(SubscribeInfo::getUserId).collectList().block().stream()
                .mapToLong(Long::longValue).toArray();

        if (ids.length == 0) {
            return;
        }

        SpacesResponse response = twitterService.lookupSpacesByCreatorIds(ids).block();

        response.getSpaces().stream().filter(space -> space.getState() == Space.State.Live).forEach(space -> {
            if (spaceInfoRepository.findBySpaceId(space.getId()).block() != null) {
                return;
            }

            spaceInfoRepository.save(new SpaceInfo(space.getId(), space.getCreatorId())).block();

            User2 creator = response.getUsersMap().get(space.getCreatorId());
            String content = SpacenowUtils.getMessage(messageSource, "spacenow.message.spaceStarted", creator.getName(),
                    creator.getUsername(), space.getId());

            subscribeInfoRepository.findByUserId(space.getCreatorId()).map(SubscribeInfo::getChannelId)
                    .flatMap(channelId -> gatewayDiscordClient.getChannelById(channelId).ofType(MessageChannel.class)
                            .flatMap(channel -> channel
                                    .createMessage(MessageCreateSpec.builder().content(content).build())))
                    .doOnError(e -> log.error(e.getMessage(), e)).subscribe();
        });
    }

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.HOURS)
    public void cleanupSpaceInfo() {
        List<String> spaceIds = spaceInfoRepository.findAll().map(SpaceInfo::getSpaceId).collectList().block();

        if (spaceIds.isEmpty()) {
            return;
        }

        SpacesResponse response = twitterService.lookupSpaces(spaceIds.toArray(String[]::new)).block();

        spaceIds.stream().forEach(spaceId -> {
            if (response.getSpaces().stream().filter(space -> space.getId().equals(spaceId))
                    .filter(space -> space.getState() != State.Ended).findAny().isPresent()) {
                // space is going, don't cleanup.
                return;
            }

            spaceInfoRepository.deleteById(spaceId).block();
        });
    }

}

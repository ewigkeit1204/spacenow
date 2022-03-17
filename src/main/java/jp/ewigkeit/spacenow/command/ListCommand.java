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
package jp.ewigkeit.spacenow.command;

import java.util.stream.Collectors;

import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.Guild;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import jp.ewigkeit.spacenow.SpacenowUtils;
import jp.ewigkeit.spacenow.entity.SubscribeInfo;
import jp.ewigkeit.spacenow.repository.SubscribeInfoRepository;
import jp.ewigkeit.spacenow.service.TwitterService;
import reactor.core.publisher.Mono;

/**
 * @author Keisuke.K <ewigkeit1204@gmail.com>
 */
@Component
public class ListCommand implements SubCommand {

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private SubscribeInfoRepository subscribeInfoRepository;

    @Autowired
    private TwitterService twitterService;

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public ApplicationCommandOptionData getCommandOptionData(Guild guild) {
        return ApplicationCommandOptionData.builder().name(getName())
                .description(SpacenowUtils.getMessage(guild, messageSource, "spacenow.command.list.description"))
                .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue()).build();
    }

    @Override
    public Publisher<Void> handle(ChatInputInteractionEvent event) {
        return event.getOption(getName()).map(option -> Mono.just(event.getInteraction().getChannelId())
                .flatMapMany(subscribeInfoRepository::findByChannelId).map(SubscribeInfo::getUserId)
                .flatMap(id -> twitterService.lookupUsers(id)).map(response -> response.getUsers().get(0))
                .map(user -> SpacenowUtils.getMessage(event, messageSource, "spacenow.message.listingFormat",
                        user.getName(), user.getUsername()))
                .switchIfEmpty(
                        Mono.just(SpacenowUtils.getMessage(event, messageSource, "spacenow.message.noSubscribedUsers")))
                .collect(
                        Collectors.joining("\n"))
                .flatMap(userlist -> event.reply()
                        .withContent(SpacenowUtils.getMessage(event, messageSource, "spacenow.message.listingUser")
                                + "\n" + userlist)))
                .orElse(Mono.empty());
    }

}

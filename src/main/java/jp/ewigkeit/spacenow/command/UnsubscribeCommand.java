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

import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.Guild;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import jp.ewigkeit.spacenow.SpacenowUtils;
import jp.ewigkeit.spacenow.UnknownUserException;
import jp.ewigkeit.spacenow.repository.SubscribeInfoRepository;
import jp.ewigkeit.spacenow.service.TwitterService;
import reactor.core.publisher.Mono;

/**
 * @author Keisuke.K <ewigkeit1204@gmail.com>
 */
@Component
public class UnsubscribeCommand implements SubCommand {

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private SubscribeInfoRepository subscribeInfoRepository;

    @Autowired
    private TwitterService twitterService;

    @Override
    public String getName() {
        return "unsubscribe";
    }

    @Override
    public ApplicationCommandOptionData getCommandOptionData(Guild guild) {
        return ApplicationCommandOptionData.builder().name(getName())
                .description(SpacenowUtils.getMessage(guild, messageSource, "spacenow.command.unsubscribe.description"))
                .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
                .addOption(ApplicationCommandOptionData.builder().name("user")
                        .description(SpacenowUtils.getMessage(guild, messageSource,
                                "spacenow.command.unsubscribe.user.description"))
                        .type(ApplicationCommandOption.Type.STRING.getValue()).required(true).build())
                .build();
    }

    @Override
    public Publisher<Void> handle(ChatInputInteractionEvent event) {
        return event.getOption(getName())
                .map(option -> Mono.justOrEmpty(option.getOption("user"))
                        .flatMap(subOption -> Mono.justOrEmpty(subOption.getValue()))
                        .flatMap(value -> SpacenowUtils.validateUsername(value.asString()))
                        .flatMap(username -> twitterService.lookupUsersByUsernames(username)
                                .filter(response -> !response.getUsers().isEmpty())
                                .switchIfEmpty(Mono.error(new UnknownUserException(username))))
                        .map(response -> response.getUsers().get(0))
                        .zipWith(Mono.just(event.getInteraction().getChannelId()))
                        .flatMap(tuple -> subscribeInfoRepository
                                .findByUserIdAndChannelId(tuple.getT1().getId(), tuple.getT2())
                                .flatMap(info -> subscribeInfoRepository.delete(info)
                                        .thenReturn(SpacenowUtils.getMessage(event, messageSource,
                                                "spacenow.message.userUnsubscribed", tuple.getT1().getName(),
                                                tuple.getT1().getUsername())))
                                .switchIfEmpty(Mono.just(SpacenowUtils.getMessage(event, messageSource,
                                        "spacenow.message.alreadyUnsubscribed", tuple.getT1().getName(),
                                        tuple.getT1().getUsername()))))
                        .flatMap(event.reply()::withContent))
                .orElse(Mono.empty());
    }

}

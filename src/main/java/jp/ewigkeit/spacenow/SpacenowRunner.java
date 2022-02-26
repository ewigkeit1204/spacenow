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
package jp.ewigkeit.spacenow;

import java.util.List;

import javax.annotation.PreDestroy;
import javax.validation.ValidationException;

import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.ReactiveEventAdapter;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import jp.ewigkeit.spacenow.command.SubCommand;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import twitter4j.TwitterException;

/**
 * @author Keisuke.K <ewigkeit1204@gmail.com>
 */
@Component
@Slf4j
public class SpacenowRunner extends ReactiveEventAdapter implements ApplicationRunner {

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private GatewayDiscordClient gatewayDiscordClient;

    @Autowired
    private List<SubCommand> subCommands;

    @Override
    public Publisher<?> onGuildCreate(GuildCreateEvent event) {
        log.debug("joined to {}", event.getGuild().getName());

        long guildId = event.getGuild().getId().asLong();
        List<ApplicationCommandOptionData> commandOptionData = subCommands.stream()
                .map(command -> command.getCommandOptionData(event.getGuild())).toList();
        List<ApplicationCommandRequest> requests = List.of(ApplicationCommandRequest.builder().name("spacenow")
                .description(SpacenowUtils.getMessage(event.getGuild(), messageSource, "spacenow.command.description"))
                .addAllOptions(commandOptionData).build());

        return event.getClient().getRestClient().getApplicationId()
                .flatMapMany(applicationId -> event.getClient().getRestClient().getApplicationService()
                        .bulkOverwriteGuildApplicationCommand(applicationId, guildId, requests));
    }

    @Override
    public Publisher<Void> onChatInputInteraction(ChatInputInteractionEvent event) {
        return Flux.fromIterable(subCommands).flatMap(command -> command.handle(event))
                .onErrorResume(TwitterException.class,
                        e -> event.reply().withContent(
                                SpacenowUtils.getMessage(event, messageSource, "spacenow.error.twitterException")))
                .onErrorResume(ValidationException.class,
                        e -> event.reply()
                                .withContent(SpacenowUtils.getMessage(event, messageSource,
                                        "spacenow.error.validationException",
                                        SpacenowUtils.getMessage(event, messageSource, e.getMessage()))))
                .onErrorResume(UnknownUserException.class, e -> event.reply().withContent(SpacenowUtils
                        .getMessage(event, messageSource, "spacenow.error.unknownUserException", e.getUsername())));
    }

    @Override
    public Publisher<?> onReady(ReadyEvent event) {
        return Mono.just(event).map(ReadyEvent::getSelf).doOnNext(user -> log.debug("login as {}", user.getUsername()));
    }

    @PreDestroy
    public void destroy() {
        log.debug("logout from discord");
        gatewayDiscordClient.logout().block();
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        this.gatewayDiscordClient.on(this).subscribe();
    }

}

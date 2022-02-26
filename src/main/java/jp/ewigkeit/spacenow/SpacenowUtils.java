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

import java.util.Locale;
import java.util.Optional;

import javax.validation.ValidationException;

import org.springframework.context.MessageSource;

import discord4j.core.event.domain.interaction.InteractionCreateEvent;
import discord4j.core.object.entity.Guild;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * @author Keisuke.K <ewigkeit1204@gmail.com>
 */
@NoArgsConstructor
public class SpacenowUtils {

    public static Mono<String> validateUsername(String username) {
        if (username.matches("[_0-9a-zA-Z]{1,15}")) {
            return Mono.just(username);
        } else {
            return Mono.error(new ValidationException("spacenow.validation.username.notvalid"));
        }
    }

    public static String getMessage(Guild guild, MessageSource messageSource, String code) {
        return getMessage(guild, messageSource, code, (Object[]) null);
    }

    public static String getMessage(Guild guild, MessageSource messageSource, String code, Object... args) {
        return getMessage(guild.getPreferredLocale(), messageSource, code, args);
    }

    public static String getMessage(InteractionCreateEvent event, MessageSource messageSource, String code) {
        return getMessage(event, messageSource, code, (Object[]) null);
    }

    public static String getMessage(InteractionCreateEvent event, MessageSource messageSource, String code,
            Object... args) {
        return getMessage(event.getInteraction().getGuildLocale().map(Locale::forLanguageTag), messageSource, code,
                args);
    }

    public static String getMessage(Optional<Locale> locale, MessageSource messageSource, String code, Object... args) {
        return getMessage(locale.orElse(Locale.getDefault()), messageSource, code, args);
    }

    public static String getMessage(Locale locale, MessageSource messageSource, String code, Object... args) {
        return messageSource.getMessage(code, args, locale);
    }

    public static String getMessage(MessageSource messageSource, String code) {
        return getMessage(Locale.getDefault(), messageSource, code, (Object[]) null);
    }

    public static String getMessage(MessageSource messageSource, String code, Object... args) {
        return getMessage(Locale.getDefault(), messageSource, code, args);
    }

}

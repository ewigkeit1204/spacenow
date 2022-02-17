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
package jp.ewigkeit.spacenow.model;

import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.URL;

import lombok.Data;

/**
 * @author Keisuke.K <ewigkeit1204@gmail.com>
 */
@Data
public class EndpointModel {

    @URL
    @NotEmpty
    private String webhookUrl;

    @NotEmpty
    private List<@Pattern(regexp = "[0-9a-zA-Z_]{1,15}", message = "{spacenow.username.notvalid}") String> creatorUsernames;

}

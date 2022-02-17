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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import twitter4j.GetUsersByKt;
import twitter4j.SpacesLookupExKt;
import twitter4j.SpacesResponse;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.UsersResponse;

/**
 * @author Keisuke.K <ewigkeit1204@gmail.com>
 */
@Service
public class TwitterService {

    @Autowired
    private Twitter twitter;

    public UsersResponse getUserIdFromUsernames(String... usernames) throws TwitterException {
        return GetUsersByKt.getUsersBy(twitter, usernames, null, null, "pinned_tweet_id");
    }

    public SpacesResponse getSpacesByCreatorIds(long... creatorIds) throws TwitterException {
        return SpacesLookupExKt.getSpacesByCreatorIds(twitter, creatorIds, "creator_id", null, "name");
    }

}

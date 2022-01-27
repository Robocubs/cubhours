/*
 * MIT License
 *
 * Copyright 2020-2021 noahhusby
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.robocubs.cubhours.users;

import com.google.api.client.util.Maps;
import com.robocubs.cubhours.database.DatabaseHandler;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserHandler {
    @Getter
    private static final UserHandler instance = new UserHandler();

    private final Map<User, TimeSlot> activeUsers = Maps.newHashMap();

    private UserHandler() {
    }

    /**
     * Set signed-in
     */
    public void signIn() {
        /*
        if(currentUser == null) {
            return;
        }
        if(TimeManager.getInstance().isUserSignedIn(currentUser.id)) {
            TimeManager.getInstance().endTracker(currentUser.id);
        } else {
            TimeManager.getInstance().startTracker(currentUser.id, currentUser.name);
        }
        EntryManager.getInstance().reset();

         */
    }

    public void createUser(String id, String displayName, String slackId, String roleId) {

    }

    /**
     * Gets whether any users are signed in
     *
     * @return True if anyone is signed in, false if not
     */
    public boolean isAnyoneSignedIn() {
        return !activeUsers.isEmpty();
    }

    /**
     * Gets a list of active users by name
     *
     * @return A list of active users
     */
    public List<String> getActiveUsersAsNames() {
        return activeUsers.keySet().stream()
                .map(User::getName)
                .collect(Collectors.toList());
    }

    /**
     * Fetches a user object from the local cache
     *
     * @param id of the user
     * @return {@link User} instance if exists, null if not
     */
    @SneakyThrows
    public User getUser(@NonNull String id) {
        return DatabaseHandler.getInstance().getFirebase().getDocumentAs("users", id, User.class);
    }
}

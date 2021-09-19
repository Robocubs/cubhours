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
import lombok.Getter;
import lombok.NonNull;

import java.util.Map;

public class UserHandler {
    @Getter
    private static final UserHandler instance = new UserHandler();

    private Map<Integer, User> users = Maps.newHashMap();

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

    /**
     * Checks and grabs user data from cloud/cache
     *
     * @param id User ID
     */
    public void submit(String id) {
        /*
        if(doesUserExist(id)) {
            try {
                if(userJSONWriter.fetch(id).name == null) {
                    System.out.println("Generating new user!");
                    JSONObject j = (JSONObject) new JSONParser().parse(db.get("users/"+id).body);
                    currentUser = new User((String) j.get("name"), id);
                    userJSONWriter.add(currentUser);
                } else {
                    System.out.println("Fetching user!");
                    currentUser = userJSONWriter.fetch(id);
                }

                entryManager.setUserUpdate(true, currentUser, TimeManager.getInstance().isUserSignedIn(id));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            entryManager.setUserUpdate(false, new User("", id), false);
        }

         */
    }

    /**
     * @param id ID of User
     * @return Returns true if user is registered in firebase, false if not
     */
    public boolean doesUserExist(String id) {
        /*
        //Check cache
        DataPacket u = db.get("users/"+id);
        System.out.println(u.body);
        return !db.get("users/"+id).body.equals("null");

         */
        return false;
    }

    /**
     * Fetches a user object from the local cache
     *
     * @param id of the user
     * @return {@link User} instance if exists, null if not
     */
    public User getUser(@NonNull String id) {
        return getUser(Integer.parseInt(id));
    }

    /**
     * Fetches a user object from the local cache
     *
     * @param id of the user
     * @return {@link User} instance if exists, null if not
     */
    public User getUser(Integer id) {
        return users.get(id);
    }
}

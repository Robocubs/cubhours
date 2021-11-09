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

package com.robocubs.cubhours.database;

import com.google.gson.Gson;
import com.robocubs.cubhours.CubConfig;
import com.robocubs.cubhours.CubHours;
import com.robocubs.cubhours.users.User;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;

/**
 * @author Noah Husby
 */
public class DatabaseHandler {
    @Getter
    private static final DatabaseHandler instance = new DatabaseHandler();

    @Getter
    private Firebase firebase = null;

    public void start() throws IOException {
        File credentials = new File(System.getProperty("user.dir"), "credentials.json");
        if (!credentials.exists()) {
            CubHours.getLogger().warning("Failed to initialize firebase! Please confirm that the credentials are configured correctly.");
            return;
        }
        firebase = new Firebase(credentials);
        initialize();
    }

    private void initialize() {
        CubHours.getLogger().info("Starting to initialize the database");
        if(!firebase.doesCollectionExist("config")) {
            CubHours.getLogger().info("Creating config settings in the cloud");
            pushConfigSettings();
        }
        if(!firebase.doesCollectionExist("users")) {
            //firebase.setDocument("users", "123456", new User(123456, "Sample Student", User.Type.STUDENT));
        }
        fetchConfigSettings();
        fetchUserCache();
        CubHours.getLogger().info("Finished initializing the database");
    }

    @SneakyThrows
    private void fetchUserCache() {

    }

    public void pushConfigSettings() {
        firebase.setDocument("config", "settings", CubConfig.cloudSettings);
    }

    public void fetchConfigSettings() {
        try {
            CubConfig.cloudSettings = firebase.getDocumentAs("config", "settings", CubConfig.CloudSettings.class);
        } catch (Exception e) {
            CubHours.getLogger().info("Failed to fetch the config settings from the cloud.");
            e.printStackTrace();
        }
    }

    public void close() {
        firebase.close();
    }
}

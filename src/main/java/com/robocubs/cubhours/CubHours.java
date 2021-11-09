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

package com.robocubs.cubhours;

import com.google.gson.Gson;
import com.noahhusby.lib.application.config.Configuration;
import com.robocubs.cubhours.database.DatabaseHandler;
import com.robocubs.cubhours.slack.SlackHandler;
import com.robocubs.cubhours.users.User;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;

import java.util.Map;

/**
 * @author Noah Husby
 */
public class CubHours extends Application {

    @Getter
    private static final CubLogger logger = new CubLogger();

    @Override
    public void start(Stage primaryStage) throws Exception {
        logger.info(String.format("Loading CubHours %s", Constants.VERSION));
        Configuration configuration = Configuration.of(CubConfig.class);
        configuration.sync(CubConfig.class);
        SlackHandler.getInstance().start();

        //Parent root = FXMLLoader.load(getClass().getResource("/fxml/main.fxml"));
        //primaryStage.setTitle("CubHours " + Constants.VERSION);
        //primaryStage.setScene(new Scene(root, primaryStage.getMaxWidth(), primaryStage.getMaxHeight()));
        //primaryStage.show();

        //DatabaseHandler.getInstance().start();
        //User user = DatabaseHandler.getInstance().getFirebase().getDocumentAs("users", "211694", User.class);
        //System.out.println(new Gson().toJson(user));
    }

    /**
     * A hack to make launching JavaFX applications compatible with shadow jars
     */
    public static void main(String[] args) {
        CubHours.launch(args);
    }
}

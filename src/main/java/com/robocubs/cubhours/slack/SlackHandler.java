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

package com.robocubs.cubhours.slack;

import com.google.common.collect.Lists;
import com.robocubs.cubhours.CubConfig;
import com.robocubs.cubhours.CubHours;
import com.robocubs.cubhours.slack.commands.ConfigCommand;
import com.robocubs.cubhours.slack.commands.DoorbellCommand;
import com.robocubs.cubhours.slack.commands.HelpCommand;
import com.robocubs.cubhours.slack.commands.HereCommand;
import com.robocubs.cubhours.slack.commands.InfoCommand;
import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import com.slack.api.bolt.socket_mode.SocketModeApp;
import com.slack.api.model.User;
import com.slack.api.socket_mode.SocketModeClient;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.List;

/**
 * @author Noah Husby
 */
public class SlackHandler {
    @Getter
    private static final SlackHandler instance = new SlackHandler();

    private SlackHandler() {
    }

    private App app;

    @SneakyThrows
    public void start() {
        AppConfig config = new AppConfig();
        config.setSingleTeamBotToken(CubConfig.slack_bot_token);
        app = new App(config);

        registerCommands(Lists.newArrayList(
                new InfoCommand(),
                new HelpCommand(),
                new ConfigCommand(),
                new DoorbellCommand(),
                new HereCommand()
        ));

        new SocketModeApp(CubConfig.slack_app_token, SocketModeClient.Backend.Tyrus, app).startAsync();
    }

    public void registerCommand(SlackCommand command) {
        app.command("/" + command.getName(), (req, ctx) -> {
            CubHours.getLogger().info(req.getPayload().getUserName() + " triggered a slack command: /" + command.getName());
            return command.onCommand(app, req, ctx);
        });
        if (command instanceof IBlockActionHandler) {
            for (String id : ((IBlockActionHandler) command).getBlockActionIds()) {
                app.blockAction(command.getName() + "-" + id, (req, ctx) -> ((IBlockActionHandler) command).onBlockAction(app, req, ctx, id));
            }
        }
        if (command instanceof IModalHandler) {
            IModalHandler modalHandler = (IModalHandler) command;
            for (String callback : modalHandler.getModalCallbacks()) {
                app.viewSubmission(command.getName() + "-" + callback, (req, ctx) -> modalHandler.onViewSubmission(app, req, ctx, callback));
                app.viewClosed(command.getName() + "-" + callback, (req, ctx) -> modalHandler.onViewClosed(app, req, ctx, callback));
            }
        }
    }

    public void registerCommands(List<SlackCommand> commandList) {
        for (SlackCommand command : commandList) {
            registerCommand(command);
        }
    }

    public User getUser(String id) {
        try {
            return app.client().usersInfo(u -> u.user(id).token(CubConfig.slack_bot_token)).getUser();
        } catch (Exception ignored) {
            return null;
        }
    }
}

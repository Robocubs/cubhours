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
import com.robocubs.cubhours.slack.modals.ConfigModal;
import com.robocubs.cubhours.users.UserPermission;
import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import com.slack.api.bolt.context.builtin.ActionContext;
import com.slack.api.bolt.context.builtin.SlashCommandContext;
import com.slack.api.bolt.context.builtin.ViewSubmissionContext;
import com.slack.api.bolt.request.builtin.BlockActionRequest;
import com.slack.api.bolt.request.builtin.SlashCommandRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.bolt.socket_mode.SocketModeApp;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.User;
import com.slack.api.model.view.View;
import com.slack.api.socket_mode.SocketModeClient;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.IOException;
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

    public Response openModal(Modal modal, ViewSubmissionContext context) {
        openModal(modal);
        return context.ackWithUpdate(modal.view());
    }

    public void openModal(Modal modal, SlashCommandContext context) {
        openModal(modal);
        try {
            context.client().viewsOpen(r -> r.triggerId(context.getTriggerId()).view(modal.view()));
        } catch (IOException | SlackApiException e) {
            e.printStackTrace();
        }
    }

    public void openModal(Modal modal, BlockActionRequest request, ActionContext context) {
        openModal(modal);
        try {
            View currentView = request.getPayload().getView();
            context.client().viewsUpdate(r -> r.viewId(currentView.getId()).hash(currentView.getHash()).view(modal.view()));
        } catch (IOException | SlackApiException e) {
            e.printStackTrace();
        }
    }

    private void openModal(Modal modal) {
        for(String actionId : modal.getActionIds()) {
            app.blockAction(modal.getName() + "-" + actionId, (req, ctx) -> modal.onBlockAction(app, req, ctx, actionId));
            app.viewSubmission(modal.getName() + "-" + actionId, (req, ctx) -> modal.onViewSubmission(app, req, ctx, actionId));
            app.viewClosed(modal.getName() + "-" + actionId, (req, ctx) -> modal.onViewClosed(app, req, ctx, actionId));
        }
        if(modal.getActionIds().length == 0) {
            app.blockAction(modal.getName(), (req, ctx) -> modal.onBlockAction(app, req, ctx, modal.getName()));
            app.viewSubmission(modal.getName(), (req, ctx) -> modal.onViewSubmission(app, req, ctx, modal.getName()));
            app.viewClosed(modal.getName(), (req, ctx) -> modal.onViewClosed(app, req, ctx, modal.getName()));
        }
    }

    public void registerCommand(SlackCommand command) {
        app.command("/" + command.getName(), (req, ctx) -> {
            CubHours.getLogger().info(req.getPayload().getUserName() + " triggered a slack command: /" + command.getName());
            return command.onCommand(app, req, ctx);
        });
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

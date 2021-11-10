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

package com.robocubs.cubhours.slack.commands;

import com.google.common.collect.Lists;
import com.robocubs.cubhours.CubConfig;
import com.robocubs.cubhours.database.DatabaseHandler;
import com.robocubs.cubhours.slack.IBlockActionHandler;
import com.robocubs.cubhours.slack.IModalHandler;
import com.robocubs.cubhours.slack.SlackCommand;
import com.robocubs.cubhours.slack.SlackHandler;
import com.robocubs.cubhours.slack.modals.ConfigModal;
import com.robocubs.cubhours.slack.modals.RolesModal;
import com.robocubs.cubhours.slack.modals.SettingsModal;
import com.robocubs.cubhours.users.Role;
import com.robocubs.cubhours.users.UserHandler;
import com.robocubs.cubhours.users.UserPermission;
import com.slack.api.app_backend.views.payload.ViewSubmissionPayload;
import com.slack.api.bolt.App;
import com.slack.api.bolt.context.builtin.ActionContext;
import com.slack.api.bolt.context.builtin.DefaultContext;
import com.slack.api.bolt.context.builtin.SlashCommandContext;
import com.slack.api.bolt.context.builtin.ViewSubmissionContext;
import com.slack.api.bolt.request.builtin.BlockActionRequest;
import com.slack.api.bolt.request.builtin.SlashCommandRequest;
import com.slack.api.bolt.request.builtin.ViewClosedRequest;
import com.slack.api.bolt.request.builtin.ViewSubmissionRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.model.view.View;
import com.slack.api.model.view.ViewState;
import lombok.SneakyThrows;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Noah Husby
 */
public class ConfigCommand extends SlackCommand {
    @Override
    @SneakyThrows
    public Response onCommand(App app, SlashCommandRequest request, SlashCommandContext context) {
        SlackHandler.getInstance().openModal(new ConfigModal(Lists.newArrayList(UserPermission.ADMIN)), context);
        return context.ack();
    }

    @Override
    public String getName() {
        return "config";
    }
}

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

import com.google.api.client.util.Maps;
import com.google.gson.Gson;
import com.robocubs.cubhours.CubConfig;
import com.robocubs.cubhours.database.DatabaseHandler;
import com.robocubs.cubhours.slack.IBlockActionHandler;
import com.robocubs.cubhours.slack.IModalHandler;
import com.robocubs.cubhours.slack.SlackCommand;
import com.robocubs.cubhours.util.CubUtil;
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
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.view.View;
import lombok.SneakyThrows;

import java.io.IOException;
import java.util.Map;

/**
 * @author Noah Husby
 */
public class ConfigCommand extends SlackCommand implements IBlockActionHandler, IModalHandler {
    @Override
    @SneakyThrows
    public Response onCommand(App app, SlashCommandRequest request, SlashCommandContext context) {
        context.client().viewsOpen(r -> r.triggerId(context.getTriggerId())
                        .viewAsString(new Gson().toJson(CubUtil.getBlock("config"))));
        return context.ack();
    }

    @Override
    public String getName() {
        return "config";
    }

    @Override
    public Response onBlockAction(App app, BlockActionRequest request, ActionContext context, String id) {
        if(id.equals("settings")) {
            showSettingsMenu(request, context);
        } else if(id.equals("settings-doorbell")) {
            CubConfig.cloudSettings.doorbell = !CubConfig.cloudSettings.doorbell;
            DatabaseHandler.getInstance().pushConfigSettings();
            showSettingsMenu(request, context);
        } else if(id.equals("settings-return")) {
            //showConfigMenu(request, context);
        }
        return context.ack();
    }

    @Override
    public String[] getBlockActionIds() {
        return new String[]{ "settings", "users", "roles", "done", "settings-doorbell", "settings-return" };
    }

    private void showSettingsMenu(BlockActionRequest request, ActionContext context) {
        try {
            View currentView = request.getPayload().getView();
            Map<String, String> placeholders = Maps.newHashMap();
            placeholders.put("doorbell_button_text", CubConfig.cloudSettings.doorbell ? ":white_check_mark: Enabled" : ":x: Disabled");
            context.client().viewsUpdate(r ->
                    r.viewId(currentView.getId())
                            .hash(currentView.getHash())
                            .viewAsString(new Gson().toJson(CubUtil.getBlock("config-settings", placeholders))));
        } catch (IOException | SlackApiException e) {
            e.printStackTrace();
        }
    }

    private void showConfigMenu(View view, MethodsClient client) {
        try {
            client.viewsUpdate(r -> r.viewId(view.getId())
                            .hash(view.getHash())
                            .viewAsString(new Gson().toJson(CubUtil.getBlock("config"))));
        } catch (IOException | SlackApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Response onViewSubmission(App app, ViewSubmissionRequest request, ViewSubmissionContext context, String callback) {
        if(callback.equals("settings")) {
            return context.ackWithUpdate(new Gson().toJson(CubUtil.getBlock("config")));
        }
        return context.ack();
    }

    @Override
    public Response onViewClosed(App app, ViewClosedRequest request, DefaultContext context, String callback) {
        return context.ack();
    }

    @Override
    public String[] getModalCallbacks() {
        return new String[]{ "settings", "general" };
    }
}

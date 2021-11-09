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
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.view.View;
import com.slack.api.model.view.ViewState;
import lombok.SneakyThrows;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Noah Husby
 */
public class ConfigCommand extends SlackCommand implements IBlockActionHandler, IModalHandler {
    @Override
    @SneakyThrows
    public Response onCommand(App app, SlashCommandRequest request, SlashCommandContext context) {
        context.client().viewsOpen(r -> r.triggerId(context.getTriggerId()).view(new ConfigModal(Lists.newArrayList(UserPermission.ADMIN)).view()));
        return context.ack();
    }

    @Override
    public String getName() {
        return "config";
    }

    @Override
    public Response onBlockAction(App app, BlockActionRequest request, ActionContext context, String id) {
        if (id.equals("settings")) {
            updateView(request, context, new SettingsModal().view());
        } else if (id.equals("settings-doorbell")) {
            CubConfig.cloudSettings.doorbell = !CubConfig.cloudSettings.doorbell;
            DatabaseHandler.getInstance().pushConfigSettings();
            updateView(request, context, new SettingsModal().view());
        } else if (id.equals("roles")) {
            String roleName = request.getPayload().getActions().get(0).getSelectedOption().getValue();
            updateView(request, context, new RolesModal(roleName.equals("new") ? null : UserHandler.getInstance().getRoles().get(roleName)).view());
        }
        return context.ack();
    }

    @Override
    public String[] getBlockActionIds() {
        return new String[]{ "settings", "users", "roles", "done", "settings-doorbell" };
    }

    @Override
    public Response onViewSubmission(App app, ViewSubmissionRequest request, ViewSubmissionContext context, String callback) {
        ViewSubmissionPayload payload = request.getPayload();
        if (callback.equals("settings")) {
            return context.ackWithUpdate(new ConfigModal(Lists.newArrayList(UserPermission.ADMIN)).view());
        } else if (callback.equals("roles")) {
            String formerName = payload.getView().getPrivateMetadata();
            Map<String, Map<String, ViewState.Value>> values = payload.getView().getState().getValues();
            String name = values.get("config-roles-edit-name-parent").get("config-roles-edit-name").getValue();
            ViewState.Value permissionState = values.get("config-roles-edit-checkbox-parent").get("config-roles-edit-checkbox");
            List<UserPermission> permissions = Lists.newArrayList();
            for (ViewState.SelectedOption option : permissionState.getSelectedOptions()) {
                permissions.add(UserPermission.valueOf(option.getValue()));
            }
            UserHandler.getInstance().getRoles().remove(formerName.toLowerCase(Locale.ROOT));
            UserHandler.getInstance().getRoles().put(name.toLowerCase(Locale.ROOT), new Role(name, permissions));
            return context.ackWithUpdate(new ConfigModal(Lists.newArrayList(UserPermission.ADMIN)).view());
        }
        return context.ack();
    }

    @Override
    public Response onViewClosed(App app, ViewClosedRequest request, DefaultContext context, String callback) {
        return context.ack();
    }

    @Override
    public String[] getModalCallbacks() {
        return new String[]{ "settings", "general", "roles" };
    }

    private void updateView(BlockActionRequest request, ActionContext context, View view) {
        View currentView = request.getPayload().getView();
        try {
            context.client().viewsUpdate(r -> r.viewId(currentView.getId()).hash(currentView.getHash()).view(view));
        } catch (IOException | SlackApiException e) {
            e.printStackTrace();
        }
    }
}

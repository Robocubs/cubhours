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
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.robocubs.cubhours.CubConfig;
import com.robocubs.cubhours.database.DatabaseHandler;
import com.robocubs.cubhours.slack.IBlockActionHandler;
import com.robocubs.cubhours.slack.IModalHandler;
import com.robocubs.cubhours.slack.SlackCommand;
import com.robocubs.cubhours.users.Role;
import com.robocubs.cubhours.users.User;
import com.robocubs.cubhours.users.UserHandler;
import com.robocubs.cubhours.users.UserPermission;
import com.robocubs.cubhours.util.CubUtil;
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
import com.slack.api.methods.response.views.ViewsUpdateResponse;
import com.slack.api.model.block.DividerBlock;
import com.slack.api.model.block.InputBlock;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.composition.MarkdownTextObject;
import com.slack.api.model.block.composition.OptionObject;
import com.slack.api.model.block.composition.PlainTextObject;
import com.slack.api.model.block.element.ButtonElement;
import com.slack.api.model.block.element.CheckboxesElement;
import com.slack.api.model.block.element.PlainTextInputElement;
import com.slack.api.model.block.element.StaticSelectElement;
import com.slack.api.model.block.element.UsersSelectElement;
import com.slack.api.model.view.View;
import com.slack.api.model.view.ViewClose;
import com.slack.api.model.view.ViewState;
import com.slack.api.model.view.ViewSubmit;
import com.slack.api.model.view.ViewTitle;
import lombok.SneakyThrows;


import java.io.IOException;
import java.util.ArrayList;
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
        List<UserPermission> temp = Lists.newArrayList();
        temp.add(UserPermission.ADMIN);
        context.client().viewsOpen(r -> r.triggerId(context.getTriggerId()).view(buildConfigView(temp)));
        return context.ack();
    }

    @Override
    public String getName() {
        return "config";
    }

    @Override
    public Response onBlockAction(App app, BlockActionRequest request, ActionContext context, String id) {
        View currentView = request.getPayload().getView();
        if(id.equals("settings")) {
            showSettingsMenu(request, context);
        } else if(id.equals("settings-doorbell")) {
            CubConfig.cloudSettings.doorbell = !CubConfig.cloudSettings.doorbell;
            DatabaseHandler.getInstance().pushConfigSettings();
            showSettingsMenu(request, context);
        } else if(id.equals("settings-return")) {
            //showConfigMenu(request, context);
        } else if(id.equals("roles")) {
            String roleName = request.getPayload().getActions().get(0).getSelectedOption().getValue();
            try {
                ViewsUpdateResponse requ = context.client().viewsUpdate(r ->
                        r.viewId(currentView.getId())
                                .hash(currentView.getHash())
                                .view(buildRoleEditView(roleName == "new" ? null : UserHandler.getInstance().getRoles().get(roleName))));
                for(String x : requ.getResponseMetadata().getMessages()) {
                    System.out.println(x);
                }
            } catch (IOException | SlackApiException e) {
                e.printStackTrace();
            }
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

    private View buildConfigView(List<UserPermission> permissions) {
        View view = new View();
        view.setType("modal");
        view.setCallbackId("config-general");
        view.setTitle(new ViewTitle("plain_text", "CubHours", true));
        view.setSubmit(new ViewSubmit("plain_text", "Done", true));
        view.setClose(new ViewClose("plain_text", "Cancel", true));
        List<LayoutBlock> blocks = new ArrayList<>();
        blocks.add(new SectionBlock(new MarkdownTextObject("*Hi!* Here's how I can help you:", false), null, null, null));
        blocks.add(new DividerBlock());
        if(permissions.contains(UserPermission.ADMIN) || permissions.contains(UserPermission.SETTINGS)) {
            ButtonElement element = new ButtonElement(new PlainTextObject("Change Settings", false), "config-settings", null, null, null, null);
            blocks.add(new SectionBlock(new MarkdownTextObject(":gear: *Settings*\nManage your team settings", false), "config-category-settings", null, element));

        }
        if(permissions.contains(UserPermission.ADMIN) || permissions.contains(UserPermission.USERS)) {
            UsersSelectElement element = new UsersSelectElement(new PlainTextObject("Choose user", true), "config-users", null, null);
            blocks.add(new SectionBlock(new MarkdownTextObject(":bust_in_silhouette: *Users*\nManage your team users", false), null, null, element));
        }
        if(permissions.contains(UserPermission.ADMIN) || permissions.contains(UserPermission.ROLES)) {
            List<OptionObject> roles = Lists.newArrayList();
            StaticSelectElement element = new StaticSelectElement(new PlainTextObject("Choose role", true), "config-roles", roles, null, null, null);
            roles.add(new OptionObject(new PlainTextObject(":pencil2: Add a new role", true), "new", null, null));
            for(Map.Entry<String, Role> entry : UserHandler.getInstance().getRoles().entrySet()) {
                roles.add(new OptionObject(new PlainTextObject(entry.getValue().getName(), true), entry.getKey(), null, null));
            }
            blocks.add(new SectionBlock(new MarkdownTextObject(":busts_in_silhouette: *Roles*\nManage your team roles", false), null, null, element));
        }

        view.setBlocks(blocks);
        return view;
    }

    private View buildRoleEditView(Role role) {
        boolean newRole = role == null;
        View view = new View();
        view.setType("modal");
        view.setCallbackId("config-roles");
        view.setTitle(new ViewTitle("plain_text", newRole ? "Create a role" : "Edit \"" + role.getName() + "\"", true));
        view.setSubmit(new ViewSubmit("plain_text", "Save", true));
        view.setClose(new ViewClose("plain_text", "Cancel", true));
        view.setPrivateMetadata(newRole ? null : role.getName());
        List<LayoutBlock> blocks = new ArrayList<>();
        {
            PlainTextInputElement element = new PlainTextInputElement("config-roles-edit-name", new PlainTextObject("What should the role be named?", false),newRole ? null : role.getName(),false,null,null,null);
            blocks.add(new InputBlock("config-roles-edit-name-parent", new PlainTextObject("What should the role be named?", false), element, null, null, false));
        }
        {
            List<OptionObject> options = Lists.newArrayList();
            List<OptionObject> initialOptions = Lists.newArrayList();
            options.add(CubUtil.composeOptionObject("*Admin*", UserPermission.ADMIN.name(), "Give the user access to all CubHours options."));
            if(!newRole && role.getPermissions().contains(UserPermission.ADMIN)) {
                initialOptions.add(options.get(0));
            }
            options.add(CubUtil.composeOptionObject("*Settings*", UserPermission.SETTINGS.name(), "Give the user access to change settings."));
            if(!newRole && role.getPermissions().contains(UserPermission.SETTINGS)) {
                initialOptions.add(options.get(1));
            }
            options.add(CubUtil.composeOptionObject("*Users*", UserPermission.USERS.name(), "Give the user access to edit users."));
            if(!newRole && role.getPermissions().contains(UserPermission.USERS)) {
                initialOptions.add(options.get(2));
            }
            options.add(CubUtil.composeOptionObject("*Roles*", UserPermission.ROLES.name(), "Give the user access to edit roles."));
            if(!newRole && role.getPermissions().contains(UserPermission.ROLES)) {
                initialOptions.add(options.get(3));
            }
            options.add(CubUtil.composeOptionObject("*Session*", UserPermission.SESSION.name(), "Give the user access to control the current session."));
            if(!newRole && role.getPermissions().contains(UserPermission.SESSION)) {
                initialOptions.add(options.get(4));
            }
            CheckboxesElement element = new CheckboxesElement("config-roles-edit-checkbox", options, initialOptions.isEmpty() ? null : initialOptions, null);
            blocks.add(new InputBlock("config-roles-edit-checkbox-parent", new PlainTextObject("Permissions",false), element, null, null, false));
        }
        view.setBlocks(blocks);
        return view;
    }

    @Override
    public Response onViewSubmission(App app, ViewSubmissionRequest request, ViewSubmissionContext context, String callback) {
        ViewSubmissionPayload payload = request.getPayload();
        if(callback.equals("settings")) {
            return context.ackWithUpdate(buildConfigView(Lists.newArrayList(UserPermission.ADMIN)));
        } else if (callback.equals("roles")) {
            String formerName = payload.getView().getPrivateMetadata();
            Map<String, Map<String, ViewState.Value>> values = payload.getView().getState().getValues();
            String name = values.get("config-roles-edit-name-parent").get("config-roles-edit-name").getValue();
            ViewState.Value permissionState = values.get("config-roles-edit-checkbox-parent").get("config-roles-edit-checkbox");
            List<UserPermission> permissions = Lists.newArrayList();
            for(ViewState.SelectedOption option : permissionState.getSelectedOptions()) {
                permissions.add(UserPermission.valueOf(option.getValue()));
            }
            UserHandler.getInstance().getRoles().remove(formerName.toLowerCase(Locale.ROOT));
            UserHandler.getInstance().getRoles().put(name.toLowerCase(Locale.ROOT), new Role(name, permissions));
            return context.ackWithUpdate(buildConfigView(Lists.newArrayList(UserPermission.ADMIN)));
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
}

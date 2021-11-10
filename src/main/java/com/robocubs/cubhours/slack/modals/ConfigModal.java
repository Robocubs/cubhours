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

package com.robocubs.cubhours.slack.modals;

import com.google.common.collect.Lists;
import com.robocubs.cubhours.CubConfig;
import com.robocubs.cubhours.database.DatabaseHandler;
import com.robocubs.cubhours.slack.Modal;
import com.robocubs.cubhours.slack.SlackHandler;
import com.robocubs.cubhours.users.Role;
import com.robocubs.cubhours.users.UserHandler;
import com.robocubs.cubhours.users.UserPermission;
import com.robocubs.cubhours.util.CubUtil;
import com.slack.api.bolt.App;
import com.slack.api.bolt.context.builtin.ActionContext;
import com.slack.api.bolt.context.builtin.DefaultContext;
import com.slack.api.bolt.context.builtin.ViewSubmissionContext;
import com.slack.api.bolt.request.builtin.BlockActionRequest;
import com.slack.api.bolt.request.builtin.ViewClosedRequest;
import com.slack.api.bolt.request.builtin.ViewSubmissionRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.model.block.DividerBlock;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.composition.OptionObject;
import com.slack.api.model.block.composition.PlainTextObject;
import com.slack.api.model.block.element.ButtonElement;
import com.slack.api.model.block.element.StaticSelectElement;
import com.slack.api.model.block.element.UsersSelectElement;

import java.util.List;
import java.util.Map;

/**
 * @author Noah Husby
 */
public class ConfigModal extends Modal {

    private final List<UserPermission> permissions;

    public ConfigModal(List<UserPermission> permissions) {
        super("CubHours", "config-callback");
        this.permissions = permissions;
    }

    @Override
    public String getName() {
        return "config";
    }

    @Override
    public String[] getActionIds() {
        return new String[]{ "settings", "users", "roles", "callback" };
    }

    @Override
    public Response onViewSubmission(App app, ViewSubmissionRequest request, ViewSubmissionContext context, String callback) {
        return context.ack();
    }

    @Override
    public Response onViewClosed(App app, ViewClosedRequest request, DefaultContext context, String callback) {
        return context.ack();
    }

    @Override
    public Response onBlockAction(App app, BlockActionRequest request, ActionContext context, String id) {
        if (id.equals("settings")) {
            SlackHandler.getInstance().openModal(new SettingsModal(), request, context);
        } else if (id.equals("roles")) {
            String roleName = request.getPayload().getActions().get(0).getSelectedOption().getValue();
            SlackHandler.getInstance().openModal(new RolesModal(roleName.equals("new") ? null : UserHandler.getInstance().getRoles().get(roleName)), request, context);
        }
        return context.ack();
    }

    @Override
    protected void setup(List<LayoutBlock> blocks) {
        blocks.add(CubUtil.composeSectionBlock("*Hi!* Here's how I can help you:"));
        blocks.add(new DividerBlock());
        if (permissions.contains(UserPermission.ADMIN) || permissions.contains(UserPermission.SETTINGS)) {
            ButtonElement element = new ButtonElement(new PlainTextObject("Change Settings", false), "config-settings", null, null, null, null);
            blocks.add(CubUtil.composeSectionBlock(":gear: *Settings*\nManage your team settings", "config-category-settings", element));
        }
        if (permissions.contains(UserPermission.ADMIN) || permissions.contains(UserPermission.USERS)) {
            UsersSelectElement element = new UsersSelectElement(new PlainTextObject("Choose user", true), "config-users", null, null);
            blocks.add(CubUtil.composeSectionBlock(":bust_in_silhouette: *Users*\nManage your team users", null, element));
        }
        if (permissions.contains(UserPermission.ADMIN) || permissions.contains(UserPermission.ROLES)) {
            List<OptionObject> roles = Lists.newArrayList();
            StaticSelectElement element = new StaticSelectElement(new PlainTextObject("Choose role", true), "config-roles", roles, null, null, null);
            roles.add(new OptionObject(new PlainTextObject(":pencil2: Add a new role", true), "new", null, null));
            for (Map.Entry<String, Role> entry : UserHandler.getInstance().getRoles().entrySet()) {
                roles.add(new OptionObject(new PlainTextObject(entry.getValue().getName(), true), entry.getKey(), null, null));
            }
            blocks.add(CubUtil.composeSectionBlock(":busts_in_silhouette: *Roles*\nManage your team roles", null, element));
        }
    }
}

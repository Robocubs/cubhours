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
import com.robocubs.cubhours.database.DatabaseHandler;
import com.robocubs.cubhours.slack.Modal;
import com.robocubs.cubhours.slack.SlackHandler;
import com.robocubs.cubhours.users.Role;
import com.robocubs.cubhours.users.UserHandler;
import com.robocubs.cubhours.users.UserPermission;
import com.robocubs.cubhours.util.CubUtil;
import com.slack.api.app_backend.views.payload.ViewSubmissionPayload;
import com.slack.api.bolt.App;
import com.slack.api.bolt.context.builtin.ActionContext;
import com.slack.api.bolt.context.builtin.DefaultContext;
import com.slack.api.bolt.context.builtin.ViewSubmissionContext;
import com.slack.api.bolt.request.builtin.BlockActionRequest;
import com.slack.api.bolt.request.builtin.ViewClosedRequest;
import com.slack.api.bolt.request.builtin.ViewSubmissionRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.model.block.InputBlock;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.composition.OptionObject;
import com.slack.api.model.block.composition.PlainTextObject;
import com.slack.api.model.block.element.CheckboxesElement;
import com.slack.api.model.block.element.PlainTextInputElement;
import com.slack.api.model.view.ViewState;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Noah Husby
 */
public class RolesModal extends Modal {

    private final Role role;
    private final List<OptionObject> options = Lists.newArrayList();
    private final List<OptionObject> initialOptions = Lists.newArrayList();

    public RolesModal(Role role) {
        super(role == null ? "Create a role" : "Edit \"" + role.getName() + "\"");
        this.submit = "Save";
        this.privateMetadata = role == null ? null : role.getName();
        this.role = role;
    }

    @Override
    public String getName() {
        return "roles";
    }

    @Override
    public String[] getActionIds() {
        return new String[]{ "callback" };
    }

    @Override
    public Response onViewSubmission(App app, ViewSubmissionRequest request, ViewSubmissionContext context, String callback) {
        ViewSubmissionPayload payload = request.getPayload();
        String formerName = payload.getView().getPrivateMetadata();
        Map<String, Map<String, ViewState.Value>> values = payload.getView().getState().getValues();
        String name = values.get("roles-edit-name-parent").get("roles-edit-name").getValue();
        name = name.strip().replace(" ", "");
        ViewState.Value permissionState = values.get("roles-edit-checkbox-parent").get("roles-edit-checkbox");
        List<UserPermission> permissions = Lists.newArrayList();
        for (ViewState.SelectedOption option : permissionState.getSelectedOptions()) {
            permissions.add(UserPermission.valueOf(option.getValue()));
        }
        if (formerName != null) {
            UserHandler.getInstance().getRoles().remove(formerName.toLowerCase(Locale.ROOT));
        }
        UserHandler.getInstance().getRoles().put(name.toLowerCase(Locale.ROOT), new Role(name, permissions));
        DatabaseHandler.getInstance().pushRoles();
        close();
        return SlackHandler.getInstance().openModal(new ConfigModal(Lists.newArrayList(UserPermission.ADMIN)), context);
    }

    @Override
    public Response onViewClosed(App app, ViewClosedRequest request, DefaultContext context, String callback) {
        return context.ack();
    }

    @Override
    public Response onBlockAction(App app, BlockActionRequest request, ActionContext context, String id) {
        return context.ack();
    }

    @Override
    protected void setup(List<LayoutBlock> blocks) {
        boolean newRole = role == null;
        {
            PlainTextInputElement element = new PlainTextInputElement("roles-edit-name", new PlainTextObject("What should the role be named?", false), newRole ? null : role.getName(), false, null, null, null);
            blocks.add(new InputBlock("roles-edit-name-parent", new PlainTextObject("What should the role be named?", false), element, null, null, false));
        }
        {
            setupPermission("*Admin*", "Give the user access to all CubHours options.", UserPermission.ADMIN);
            setupPermission("*Settings*", "Give the user access to change settings.", UserPermission.SETTINGS);
            setupPermission("*Users*", "Give the user access to edit users.", UserPermission.USERS);
            setupPermission("*Roles*", "Give the user access to edit roles.", UserPermission.ROLES);
            setupPermission("*Session*", "Give the user access to control the current session.", UserPermission.SESSION);
            setupPermission("*None*", null, UserPermission.NONE);
            CheckboxesElement element = new CheckboxesElement("roles-edit-checkbox", options, initialOptions.isEmpty() ? null : initialOptions, null);
            blocks.add(new InputBlock("roles-edit-checkbox-parent", new PlainTextObject("Permissions", false), element, null, null, false));
        }
    }

    private void setupPermission(String title, String description, UserPermission permission) {
        OptionObject option = CubUtil.composeOptionObject(title, permission.name(), description);
        if (role != null && ((role.getPermissions().contains(UserPermission.ADMIN) && permission != UserPermission.NONE) || role.getPermissions().contains(permission) || (initialOptions.isEmpty() && permission == UserPermission.NONE))) {
            initialOptions.add(option);
        }
        options.add(option);
    }
}

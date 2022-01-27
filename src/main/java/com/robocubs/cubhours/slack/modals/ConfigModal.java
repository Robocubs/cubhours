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

import com.google.api.client.util.Maps;
import com.google.common.collect.Lists;
import com.robocubs.cubhours.CubConfig;
import com.robocubs.cubhours.database.DatabaseHandler;
import com.robocubs.cubhours.slack.Modal;
import com.robocubs.cubhours.slack.SlackHandler;
import com.robocubs.cubhours.users.User;
import com.robocubs.cubhours.users.UserHandler;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author Noah Husby
 */
public class ConfigModal extends Modal {

    public ConfigModal() {
        super("CubHours");
    }

    @Override
    public String getName() {
        return "config";
    }

    @Override
    public String[] getActionIds() {
        return new String[]{ "settings", "users", "seasons", "callback" };
    }

    @Override
    public Response onViewSubmission(App app, ViewSubmissionRequest request, ViewSubmissionContext context, String callback) {
        close();
        return context.ack();
    }

    @Override
    public Response onViewClosed(App app, ViewClosedRequest request, DefaultContext context, String callback) {
        close();
        return context.ack();
    }

    @Override
    public Response onBlockAction(App app, BlockActionRequest request, ActionContext context, String id) {
        if (id.equals("settings")) {
            close();
            SlackHandler.getInstance().openModal(new SettingsModal(), request, context);
        } else if (id.equals("seasons")) {
            close();
            String seasonName = request.getPayload().getActions().get(0).getSelectedOption().getValue();
            SlackHandler.getInstance().openModal(new SeasonModal(seasonName.equals("new") ? null : seasonName), request, context);
        } else if (id.equals("users")) {
            close();
            String slackId = request.getPayload().getActions().get(0).getSelectedUser();
            com.slack.api.model.User slackUser = SlackHandler.getInstance().getUser(slackId);
            if (slackUser.isBot() || slackUser.isWorkflowBot() || slackUser.getName().equalsIgnoreCase("slackbot")) {
                close();
                SlackHandler.getInstance().openModal(new ErrorModal("Cannot configure an ID for a bot!"), request, context);
                return context.ack();
            }
            if (CubConfig.cloudSettings.slackLookup.containsKey(slackId)) {
                User user = UserHandler.getInstance().getUser(CubConfig.cloudSettings.slackLookup.get(slackId));
                if (user != null) {
                    SlackHandler.getInstance().openModal(new UserModal(user), request, context);
                } else {
                    // Somehow, a reverse lookup exists, but the user itself is missing
                    CubConfig.cloudSettings.slackLookup.remove(slackId);
                    DatabaseHandler.getInstance().pushConfigSettings();
                    user = new User();
                    user.setSlackId(slackId);
                    SlackHandler.getInstance().openModal(new UserModal(user), request, context);
                }
            } else {
                User user = new User();
                user.setSlackId(slackId);
                SlackHandler.getInstance().openModal(new UserModal(user), request, context);
            }
        }
        return context.ack();
    }

    @Override
    protected void setup(List<LayoutBlock> blocks) {
        blocks.add(CubUtil.composeSectionBlock("*Hi!* Here's how I can help you:"));
        blocks.add(new DividerBlock());
        {
            ButtonElement element = new ButtonElement(new PlainTextObject("Change Settings", false), createActionId("settings"), null, null, null, null);
            blocks.add(CubUtil.composeSectionBlock(":gear: *Settings*\nManage your team settings", "config-category-settings", element));
        }
        {
            UsersSelectElement element = new UsersSelectElement(new PlainTextObject("Choose user", true), createActionId("users"), null, null);
            blocks.add(CubUtil.composeSectionBlock(":bust_in_silhouette: *Users*\nManage your team users", null, element));
        }
        {
            Map<LocalDate, String> seasons = Maps.newHashMap();
            for (Map.Entry<String, String> entry : CubConfig.cloudSettings.seasons.entrySet()) {
                seasons.put(LocalDate.parse(entry.getValue()), entry.getKey());
            }
            List<OptionObject> seasonsList = Lists.newArrayList();
            StaticSelectElement element = new StaticSelectElement(new PlainTextObject("Choose season", true), createActionId("seasons"), seasonsList, null, null, null);
            seasonsList.add(new OptionObject(new PlainTextObject(":pencil2: Add a new season", true), "new", null, null));
            SortedSet<LocalDate> sortedKeys = new TreeSet<>(seasons.keySet());
            for (LocalDate key : sortedKeys) {
                seasonsList.add(new OptionObject(new PlainTextObject(seasons.get(key), true), seasons.get(key), null, null));
            }
            blocks.add(CubUtil.composeSectionBlock(":calendar: *Seasons*\nManage your team's season", null, element));
        }
    }
}

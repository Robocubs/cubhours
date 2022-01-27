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

import com.robocubs.cubhours.CubConfig;
import com.robocubs.cubhours.database.DatabaseHandler;
import com.robocubs.cubhours.slack.Modal;
import com.robocubs.cubhours.slack.SlackHandler;
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
import com.slack.api.model.block.composition.PlainTextObject;
import com.slack.api.model.block.element.ButtonElement;
import com.slack.api.model.block.element.DatePickerElement;
import com.slack.api.model.block.element.PlainTextInputElement;
import com.slack.api.model.view.ViewState;

import java.util.List;
import java.util.Map;

/**
 * @author Noah Husby
 */
public class SeasonModal extends Modal {

    private final String season;

    public SeasonModal(String season) {
        super(season == null ? "Create a role" : "Edit \"" + season + "\"");
        this.submit = "Save";
        this.privateMetadata = season;
        this.season = season;
    }

    @Override
    public String getName() {
        return "seasons";
    }

    @Override
    public String[] getActionIds() {
        return new String[]{ "delete", "callback" };
    }

    @Override
    public Response onViewSubmission(App app, ViewSubmissionRequest request, ViewSubmissionContext context, String callback) {
        ViewSubmissionPayload payload = request.getPayload();
        String formerName = payload.getView().getPrivateMetadata();
        Map<String, Map<String, ViewState.Value>> values = payload.getView().getState().getValues();
        String name = values.get("seasons-edit-name-parent").get("seasons-edit-name").getValue();
        String date = values.get("seasons-edit-date-parent").get("seasons-edit-date").getSelectedDate();
        if (formerName != null) {
            CubConfig.cloudSettings.seasons.remove(formerName);
        }
        CubConfig.cloudSettings.seasons.put(name, date);
        DatabaseHandler.getInstance().pushConfigSettings();
        close();
        return SlackHandler.getInstance().openModal(new ConfigModal(), context);
    }

    @Override
    public Response onViewClosed(App app, ViewClosedRequest request, DefaultContext context, String callback) {
        return context.ack();
    }

    @Override
    public Response onBlockAction(App app, BlockActionRequest request, ActionContext context, String id) {
        if (id.equals("delete")) {
            CubConfig.cloudSettings.seasons.remove(season);
            SlackHandler.getInstance().openModal(new ConfigModal(), request, context);
        }
        return context.ack();
    }

    @Override
    protected void setup(List<LayoutBlock> blocks) {
        boolean newSeason = season == null;
        {
            PlainTextInputElement element = new PlainTextInputElement("seasons-edit-name", new PlainTextObject("What should the season be named?", false), newSeason ? null : season, false, null, null, null);
            blocks.add(new InputBlock("seasons-edit-name-parent", new PlainTextObject("What should the role be named?", false), element, null, null, false));
        }
        {
            DatePickerElement element = new DatePickerElement("seasons-edit-date", new PlainTextObject("Select a date", false), newSeason ? null : CubConfig.cloudSettings.seasons.get(season), null);
            blocks.add(new InputBlock("seasons-edit-date-parent", new PlainTextObject("Start date", false), element, null, null, false));
        }
        {
            ButtonElement element = CubUtil.composeButtonElement("Delete Season", createActionId("delete"));
            blocks.add(CubUtil.composeSectionBlock("Delete the season", null, element));
        }
    }
}
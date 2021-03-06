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

import com.robocubs.cubhours.util.CubUtil;
import com.slack.api.bolt.App;
import com.slack.api.bolt.context.builtin.ActionContext;
import com.slack.api.bolt.context.builtin.DefaultContext;
import com.slack.api.bolt.context.builtin.ViewSubmissionContext;
import com.slack.api.bolt.request.builtin.BlockActionRequest;
import com.slack.api.bolt.request.builtin.ViewClosedRequest;
import com.slack.api.bolt.request.builtin.ViewSubmissionRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.view.View;
import com.slack.api.model.view.ViewClose;
import com.slack.api.model.view.ViewSubmit;
import com.slack.api.model.view.ViewTitle;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Noah Husby
 */
@RequiredArgsConstructor
@AllArgsConstructor
public abstract class Modal {
    protected final String title;
    protected String submit = "Done";
    protected String close = "Close";
    protected String privateMetadata;

    @Getter
    private final String salt = CubUtil.getSaltString();
    private final String callback = createActionId("callback");

    public abstract String getName();

    public abstract String[] getActionIds();

    public abstract Response onViewSubmission(App app, ViewSubmissionRequest request, ViewSubmissionContext context, String callback);

    public abstract Response onViewClosed(App app, ViewClosedRequest request, DefaultContext context, String callback);

    public abstract Response onBlockAction(App app, BlockActionRequest request, ActionContext context, String id);

    protected abstract void setup(List<LayoutBlock> blocks);

    public void close() {
        SlackHandler.getInstance().closeModal(this);
    }

    protected String createActionId(String id) {
        return String.format("%s_%s_%s", getName(), id, getSalt());
    }

    public View view() {
        View view = new View();
        view.setType("modal");
        view.setCallbackId(callback);
        view.setTitle(new ViewTitle("plain_text", title, true));
        view.setSubmit(new ViewSubmit("plain_text", submit, true));
        view.setClose(new ViewClose("plain_text", close, true));
        view.setPrivateMetadata(privateMetadata);
        List<LayoutBlock> blocks = new ArrayList<>();
        setup(blocks);
        view.setBlocks(blocks);
        return view;
    }
}

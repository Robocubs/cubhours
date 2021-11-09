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
import com.robocubs.cubhours.slack.Modal;
import com.robocubs.cubhours.util.CubUtil;
import com.slack.api.model.block.DividerBlock;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.element.ButtonElement;

import java.util.List;

/**
 * @author Noah Husby
 */
public class SettingsModal extends Modal {
    public SettingsModal() {
        super(":gear: Settings", "config-settings");
    }

    @Override
    protected void setup(List<LayoutBlock> blocks) {
        blocks.add(new DividerBlock());
        {
            ButtonElement element = CubUtil.composeButtonElement(CubConfig.cloudSettings.doorbell ? ":white_check_mark: Enabled" : ":x: Disabled", "config-settings-doorbell");
            blocks.add(CubUtil.composeSectionBlock(":bell: *Doorbell*\nToggle the doorbell command", null, element));
        }
    }
}

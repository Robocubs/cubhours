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

package com.robocubs.cubhours;

import com.google.common.collect.Maps;
import com.noahhusby.lib.application.config.Config;

import java.util.Map;

/**
 * @author Noah Husby
 */
@Config()
public class CubConfig {
    @Config.Comment({
            "Enable external scanner mode [E.g. Barcode]"
    })
    public static boolean scan = true;

    @Config.Comment({
            "Enable slack support"
    })
    public static boolean enable_slack_support = true;

    @Config.Comment({
            "The bot token for slack support"
    })
    public static String slack_bot_token = "";

    @Config.Comment({
            "The app token for slack support"
    })
    public static String slack_app_token = "";

    @Config.Ignore
    public static CloudSettings cloudSettings = new CloudSettings();

    public static class CloudSettings {
        public boolean doorbell = true;
        public Map<String, String> seasons = Maps.newHashMap();
        public Map<String, String> slackLookup = Maps.newHashMap();
    }
}

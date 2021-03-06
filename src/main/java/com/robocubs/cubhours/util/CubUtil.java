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

package com.robocubs.cubhours.util;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.composition.MarkdownTextObject;
import com.slack.api.model.block.composition.OptionObject;
import com.slack.api.model.block.composition.PlainTextObject;
import com.slack.api.model.block.composition.TextObject;
import com.slack.api.model.block.element.BlockElement;
import com.slack.api.model.block.element.ButtonElement;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Noah Husby
 */
@UtilityClass
public class CubUtil {

    public static TextObject composeTextObject(String type, String text) {
        return new TextObject() {
            @Override
            public String getText() {
                return text;
            }

            @Override
            public String getType() {
                return type;
            }
        };
    }

    public static OptionObject composeOptionObject(String text, String value) {
        return composeOptionObject(text, value, null);
    }

    public static OptionObject composeOptionObject(String text, String value, String description) {
        return composeOptionObject(text, value, description, null);
    }

    public static OptionObject composeOptionObject(String text, String value, String description, String url) {
        PlainTextObject descriptionTextObject = description == null ? null : new PlainTextObject(description, true);
        return new OptionObject(new MarkdownTextObject(text, false), value, descriptionTextObject, url);
    }

    public static SectionBlock composeSectionBlock(String text) {
        return composeSectionBlock(text, null);
    }

    public static SectionBlock composeSectionBlock(String text, String blockId) {
        return composeSectionBlock(text, blockId, null);
    }

    public static SectionBlock composeSectionBlock(String text, String blockId, BlockElement element) {
        return new SectionBlock(new MarkdownTextObject(text, false), blockId, null, element);
    }

    public static ButtonElement composeButtonElement(String text, String actionId) {
        return composeButtonElement(text, actionId, null, null, null);
    }

    public static ButtonElement composeButtonElement(String text, String actionId, String url, String value, String style) {
        return new ButtonElement(new PlainTextObject(text, true), actionId, url, value, style, null);
    }

    @SneakyThrows
    public static JsonElement getBlock(String block) {
        String content = Resources.toString(CubUtil.class.getResource("/blocks/" + block + ".json"), Charsets.UTF_8);
        return new Gson().fromJson(content, JsonElement.class);
    }

    public static ExecutorService newSingleThreadExecutor(String name) {
        return Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat(name + "-%d").build());
    }

    public static ScheduledExecutorService newSingleThreadScheduledExecutor(String name) {
        return Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat(name + "-%d").build());
    }

    public static ExecutorService newThreadPoolExecutor(int threads, String name) {
        return Executors.newFixedThreadPool(threads, new ThreadFactoryBuilder().setNameFormat(name + "-%d").build());
    }

    public static ScheduledExecutorService newThreadPoolScheduledExecutor(int threads, String name) {
        return Executors.newScheduledThreadPool(threads, new ThreadFactoryBuilder().setNameFormat(name + "-%d").build());
    }

    public static String getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 6) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        return salt.toString();
    }
}

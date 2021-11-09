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

import com.robocubs.cubhours.slack.SlackCommand;
import com.robocubs.cubhours.users.UserHandler;
import com.slack.api.bolt.App;
import com.slack.api.bolt.context.builtin.SlashCommandContext;
import com.slack.api.bolt.request.builtin.SlashCommandRequest;
import com.slack.api.bolt.response.Response;

import java.util.List;

/**
 * @author Noah Husby
 */
public class HereCommand extends SlackCommand {
    @Override
    public Response onCommand(App app, SlashCommandRequest request, SlashCommandContext context) {
        List<String> names = UserHandler.getInstance().getActiveUsersAsNames();
        if(names.isEmpty()) {
            return context.ack(":notebook: There is nobody signed in.");
        }
        StringBuilder message = new StringBuilder("**Signed In: " + names.get(0));
        for(int i = 1; i < names.size(); i++) {
            message.append(", ");
            message.append(names.get(i));
        }
        return context.ack(message.toString());
    }

    @Override
    public String getName() {
        return "here";
    }
}

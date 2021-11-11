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

package com.robocubs.cubhours.gui;

import com.google.api.client.util.Lists;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import lombok.RequiredArgsConstructor;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Noah Husby
 */
@RequiredArgsConstructor
public class ClockRenderer {
    private final TextFlow flow;
    private LocalTime lastTime;
    private List<Map.Entry<String, Boolean>> lastText = Lists.newArrayList();
    private List<List<Map.Entry<String, Boolean>>> animations = Lists.newArrayList();
    private boolean isUpdating = false;

    public void update() {
        try {
            LocalTime now = LocalTime.now();
            if (!isUpdateRequired() && !isUpdating) {
                return;
            }
            lastTime = now;
            if (lastText.isEmpty()) {
                lastText = generatePrompt();
                render(lastText);
                return;
            }
            if (!isUpdating) {
                isUpdating = true;
                animations = createPromptAnimation();
            }

            if (animations.isEmpty()) {
                isUpdating = false;
                return;
            }

            render(animations.get(0));
            animations.remove(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private List<List<Map.Entry<String, Boolean>>> createPromptAnimation() {
        List<List<Map.Entry<String, Boolean>>> prompts = Lists.newArrayList();
        List<Map.Entry<String, Boolean>> goal = generatePrompt();
        if (lastText.isEmpty()) {
            prompts.add(goal);
            return prompts;
        }

        if (goal.size() == lastText.size()) {
            List<Integer> difference = Lists.newArrayList();
            for (int i = 0; i < goal.size(); i++) {
                if (!goal.get(i).getKey().equals(lastText.get(i).getKey())) {
                    difference.add(i);
                }
            }
            if (difference.size() == 1) {
                Map.Entry<String, Boolean> oldDiff = lastText.get(difference.get(0));
                String oldDiffText = oldDiff.getKey();
                Map.Entry<String, Boolean> newDiff = goal.get(difference.get(0));
                String newDiffText = "";

                while (oldDiffText.length() > 0) {
                    List<Map.Entry<String, Boolean>> temp = Lists.newArrayList();
                    for (int i = 0; i < goal.size(); i++) {
                        if (difference.get(0) == i) {
                            oldDiffText = oldDiffText.substring(0, oldDiffText.length() - 1);
                            temp.add(new AbstractMap.SimpleEntry<>(oldDiffText, oldDiff.getValue()));
                        } else {
                            temp.add(goal.get(i));
                        }
                    }
                    prompts.add(temp);
                }

                while (newDiffText.length() != newDiff.getKey().length()) {
                    List<Map.Entry<String, Boolean>> temp = Lists.newArrayList();
                    for (int i = 0; i < goal.size(); i++) {
                        if (difference.get(0) == i) {
                            newDiffText = newDiff.getKey().substring(0, newDiffText.length() + 1);
                            temp.add(new AbstractMap.SimpleEntry<>(newDiffText, newDiff.getValue()));
                        } else {
                            temp.add(goal.get(i));
                        }
                    }
                    prompts.add(temp);
                }
                lastText = goal;
                return prompts;
            }
        }

        String[] lastTextStrings = new String[lastText.size()];
        Boolean[] lastTextPrimary = new Boolean[lastText.size()];
        for (int i = 0; i < lastText.size(); i++) {
            Map.Entry<String, Boolean> entry = lastText.get(i);
            lastTextStrings[i] = entry.getKey();
            lastTextPrimary[i] = entry.getValue();
        }

        for (int i = lastText.size(); i > 1; i--) {
            while (lastTextStrings[i - 1].length() > 0) {
                List<Map.Entry<String, Boolean>> temp = Lists.newArrayList();
                for (int x = 0; x < lastText.size(); x++) {
                    if (lastTextStrings[x].length() == 0) {
                        continue;
                    }
                    temp.add(new AbstractMap.SimpleEntry<>(lastTextStrings[x], lastTextPrimary[x]));
                }
                lastTextStrings[i - 1] = lastTextStrings[i - 1].substring(0, lastTextStrings[i - 1].length() - 1);
                prompts.add(temp);
            }
        }

        String[] goalTextStrings = new String[goal.size()];
        goalTextStrings[0] = goal.get(0).getKey();
        for (int i = 1; i < goalTextStrings.length; i++) {
            goalTextStrings[i] = "";
        }
        for (int i = 0; i < goal.size(); i++) {
            if (i == 0) {
                List<Map.Entry<String, Boolean>> temp = Lists.newArrayList();
                temp.add(goal.get(0));
                prompts.add(temp);
                continue;
            }
            while (!goalTextStrings[i].equals(goal.get(i).getKey())) {
                List<Map.Entry<String, Boolean>> temp = Lists.newArrayList();
                for (int x = 0; x < goal.size(); x++) {
                    if (goalTextStrings[x].length() == 0) {
                        continue;
                    }
                    temp.add(new AbstractMap.SimpleEntry<>(goalTextStrings[x], goal.get(x).getValue()));
                }
                goalTextStrings[i] = goal.get(i).getKey().substring(0, goalTextStrings[i].length() + 1);
                prompts.add(temp);
            }
        }
        prompts.add(goal);
        lastText = goal;
        return prompts;
    }

    private List<Map.Entry<String, Boolean>> generatePrompt() {
        LocalTime time = LocalTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm");
        String[] timeSplit = dateTimeFormatter.format(time).split(":");
        int hour = Integer.parseInt(timeSplit[0]);
        int minute = Integer.parseInt(timeSplit[1]);
        String[] nums = { "zero", "one", "two", "three", "four",
                "five", "six", "seven", "eight", "nine",
                "ten", "eleven", "twelve", "thirteen",
                "fourteen", "fifteen", "sixteen", "seventeen",
                "eighteen", "nineteen", "twenty", "twenty one",
                "twenty two", "twenty three", "twenty four",
                "twenty five", "twenty six", "twenty seven",
                "twenty eight", "twenty nine", "thirty"
        };

        List<Map.Entry<String, Boolean>> clockText = com.google.common.collect.Lists.newArrayList();
        clockText.add(new AbstractMap.SimpleEntry<>("it's ", false));
        if (minute == 0) {
            clockText.add(new AbstractMap.SimpleEntry<>(nums[hour], true));
            clockText.add(new AbstractMap.SimpleEntry<>(" o' clock", false));
        } else if (minute == 1) {
            clockText.add(new AbstractMap.SimpleEntry<>("one", true));
            clockText.add(new AbstractMap.SimpleEntry<>(" minute past ", false));
            clockText.add(new AbstractMap.SimpleEntry<>(nums[hour], true));
        } else if (minute == 59) {
            clockText.add(new AbstractMap.SimpleEntry<>("one", true));
            clockText.add(new AbstractMap.SimpleEntry<>(" minute to ", false));
            clockText.add(new AbstractMap.SimpleEntry<>(nums[(hour % 12) + 1], true));
        } else if (minute == 15) {
            clockText.add(new AbstractMap.SimpleEntry<>("quarter past ", false));
            clockText.add(new AbstractMap.SimpleEntry<>(nums[hour], true));
        } else if (minute == 45) {
            clockText.add(new AbstractMap.SimpleEntry<>("quarter to ", false));
            clockText.add(new AbstractMap.SimpleEntry<>(nums[(hour % 12) + 1], true));
        } else if (minute <= 30) {
            clockText.add(new AbstractMap.SimpleEntry<>(nums[minute], true));
            clockText.add(new AbstractMap.SimpleEntry<>(" minutes past ", false));
            clockText.add(new AbstractMap.SimpleEntry<>(nums[hour], true));
        } else {
            clockText.add(new AbstractMap.SimpleEntry<>(nums[60 - minute], true));
            clockText.add(new AbstractMap.SimpleEntry<>(" minutes to ", false));
            clockText.add(new AbstractMap.SimpleEntry<>(nums[(hour % 12) + 1], true));
        }
        return clockText;
    }

    private void render(List<Map.Entry<String, Boolean>> clockText) {
        List<Node> nodes = com.google.common.collect.Lists.newArrayList();
        for (Map.Entry<String, Boolean> entry : clockText) {
            Text text = new Text(entry.getKey().toUpperCase(Locale.ROOT));
            text.getStyleClass().add("clock");
            if (entry.getValue()) {
                text.setFill(Color.rgb(173, 46, 60));
            }
            nodes.add(text);
        }
        Platform.runLater(() -> {
            flow.getChildren().clear();
            flow.getChildren().addAll(nodes);
        });
    }

    private boolean isUpdateRequired() {
        LocalTime now = LocalTime.now();
        if (lastTime == null) {
            lastTime = now;
            return true;
        }
        return !(now.getHour() == lastTime.getHour() && now.getMinute() == lastTime.getMinute());
    }
}

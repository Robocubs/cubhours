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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * @author Noah Husby
 */
@RequiredArgsConstructor
public class ClockRenderer {
    private final TextFlow flow;
    private LocalTime lastTime;
    private List<ClockText> lastPayload = Lists.newArrayList();
    private List<List<ClockText>> animations = Lists.newArrayList();
    private boolean isUpdating = false;

    private final String placeholder = " ";

    public void update() {
        try {
            if (!isUpdateRequired() && !isUpdating) {
                return;
            }
            lastTime = LocalTime.now();
            if (lastPayload.isEmpty()) {
                lastPayload = generatePrompt();
                render(lastPayload);
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


    private List<List<ClockText>> createPromptAnimation() {
        List<List<ClockText>> prompts = Lists.newArrayList();
        List<ClockText> goal = generatePrompt();
        if (lastPayload.isEmpty()) {
            prompts.add(goal);
            return prompts;
        }

        if (goal.size() == lastPayload.size()) {
            List<Integer> difference = Lists.newArrayList();
            for (int i = 0; i < goal.size(); i++) {
                if (!goal.get(i).getText().equals(lastPayload.get(i).getText())) {
                    difference.add(i);
                }
            }

            if (difference.size() == 1) {
                ClockText oldDiff = lastPayload.get(difference.get(0));
                ClockText newDiff = goal.get(difference.get(0));
                String newDiffText = newDiff.getText();
                String oldDiffText = oldDiff.getText();
                newDiff.setText("");

                while (oldDiff.getText().length() > 1) {
                    List<ClockText> temp = Lists.newArrayList();
                    for (int i = 0; i < goal.size(); i++) {
                        if (difference.get(0) == i) {
                            oldDiff.setText(oldDiff.getText().substring(0, oldDiff.getText().length() - 1));
                            ClockText oldDiffDup = oldDiff.copy();
                            while (oldDiffDup.getText().length() < oldDiffText.length()) {
                                oldDiffDup.setText(oldDiffDup.getText() + placeholder);
                            }
                            temp.add(oldDiffDup);
                        } else {
                            temp.add(goal.get(i));
                        }
                    }
                    prompts.add(temp);
                }

                while (newDiffText.length() != newDiff.getText().length()) {
                    List<ClockText> temp = Lists.newArrayList();
                    for (int i = 0; i < goal.size(); i++) {
                        if (difference.get(0) == i) {
                            newDiff.setText(newDiffText.substring(0, newDiff.getText().length() + 1));
                            ClockText newDiffDup = newDiff.copy();
                            newDiffDup.setText(newDiffDup.getText() + String.join("", Collections.nCopies(newDiffText.length() - newDiff.getText().length(), placeholder)));
                            temp.add(newDiffDup);
                        } else {
                            temp.add(goal.get(i));
                        }
                    }
                    prompts.add(temp);
                }
                lastPayload = goal;
                return prompts;
            }
        }

        for (int i = lastPayload.size(); i > 1; i--) {
            while (lastPayload.get(i - 1).getText().length() > 0) {
                List<ClockText> temp = Lists.newArrayList();
                for (ClockText clockText : lastPayload) {
                    if (clockText.getText().length() == 0) {
                        continue;
                    }
                    temp.add(clockText.copy());
                }
                ClockText temp2 = lastPayload.get(i - 1);
                temp2.setText(temp2.getText().substring(0, temp2.getText().length() - 1));
                prompts.add(temp);
            }
        }

        String[] goalTextStrings = new String[goal.size()];
        for (int i = 0; i < goalTextStrings.length; i++) {
            goalTextStrings[i] = goal.get(i).getText();
            if (i != 0) {
                goal.get(i).setText("");
            }
        }
        for (int i = 0; i < goal.size(); i++) {
            if (i == 0) {
                List<ClockText> temp = Lists.newArrayList();
                temp.add(goal.get(0));
                prompts.add(temp);
                continue;
            }
            while (!goalTextStrings[i].equals(goal.get(i).getText())) {
                List<ClockText> temp = Lists.newArrayList();
                int x = 0;
                for (ClockText clockText : goal) {
                    if (!(x != 0 && goal.get(x).getText().trim().isEmpty() && !goalTextStrings[x].trim().isEmpty())) {
                        ClockText clockTextDup = clockText.copy();
                        while (clockTextDup.getText().length() < goalTextStrings[x].length()) {
                            clockTextDup.setText(clockTextDup.getText() + placeholder);
                        }
                        temp.add(clockTextDup);
                    }
                    x++;
                }
                ClockText temp2 = goal.get(i);
                temp2.setText(goalTextStrings[i].substring(0, temp2.getText().length() + 1));
                prompts.add(temp);
            }
        }
        prompts.add(goal);
        lastPayload = goal;
        return prompts;
    }

    private List<ClockText> generatePrompt() {
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

        List<ClockText> clockTextPayload = Lists.newArrayList();
        clockTextPayload.add(new ClockText("it's", false));
        clockTextPayload.add(new ClockText(" ", false));
        if (minute == 0) {
            clockTextPayload.add(new ClockText(nums[hour], true));
            clockTextPayload.add(new ClockText(" ", false));
            clockTextPayload.add(new ClockText("o'", false));
            clockTextPayload.add(new ClockText(" ", false));
            clockTextPayload.add(new ClockText("clock", false));
        } else if (minute == 1) {
            clockTextPayload.add(new ClockText("one", true));
            clockTextPayload.add(new ClockText(" ", false));
            clockTextPayload.add(new ClockText("minute", false));
            clockTextPayload.add(new ClockText(" ", false));
            clockTextPayload.add(new ClockText("past", false));
            clockTextPayload.add(new ClockText(" ", false));
            clockTextPayload.add(new ClockText(nums[hour], true));
        } else if (minute == 59) {
            clockTextPayload.add(new ClockText("one", true));
            clockTextPayload.add(new ClockText(" ", false));
            clockTextPayload.add(new ClockText("minute", false));
            clockTextPayload.add(new ClockText(" ", false));
            clockTextPayload.add(new ClockText("to", false));
            clockTextPayload.add(new ClockText(" ", false));
            clockTextPayload.add(new ClockText(nums[(hour % 12) + 1], true));
        } else if (minute == 15) {
            clockTextPayload.add(new ClockText("quarter", false));
            clockTextPayload.add(new ClockText(" ", false));
            clockTextPayload.add(new ClockText("past", false));
            clockTextPayload.add(new ClockText(" ", false));
            clockTextPayload.add(new ClockText(nums[hour], true));
        } else if (minute == 45) {
            clockTextPayload.add(new ClockText("quarter", false));
            clockTextPayload.add(new ClockText(" ", false));
            clockTextPayload.add(new ClockText("to", false));
            clockTextPayload.add(new ClockText(" ", false));
            clockTextPayload.add(new ClockText(nums[(hour % 12) + 1], true));
        } else if (minute <= 30) {
            String temp = nums[minute];
            if (temp.contains(" ")) {
                String[] temp2 = temp.split(" ");
                clockTextPayload.add(new ClockText(temp2[0], true));
                clockTextPayload.add(new ClockText(" ", false));
                clockTextPayload.add(new ClockText(temp2[1], true));
            } else {
                clockTextPayload.add(new ClockText(temp, true));
            }
            clockTextPayload.add(new ClockText(" ", false));
            clockTextPayload.add(new ClockText("minutes", false));
            clockTextPayload.add(new ClockText(" ", false));
            clockTextPayload.add(new ClockText("past", false));
            clockTextPayload.add(new ClockText(" ", false));
            clockTextPayload.add(new ClockText(nums[hour], true));
        } else {
            String temp = nums[60 - minute];
            if (temp.contains(" ")) {
                String[] temp2 = temp.split(" ");
                clockTextPayload.add(new ClockText(temp2[0], true));
                clockTextPayload.add(new ClockText(" ", false));
                clockTextPayload.add(new ClockText(temp2[1], true));
            } else {
                clockTextPayload.add(new ClockText(temp, true));
            }
            clockTextPayload.add(new ClockText(" ", false));
            clockTextPayload.add(new ClockText("minutes", false));
            clockTextPayload.add(new ClockText(" ", false));
            clockTextPayload.add(new ClockText("to", false));
            clockTextPayload.add(new ClockText(" ", false));
            clockTextPayload.add(new ClockText(nums[(hour % 12) + 1], true));
        }
        return clockTextPayload;
    }

    private void render(List<ClockText> clockTextPayload) {
        List<Node> nodes = com.google.common.collect.Lists.newArrayList();
        for (ClockText clockText : clockTextPayload) {
            Text text = new Text(clockText.getText().toUpperCase(Locale.ROOT));
            if (clockText.getText().trim().isEmpty() || clockText.getText().contains("it")) {
                text.getStyleClass().add("clock_space");
            } else {
                text.getStyleClass().add("clock");
            }
            if (clockText.isPrimary()) {
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

    @Data
    @AllArgsConstructor
    public static class ClockText {
        public String text;
        public final boolean primary;

        public ClockText copy() {
            return new ClockText(text, primary);
        }
    }
}

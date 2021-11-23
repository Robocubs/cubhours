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

import com.robocubs.cubhours.Constants;
import com.robocubs.cubhours.users.UserHandler;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.swing.*;

/**
 * @author Noah Husby
 */
@RequiredArgsConstructor
public class MenuRenderer {

    private final GUIController controller;

    private Mode lastMode = Mode.CLOSED;

    private boolean openMenu = false;

    public void open() {
        openMenu = true;
    }

    public void close() {
        openMenu = false;
    }

    public boolean isMenuOpened() {
        return openMenu;
    }

    boolean temp = false;

    public void update() {
        try {
            // Change current mode depending on current state
            Mode currentMode;
            if(!openMenu) {
                currentMode = UserHandler.getInstance().isAnyoneSignedIn() ? Mode.SESSION : Mode.CLOSED;
            } else {
                currentMode = Mode.OPEN;
                if(lastMode != currentMode) {
                    FadeTransition transition = new FadeTransition(Duration.millis(50), controller.getHome_vbox());
                    transition.setFromValue(1.0);
                    transition.setToValue(0.0);
                    transition.onFinishedProperty().set(event -> controller.resizeElements(true));
                    transition.play();
                }
            }

            lastMode = currentMode;

            if(controller.getStage() == null) {
                return;
            }

            double width = (currentMode.getValue() / 100.0) * controller.getStage().getWidth();
            double currentWidth = controller.getMenu_card().getWidth();
            if(Math.floor(width) != Math.floor(currentWidth)) {
                temp = false;
                double val;
                if(currentWidth > width) {
                    val = currentWidth - Math.min(Constants.Layout.pixelsPerAnimationCycle, currentWidth - width);
                } else {
                    val = currentWidth + Math.min(Constants.Layout.pixelsPerAnimationCycle, width - currentWidth);
                }
                val = Math.floor(val);
                controller.getMenu_card().setMinWidth(val);
                controller.getMenu_card().setPrefWidth(val);
                controller.getMenu_card().setMinWidth(val);
            } else {
                if(!temp) {
                    temp = true;
                    if(!isMenuOpened()) {
                        controller.resizeElements(true);
                        FadeTransition transition = new FadeTransition(Duration.millis(200), controller.getHome_vbox());
                        transition.setFromValue(0.0);
                        transition.setToValue(1.0);
                        transition.onFinishedProperty().set(event -> controller.resizeElements(true));
                        transition.play();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @RequiredArgsConstructor
    public enum Mode {
        CLOSED(Constants.Layout.closedMenu), SESSION(Constants.Layout.sessionMenu), OPEN(Constants.Layout.openMenu);

        @Getter
        private final int value;
    }
}

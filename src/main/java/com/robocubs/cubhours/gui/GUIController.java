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

import com.google.common.collect.Lists;
import com.robocubs.cubhours.Constants;
import com.robocubs.cubhours.CubConfig;
import com.robocubs.cubhours.users.User;
import com.robocubs.cubhours.users.UserHandler;
import com.robocubs.cubhours.util.CubUtil;
import com.robocubs.cubhours.util.TimedTrigger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

import java.net.URL;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

public class GUIController implements javafx.fxml.Initializable {

    private final TimedTrigger notifierClock = new TimedTrigger(this::resetInputFrame, Constants.notificationLength, TimeUnit.SECONDS);
    private final TimedTrigger interactionClock = new TimedTrigger(this::resetInputFrame, Constants.interactionClock, TimeUnit.SECONDS);

    private MenuRenderer menuRenderer;

    private String scanMessage;
    private final List<Button> controls = Lists.newArrayList();

    private EntryType entryType = EntryType.SCAN;

    private String submitId = "";

    private Map.Entry<Double, Double> lastDimensions = null;

    /**
     * Sets the current id value based upon a key entry event
     *
     * @param value 0-9 for submission, or -1 for backspace.
     */
    public void newKeyEntry(int value) {
        if (entryType != EntryType.KEYPAD) {
            entryType = EntryType.KEYPAD;
            submitId = "";
        }

        if (interactionClock.isActive() || notifierClock.isActive()) {
            resetInputFrame();
            submitId = "";
        }

        submitId = value == -1 ? Optional.ofNullable(submitId).filter(sStr -> sStr.length() != 0).map(sStr
                -> sStr.substring(0, sStr.length() - 1)).orElse(submitId) : submitId + value;
        renderInputFrame();
    }

    /**
     * Requests a new submission from the scanner
     */
    public void newScanEntry() {
        if (!CubConfig.scan) {
            return;
        }
        CubUtil.newSingleThreadScheduledExecutor("scan-entry").schedule(this::submit, 200, TimeUnit.MILLISECONDS);
        entryType = EntryType.SCAN;
    }

    /**
     * Fetches the user based upon value from input method
     */
    public void submit() {
        if (entryType == EntryType.SCAN) {
            try {
                if (!id_entry.getText().equals("")) {
                    submitId = String.valueOf(Integer.parseInt(id_entry.getText()));
                }
            } catch (Exception e) {
                error("Error! Rescan", scanMessage);
                submitId = "";
                return;
            }
        }
        id_entry.setText("");
        User user = UserHandler.getInstance().getUser(submitId);
        if (user != null) {
            interactionClock.execute();
            id_print.setText(String.valueOf(user.getId()));
            id_info.setText(String.format("User: %s", user.getName()));
            Platform.runLater(() -> {
                if (false) {
                    sign_in_control.setText("Sign Out");
                    sign_in_control.setStyle("-fx-background-color: F5F5F6; -fx-border-color: red;");
                } else {
                    sign_in_control.setText("Sign In");
                    sign_in_control.setStyle("-fx-background-color: F5F5F6; -fx-border-color: lime;");
                }
            });
        } else {
            error("No User Found!", submitId);
        }
    }

    /**
     * Displays an error in the reset frame
     *
     * @param error
     * @param head
     */
    private void error(String error, String head) {
        notifierClock.execute();
        id_info.setFill(Constants.Colors.red);
        id_info.setText(error);
        id_print.setText(head);
        renderInputFrame();
    }

    /**
     * Resets the frame to default values
     */
    public void resetInputFrame() {
        id_print.setText(scanMessage);
        id_info.setFill(Constants.Colors.white);
        id_info.setText("");
        submitId = "";
        notifierClock.cancel();
        interactionClock.cancel();
    }

    /**
     * Renders the input frame from the current status
     */
    private void renderInputFrame() {
        if (interactionClock.isActive()) {
            showControls();
        } else if (notifierClock.isActive()) {
            hideControls();
        } else {
            id_info.setText("");
            id_print.setText(submitId.equals("") ? scanMessage : submitId);
            hideControls();
        }
    }

    /**
     * Show all controls for current user
     */
    private void showControls() {
        Platform.runLater(() -> controls.forEach(button -> button.setVisible(true)));
    }

    /**
     * Hide all controls for current user
     */
    private void hideControls() {
        Platform.runLater(() -> controls.forEach(button -> button.setVisible(false)));
    }

    /*
     * JavaFX Elements
     */

    @Getter
    private static GUIController instance;

    @Getter
    @Setter
    private Stage stage;

    @FXML
    private Text id_print;

    @FXML
    private TextField id_entry;

    @FXML
    private Text id_info;

    @FXML
    private Button sign_in_control;

    @FXML
    private Button check_hours_control;

    @FXML
    private Button user_history_control;

    @FXML
    private Text clock;

    @FXML
    @Getter
    private Pane menu_card;

    @FXML
    @Getter
    private TextFlow clock_flow;

    @FXML
    @Getter
    private VBox home_vbox;

    @FXML
    private ScrollPane userScrollPane;

    @FXML
    private VBox userVbox;

    /*
     * JavaFX Events
     */

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        instance = this;
        menuRenderer = new MenuRenderer(this);
        CubUtil.newSingleThreadScheduledExecutor("menu").scheduleAtFixedRate(menuRenderer::update, 0, 10, TimeUnit.MICROSECONDS);
        menu_card.setBackground(new Background(new BackgroundFill(Color.rgb(173, 46, 60, 0.10), new CornerRadii(16, 0, 0, 16, false), Insets.EMPTY)));
        ClockRenderer clockRenderer = new ClockRenderer(clock_flow);
        clock_flow.setLineSpacing(-30);
        clock_flow.setPrefWidth(Region.USE_COMPUTED_SIZE);
        CubUtil.newSingleThreadScheduledExecutor("clock").scheduleAtFixedRate(clockRenderer::update, 0, 100, TimeUnit.MILLISECONDS);
        CubUtil.newSingleThreadScheduledExecutor("resizer").scheduleAtFixedRate(this::resizeElements, 0, 50, TimeUnit.MILLISECONDS);
        for (int i = 0; i < 200; i++) {
            userVbox.getChildren().add(new Label(String.valueOf(i)));
        }
        /*
        id_entry.setFocusTraversable(false);

        controls = Lists.newArrayList(sign_in_control, check_hours_control, user_history_control);
        hideControls();

        scanMessage = CubConfig.scan ? "Enter or scan an ID #" : "Enter an ID #";

        if (CubConfig.scan) {
            CubUtil.newSingleThreadScheduledExecutor("focus-checker").scheduleAtFixedRate(() -> {
                if (!id_entry.isFocused()) {
                    Platform.runLater(() -> id_entry.requestFocus());
                }
            }, 0, 500, TimeUnit.MILLISECONDS);
        }

        renderInputFrame();

         */
    }

    public void resizeElements() {
        resizeElements(false);
    }

    public void resizeElements(boolean override) {
        if (!override && (stage == null || lastDimensions != null && stage.getWidth() == lastDimensions.getKey() && stage.getHeight() == lastDimensions.getValue())) {
            return;
        }
        lastDimensions = new AbstractMap.SimpleEntry<>(stage.getWidth(), stage.getHeight());
        double width = stage.getWidth();
        double height = stage.getHeight();

        userScrollPane.setMinHeight(menu_card.getHeight());
        userScrollPane.setMaxHeight(menu_card.getHeight());
        userScrollPane.setPrefHeight(menu_card.getHeight());

        home_vbox.setMinWidth(menuRenderer.isMenuOpened() ? 50 : -1);

        double halfVboxWidth = Math.max((home_vbox.getWidth() / 3) * 2, 700);
        clock_flow.setMinWidth(halfVboxWidth);
        clock_flow.setPrefWidth(halfVboxWidth);
        clock_flow.setMaxWidth(halfVboxWidth);
    }

    @FXML
    void onMenuClickedEvent(MouseEvent event) {
        if (menuRenderer.isMenuOpened()) {
            menuRenderer.close();
        } else {
            menuRenderer.open();
        }
    }

    @FXML
    void scanEntry(KeyEvent event) {
        newScanEntry();
    }

    @FXML
    void kpBackspacePressed(ActionEvent event) {
        newKeyEntry(-1);
    }

    @FXML
    void kpEightPressed(ActionEvent event) {
        newKeyEntry(8);
    }

    @FXML
    void kpEnterPressed(ActionEvent event) {
        submit();
    }

    @FXML
    void kpFivePressed(ActionEvent event) {
        newKeyEntry(5);
    }

    @FXML
    void kpFourPressed(ActionEvent event) {
        newKeyEntry(4);
    }

    @FXML
    void kpNinePressed(ActionEvent event) {
        newKeyEntry(9);
    }

    @FXML
    void kpOnePressed(ActionEvent event) {
        newKeyEntry(1);
    }

    @FXML
    void kpSevenPressed(ActionEvent event) {
        newKeyEntry(7);
    }

    @FXML
    void kpSixPressed(ActionEvent event) {
        newKeyEntry(6);
    }

    @FXML
    void kpThreePressed(ActionEvent event) {
        newKeyEntry(3);
    }

    @FXML
    void kpTwoPressed(ActionEvent event) {
        newKeyEntry(2);
    }

    @FXML
    void kpZeroPressed(ActionEvent event) {
        newKeyEntry(0);
    }

    @FXML
    void signInPressed(ActionEvent event) {
        UserHandler.getInstance().signIn();
    }
}

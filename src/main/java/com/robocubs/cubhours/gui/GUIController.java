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
import com.robocubs.cubhours.users.User;
import com.robocubs.cubhours.users.UserHandler;
import com.robocubs.cubhours.util.CubUtil;
import com.robocubs.cubhours.util.TimedTrigger;
import com.robocubs.cubhours.util.javafx.TextWrapper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import lombok.Getter;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

@Getter
public class GUIController implements javafx.fxml.Initializable {

    private final TimedTrigger notifierClock = new TimedTrigger(this::reset, 5, TimeUnit.SECONDS);
    private final TimedTrigger interactionClock = new TimedTrigger(this::reset, 20, TimeUnit.SECONDS);

    private TextWrapper id_info_wrapper;
    private TextWrapper id_print_wrapper;

    private boolean focusRequested = false;
    private boolean submitRequested = false;
    private boolean controlsHidden = false;

    private List<Button> controls = Lists.newArrayList();

    private EntryType entryType = EntryType.SCAN;

    private String submitID = "";

    // LOOP
    {
        /*
        returnToFocus();

        if(interactionClock.isActive()) {
            showControls();
        } else if(notifierClock.isActive()) {
            hideControls();
        } else {
            id_info_wrapper.setText("");
            if(submitID.equals("")) {
                id_print_wrapper.setText("Enter or scan an ID #");
            } else {
                id_print_wrapper.setText(submitID);
            }
            hideControls();
        }

         */
    }

    /**
     * Adds an integer to the end of the submit ID. 0-9 for submission. -1 for backspace.
     *
     * @param k Keypad Entry
     */
    public void newKeyEntry(int k) {
        if (entryType != EntryType.KEYPAD) {
            entryType = EntryType.KEYPAD;
            submitID = "";
        }

        if (interactionClock.isActive() || notifierClock.isActive()) {
            reset();
        }

        if (k == -1) {
            submitID = Optional.ofNullable(submitID).filter(sStr -> sStr.length() != 0).map(sStr
                    -> sStr.substring(0, sStr.length() - 1)).orElse(submitID);
            return;
        }

        submitID += k;
    }

    /**
     * Updates the checker. The scanner will submit .2 seconds after the final digit is scanned.
     */
    public void requestScanEntry() {
        hideControls();
        CubUtil.newSingleThreadScheduledExecutor("scan-entry").schedule(this::newScanEntry, 200, TimeUnit.MILLISECONDS);
    }

    /**
     * Grabs the scan ID and sets it to the submit ID
     */
    public void newScanEntry() {
        entryType = EntryType.SCAN;
        reset();

        try {
            if (!id_entry.getText().equals("")) {
                submitID = String.valueOf(Integer.parseInt(id_entry.getText()));
            }
        } catch (Exception e) {
            error("Error! Rescan", "Enter or scan an ID #");
            submitID = "";
        }
        id_entry.setText("");
        submitToCloud();
    }

    public void submitToCloud() {
        if (!submitID.equals("")) {
            UserHandler.getInstance().submit(submitID);
        }
    }

    private void error(String error, String head) {
        notifierClock.execute();
        id_info.setFill(Paint.valueOf("rgb(255,0,0)"));
        id_info.setText(error);
        id_print.setText(head);
    }

    public void reset() {
        id_print.setText("Scan or enter an ID #");
        id_info.setFill(Paint.valueOf("rgb(0,0,0)"));
        id_info.setText("");
        submitID = "";
        notifierClock.cancel();
        interactionClock.cancel();
        //UserHandler.getInstance().reset();
    }

    private void returnToFocus() {
        if (!id_entry.isFocused() && !focusRequested) {
            focusRequested = true;
            Platform.runLater(() -> {
                focusRequested = false;
                id_entry.requestFocus();
            });
        }
    }

    public void setUserUpdate(boolean success, User user, boolean signedIn) {
        if (success) {
            interactionClock.execute();
            //id_print.setText(user.id);
            //id_info.setText("User: "+user.name);
            Platform.runLater(() -> {
                if (signedIn) {
                    sign_in_control.setText("Sign Out");
                    sign_in_control.setStyle("-fx-background-color: F5F5F6; -fx-border-color: red;");
                } else {
                    sign_in_control.setText("Sign In");
                    sign_in_control.setStyle("-fx-background-color: F5F5F6; -fx-border-color: lime;");
                }
            });
        } else {
            //error("No User Found!", String.valueOf(user.id));
        }
    }

    private void showControls() {
        if (controlsHidden) {
            controlsHidden = false;
            Platform.runLater(() -> controls.forEach(button -> button.setVisible(true)));
        }
    }

    private void hideControls() {
        if (!controlsHidden) {
            controlsHidden = true;
            Platform.runLater(() -> controls.forEach(button -> button.setVisible(false)));
        }
    }

    /*
     * JavaFX Elements
     */

    @Getter
    private static GUIController instance;

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

    /*
     * JavaFX Events
     */

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        instance = this;
        id_entry.setFocusTraversable(false);
        id_entry.requestFocus();

        sign_in_control.setVisible(false);
        check_hours_control.setVisible(false);
        user_history_control.setVisible(false);

        id_info_wrapper = new TextWrapper(id_info);
        id_print_wrapper = new TextWrapper(id_print);
        controls = Lists.newArrayList(sign_in_control, check_hours_control, user_history_control);
    }

    @FXML
    void scanEntry(KeyEvent event) {
        requestScanEntry();
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
        submitToCloud();
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

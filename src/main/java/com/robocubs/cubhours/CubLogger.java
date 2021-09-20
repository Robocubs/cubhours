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

import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * @author Noah Husby
 */
public class CubLogger extends Logger {
    public CubLogger() {
        super("Cubhours", null);
        setUseParentHandlers(false);

        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter() {
            private static final String format = "[%1$tT %2$S]: %4$s %n";

            @Override
            public synchronized String format(LogRecord lr) {
                StringBuilder builder = new StringBuilder();
                if (lr.getLevel() == Level.INFO) {
                    builder.append("\033[0;37m");
                }
                if (lr.getLevel() == Level.WARNING) {
                    builder.append("\033[0;33m");
                }
                if (lr.getLevel() == Level.SEVERE) {
                    builder.append("\033[0;31m");
                }
                builder.append(String.format(format,
                        new Date(lr.getMillis()),
                        lr.getLevel().getLocalizedName(),
                        Thread.currentThread().getName(),
                        lr.getMessage()));
                builder.append("\033[0m");
                return builder.toString();
            }
        });
        addHandler(handler);
    }
}

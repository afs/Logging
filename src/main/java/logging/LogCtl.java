/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package logging;

import logging.setup.LoggingSetup;
import org.slf4j.Logger;

/** Setup and control of logging.
 * A simple abstraction away from log4j or java.util.logging
 * <p>
 * Names (case insensitive):
 * <ul>
 * <li>"trace"
 * <li>"debug"
 * <li>"info"
 * <li>"warn" or "warning"
 * <li>"error"
 * <li>"off"
 * </ul>
 * @see LogFmt
 */

public class LogCtl {

    static { setLogging(); }
    private static LoggingSetup loggingSetup = null;

    static public void setLogging() {
        LoggingSystem.setLogging();
        loggingSetup = LoggingSystem.config();
    }

    /** Set the level of a logger.*/
    static public void setLevel(Logger logger, String level) {
        setLevel(logger.getName(), level);
    }

    /** Set the level of a logger. */
    static public void setLevel(Class<? > logger, String level) {
        setLevel(logger.getName(), level);
    }

    static public void setLevel(String logger, String level) {
        loggingSetup.setLevel(logger, level);
    }

    /**
     * Turn on a logger (all levels).
     */
    static public void enable(Logger logger) {
        enable(logger.getName());
    }

    /**
     * Turn on a logger (all levels).
     */
    static public void enable(String logger) {
        setLevel(logger, "all");
    }

    /**
     * Turn on a logger (all levels).
     */
    static public void disable(Logger logger) {
        setLevel(logger.getName(), "OFF");
    }

    /**
     * Turn on a logger (all levels). Works for Log4j and Java logging as the
     * logging provider to Apache common logging or slf4j.
     */
    static public void disable(String logger) {
        setLevel(logger, "OFF");
    }

    /**
     * Set to info level. Works for Log4j and Java logging as the logging
     * provider to Apache common logging or slf4j.
     */
    static public void setInfo(String logger) {
        setLevel(logger, "info");
    }

    /**
     * Set to info level. Works for Log4j and Java logging as the logging
     * provider to Apache common logging or slf4j.
     */
    static public void setInfo(Class<? > logger) {
        setLevel(logger.getName(), "info");
    }

    /**
     * Set to warning level. Works for Log4j and Java logging as the logging
     * provider to Apache common logging or slf4j.
     */
    static public void setWarn(String logger) {
        setLevel(logger, "warn");
    }

    /**
     * Set to warning level. Works for Log4j and Java logging as the logging
     * provider to Apache common logging or slf4j.
     */
    static public void setWarn(Class<? > logger) {
        setLevel(logger.getName(), "warn");
    }

    /**
     * Set to error level. Works for Log4j and Java logging as the logging
     * provider to Apache common logging or slf4j.
     */
    static public void setError(String logger) {
        setLevel(logger, "error");
    }

    /**
     * Set to error level. Works for Log4j and Java logging as the logging
     * provider to Apache common logging or slf4j.
     */
    static public void setError(Class<? > logger) {
        setLevel(logger.getName(), "error");
    }
}


/*
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

package logging.impl.slf4j18;

import org.slf4j.helpers.MarkerIgnoringBase;

/** 
 * An implementation of the SLF4J framework.
 * Unlike proper SLF4J, the format is a java-style format.
 * Less simple than the {@code org.slf4j.impl.SimpleLogger}
 */

public class FmtSimple extends MarkerIgnoringBase {

    public FmtSimple(String n) {}
    
    enum LOG_LEVEL { 
        TRACE(10), DEBUG(20), INFO(30), WARN(40) , ERROR(40);

        private int val;

        LOG_LEVEL(int val) { this .val = val; }
    }

    private void log(LOG_LEVEL level, String msg) {}
    private void log(LOG_LEVEL level, String msg, Throwable t) {}
    private void log(LOG_LEVEL level, String format, Object arg) {}
    private void log(LOG_LEVEL level, String format, Object arg1, Object arg2) {}
    private void log(LOG_LEVEL level, String format, Object...args) {}

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public void trace(String msg) {
        log(LOG_LEVEL.TRACE, msg);
    }


    @Override
    public void trace(String format, Object arg) {
        log(LOG_LEVEL.TRACE, format, arg);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        log(LOG_LEVEL.TRACE, format, arg1, arg2);
    }

    @Override
    public void trace(String format, Object...arguments) {
        log(LOG_LEVEL.TRACE, format, arguments);
    }

    @Override
    public void trace(String msg, Throwable t) {
        log(LOG_LEVEL.TRACE, msg, t);
    }

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public void debug(String msg) {
        log(LOG_LEVEL.DEBUG, msg);
    }


    @Override
    public void debug(String format, Object arg) {
        log(LOG_LEVEL.DEBUG, format, arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        log(LOG_LEVEL.DEBUG, format, arg1, arg2);
    }

    @Override
    public void debug(String format, Object...arguments) {
        log(LOG_LEVEL.DEBUG, format, arguments);
    }

    @Override
    public void debug(String msg, Throwable t) {
        log(LOG_LEVEL.DEBUG, msg, t);
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public void info(String msg) {
        log(LOG_LEVEL.INFO, msg);
    }

    @Override
    public void info(String format, Object arg) {
        log(LOG_LEVEL.INFO, format, arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        log(LOG_LEVEL.INFO, format, arg1, arg2);
    }

    @Override
    public void info(String format, Object...arguments) {
        log(LOG_LEVEL.INFO, format, arguments);
    }

    @Override
    public void info(String msg, Throwable t) {
        log(LOG_LEVEL.INFO, msg, t);
    }

    @Override
    public boolean isWarnEnabled() {
        return false;
    }

    @Override
    public void warn(String msg) {
        log(LOG_LEVEL.WARN, msg);
    }

    @Override
    public void warn(String format, Object arg) {
        log(LOG_LEVEL.WARN, format, arg);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        log(LOG_LEVEL.WARN, format, arg1, arg2);
    }

    @Override
    public void warn(String format, Object...arguments) {
        log(LOG_LEVEL.WARN, format, arguments);
    }

    @Override
    public void warn(String msg, Throwable t) {
        log(LOG_LEVEL.WARN, msg, t);
    }

    @Override
    public boolean isErrorEnabled() {
        return false;
    }

    @Override
    public void error(String msg) {
        log(LOG_LEVEL.ERROR, msg);
    }

    @Override
    public void error(String format, Object arg) {
        log(LOG_LEVEL.ERROR, format, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        log(LOG_LEVEL.ERROR, format, arg1, arg2);
    }

    @Override
    public void error(String format, Object...arguments) {
        log(LOG_LEVEL.ERROR, format, arguments);
    }

    @Override
    public void error(String msg, Throwable t) {
        log(LOG_LEVEL.ERROR, msg, t);
    }

}

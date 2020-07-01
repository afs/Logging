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

package logging.setup;

import java.io.IOException;
import java.io.InputStream;

import logging.LoggingDefaults;

/** java.util.logging (JUL) setup */
public class LoggingSetupJUL extends LoggingSetup {

    @Override
    protected String getDisplayName() {
        return "JUL";
    }

    @Override
    protected void initFromInputStream(InputStream inputStream, String name) throws IOException {
        // A call of getLogManager().reset() closes handlers and streams.
        // logging.ConsoleHandlerStream.reset() stops this.
        java.util.logging.LogManager.getLogManager().readConfiguration(inputStream);
    }

    @Override
    protected String[] getLoggingSetupFilenames() {
        return new String[] {"logging.properties"};
    }

    @Override
    protected String getSystemProperty() {
        return "java.util.logging.config.file";
    }

    @Override
    protected String getDefaultString() {
        return LoggingDefaults.defaultJUL;
    }

    @Override
    public void setLevel(String logger, String levelName) {
        java.util.logging.Level level = java.util.logging.Level.ALL;
        if ( levelName == null )
            level = null;
        else if ( levelName.equalsIgnoreCase("info") )
            level = java.util.logging.Level.INFO;
        else if ( levelName.equalsIgnoreCase("trace") )
            level = java.util.logging.Level.FINER;
        else if ( levelName.equalsIgnoreCase("debug") )
            level = java.util.logging.Level.FINE;
        else if ( levelName.equalsIgnoreCase("warn") || levelName.equalsIgnoreCase("warning") )
            level = java.util.logging.Level.WARNING;
        else if ( levelName.equalsIgnoreCase("error") || levelName.equalsIgnoreCase("severe") )
            level = java.util.logging.Level.SEVERE;
        else if ( levelName.equalsIgnoreCase("OFF") )
            level = java.util.logging.Level.OFF;
        java.util.logging.Logger.getLogger(logger).setLevel(level);
    }
}
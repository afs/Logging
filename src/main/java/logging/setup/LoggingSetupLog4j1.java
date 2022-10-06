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

public class LoggingSetupLog4j1 {}

//import java.io.IOException;
//import java.io.InputStream;
//
//import logging.LoggingDefaults;
//import org.apache.log4j.LogManager;
//import org.apache.log4j.xml.DOMConfigurator;
//
///** Log4j1 setup */
//public class LoggingSetupLog4j1 extends LoggingSetup {
//
//    @Override
//    protected String getDisplayName() {
//        return "Log4j1";
//    }
//
//    @Override
//    protected void initFromInputStream(InputStream inputStream, String name) throws IOException {
//        if ( name == null ) {
//            org.apache.log4j.PropertyConfigurator.configure(inputStream);
//            return;
//        }
//        if ( name.endsWith(".properties") )
//            org.apache.log4j.PropertyConfigurator.configure(inputStream);
//        else if ( name.endsWith(".xml") ) {
//            new DOMConfigurator().doConfigure(inputStream, LogManager.getLoggerRepository());
//        }
//    }
//
//    @Override
//    protected String[] getLoggingSetupFilenames() {
//        return new String[] {"log4j.properties"};
//    }
//
//    @Override
//    protected String getSystemProperty() {
//        return "log4j.configuration";
//    }
//
//    @Override
//    protected String getDefaultString() {
//        return LoggingDefaults.defaultLog4j1;
//    }
//
//    private boolean log4j1MsgLoggedOnce = false;
//    @Override
//    public void setLevel(String logger, String levelName) {
//        org.apache.log4j.Level level = org.apache.log4j.Level.ALL;
//        if ( levelName == null )
//            level = null;
//        else if ( levelName.equalsIgnoreCase("info") )
//            level = org.apache.log4j.Level.INFO;
//        else if ( levelName.equalsIgnoreCase("debug") )
//            level = org.apache.log4j.Level.DEBUG;
//        else if ( levelName.equalsIgnoreCase("trace") )
//            level = org.apache.log4j.Level.TRACE;
//        else if ( levelName.equalsIgnoreCase("warn") || levelName.equalsIgnoreCase("warning") )
//            level = org.apache.log4j.Level.WARN;
//        else if ( levelName.equalsIgnoreCase("error") || levelName.equalsIgnoreCase("severe") )
//            level = org.apache.log4j.Level.ERROR;
//        else if ( levelName.equalsIgnoreCase("OFF") )
//            level = org.apache.log4j.Level.OFF;
//        org.apache.log4j.LogManager.getLogger(logger).setLevel(level);
//    }
//}

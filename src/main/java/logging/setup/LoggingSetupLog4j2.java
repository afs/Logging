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

/** Log4j2 setup */
public class LoggingSetupLog4j2 extends LoggingSetup {

    @Override
    protected String getDisplayName() {
        return "Log4j2";
    }

    @Override
    protected void initFromInputStream(InputStream inputStream, String name) throws IOException {
        // Dispatch name to syntax.
        org.apache.logging.log4j.core.config.ConfigurationSource source = new org.apache.logging.log4j.core.config.ConfigurationSource(inputStream);
        org.apache.logging.log4j.core.config.ConfigurationFactory factory = null;
        if ( name == null )
            // Default.
            factory =  org.apache.logging.log4j.core.config.ConfigurationFactory.getInstance();
        else if ( name.endsWith(".yaml") || name.endsWith(".ym") )
            factory = new org.apache.logging.log4j.core.config.yaml.YamlConfigurationFactory();
        else if ( name.endsWith(".properties")  )
            factory = new org.apache.logging.log4j.core.config.properties.PropertiesConfigurationFactory();
        else
            factory =  org.apache.logging.log4j.core.config.ConfigurationFactory.getInstance();

        org.apache.logging.log4j.core.config.Configuration configuration = factory.getConfiguration(null, source);
        org.apache.logging.log4j.core.config.Configurator.reconfigure(configuration);
    }

    @Override
    protected String[] getLoggingSetupFilenames() {
        return new String[] { "log4j2.properties", "log4j2.yaml", "log4j2.yml", "log4j2.json", "log4j2.jsn", "log4j2.xml" };
    }

    @Override
    protected String getSystemProperty() {
        return "log4j.configurationFile";
    }

    @Override
    protected String getDefaultString() {
        return LoggingDefaults.defaultLog4j2_xml;
    }

    private boolean log4j2MsgLoggedOnce = false;
    @Override
    public void setLevel(String logger, String levelName) {

        org.apache.logging.log4j.Level level = org.apache.logging.log4j.Level.ALL;
        if ( levelName == null )
            level = null;
        else if ( levelName.equalsIgnoreCase("info") )
            level = org.apache.logging.log4j.Level.INFO;
        else if ( levelName.equalsIgnoreCase("debug") )
            level = org.apache.logging.log4j.Level.DEBUG;
        else if ( levelName.equalsIgnoreCase("trace") )
            level = org.apache.logging.log4j.Level.TRACE;
        else if ( levelName.equalsIgnoreCase("warn") || levelName.equalsIgnoreCase("warning") )
            level = org.apache.logging.log4j.Level.WARN;
        else if ( levelName.equalsIgnoreCase("error") || levelName.equalsIgnoreCase("severe") )
            level = org.apache.logging.log4j.Level.ERROR;
        else if ( levelName.equalsIgnoreCase("OFF") )
            level = org.apache.logging.log4j.Level.OFF;

        org.apache.logging.log4j.core.config.Configurator.setLevel(logger, level);
        // You can also set the root logger:
        //Configurator.setRootLevel(Level.DEBUG);
    }
}
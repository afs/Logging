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

package logging;

import static logging.PkgLib.asUTF8bytes;
import static logging.PkgLib.exception;

import java.io.*;
import java.net.URL;

/**
 * Provide encapsulation of the different logging provided when used with slf4j (or used
 * directly). Users of a library that uses slf4j, or the libraries test code, are exposed
 * to the logging setup at runtime. This class tries to smooth that out. It has a
 * collection of default setups for each supported system that log to the console on
 * stdout.
 *
 * It initializes logging providers in a consistent fashion for any supported logging
 * provider. The applications choice of logging is determined by the jars on the classpath
 * (this is due to slf4j). Logging is always initialized somehow.
 * <p>
 * Normal use:
 * * <pre>
 * LoggingSetup.setLogging();</pre>
 *
 * before any logging occurs. This is safe to call multiple times unless
 * {@code allowLoggingReset(true)} has been called.
 * <p>
 * To debug logging setup:
 *
 * <pre>
 * LoggingSetup.logLoggingSetup(true);
 * LoggingSetup.setLogging();</pre>
 * and to re-initialize (this is quite sensitive the actual logging implementation
 * details):
 *
 * <pre>
 * LoggingSetup.allowLoggingReset(true);
 * LoggingSetup.setLogging();</pre>
 *
 * Currently covered:
 * <ul>
 * <li>log4j2
 * <li>log4j1
 * <li>JUL (java.util.logging)
 * </ul>
 *
 * The configuration for logging is chosen based on following steps until one succeeds.
 * <ol>
 * <li>Is logging already initialized? (test the system property).
 * <li>Use file:{config} (for appropriate {config} name).
 * <li>Look on the classpath:{config} as a java resource
 * <li>Look on the classpath:{PathBase}/{config} as a java resource
 * <li>Use a default string.
 * </ol>
 */

public abstract class LoggingSetup {
    private static boolean logLogging         = false;
    private static boolean allowLoggingReset  = true;

    private static Object lock = new Object();
    private static volatile boolean loggingInitialized = false;


    /** Configure the logging setup system. */
    public static  void logLoggingSetup(boolean logSetup) {
        logLogging = logSetup;
    }

    /**
     * Switch off logging setting. Used so that an application's
     * logging setup is not overwritten.
     */
    public static synchronized void allowLoggingReset(boolean value) {
        allowLoggingReset = value;
        loggingInitialized = false;
    }

    // Classpath resource  to look in for logging implementation configuration files
    // (log4j.properties, logging.properties).
    private static String pathBase  = "log-conf/";

    /**
     * Get the path used to lookup a {config} file.
     */
    public static String getPathBase() {
        return pathBase;
    }

    /**
     * Set the path used to lookup a {config} file. This is the resource path, the file is
     * "path/{config}".
     * Must be called before {@link #setLogging()}.
     * Set to null for don't look
     */
    public static void setPathBase(String string) {
        if ( string != null && !string.endsWith("/") )
            string = string + "/";
        pathBase = string;
    }

    private static LoggingSetup theLoggingSetup = new LoggingSetupNoOp();

    public static LoggingSetup getLoggingSetup() {
        return theLoggingSetup;
    }

    /** Setup logging.
     * This is the one call needed
     * <pre>
     * static { LoggingSetup.setLogging(); }</pre>
     * and logging will initialize.
     *
     * This call can be called multiple time and it thread safe.; looking wil initialize the first
     */
    public static void setLogging() {
        if ( loggingInitialized )
            return;
        synchronized(lock) {
            setLoggingInternal();
        }
    }

    private static void setLoggingInternal() {
        if ( loggingInitialized )
            return;
        if ( !allowLoggingReset )
            return;
        loggingInitialized = true;
        allowLoggingReset = false;

        // Discover the binding for logging
        if ( checkForSimple() ) {
            // No specific setup.
            logLogging("slf4j-simple logging found");
            return;
        }

        // Local: like slf4j simple but with java-style formatting
        boolean hasFmtSimple = checkForFmtSimple();

        boolean hasLog4j1 = checkForLog4J1();
        boolean hasLog4j2 = checkForLog4J2();
        boolean hasJUL = checkForJUL();

        if ( !hasLog4j1 && !hasLog4j2 && !hasJUL ) {
            // Do nothing - hope logging gets initialized automatically. e.g. logback.
            // In some ways this is the preferred outcome for the war file.
            //
            // The standalone server we have to make a decision and it is better
            // if it uses the predefined format.
            logLogging("None of Log4j1, Log4j2 nor JUL setup for slf4j");
            return;
        }

        if ( hasLog4j1 && hasLog4j2 && hasJUL ) {
            logAlways("Found Log4j1, Log4j2 and JUL setups for slf4j; using Log4j2");
            hasJUL = false;
            hasLog4j1 = false;
        } else if ( hasLog4j1 && hasJUL ) {
            logAlways("Found both Log4j and JUL setup for slf4j; using Log4j1");
            hasJUL = false;
        } else if ( hasLog4j2 && hasJUL ) {
            logAlways("Found both Log4j and JUL setup for slf4j; using Log4j2");
            hasJUL = false;
        } else if ( hasLog4j1 && hasLog4j2 ) {
            logAlways("Found both Log4j1 and Log4j2 setup for slf4j; using Log4j2");
            hasLog4j1 = false;
        }

        LoggingSetup loggingSetup = null;
        if ( hasLog4j1 )
            loggingSetup = new LoggingSetupLog4j1();
        if ( hasLog4j2 )
            loggingSetup = new LoggingSetupLog4j2();
        if ( hasJUL )
            loggingSetup = new LoggingSetupJUL();

        if ( loggingSetup == null ) {
            logAlways("Failed to find a provider for slf4j");
            // XXX Built-in.
            return;
        }

        loggingSetup.setup();
        theLoggingSetup = loggingSetup;
    }

    public void setup() {
        if ( maybeAlreadySet() ) {
            logLogging("already set");
            return;
        }

        logLogging("Setup: "+getDisplayName());

        String[] files = getLoggingSetupFilenames();

        for ( String fn : files ) {
            if ( tryFileFor(fn) )
                return;
            if ( tryClassPathFor(fn) )
                return;
            if ( pathBase != null && tryClassPathFor(pathBase + fn) )
                return;
        }
        defaultSetup();
    }

    // Initialization code - each has a default of calling initFromInputStream
    // but code may wish to intercept the "tryMethod" point.

    /**
     * Get a string that is the default configuration and use that to configure logging
     */
    protected void defaultSetup() {
        logLogging("Use default setup");
        byte b[] = asUTF8bytes(getDefaultString());
        initFromBytes(b);
    }

    protected void initFromBytes(byte[] b) {
        try (InputStream input = new ByteArrayInputStream(b)) {
            initFromInputStream(input, null);
        }
        catch (IOException ex) {
            exception(ex);
        }
    }

    protected boolean tryFileFor(String fn) {
        if ( fn == null )
            return false;
        logLogging("try file %s", fn);
        try {
            return initFromFile(fn);
        }
        catch (Throwable th) {}
        return false;
    }

    protected boolean initFromFile(String fn) throws IOException {
        File f = new File(fn);
        if ( f.exists() ) {
            logLogging("found file:" + fn);
            try (InputStream input = new BufferedInputStream(new FileInputStream(f))) {
                initFromInputStream(input, fn);
            }
            System.setProperty(getSystemProperty(), "file:" + fn);
            return true;
        }
        return false;
    }

    protected boolean tryClassPathFor(String resourceName) {
        logLogging("try classpath %s", resourceName);
        try {
            return initFromURL(resourceName);
        } catch (Throwable th) {}
        return false;
    }

    protected boolean initFromURL(String resourceName) {
        URL url = getResource(resourceName);
        if ( url == null )
            return false;
        logLogging("found via classpath %s", url);
        try {
            initFromInputStream(url.openStream(), resourceName);
        }
        catch (IOException e) { exception(e); return false; }
        System.setProperty(getSystemProperty(), url.toString());
        return true;
    }

    /** Open by classpath or return null */
    protected URL getResource(String resourceName) {
        URL url = this.getClass().getClassLoader().getResource(resourceName);
        if ( url == null )
            return null;
        // Skip any thing that looks like test code.
        if ( url.toString().contains("-tests.jar") || url.toString().contains("test-classes") )
            return null;
        return url;
    }

    /** Has logging been setup? */
    protected boolean maybeAlreadySet() {
        // No loggers have been created but configuration may have been set up.
        String propName = getSystemProperty();
        String x = System.getProperty(propName, null);
        if ( x != null ) {
            logLogging("%s = %s", propName, x);
            return true;
        }
        return false;
    }

    // slf4j v1.7.0
    // slf4j 1.8.x uses ServiceLoader

    // Need both "org.slf4j.impl.Log4jLoggerAdapater" and "org.apache.log4j.Logger"
    private static boolean checkForLog4J1() {
        boolean bLog4j = checkForClass("org.apache.log4j.Logger");
        boolean bLog4jBinding = checkForClass("org.slf4j.impl.Log4jLoggerAdapter");
        if ( bLog4j && bLog4jBinding )
            return true;
        if ( !bLog4j && !bLog4jBinding )
            return false;
        if ( !bLog4j ) {
            logAlways("Classpath has the slf4j-log4j binding but not log4j1");
            return false;
        }
        if ( !bLog4jBinding ) {
            logAlways("Classpath has log4j1 but not the slf4j-log4j binding");
            return false;
        }
        return false;
    }

    // Need both "org.slf4j.impl.Log4jLoggerAdapater" and "org.apache.logging.log4j.Logger"
    private static boolean checkForLog4J2() {
        boolean bLog4j = checkForClass("org.apache.logging.log4j.Logger");
        boolean bLog4jBinding = checkForClass("org.apache.logging.slf4j.Log4jLoggerFactory");
        if ( bLog4j && bLog4jBinding )
            return true;
        if ( !bLog4j && !bLog4jBinding )
            return false;
        if ( !bLog4j ) {
            logAlways("Classpath has the log4j-slf4j-impl bridge but not log4j2");
            return false;
        }
        if ( !bLog4jBinding ) {
            logAlways("Classpath has log4j2 but not the log4j-slf4j-impl bridge");
            return false;
        }
        return false;
    }

    private static boolean checkForJUL() {
        return checkForClass("org.slf4j.impl.JDK14LoggerAdapter");
    }

    private static boolean checkForSimple() {
        return checkForClass("org.slf4j.impl.SimpleLogger");
    }

    private static boolean checkForFmtSimple() {
        return checkForClass("logging.impl.FmtSimpleFactory");
    }

    private static boolean checkForClass(String className) {
        try {
            Class.forName(className);
            return true;
        }
        catch (ClassNotFoundException ex) {
            return false;
        }
    }

    /** Log for the logging setup process */
    protected static void logLogging(String fmt, Object... args) {
        if ( logLogging ) {
            logAlways(fmt, args);
        }
    }

    private static PrintStream logLogger = System.err;

    /** Log for the logging setup messages like warnings. */
    protected static void logAlways(String fmt, Object... args) {
        fmt = "[Logging setup] "+fmt;
        if ( ! fmt.endsWith("\n") )
            fmt = fmt+"\n";
        // Do as one call so that is does not get broken up.
        logLogger.printf(fmt, args);
    }

    /**
     * Return a array of files names / resource names to try.
     * Do not return null.
     */
    protected abstract String[] getLoggingSetupFilenames();

    protected abstract String getSystemProperty();

    protected abstract String getDefaultString();

    /** Configure logging from an input stream */
    protected abstract void initFromInputStream(InputStream inputStream, String name) throws IOException;

    protected abstract String getDisplayName();

    public abstract void setLevel(String logger, String level);

    /** Log4j setup */
    static class LoggingSetupLog4j2 extends LoggingSetup {

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
                factory =  org.apache.logging.log4j.core.config.ConfigurationFactory.getInstance();
            else if ( name.endsWith(".yaml") || name.endsWith(".ym") )
                factory = new org.apache.logging.log4j.core.config.yaml.YamlConfigurationFactory();
            else if ( name.endsWith(".properties")  )
                factory = new org.apache.logging.log4j.core.config.properties.PropertiesConfigurationFactory();
            else
                factory =  org.apache.logging.log4j.core.config.ConfigurationFactory.getInstance();

            org.apache.logging.log4j.core.config.Configuration configuration = factory.getConfiguration(null, source);
            org.apache.logging.log4j.core.config.Configurator.initialize(configuration);

//            // Guess work. Needs testing.
//            // File extension for the syntax.
//            org.apache.logging.log4j.core.config.ConfigurationSource source = new ConfigurationSource(inputStream, new File("input.properties"));
//            org.apache.logging.log4j.core.config.Configuration config =
//                org.apache.logging.log4j.core.config.ConfigurationFactory.getInstance().getConfiguration(null, source);
//            org.apache.logging.log4j.core.config.Configurator.initialize(config);
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
            try {
                org.apache.log4j.Level level = org.apache.log4j.Level.ALL;
                if ( levelName.equalsIgnoreCase("info") )
                    level = org.apache.log4j.Level.INFO;
                else if ( levelName.equalsIgnoreCase("debug") )
                    level = org.apache.log4j.Level.DEBUG;
                else if ( levelName.equalsIgnoreCase("trace") )
                    level = org.apache.log4j.Level.TRACE;
                else if ( levelName.equalsIgnoreCase("warn") || levelName.equalsIgnoreCase("warning") )
                    level = org.apache.log4j.Level.WARN;
                else if ( levelName.equalsIgnoreCase("error") )
                    level = org.apache.log4j.Level.ERROR;
                else if ( levelName.equalsIgnoreCase("OFF") )
                    level = org.apache.log4j.Level.OFF;
                if ( level != null )
                    org.apache.log4j.LogManager.getLogger(logger).setLevel(level);
            }
            catch (NoClassDefFoundError ex) {
                if ( ! log4j2MsgLoggedOnce ) {
                    logAlways("NoClassDefFoundError (log4j2)");
                    log4j2MsgLoggedOnce = true;
                }
            }
        }
    }

    /** Log4j1 setup */
    static class LoggingSetupLog4j1 extends LoggingSetup {

        @Override
        protected String getDisplayName() {
            return "Log4j1";
        }

        @Override
        protected void initFromInputStream(InputStream inputStream, String name) throws IOException {
            org.apache.log4j.PropertyConfigurator.configure(inputStream);
        }

        @Override
        protected String[] getLoggingSetupFilenames() {
            return new String[] {"log4j.properties"};
        }

        @Override
        protected String getSystemProperty() {
            return "log4j.configuration";
        }

        @Override
        protected String getDefaultString() {
            return LoggingDefaults.defaultLog4j1;
        }

        private boolean log4j1MsgLoggedOnce = false;
        @Override
        public void setLevel(String logger, String levelName) {
            try {
                org.apache.log4j.Level level = org.apache.log4j.Level.ALL;
                if ( levelName.equalsIgnoreCase("info") )
                    level = org.apache.log4j.Level.INFO;
                else if ( levelName.equalsIgnoreCase("debug") )
                    level = org.apache.log4j.Level.DEBUG;
                else if ( levelName.equalsIgnoreCase("trace") )
                    level = org.apache.log4j.Level.TRACE;
                else if ( levelName.equalsIgnoreCase("warn") || levelName.equalsIgnoreCase("warning") )
                    level = org.apache.log4j.Level.WARN;
                else if ( levelName.equalsIgnoreCase("error") )
                    level = org.apache.log4j.Level.ERROR;
                else if ( levelName.equalsIgnoreCase("OFF") )
                    level = org.apache.log4j.Level.OFF;
                if ( level != null )
                    org.apache.log4j.LogManager.getLogger(logger).setLevel(level);
            }
            catch (NoClassDefFoundError ex) {
                if ( ! log4j1MsgLoggedOnce ) {
                    logAlways("NoClassDefFoundError (log4j)");
                    log4j1MsgLoggedOnce = true;
                }
            }
        }
    }

    /** java.util.logging (JUL) setup */
    static class LoggingSetupJUL extends LoggingSetup {

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
            if ( levelName.equalsIgnoreCase("info") )
                level = java.util.logging.Level.INFO;
            else if ( levelName.equalsIgnoreCase("trace") )
                level = java.util.logging.Level.FINER;
            else if ( levelName.equalsIgnoreCase("debug") )
                level = java.util.logging.Level.FINE;
            else if ( levelName.equalsIgnoreCase("warn") || levelName.equalsIgnoreCase("warning") )
                level = java.util.logging.Level.WARNING;
            else if ( levelName.equalsIgnoreCase("error") )
                level = java.util.logging.Level.SEVERE;
            else if ( levelName.equalsIgnoreCase("OFF") )
                level = java.util.logging.Level.OFF;
            if ( level != null )
                java.util.logging.Logger.getLogger(logger).setLevel(level);
        }
    }

    /** Setup for when we don't do anything */
    static class LoggingSetupNoOp extends LoggingSetup {
        @Override
        protected String getDisplayName() {
            return "NoOp";
        }

        @Override
        protected String[] getLoggingSetupFilenames() {
            return new String[0];
        }

        @Override
        protected String getSystemProperty() {
            return null;
        }

        @Override
        protected String getDefaultString() {
            return null;
        }

        @Override
        protected void initFromInputStream(InputStream inputStream, String name) throws IOException {}

        @Override
        public void setLevel(String logger, String level) {}
    }
}

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

import static logging.LoggingSystem.logLogging;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import logging.LoggingSystem;

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
            if ( LoggingSystem.getPathBase() != null && tryClassPathFor(LoggingSystem.getPathBase() + fn) )
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
        try (InputStream input = new ByteArrayInputStream(b)) {
            // null indicates the format of the built-in default.
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
            LoggingSystem.logLogging("%s = %s", propName, x);
            return true;
        }
        return false;
    }

    /* package */ static void exception(IOException ex) {
        throw new RuntimeException(ex);
    }

    /* package */ static byte[] asUTF8bytes(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }
    
//    // slf4j v1.7.0
//    // slf4j 1.8.x uses ServiceLoader
//
//    // Need both "org.slf4j.impl.Log4jLoggerAdapater" and "org.apache.log4j.Logger"
//    private static boolean checkForLog4J1() {
//        boolean bLog4j = checkForClass("org.apache.log4j.Logger");
//        boolean bLog4jBinding = checkForClass("org.slf4j.impl.Log4jLoggerAdapter");
//        if ( bLog4j && bLog4jBinding )
//            return true;
//        if ( !bLog4j && !bLog4jBinding )
//            return false;
//        if ( !bLog4j ) {
//            logAlways("Classpath has the slf4j-log4j binding but not log4j1");
//            return false;
//        }
//        if ( !bLog4jBinding ) {
//            logAlways("Classpath has log4j1 but not the slf4j-log4j binding");
//            return false;
//        }
//        return false;
//    }
//
//    // Need both "org.slf4j.impl.Log4jLoggerAdapater" and "org.apache.logging.log4j.Logger"
//    private static boolean checkForLog4J2() {
//        boolean bLog4j = checkForClass("org.apache.logging.log4j.Logger");
//        boolean bLog4jBinding = checkForClass("org.apache.logging.slf4j.Log4jLoggerFactory");
//        if ( bLog4j && bLog4jBinding )
//            return true;
//        if ( !bLog4j && !bLog4jBinding )
//            return false;
//        if ( !bLog4j ) {
//            logAlways("Classpath has the log4j-slf4j-impl bridge but not log4j2");
//            return false;
//        }
//        if ( !bLog4jBinding ) {
//            logAlways("Classpath has log4j2 but not the log4j-slf4j-impl bridge");
//            return false;
//        }
//        return false;
//    }
//
//    private static boolean checkForJUL() {
//        return checkForClass("org.slf4j.impl.JDK14LoggerAdapter");
//    }
//
//    private static boolean checkForSimple() {
//        return checkForClass("org.slf4j.impl.SimpleLogger");
//    }
//
//    private static boolean checkForFmtSimple() {
//        return checkForClass("logging.impl.FmtSimpleFactory");
//    }
//
//    private static boolean checkForClass(String className) {
//        try {
//            Class.forName(className);
//            return true;
//        }
//        catch (ClassNotFoundException ex) {
//            return false;
//        }
//    }
//
//    /** Log for the logging setup process */
//    protected static void logLogging(String fmt, Object... args) {
//        if ( logLogging ) {
//            logAlways(fmt, args);
//        }
//    }
//
//    private static PrintStream logLogger = System.err;
//
//    /** Log for the logging setup messages like warnings. */
//    protected static void logAlways(String fmt, Object... args) {
//        fmt = "[Logging setup] "+fmt;
//        if ( ! fmt.endsWith("\n") )
//            fmt = fmt+"\n";
//        // Do as one call so that is does not get broken up.
//        logLogger.printf(fmt, args);
//    }

    /**
     * Return a array of files names / resource names to try.
     * Do not return null.
     */
    protected abstract String[] getLoggingSetupFilenames();

    protected abstract String getSystemProperty();

    protected abstract String getDefaultString();

    /** Configure logging from an input stream.
     *  Name is the file read, or null for the logging systems default built-in string format. */
    protected abstract void initFromInputStream(InputStream inputStream, String name) throws IOException;

    protected abstract String getDisplayName();

    public abstract void setLevel(String logger, String level);
}

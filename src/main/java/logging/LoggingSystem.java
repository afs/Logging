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

import java.io.PrintStream;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import logging.setup.*;
import org.slf4j.spi.SLF4JServiceProvider;

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
 * LoggingSystem.setLogging();</pre>
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
 * LoggingSystem.allowLoggingReset(true);
 * LoggingSystem.setLogging();</pre>
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

public class LoggingSystem {
    // <<<< Log logging

    private static boolean logLogging         = false;
    private static PrintStream logLogger = System.err;

    /** Set whether to log the logging setup. */
    public static void logLoggingSetup(boolean logSetup) {
        logLogging = logSetup;
    }

    /** Log for the logging setup process */
    public static void logLogging(String fmt, Object... args) {
        if ( logLogging ) {
            logAlways(fmt, args);
        }
    }

    /** Log for the logging setup messages like warnings. */
    public static void logAlways(String fmt, Object... args) {
        fmt = "[Logging setup] "+fmt;
        if ( ! fmt.endsWith("\n") )
            fmt = fmt+"\n";
        // Do as one call so that is does not get broken up.
        logLogger.printf(fmt, args);
    }

    // >>>> Log logging

    private static boolean allowLoggingReset  = true;

    private static Object lock = new Object();
    private static volatile boolean loggingInitialized = false;

    /**
     * Switch off logging setting. Used so that an application's
     * logging setup is not overwritten.
     */
    public static synchronized void allowLoggingReset(boolean value) {
        allowLoggingReset = value;
        loggingInitialized = false;
    }

    // Classpath resource to look in for logging implementation configuration files
    // (log4j.properties, log4j2.properties, logging.properties etc).
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

    public static LoggingSetup config() {
        setLogging();
        return theLoggingSetup;
    }

    /** Setup logging.
     * This is the one call needed.
     * <pre>
     * static { LoggingSetup.setLogging(); }</pre>
     * and logging will initialize.
     *
     * This call can be called multiple time and it is thread safe.
     */
    public static void setLogging() {
        if ( loggingInitialized )
            return;
        synchronized(lock) {
            setLoggingInternal();
        }
    }

    private static void setLoggingInternal() {
        setLoggingInternal_slf4j20();
        //setLoggingInternal_slf4j17();
    }

    // ==== Support for slf4j 1.8

    private static void setLoggingInternal_slf4j20() {
        if ( loggingInitialized )
            return;
        if ( !allowLoggingReset )
            return;
        loggingInitialized = true;
        allowLoggingReset = false;

        List<ServiceLoader.Provider<SLF4JServiceProvider>> providers = search();

        // Discover the binding for logging
        if ( checkForSimple(providers) ) {
            // No specific setup.
            logLogging("slf4j-simple logging found");
            return;
        }

        // Local: like slf4j simple but with java-style formatting
        boolean hasFmtSimple = checkForFmtSimple(providers);

        boolean hasLog4j1 = checkForLog4J1(providers);
        boolean hasLog4j2 = checkForLog4J2(providers);
        boolean hasJUL = checkForJUL(providers);

        if ( !hasLog4j1 && !hasLog4j2 && !hasJUL ) {
            // Do nothing - hope logging gets initialized automatically. e.g. logback.
            // In some ways this is the preferred outcome for the war file.
            logLogging("None of Log4j1, Log4j2 nor JUL setup for slf4j");
            return;
        }

        if ( hasLog4j1 && hasLog4j2 && hasJUL ) {
            logAlways("Found Log4j1, Log4j2 and JUL setups for slf4j; using Log4j2");
            hasJUL = false;
            hasLog4j1 = false;
        } else if ( hasLog4j2 && hasJUL ) {
            logAlways("Found both Log4j2 and JUL setups for slf4j; using Log4j2");
            hasJUL = false;
        } else if ( hasLog4j1 && hasJUL ) {
            logAlways("Found both Log4j1 and JUL setups for slf4j; using Log4j1");
            hasJUL = false;
        } else if ( hasLog4j1 && hasLog4j2 ) {
            logAlways("Found both Log4j1 and Log4j2 setup for slf4j; using Log4j2");
            hasLog4j1 = false;
        }

        LoggingSetup loggingSetup = null;
        if ( hasLog4j1 ) {
            //loggingSetup = new LoggingSetupLog4j1();
            throw new RuntimeException("log4j v1 is not secure. No longer supported");
        }
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

    // slf4j 1.8.x uses ServiceLoader
    // Support for slf4j v1.8.x, v2.0.x.
    // slf4j uses java.util.ServiceLoader for "org.slf4j.spi.SLF4JServiceProvider"


    private static List<ServiceLoader.Provider<SLF4JServiceProvider>> search() {
        ServiceLoader<SLF4JServiceProvider> sl =
            ServiceLoader.load(SLF4JServiceProvider.class, LoggingSystem.class.getClassLoader()) ;
        List<ServiceLoader.Provider<org.slf4j.spi.SLF4JServiceProvider>> x = sl.stream().collect(Collectors.toList());
        return x;
    }

    private static boolean checkForClassServiceLoader(List<ServiceLoader.Provider<SLF4JServiceProvider>> providers, String clsName) {
        for ( ServiceLoader.Provider<SLF4JServiceProvider> p : providers ) {
            String providerClsName = p.get().getClass().getName();
            if ( providerClsName.equals(clsName) )
                return true;
        }
        return false;
    }

    private static boolean checkForSimple(List<ServiceLoader.Provider<SLF4JServiceProvider>> providers) {
        return checkForClassServiceLoader(providers, "org.slf4j.impl.SimpleLogger");
    }

    private static boolean checkForFmtSimple(List<ServiceLoader.Provider<SLF4JServiceProvider>> providers) {
        return checkForClassServiceLoader(providers, "logging.impl.FmtSimpleFactory");
    }
    private static boolean checkForLog4J2(List<ServiceLoader.Provider<SLF4JServiceProvider>> providers) {
        boolean bLog4j = checkForClassServiceLoader(providers, "org.apache.logging.log4j.Logger");
        boolean bLog4jBinding = checkForClassServiceLoader(providers, "org.apache.logging.slf4j.Log4jLoggerFactory");
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

    private static boolean checkForLog4J1(List<ServiceLoader.Provider<SLF4JServiceProvider>> providers) {
        boolean bLog4j = checkForClassServiceLoader(providers, "org.apache.log4j.Logger");
        boolean bLog4jBinding = checkForClassServiceLoader(providers, "org.slf4j.impl.Log4jLoggerAdapter");
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

    private static boolean checkForJUL(List<ServiceLoader.Provider<SLF4JServiceProvider>> providers) {
        return checkForClass("org.slf4j.impl.JDK14LoggerAdapter");
    }

    // ==== Support for slf4j v1.7.0

    private static void setLoggingInternal_slf4j17() {
        if ( loggingInitialized )
            return;
        if ( !allowLoggingReset )
            return;
        loggingInitialized = true;
        allowLoggingReset = false;

        // Discover the binding for logging
        if ( checkForSimple_slf4j17() ) {
            // No specific setup.
            logLogging("slf4j-simple logging found");
            return;
        }


        // Local: like slf4j simple but with java-style formatting
        boolean hasFmtSimple = checkForFmtSimple_slf4j17();

        boolean hasLog4j1 = checkForLog4J1_slf4j17();
        boolean hasLog4j2 = checkForLog4J2_slf4j17();
        boolean hasJUL = checkForJUL_slf4j17();

        if ( !hasLog4j1 && !hasLog4j2 && !hasJUL ) {
            // Do nothing - hope logging gets initialized automatically. e.g. logback.
            // In some ways this is the preferred outcome for the war file.
            logLogging("None of Log4j1, Log4j2 nor JUL setup for slf4j");
            return;
        }

        if ( hasLog4j1 && hasLog4j2 && hasJUL ) {
            logAlways("Found Log4j1, Log4j2 and JUL setups for slf4j; using Log4j2");
            hasJUL = false;
            hasLog4j1 = false;
        } else if ( hasLog4j2 && hasJUL ) {
            logAlways("Found both Log4j2 and JUL setups for slf4j; using Log4j2");
            hasJUL = false;
        } else if ( hasLog4j1 && hasJUL ) {
            logAlways("Found both Log4j1 and JUL setups for slf4j; using Log4j1");
            hasJUL = false;
        } else if ( hasLog4j1 && hasLog4j2 ) {
            logAlways("Found both Log4j1 and Log4j2 setup for slf4j; using Log4j2");
            hasLog4j1 = false;
        }

        LoggingSetup loggingSetup = null;
//        if ( hasLog4j1 )
//            loggingSetup = new LoggingSetupLog4j1();
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


    //private static final boolean hasLog4j1 = checkForClass("org.apache.log4j.Level");
    //private static final boolean hasLog4j2 = checkForClass("org.apache.logging.log4j.Level");

    // Need both "org.slf4j.impl.Log4jLoggerAdapater" and "org.apache.log4j.Logger"
    private static boolean checkForLog4J1_slf4j17() {
        // Checks for both jars - "log4j:log4j" and "org.slf4j:slf4j-log4j12"
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
    private static boolean checkForLog4J2_slf4j17() {
        // Checks for both jars - "org.apache.logging.log4j-core" and "org.apache.logging.log4j:log4j-slf4j-impl".
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

    private static boolean checkForJUL_slf4j17() {
        return checkForClass("org.slf4j.impl.JDK14LoggerAdapter");
    }

    private static boolean checkForSimple_slf4j17() {
        return checkForClass("org.slf4j.impl.SimpleLogger");
    }

    private static boolean checkForFmtSimple_slf4j17() {
        return checkForClass("logging.impl.FmtSimpleFactory");
    }

    /** Check for a class on the classpath */
    private static boolean checkForClass(String className) {
        try {
            Class.forName(className);
            return true;
        }
        catch (ClassNotFoundException ex) {
            return false;
        }
    }
}

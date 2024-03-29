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

package logging.dev;

import logging.LoggingSystem;
import logging.setup.LoggingSetupJUL;

public class DevLogging {

    /*
     * Provided:
     *     Better diagnostics
     *     "Hunt the setup file", not -D
     *     Built-in defaults.
     *
     * Is this slf4j 1.8 (ServiceLoader) dependent? Split out.
     *    Two+ maven modules for testing.
     */

    //** Init - not getDefaultString - just call an "default init"

    //    Syslog protocol is described in
    //
    //    RFC 3164 - The BSD Syslog Protocol
    // JUL -> syslog http://rusv.github.io/agafua-syslog/

    // JUL: Note in logging.properties the ".level" affects the loggers.
    // Set and reset : set once

    // Redesign : test if initialization alredy built in.

//    static {
//        final StaticLoggerBinder binder = StaticLoggerBinder.getSingleton();
//        final String clsName = binder.getLoggerFactoryClassStr();
//        if ( clsName.contains("JDK14LoggerFactory") )
//           ; // LogCtl.setJavaLoggingDft();
//        else if ( clsName.contains("Log4jLoggerFactory") )
//           ; //LogCtl.setLog4j();
//    }

    /* slf4j notes:
     * Initialization is done
     *    org.slf4j.LoggerFacory.preformInitialization->bind->findPossibleStaticLoggerBinderPathSet
     *    StaticLoggerBinder.getSingleton();
     *
     * slf4j 1.8.0 uses ServiceLoader.
     * + JPMS modules
     */

    public static void main(String...a) {
        // Needs work
        // Defaulting to built in is not working.

        LoggingSystem.logLoggingSetup(true);
        LoggingSystem.setLogging();
        org.slf4j.Logger x = org.slf4j.LoggerFactory.getLogger("SLF4J_1");
        x.info("Info1");
        System.err.flush();
        System.out.flush();
        LoggingSystem.allowLoggingReset(true);
        System.err.println("Reset logging");
        LoggingSystem.setLogging();
        x = org.slf4j.LoggerFactory.getLogger("SLF4J_2");
        x.info("Info2");

        java.util.logging.Logger LOG0 = java.util.logging.Logger.getLogger("JUL0");
        LOG0.info("Information:JUL");

        System.exit(0);

        //LoggingSetup.logSetup(true);
//          org.slf4j.Logger x = org.slf4j.LoggerFactory.getLogger("FOO");
//          // org.slf4j.impl.Log4jLoggerAdapater or org.slf4j.impl.JDK14LoggerAdapter
//          // Except too late!

        LoggingSystem.logLoggingSetup(true);
        LoggingSystem.setLogging();
        // Some way to say "if both, use JUL" (dft is log4j)


//        System.out.println("stdout");System.out.flush();
//        System.err.println("stderr");System.err.flush();

        LoggingSystem.allowLoggingReset(true);
        // Dev - direct to JUL.
        if ( true ) {
            System.err.println("Reset - JUL");
            // Re-initializing JUL does not work - in fact, it turns everything off.
            new LoggingSetupJUL()
                .setup();
            java.util.logging.Logger LOG = java.util.logging.Logger.getLogger("JUL");
            LOG.info("Information:JUL");
            System.err.flush();
            System.out.flush();
        }

        org.slf4j.Logger x1 = org.slf4j.LoggerFactory.getLogger("LOGGER1");
        x1.info("Info2");
        System.err.flush();
        System.out.flush();
    }

    private static void log4j2() {
        System.setProperty("log4j.configurationFile", "log4j2.properties");
        org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(DevLogging.class);
        org.apache.logging.log4j.Logger LOG1 = org.apache.logging.log4j.LogManager.getLogger("org.apache.jena");
        LOG.info("Message log4j2");
        LOG.debug("Message log4j2");

        LOG1.info("Message log4j2");
        LOG1.debug("Message log4j2");

//        LOG.error("Message");
//        LOG.fatal("Message");
    }
}

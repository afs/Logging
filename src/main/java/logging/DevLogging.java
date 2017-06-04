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

import logging.LoggingSetup.LoggingSetupLog4j;
import org.slf4j.impl.StaticLoggerBinder;

public class DevLogging {

    // Logging defaults : copy over Jena atlas base code [DONE]
    
    // Example / current jena
    static { 
        final StaticLoggerBinder binder = StaticLoggerBinder.getSingleton();
        final String clsName = binder.getLoggerFactoryClassStr();
        if ( clsName.contains("JDK14LoggerFactory") )
            ; // LogCtl.setJavaLoggingDft(); 
        else if ( clsName.contains("Log4jLoggerFactory") )
            ; //LogCtl.setLog4j(); 
    }
    
    // Migrate and clean up:
    
//    public static void main(String...a) {
//        org.slf4j.Logger x = org.slf4j.LoggerFactory.getLogger("LOGGER") ;
//        x.info("Info");
//    }
    
    public static void main(String...a) {
        //LoggingSetup.logSetup(true);
//          org.slf4j.Logger x = org.slf4j.LoggerFactory.getLogger("FOO") ;
//          // org.slf4j.impl.Log4jLoggerAdapater or org.slf4j.impl.JDK14LoggerAdapter
//          // Except too late!

        LoggingSetup.setLogging() ;
        org.slf4j.Logger x = org.slf4j.LoggerFactory.getLogger("LOGGER") ;
        x.info("Info");
        
        //LoggingSetup.setLogging() ;
        // Dev - direct to JUL.
        if ( true ) {
            //new LoggingSetupJUL().setup();
            java.util.logging.Logger LOG = java.util.logging.Logger.getLogger("LOGGER") ;
            LOG.info("Information1:JUL");
        }
        // Dev - direct to Log4j.
        if ( false ) {
            new LoggingSetupLog4j().setup();
            org.apache.log4j.Logger LOG1 = org.apache.log4j.Logger.getLogger("LOGGER") ;
            LOG1.info("Information2:L4J");
        }
    }
}

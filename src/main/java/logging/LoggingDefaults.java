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

import logging.jul.ConsoleHandlerStream;

/** 
 * Default settings for logging (JUL, Log4j v1).
 * Better to set explicitly. 
 */
public class LoggingDefaults {
    /** Default logging setup when using log4j */ 
    public static final String defaultLog4j = String.join("\n", 
          "## Plain output with level, to stderr"
          ,"log4j.appender.plain=org.apache.log4j.ConsoleAppender"
          ,"log4j.appender.plain.target=System.out"
          ,"log4j.appender.plain.layout=org.apache.log4j.PatternLayout"
          ,"log4j.appender.plain.layout.ConversionPattern=%d{HH:mm:ss} %-5p %-20c{1} :: %m%n"
          ,"## Everything"
          ,"log4j.rootLogger=INFO, plain"
           );
    
    private static final String nameConsoleHandlerStream = ConsoleHandlerStream.class.getName();
    
    /** Default logging setup when using java.util.logging (JUL) */
    public static final String defaultJUL = String.join
        ("\n"
        ,"handlers="+nameConsoleHandlerStream
        // These are the defaults in ConsoleHandlerStream
        //,"logging.jul.ConsoleHandlerStream.level=INFO"
        //,"logging.jul.ConsoleHandlerStream.formatter=logging.jul.TextFormatter"
        //,"logging.jul.TextFormatter.format=%5$tT %3$-5s %2$-20s :: %6$s"
        );
}

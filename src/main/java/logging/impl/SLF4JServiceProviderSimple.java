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

package logging.impl;

import logging.LoggingSystem;
import logging.impl.slf4j20.FmtSimpleFactorySLF4J2;
import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.helpers.NOPMDCAdapter;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

/** For SLF4J 2.x */
public class SLF4JServiceProviderSimple implements SLF4JServiceProvider {

    private ILoggerFactory loggerFactory = new FmtSimpleFactorySLF4J2();

    private IMarkerFactory markerFactory =  new BasicMarkerFactory();
    private MDCAdapter mdcAdapter =  new NOPMDCAdapter();

    @Override
    public ILoggerFactory getLoggerFactory() {

        return loggerFactory;
    }

    @Override
    public IMarkerFactory getMarkerFactory() {
        return markerFactory;
    }

    @Override
    public MDCAdapter getMDCAdapter() {
        return mdcAdapter;
    }

    @Override
    public void initialize() {
        LoggingSystem.logLogging("Load SLF4JServiceProviderSimple");
    }

    @Override
    public String getRequestedApiVersion() {
        return null;
    }

    // slf4j will print if it finds multiple providers.
    // Leave to the default so all output looks similar.
//    @Override
//    public String toString() {
//        return this.getClass().getName();
//    }
}

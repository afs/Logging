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

package org.slf4j.impl;

import logging.impl.slf4j17.FmtSimpleFactorySLF4J17;
import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

/**
 * A SLF4J {@linkplain LoggerFactoryBinder} that to log4j if possible else JUL.
 */
@SuppressWarnings("deprecation")
public class StaticLoggerBinderAuto implements LoggerFactoryBinder {
    // Rename to activate.
    private static StaticLoggerBinderAuto SINGLETON = new StaticLoggerBinderAuto();

    public static final StaticLoggerBinderAuto getSingleton() {
        return SINGLETON;
    }

    private void decide() {

    }

    // SLF4J checking mechanism.
//    // to avoid constant folding by the compiler, this field must *not* be final
//    public static String REQUESTED_API_VERSION = "1.7.0"; // !final

    @Override
    public ILoggerFactory getLoggerFactory() {
        return new FmtSimpleFactorySLF4J17();
    }

    @Override
    public String getLoggerFactoryClassStr() {
        return FmtSimpleFactorySLF4J17.class.getName();
    }
}

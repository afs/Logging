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

package logging.impl.slf4j17;

import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

// Must be in org.slf4j.impl. to work (slf4j 1.7.x; slf4j 1.8.x uses ServiceLoader<SLF4JServiceProvider>)
@SuppressWarnings("deprecation")
public class StaticLoggerBinder implements LoggerFactoryBinder {

  private static StaticLoggerBinder SINGLETON = new StaticLoggerBinder();

  public static final StaticLoggerBinder getSingleton() {
      return SINGLETON;
  }

  @Override
  public ILoggerFactory getLoggerFactory() {
      return new FmtSimpleFactorySLF4J17();
  }

  @Override
  public String getLoggerFactoryClassStr() {
      return FmtSimpleFactorySLF4J17.class.getName();
  }
}
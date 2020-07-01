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

/** Setup for when we don't do anything */
public class LoggingSetupNoOp extends LoggingSetup {
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
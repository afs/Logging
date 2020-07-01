/*
 * Licensed to the Apache Software Foundation (ASF) under onef;
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

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

public class DevLog4j2 {
    public static void main(String...a) throws IOException {
//        InputStream in = new FileInputStream("x-log4j2.yaml");
//        ConfigurationSource source = new ConfigurationSource(in);
//        ConfigurationFactory factory = new YamlConfigurationFactory();
//        Configuration configuration = factory.getConfiguration(null, source);
//        Configurator.initialize(configuration);

        Configurator.initialize(null, "log4j2.xml");

        Logger LOG = LogManager.getLogger(DevLog4j2.class);
        LOG.info("Message");
    }
}

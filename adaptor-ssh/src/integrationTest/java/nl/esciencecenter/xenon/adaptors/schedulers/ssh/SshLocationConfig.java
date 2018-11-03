/*
 * Copyright 2013 Netherlands eScience Center
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.esciencecenter.xenon.adaptors.schedulers.ssh;

import nl.esciencecenter.xenon.adaptors.schedulers.SchedulerLocationConfig;

public class SshLocationConfig extends SchedulerLocationConfig {

    public SshLocationConfig(String location, String workdir) {
        super(location, workdir, new String[] { "single", "multi", "unlimited" }, "single");
    }

    @Override
    public boolean supportsInteractive() {
        return true;
    }

    @Override
    public boolean isEmbedded() {
        return true;
    }
}

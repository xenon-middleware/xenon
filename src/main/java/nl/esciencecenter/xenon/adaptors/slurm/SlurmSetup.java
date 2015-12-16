/**
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
package nl.esciencecenter.xenon.adaptors.slurm;

import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.IncompatibleVersionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version 1.0
 * @since 1.0
 */
public class SlurmSetup {

    private static final Logger LOGGER = LoggerFactory.getLogger(SlurmSetup.class);

    private static final String[] SUPPORTED_VERSIONS = { "2.3.", "2.5.", "2.6.", "14.03.0", "14.11.9-Bull.1.0"};

    private final boolean accountingAvailable;
    private final String version;

    SlurmSetup(Map<String, String> info, boolean ignoreVersion, boolean disableAccounting) throws XenonException {
        version = info.get("SLURM_VERSION");

        if (version == null) {
            throw new XenonException(SlurmAdaptor.ADAPTOR_NAME, "Slurm config does not contain version info");
        }

        checkVersion(ignoreVersion);

        String accountingType = info.get("AccountingStorageType");

        if (accountingType == null) {
            throw new XenonException(SlurmAdaptor.ADAPTOR_NAME, "Slurm config does not contain expected accounting info");
        }

        accountingAvailable = !(accountingType.equals("accounting_storage/none") || disableAccounting);

        LOGGER.debug("Created new SlurmConfig. version = \"{}\", accounting available: {}", version, accountingAvailable);
    }

    private void checkVersion(boolean ignoreVersion) throws IncompatibleVersionException {
        for (String supportedVersion : SUPPORTED_VERSIONS) {
            if (version.startsWith(supportedVersion)) {
                return;
            }
        }
        if (ignoreVersion) {
            LOGGER.warn("Slurm version {} not supported by Slurm Adaptor. Ignoring as requested by {} property", version,
                    SlurmAdaptor.IGNORE_VERSION_PROPERTY);
        } else {
            throw new IncompatibleVersionException(SlurmAdaptor.ADAPTOR_NAME, "Slurm version " + version
                    + " not supported by Slurm Adaptor. Set " + SlurmAdaptor.IGNORE_VERSION_PROPERTY + " in the properties passed to xenon.jobs().newScheduler() to ignore");
        }
    }

    public boolean accountingAvailable() {
        return accountingAvailable;
    }
}

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
package nl.esciencecenter.octopus.adaptors.slurm;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.octopus.exceptions.IncompatibleVersionException;
import nl.esciencecenter.octopus.exceptions.OctopusException;

/**
 * @author Niels Drost <N.Drost@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public class SlurmConfig {

    private static final Logger logger = LoggerFactory.getLogger(SlurmConfig.class);

    private static final String[] supportedVersions = { "2.3", "2.5" };

    private final boolean accountingAvailable;
    private final String version;

    SlurmConfig(Map<String, String> info, boolean ignoreVersion) throws OctopusException {
        version = info.get("SLURM_VERSION");

        if (version == null) {
            throw new OctopusException(SlurmAdaptor.ADAPTOR_NAME, "Slurm config does not contain version info");
        }

        checkVersion(ignoreVersion);

        String accountingType = info.get("AccountingStorageType");

        if (accountingType == null) {
            throw new OctopusException(SlurmAdaptor.ADAPTOR_NAME, "Slurm config does not contain expected accounting info");
        }
        accountingAvailable = !accountingType.equals("accounting_storage/none");

        logger.debug("Created new SlurmConfig. version = \"{}\", accounting available: {}", version, accountingAvailable);
    }

    private void checkVersion(boolean throwException) throws IncompatibleVersionException {
        for (String supportedVersion : supportedVersions) {
            if (version.startsWith(supportedVersion)) {
                return;
            }
        }
        if (throwException) {
            throw new IncompatibleVersionException(SlurmAdaptor.ADAPTOR_NAME, "Slurm version " + version
                    + " not supported by Slurm Adaptor. Set " + SlurmAdaptor.IGNORE_VERSION_PROPERTY + "to ignore");
        } else {
            logger.warn("Slurm version {} not supported by Slurm Adaptor. Ignoring as requested by {} property", version,
                    SlurmAdaptor.IGNORE_VERSION_PROPERTY);
        }
    }

    boolean accountingAvailable() {
        return accountingAvailable;
    }
}

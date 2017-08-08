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
package nl.esciencecenter.xenon.adaptors.schedulers.slurm;

import static nl.esciencecenter.xenon.adaptors.schedulers.slurm.SlurmSchedulerAdaptor.ADAPTOR_NAME;
import static nl.esciencecenter.xenon.adaptors.schedulers.slurm.SlurmSchedulerAdaptor.SUPPORTED_VERSIONS;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.schedulers.IncompatibleVersionException;

/**
 * @version 1.0
 * @since 1.0
 */
public class SlurmSetup {

    private static final Logger LOGGER = LoggerFactory.getLogger(SlurmSetup.class);

    private final boolean accountingAvailable;
    private final String version;

    SlurmSetup(Map<String, String> info, boolean disableAccounting) throws XenonException {
        version = info.get("SLURM_VERSION");

        if (version == null) {
            throw new XenonException(ADAPTOR_NAME, "Slurm config does not contain version info");
        }

        if (!checkVersion()) {
            // Unsupported version, so print warning
            LOGGER.warn("Slurm version {} not officially supported by Slurm Adaptor.", version);
        }

        String accountingType = info.get("AccountingStorageType");

        if (accountingType == null) {
            throw new XenonException(ADAPTOR_NAME, "Slurm config does not contain expected accounting info");
        }

        accountingAvailable = !(accountingType.equals("accounting_storage/none") || disableAccounting);

        LOGGER.debug("Created new SlurmConfig. version = \"{}\", accounting available: {}", version, accountingAvailable);
    }

    public boolean checkVersion() throws IncompatibleVersionException {
        for (String supportedVersion : SUPPORTED_VERSIONS) {
            if (version.startsWith(supportedVersion)) {
                return true;
            }
        }

        return false;
    }

    public boolean accountingAvailable() {
        return accountingAvailable;
    }

    public String version() {
        return version;
    }

}

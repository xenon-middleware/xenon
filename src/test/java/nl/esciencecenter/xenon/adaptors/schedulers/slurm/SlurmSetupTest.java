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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import nl.esciencecenter.xenon.XenonException;

/**
 * 
 */
public class SlurmSetupTest {

    @Test
    public void test_validConfig() throws XenonException {

        //Relevant part of config used in current implementation
        Map<String, String> configInfo = new HashMap<>();
        configInfo.put("SLURM_VERSION", "2.3.4");
        configInfo.put("AccountingStorageType", "accounting_storage/filetxt");

        SlurmSetup config = new SlurmSetup(configInfo, false);

        assertTrue(config.accountingAvailable());
        assertEquals("2.3.4", config.version());
    }

    @Test
    public void test_versionNumberWithPostfix() throws XenonException {

        //Relevant part of config used in current implementation
        Map<String, String> configInfo = new HashMap<>();
        configInfo.put("SLURM_VERSION", "2.5.withsomerandomextraversioninfo");
        configInfo.put("AccountingStorageType", "accounting_storage/filetxt");

        SlurmSetup config = new SlurmSetup(configInfo, false);

        assertTrue(config.accountingAvailable());
    }

    @Test
    public void test_accountingDisabled() throws XenonException {
        //Relevant part of config used in current implementation
        Map<String, String> configInfo = new HashMap<>();
        configInfo.put("SLURM_VERSION", "2.3.4");
        configInfo.put("AccountingStorageType", "accounting_storage/none");

        SlurmSetup config = new SlurmSetup(configInfo, false);

        assertFalse(config.accountingAvailable());
    }

    @Test
    public void test_forcedAccountingDisabled() throws XenonException {
        //Relevant part of config used in current implementation
        Map<String, String> configInfo = new HashMap<>();
        configInfo.put("SLURM_VERSION", "2.3.4");
        configInfo.put("AccountingStorageType", "accounting_storage/fixetxt");

        SlurmSetup config = new SlurmSetup(configInfo, true);

        assertFalse(config.accountingAvailable());
    }

    @Test(expected = XenonException.class)
    public void test_noVersion_Exception() throws XenonException {
        //Relevant part of config used in current implementation
        Map<String, String> configInfo = new HashMap<>();
        configInfo.put("AccountingStorageType", "accounting_storage/filetxt");

        new SlurmSetup(configInfo, false);
    }

    @Test
    public void test_invalidVersion_Exception() throws XenonException {
        //Relevant part of config used in current implementation
        Map<String, String> configInfo = new HashMap<>();
        configInfo.put("SLURM_VERSION", "1.2.3");
        configInfo.put("AccountingStorageType", "accounting_storage/filetxt");

        SlurmSetup s = new SlurmSetup(configInfo, false);
        assertFalse(s.checkVersion());
    }

    @Test
    public void test_invalidVersionWithNoPeriod() throws XenonException {
        //Relevant part of config used in current implementation
        Map<String, String> configInfo = new HashMap<>();
        configInfo.put("SLURM_VERSION", "2.5thereisnoperiodhere");
        configInfo.put("AccountingStorageType", "accounting_storage/filetxt");

        SlurmSetup s = new SlurmSetup(configInfo, false);
        assertFalse(s.checkVersion());
    }

    @Test
    public void test_invalidVersion_Ignored() throws XenonException {
        //Relevant part of config used in current implementation
        Map<String, String> configInfo = new HashMap<>();
        configInfo.put("SLURM_VERSION", "1.2.3");
        configInfo.put("AccountingStorageType", "accounting_storage/filetxt");

        SlurmSetup config = new SlurmSetup(configInfo, false);

        //check if the rest of the config is actually done
        assertTrue(config.accountingAvailable());
    }

    @Test(expected = XenonException.class)
    public void test_noAccountingConfig_Exception() throws XenonException {
        //Relevant part of config used in current implementation
        Map<String, String> configInfo = new HashMap<>();
        configInfo.put("SLURM_VERSION", "2.3.4");

        new SlurmSetup(configInfo, false);
    }

}

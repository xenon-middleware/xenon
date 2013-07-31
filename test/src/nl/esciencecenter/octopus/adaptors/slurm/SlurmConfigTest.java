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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.octopus.exceptions.IncompatibleVersionException;
import nl.esciencecenter.octopus.exceptions.OctopusException;

import org.junit.Test;

/**
 * @author Niels Drost
 * 
 */
public class SlurmConfigTest {

    @Test
    public void test_validConfig() throws OctopusException {

        //Relevant part of config used in current implementation
        Map<String, String> configInfo = new HashMap<String, String>();
        configInfo.put("SLURM_VERSION", "2.3.4");
        configInfo.put("AccountingStorageType", "accounting_storage/filetxt");

        SlurmConfig config = new SlurmConfig(configInfo, false);

        assertTrue(config.accountingAvailable());
    }
    
    @Test
    public void test_versionNumberWithPostfix() throws OctopusException {

        //Relevant part of config used in current implementation
        Map<String, String> configInfo = new HashMap<String, String>();
        configInfo.put("SLURM_VERSION", "2.5.withsomerandomextraversioninfo");
        configInfo.put("AccountingStorageType", "accounting_storage/filetxt");

        SlurmConfig config = new SlurmConfig(configInfo, false);

        assertTrue(config.accountingAvailable());
    }

    @Test
    public void test_accountingDisabled() throws OctopusException {
        //Relevant part of config used in current implementation
        Map<String, String> configInfo = new HashMap<String, String>();
        configInfo.put("SLURM_VERSION", "2.3.4");
        configInfo.put("AccountingStorageType", "accounting_storage/none");

        SlurmConfig config = new SlurmConfig(configInfo, false);

        assertFalse(config.accountingAvailable());
    }

    @Test(expected = OctopusException.class)
    public void test_noVersion_Exception() throws OctopusException {
        //Relevant part of config used in current implementation
        Map<String, String> configInfo = new HashMap<String, String>();
        configInfo.put("AccountingStorageType", "accounting_storage/filetxt");

        new SlurmConfig(configInfo, false);
    }
    
    @Test(expected = IncompatibleVersionException.class)
    public void test_invalidVersion_Exception() throws OctopusException {
        //Relevant part of config used in current implementation
        Map<String, String> configInfo = new HashMap<String, String>();
        configInfo.put("SLURM_VERSION", "1.2.3");
        configInfo.put("AccountingStorageType", "accounting_storage/filetxt");

        new SlurmConfig(configInfo, false);
    }
    
    @Test(expected = IncompatibleVersionException.class)
    public void test_invalidVersionWithNoPeriod_Exception() throws OctopusException {
        //Relevant part of config used in current implementation
        Map<String, String> configInfo = new HashMap<String, String>();
        configInfo.put("SLURM_VERSION", "2.5thereisnoperiodhere");
        configInfo.put("AccountingStorageType", "accounting_storage/filetxt");

        new SlurmConfig(configInfo, false);
    }

    @Test
    public void test_invalidVersion_Ignored() throws OctopusException {
        //Relevant part of config used in current implementation
        Map<String, String> configInfo = new HashMap<String, String>();
        configInfo.put("SLURM_VERSION", "1.2.3");
        configInfo.put("AccountingStorageType", "accounting_storage/filetxt");

        SlurmConfig config = new SlurmConfig(configInfo, true);

        //check if the rest of the config is actually done
        assertTrue(config.accountingAvailable());
    }
    
    @Test(expected = OctopusException.class)
    public void test_noAccountingConfig_Exception() throws OctopusException {
        //Relevant part of config used in current implementation
        Map<String, String> configInfo = new HashMap<String, String>();
        configInfo.put("SLURM_VERSION", "2.3.4");

        new SlurmConfig(configInfo, false);
    }

    
    

}

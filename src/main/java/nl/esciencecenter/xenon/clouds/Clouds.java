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

package nl.esciencecenter.xenon.clouds;

import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.credentials.Credential;

/**
 * The Clouds API of Xenon.
 * 
 * This interface contains various methods for connecting to clouds, starting VMs, and retrieving information about
 * clouds and VMs.
 * 
 * @version 2.0
 * @since 2.0
 */
public interface Clouds {

    /**
     * Create a new Cloud that represents a connection to a cloud provider  
     * at the <code>location</code>, using the <code>scheme</code>
     * and <code>credentials</code> to get access. Make sure to always close 
     * {@code Cloud} instances by calling {@code close(Cloud)} when
     * you no longer need them, otherwise their associated resources remain 
     * allocated.
     * 
     * @param scheme
     *            the scheme used to access the Cloud.
     * @param location
     *            the location of the Cloud.
     * @param credential
     *            the Credentials to use to get access to the Cloud.
     * @param properties
     *            optional properties to configure the Cloud when it is created.
     * 
     * @return the new Cloud.
     * 
     * @throws UnknownPropertyException
     *             If a unknown property was provided.
     * @throws InvalidPropertyException
     *             If a known property was provided with an invalid value.
     * @throws InvalidLocationException
     *             If the location was invalid.
     * @throws InvalidCredentialException
     *             If the credential is invalid to access the location.
     * 
     * @throws XenonException
     *             If the creation of the Cloud failed.
     */
    Cloud newCloud(String scheme, String location, Credential credential, Map<String, String> properties) 
            throws XenonException;
    
    /**
     * Close a connection to a Cloud.
     * 
     * @param cloud
     *            the Cloud to close.
     * 
     * @throws NoSuchSchedulerException
     *             If the cloud is not known.
     * @throws XenonException
     *             If the cloud failed to close.
     */
    void close(Cloud cloud) throws XenonException;

    /**
     * Test if a connection to Cloud is open.
     * 
     * @param cloud
     *            the Cloud to test.
     * 
     * @throws XenonException
     *             If the test failed.
     * @throws XenonException
     *             If an I/O error occurred.
     */
    boolean isOpen(Cloud cloud) throws XenonException;
    
    /**
     * Create and launch a new <code>VirtualMachine</code> instance in the specified <code>Cloud</code>. 
     * 
     * @param cloud
     * @param description
     * @return the <code>VirtualMachine</code> that has been launched
     * @throws XenonException
     */
    VirtualMachine launchVirtualMachine(Cloud cloud, VirtualMachineDescription description) throws XenonException;

    
    /**
     * Create and launch a set of <code>VirtualMachine</code>s instances in the specified <code>Cloud</code>. 
     * 
     * @param cloud
     * @param description
     * @param count
     * @return an array of the <code>VirtualMachine</code> that has been launched
     * @throws XenonException
     */
    VirtualMachine [] launchVirtualMachines(Cloud cloud, VirtualMachineDescription description, int count) throws XenonException;
    
    /**
     * Suspend a running <code>VirtualMachine</code> instance. Once suspended, the instance is usually free of charge and some
     * (but not necessarily all) disk state is kept. The instance can be resumed later using <code>resumeVirtualMachine</code>.   
     * 
     * @param virtualMachine
     * @return the <code>VirtualMachineStatus</code> of the <code>VirtualMachine</code> 
     * @throws XenonException
     */
    VirtualMachineStatus startVirtualMachine(VirtualMachine virtualMachine) throws XenonException;
    
    /** 
     * Resume a <code>VirtualMachine</code> that was suspended earlier. It is very likely that the  <code>VirtualMachine</code>
     * will be resumed on a different physical host, getting a different IP address, etc. 
     * 
     * @param virtualMachine
     * @return the <code>VirtualMachineStatus</code> of the <code>VirtualMachine</code>
     * @throws XenonException
     */
    VirtualMachineStatus stopVirtualMachine(VirtualMachine virtualMachine) throws XenonException;
    
    /** 
     * Terminate a <code>VirtualMachine</code>, removing the instance and all data on disk is removed and cannot be restarted.
     * 
     * @param virtualMachine
     * @return the <code>VirtualMachineStatus</code> of the <code>VirtualMachine</code>
     * @throws XenonException
     */    
    VirtualMachineStatus terminateVirtualMachine(VirtualMachine virtualMachine) throws XenonException;

    /**
     * Reboot a <code>VirtualMachine</code>. Unlike suspend-resume, the <code>VirtualMachine</code> will remain on the same 
     * physical host, usually keeping its IP address, etc. 
     * 
     * @param virtualMachine
     * @return the <code>VirtualMachineStatus</code> of the <code>VirtualMachine</code>
     * @throws XenonException
     */    
    VirtualMachineStatus rebootVirtualMachine(VirtualMachine virtualMachine) throws XenonException;
    
    
    
    
    
    
    VirtualMachineStatus getVirtualMachineStatus(VirtualMachine virtualMachine) throws XenonException;
    
    VirtualMachineStatus [] getVirtualMachineStatuses(VirtualMachine... virtualMachines) throws XenonException;
    
    VirtualMachineStatus waitUntilRunning(VirtualMachine virtualMachine, long timeout) throws XenonException;
    
    VirtualMachineStatus waitUntilDone(VirtualMachine virtualMachine, long timeout) throws XenonException;
    
        
    
    
    
    
}

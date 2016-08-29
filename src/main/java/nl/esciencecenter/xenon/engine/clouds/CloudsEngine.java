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

package nl.esciencecenter.xenon.engine.clouds;

import java.util.HashSet;
import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.clouds.Cloud;
import nl.esciencecenter.xenon.clouds.Clouds;
import nl.esciencecenter.xenon.clouds.VirtualMachine;
import nl.esciencecenter.xenon.clouds.VirtualMachineDescription;
import nl.esciencecenter.xenon.clouds.VirtualMachineStatus;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.engine.Adaptor;
import nl.esciencecenter.xenon.engine.XenonEngine;

/**
 * 
 * 
 * @version 2.0
 * @since 2.0
 */
public class CloudsEngine implements Clouds {

    private final XenonEngine xenonEngine;

    public CloudsEngine(XenonEngine xenonEngine) {
        this.xenonEngine = xenonEngine;
    }
    
    private Adaptor getAdaptor(Cloud cloud) throws XenonException {
        return xenonEngine.getAdaptor(cloud.getAdaptorName());
    }
       
    @Override
    public Cloud newCloud(String scheme, String location, Credential credential, Map<String, String> properties)
            throws XenonException {
        
        Adaptor adaptor = xenonEngine.getAdaptorFor(scheme);
        return adaptor.cloudsAdaptor().newCloud(scheme, location, credential, properties);
    }

    @Override
    public void close(Cloud cloud) throws XenonException {
        getAdaptor(cloud).cloudsAdaptor().close(cloud);
    }

    @Override
    public boolean isOpen(Cloud cloud) throws XenonException {
        return getAdaptor(cloud).cloudsAdaptor().isOpen(cloud);
    }

    @Override
    public VirtualMachine launchVirtualMachine(Cloud cloud, VirtualMachineDescription description) throws XenonException {
        return getAdaptor(cloud).cloudsAdaptor().launchVirtualMachine(cloud, description);
    }

    @Override
    public VirtualMachineStatus startVirtualMachine(VirtualMachine virtualMachine) throws XenonException {
        return getAdaptor(virtualMachine.getCloud()).cloudsAdaptor().startVirtualMachine(virtualMachine);
    }

    @Override
    public VirtualMachineStatus stopVirtualMachine(VirtualMachine virtualMachine) throws XenonException {
        return getAdaptor(virtualMachine.getCloud()).cloudsAdaptor().stopVirtualMachine(virtualMachine);
    }

    @Override
    public VirtualMachineStatus terminateVirtualMachine(VirtualMachine virtualMachine) throws XenonException {
        return getAdaptor(virtualMachine.getCloud()).cloudsAdaptor().terminateVirtualMachine(virtualMachine);
    }

    @Override
    public VirtualMachineStatus rebootVirtualMachine(VirtualMachine virtualMachine) throws XenonException {
        return getAdaptor(virtualMachine.getCloud()).cloudsAdaptor().rebootVirtualMachine(virtualMachine);
    }

    @Override
    public VirtualMachineStatus getVirtualMachineStatus(VirtualMachine virtualMachine) throws XenonException {
        return getAdaptor(virtualMachine.getCloud()).cloudsAdaptor().getVirtualMachineStatus(virtualMachine);
    }

    private String[] getAdaptors(VirtualMachine[] in) {

        HashSet<String> result = new HashSet<>();

        for (VirtualMachine vm : in) {
            if (vm != null) {
                result.add(vm.getCloud().getAdaptorName());
            }
        }

        return result.toArray(new String[result.size()]);
    }

    private void selectVirtualMachines(String adaptorName, VirtualMachine[] in, VirtualMachine[] out) {
        for (int i = 0; i < in.length; i++) {
            if (in[i] != null && adaptorName.equals(in[i].getCloud().getAdaptorName())) {
                out[i] = in[i];
            } else {
                out[i] = null;
            }
        }
    }

    private void getVirtualMachinesStatus(String adaptor, VirtualMachine[] in, VirtualMachineStatus[] out) {

        VirtualMachineStatus[] result = null;
        XenonException exception = null;

        try {
            result = xenonEngine.getAdaptor(adaptor).cloudsAdaptor().getVirtualMachineStatuses(in);
        } catch (XenonException e) {
            exception = e;
        }

        for (int i = 0; i < in.length; i++) {
            if (in[i] != null) {
                if (result != null) {
                    out[i] = result[i];
                } else {
                    out[i] = new VirtualMachineStatusImplementation(in[i], null, exception, false, false, null);
                }
            }
        }
    }
    
    @Override
    public VirtualMachineStatus[] getVirtualMachineStatuses(VirtualMachine... virtualMachines) throws XenonException {
        
        // First check for the three simple cases; null, no jobs or 1 job.
        if (virtualMachines == null || virtualMachines.length == 0) {
            return new VirtualMachineStatus[0];
        }

        if (virtualMachines.length == 1) {

            if (virtualMachines[0] == null) {
                return new VirtualMachineStatus[1];
            }

            try {
                return new VirtualMachineStatus[] { getVirtualMachineStatus(virtualMachines[0]) };
            } catch (Exception e) {
                return new VirtualMachineStatus[] { new VirtualMachineStatusImplementation(virtualMachines[0], null, e, false, 
                        false, null) };
            }
        }

        // If we have more than one job, we first collect all adaptor names. 
        String[] adaptors = getAdaptors(virtualMachines);

        // Next we traverse over the names, and get the JobStatus for each adaptor individually, merging the result into the 
        // overall result on the fly.
        VirtualMachineStatus[] result = new VirtualMachineStatus[virtualMachines.length];
        VirtualMachine[] tmp = new VirtualMachine[virtualMachines.length];

        for (String adaptor : adaptors) {
            selectVirtualMachines(adaptor, virtualMachines, tmp);
            getVirtualMachinesStatus(adaptor, tmp, result);
        }

        return result;
    }

    @Override
    public VirtualMachineStatus waitUntilRunning(VirtualMachine virtualMachine, long timeout) throws XenonException {
        return getAdaptor(virtualMachine.getCloud()).cloudsAdaptor().waitUntilRunning(virtualMachine, timeout);
    }

    @Override
    public VirtualMachineStatus waitUntilDone(VirtualMachine virtualMachine, long timeout) throws XenonException {
        return getAdaptor(virtualMachine.getCloud()).cloudsAdaptor().waitUntilDone(virtualMachine, timeout);
    }

    @Override
    public VirtualMachine[] launchVirtualMachines(Cloud cloud, VirtualMachineDescription description, int count)
            throws XenonException {
        // TODO Auto-generated method stub
        throw new XenonException("launchVirtualMachines not implemented yet!", null);
    }
}

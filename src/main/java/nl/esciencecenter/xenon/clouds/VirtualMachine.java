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

/**
 * @version 2.0
 * @since 2.0
 *
 */
public interface VirtualMachine {

    /**
     * Returns the {@link VirtualMachineDescription} that was used to create this VirtualMachine.
     * 
     * @return the VirtualMachineDescription that belongs to this VirtualMachine
     */
    VirtualMachineDescription getVirtualMachineDescription();

    /**
     * Returns the {@link Cloud} that was used to create this VitualMachine.
     * 
     * @return the <code>Cloud</code> to which this <code>VirtualMachine</code> belongs.
     */
    Cloud getCloud();

    /**
     * Returns the identifier that was assigned to this VirtualMachine by the Cloud.
     * 
     * @return the identifier that was assigned to this VirtualMachine by the Cloud.
     */
    String getIdentifier();
    
}

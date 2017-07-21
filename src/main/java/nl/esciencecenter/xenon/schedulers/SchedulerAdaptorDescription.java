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

package nl.esciencecenter.xenon.schedulers;

import nl.esciencecenter.xenon.AdaptorDescription;

/**
 * 
 */
public interface SchedulerAdaptorDescription extends AdaptorDescription {

	/**
     * Is this scheduler embedded in Xenon ? 
     * 
     * @return
     * 		if this scheduler is embedded in Xenon ?
     */
    boolean isEmbedded();
    
    
    /**
     * Does this scheduler support batch jobs ?
     * 
     * @return
     * 		if this scheduler supports batch jobs
     */
    boolean supportsBatch();
    
    /**
     * Does this scheduler support interactive jobs ?
     * 
     * @return
     * 		if this scheduler supports interactive jobs
     */
    boolean supportsInteractive();    
}

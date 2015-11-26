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
package nl.esciencecenter.xenon.files;

/**
 * Copy represents an asynchronous copy operation.
 * <p>
 * A <code>Copy</code> is returned as the result of the {@link Files#copy(Path, Path, CopyOption []) Files.copy} 
 * method, when the option {@link CopyOption#ASYNCHRONOUS CopyOption.ASYNCHRONOUS} is provided. This <code>Copy</code> can then 
 * be used to retrieve the status of the copy operation using {@link Files#getCopyStatus(Copy) Files.getCopyStatus} or cancel it 
 * using {@link Files#cancelCopy(Copy) Files.cancelCopy}.
 * </p>
 * 
 * @version 1.0
 * @since 1.0
 * @see Files
 * @see CopyOption
 */
public interface Copy {

    /**
     * Retrieve the source of the copy.
     * 
     * @return the source of the copy.
     */
    Path getSource();

    /**
     * Retrieve the target of the copy.
     * 
     * @return the target of the copy.
     */
    Path getTarget();
}

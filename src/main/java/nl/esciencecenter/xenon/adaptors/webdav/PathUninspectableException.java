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
package nl.esciencecenter.xenon.adaptors.webdav;

import nl.esciencecenter.xenon.XenonException;

public class PathUninspectableException extends XenonException {
    private static final long serialVersionUID = 1L;

    public PathUninspectableException(String adaptorName, String message) {
        super(adaptorName, message);
    }

    public PathUninspectableException(String adaptorName, String message, Throwable nested) {
        super(adaptorName, message, nested);
    }

}

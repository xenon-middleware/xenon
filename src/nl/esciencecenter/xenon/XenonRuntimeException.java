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
package nl.esciencecenter.xenon;

/**
 * XenonRuntimeException is the parent for all runtime exceptions raised in Xenon. 
 * 
 * Next to the regular exception message and cause, XenonRuntimeException add a source adaptor name to all runtime exceptions.  
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public class XenonRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final String adaptorName;

    public XenonRuntimeException(String adaptorName, String message) {
        super(message);
        this.adaptorName = adaptorName;
    }

    public XenonRuntimeException(String adaptorName, String message, Throwable t) {
        super(message, t);
        this.adaptorName = adaptorName;
    }

    @Override
    public String getMessage() {
        String result = super.getMessage();
        if (adaptorName != null) {
            result = adaptorName + " adaptor: " + result;
        }

        return result;
    }

}

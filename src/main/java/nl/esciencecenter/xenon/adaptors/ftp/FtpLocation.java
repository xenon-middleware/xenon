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
package nl.esciencecenter.xenon.adaptors.ftp;

import nl.esciencecenter.xenon.InvalidLocationException;
import nl.esciencecenter.xenon.adaptors.generic.Location;

public class FtpLocation extends Location {

    public FtpLocation(String user, String host, int port) {
        super(user, host, port);
    }

    protected FtpLocation(String location) throws InvalidLocationException {
        super(location, FtpAdaptor.ADAPTOR_FILE_SCHEME.get(0));
    }

    @Override
    protected String getAdaptorName() {
        return FtpAdaptor.ADAPTOR_NAME;
    }

    @Override
    protected int getDefaultPort() {
        return FtpAdaptor.DEFAULT_PORT;
    }

    public static FtpLocation parse(String location) throws InvalidLocationException {
        return new FtpLocation(location);
    }
}

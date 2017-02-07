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

import nl.esciencecenter.xenon.InvalidLocationException;
import nl.esciencecenter.xenon.adaptors.generic.Location;

public class WebdavLocation extends Location {

    public WebdavLocation(String user, String host, int port) {
        super(user, host, port);
    }

    protected WebdavLocation(String location) throws InvalidLocationException {
        super(location, WebdavAdaptor.ADAPTOR_SCHEME.get(0));
    }

    @Override
    protected String getAdaptorName() {
        return WebdavAdaptor.ADAPTOR_NAME;
    }

    @Override
    protected int getDefaultPort() {
        return WebdavAdaptor.DEFAULT_PORT;
    }

    public static WebdavLocation parse(String location) throws InvalidLocationException {
        return new WebdavLocation(location);
    }

    public static WebdavLocation parse(String location, String scheme) throws InvalidLocationException {
        return new WebdavLocation(location);
    }
}

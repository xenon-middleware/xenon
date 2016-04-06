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

import java.util.List;

import org.apache.jackrabbit.webdav.MultiStatusResponse;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.engine.files.PathAttributesPairImplementation;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.files.PathAttributesPair;

public class WebdavDirectoryAttributeStream extends WebdavDirectoryStreamBase<PathAttributesPair> {

    WebdavDirectoryAttributeStream(Path dir, nl.esciencecenter.xenon.files.DirectoryStream.Filter filter,
            List<MultiStatusResponse> list) throws XenonException {
        super(dir, filter, list);
    }

    @Override
    protected PathAttributesPair getStreamElementFromEntry(MultiStatusResponse entry, Path entryPath) throws XenonException {
        return new PathAttributesPairImplementation(entryPath, new WebdavFileAttributes(entry.getProperties(WebdavFiles.OK)));
    }
}

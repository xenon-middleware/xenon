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
package nl.esciencecenter.xenon.adaptors;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.ServiceLoader;

import nl.esciencecenter.xenon.UnknownAdaptorException;
import nl.esciencecenter.xenon.adaptors.filesystems.FileAdaptor;
import nl.esciencecenter.xenon.adaptors.schedulers.SchedulerAdaptor;
import nl.esciencecenter.xenon.filesystems.FileSystemAdaptorDescription;
import nl.esciencecenter.xenon.schedulers.SchedulerAdaptorDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdaptorLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdaptorLoader.class);

    /** The name of this component, for use in exceptions */
    private static final String COMPONENT_NAME = "AdaptorLoader";

    private static final HashMap<String, FileAdaptor> fileAdaptors = new LinkedHashMap<>();

    private static final HashMap<String, SchedulerAdaptor> schedulerAdaptors = new LinkedHashMap<>();

    private static boolean loaded = false;

    static {
        loadAdaptors();
    }

    private static void loadAdaptors() {
        loadSchedulerAdaptors();
        loadFileSystemAdaptors();
    }

    private static void loadFileSystemAdaptors() {
        ServiceLoader<FileAdaptor> loader = ServiceLoader.load(FileAdaptor.class);
        Iterator<FileAdaptor> iterator = loader.iterator();
        fileAdaptors.clear();

        LOGGER.trace("loading filesystem adaptors");

        while (iterator.hasNext()) {
            FileAdaptor adaptor = iterator.next();
            LOGGER.trace("   loading: " + adaptor.getName());
            fileAdaptors.put(adaptor.getName(), adaptor);
        }
    }

    private static void loadSchedulerAdaptors() {
        ServiceLoader<SchedulerAdaptor> loader = ServiceLoader.load(SchedulerAdaptor.class);
        Iterator<SchedulerAdaptor> iterator = loader.iterator();
        schedulerAdaptors.clear();

        LOGGER.trace("loading scheuduler adaptors");

        while (iterator.hasNext()) {
            SchedulerAdaptor adaptor = iterator.next();
            LOGGER.trace("   loading: " + adaptor.getName());
            schedulerAdaptors.put(adaptor.getName(), adaptor);
        }
    }

    private static void checkAdaptorName(String adaptorName) throws UnknownAdaptorException {
        if (adaptorName == null) {
            throw new IllegalArgumentException("Adaptor name may not be null");
        }

        if (adaptorName.trim().isEmpty()) {
            throw new UnknownAdaptorException(COMPONENT_NAME, "Adaptor name may not be empty");
        }
    }

    public static FileAdaptor getFileAdaptor(String adaptorName) throws UnknownAdaptorException {
        checkAdaptorName(adaptorName);

        if (!fileAdaptors.containsKey(adaptorName)) {
            throw new UnknownAdaptorException(COMPONENT_NAME, String.format("Adaptor '%s' not found", adaptorName));
        }

        return fileAdaptors.get(adaptorName);
    }

    public static String[] getFileAdaptorNames() {
        return fileAdaptors.keySet().toArray(new String[fileAdaptors.size()]);
    }

    public static FileSystemAdaptorDescription[] getFileAdaptorDescriptions() {
        return fileAdaptors.values().toArray(new FileSystemAdaptorDescription[fileAdaptors.size()]);
    }

    public static SchedulerAdaptor getSchedulerAdaptor(String adaptorName) throws UnknownAdaptorException {
        checkAdaptorName(adaptorName);

        if (!schedulerAdaptors.containsKey(adaptorName)) {
            throw new UnknownAdaptorException(COMPONENT_NAME, String.format("Adaptor '%s' not found", adaptorName));
        }

        return schedulerAdaptors.get(adaptorName);
    }

    public static String[] getSchedulerAdaptorNames() {
        return schedulerAdaptors.keySet().toArray(new String[schedulerAdaptors.size()]);
    }

    public static SchedulerAdaptorDescription[] getSchedulerAdaptorDescriptions() {
        return schedulerAdaptors.values().toArray(new SchedulerAdaptorDescription[schedulerAdaptors.size()]);
    }

}

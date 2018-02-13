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

public class AdaptorLoader {

    /** The name of this component, for use in exceptions */
    private static final String COMPONENT_NAME = "AdaptorLoader";

    private static final HashMap<String, FileAdaptor> fileAdaptors = new LinkedHashMap<>();

    private static final HashMap<String, SchedulerAdaptor> schedulerAdaptors = new LinkedHashMap<>();

    static {
        loadAdaptors();
    }

    private static void loadAdaptors() {
        loadSchedulerAdaptors();
        loadFileAdaptors();
    }

    private static void loadSchedulerAdaptors() {
        ServiceLoader<FileAdaptor> loader = ServiceLoader.load(FileAdaptor.class);
        Iterator<FileAdaptor> iterator = loader.iterator();
        fileAdaptors.clear();
        while (iterator.hasNext()) {
            FileAdaptor adaptor = iterator.next();
            fileAdaptors.put(adaptor.getName(), adaptor);
        }
    }

    private static void loadFileAdaptors() {
        ServiceLoader<SchedulerAdaptor> loader = ServiceLoader.load(SchedulerAdaptor.class);
        Iterator<SchedulerAdaptor> iterator = loader.iterator();
        schedulerAdaptors.clear();
        while (iterator.hasNext()) {
            SchedulerAdaptor adaptor = iterator.next();
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

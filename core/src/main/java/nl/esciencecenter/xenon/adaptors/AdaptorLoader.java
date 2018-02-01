package nl.esciencecenter.xenon.adaptors;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import nl.esciencecenter.xenon.UnknownAdaptorException;
import nl.esciencecenter.xenon.adaptors.filesystems.FileAdaptor;
import nl.esciencecenter.xenon.adaptors.schedulers.SchedulerAdaptor;
import nl.esciencecenter.xenon.filesystems.FileSystemAdaptorDescription;
import nl.esciencecenter.xenon.schedulers.SchedulerAdaptorDescription;
import nl.esciencecenter.xenon.utils.StreamForwarder;

public class AdaptorLoader {

    /** The name of this component, for use in exceptions */
    private static final String COMPONENT_NAME = "AdaptorLoader";

    private static final HashMap<String, FileAdaptor> fileAdaptors = new LinkedHashMap<>();

    private static final HashMap<String, SchedulerAdaptor> schedulerAdaptors = new LinkedHashMap<>();

    static {
        loadAdaptors();
    }

    private static void loadAdaptors() {

        ClassLoader parent = Thread.currentThread().getContextClassLoader();

        // System.out.println("Parent CL = " + parent);

        URL[] classpath = ((URLClassLoader) parent).getURLs();

        System.out.println("CLASSPATH is " + Arrays.toString(classpath));

        for (URL u : classpath) {
            if (u.getFile().endsWith(".jar")) {
                try {
                    loadAdaptor(u);
                } catch (Exception e) {
                    System.err.println("Failed to load adaptor from " + u);
                }
            }
        }
    }

    private static void loadAdaptor(URL url) throws IOException {

        JarFile f = new JarFile(new File(url.getFile()));

        Attributes attributes = f.getManifest().getMainAttributes();

        String adaptorlist = attributes.getValue("Xenon-Adaptors");

        if (adaptorlist == null || adaptorlist.trim().isEmpty()) {
            f.close();
            System.out.println("No adaptors found in " + url);
            return;
        }

        // System.out.println("Found filesystem adaptors: \"" + adaptorlist + "\"");

        String[] adaptorClasses = adaptorlist.split(",");

        try {
            List<URL> classpath = prepareJars(url, f);
            f.close();

            for (String adaptor : adaptorClasses) {
                loadAdaptor(adaptor.trim(), classpath, url);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<URL> prepareJars(URL url, JarFile jarJar) throws Exception {

        Enumeration<JarEntry> entries = jarJar.entries();

        ArrayList<URL> urls = new ArrayList<>();
        urls.add(url);

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();

            if (!entry.isDirectory() && entry.getName().endsWith(".jar") && !entry.getName().startsWith("slf4j-api-")) {
                File temp = File.createTempFile("xenon-tmp-", ".jar");
                InputStream in = jarJar.getInputStream(entry);
                OutputStream out = new FileOutputStream(temp);
                new StreamForwarder(in, out).join();
                urls.add(temp.toURI().toURL());
            }
        }

        return urls;
    }

    private static void loadAdaptor(String name, List<URL> classpath, URL jarJar) throws Exception {

        AdaptorClassLoader loader = new AdaptorClassLoader(classpath);

        Class<?> clazz = loader.loadClass(name);

        // if (!clazz.isInstance(Adaptor.class)) {
        // System.out.println("Loaded adaptor " + name + " is not a Xenon adaptor");
        // return;
        // }

        Adaptor adaptor = (Adaptor) clazz.newInstance();

        if (adaptor instanceof FileAdaptor) {
            System.out.println("Loaded \"" + adaptor.getName() + "\" FileSystemAdaptor from " + jarJar);
            fileAdaptors.put(adaptor.getName(), (FileAdaptor) adaptor);
        } else if (adaptor instanceof SchedulerAdaptor) {
            System.out.println("Loaded \"" + adaptor.getName() + "\" SchedulerAdaptor from " + jarJar);
            schedulerAdaptors.put(adaptor.getName(), (SchedulerAdaptor) adaptor);
        } else {
            System.out.println("Failed recognize adaptor " + adaptor.getName() + " from " + jarJar);
        }
    }

    private static void checkAdaptorName(String adaptorName) throws UnknownAdaptorException {
        if (adaptorName == null || adaptorName.trim().isEmpty()) {
            throw new UnknownAdaptorException(COMPONENT_NAME, "Adaptor name may not be null or empty");
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

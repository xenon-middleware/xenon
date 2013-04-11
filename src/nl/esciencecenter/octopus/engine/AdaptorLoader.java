package nl.esciencecenter.octopus.engine;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import nl.esciencecenter.octopus.engine.loader.JarFileSystem;
import nl.esciencecenter.octopus.engine.loader.JarFsClassLoader;
import nl.esciencecenter.octopus.exceptions.OctopusException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class used to load adaptors.
 * 
 * @author Niels Drost
 * 
 */
class AdaptorLoader {

    /**
     * A helper class to compare java.io.File names, so that they can be sorted, and the order becomes predictable and
     * reproducible.
     */
    private static class FileComparator implements Comparator<java.io.File> {
        public int compare(java.io.File f1, java.io.File f2) {
            return f1.getName().compareTo(f2.getName());
        }
    }

    /**
     * A helper class to get the call context. It subclasses SecurityManager to make getClassContext() accessible. Don't install
     * this as an actual security manager!
     */
    private static final class CallerResolver extends SecurityManager {
        protected Class<?>[] getClassContext() {
            return super.getClassContext();
        }
    }

    /**
     * Determines if loader l2 is a child of loader l1.
     * 
     * @param l1
     * @param l2
     * @return true if l2 is a child of l1.
     */
    private static boolean isChild(ClassLoader l1, ClassLoader l2) {
        if (l1 == null) {
            // Primordial loader is parent of all classloaders.
            return true;
        }
        while (l2 != null) {
            if (l1 == l2) {
                return true;
            }
            l2 = l2.getParent();
        }
        return false;
    }

    /**
     * This method tries to determine a suitable classloader to be used as parent classloader for the URLClassloaders of the
     * adaptors. Sometimes, the classloader that loaded the OctopusEngine class is not a good candidate because this probably is
     * just the system classloader. A better candidate might be the classloader of the class that prompted the loading of Octopus
     * in the first place, or the context classloader.
     * 
     * @return the classloader to be used.
     */
    private static ClassLoader getParentClassLoader() {
        // Find the Class instance of the class that prompted the loading of
        // Octopus
        Class<?>[] callers = (new CallerResolver()).getClassContext();
        Class<?> callerClass = null;
        for (Class<?> c : callers) {
            String name = c.getCanonicalName();
            if (name != null && name.startsWith("nl.esciencecenter.octopus")) {
                continue;
            }
            callerClass = c;
            break;
        }
        // If we cannot find it, use the OctopusEngine class instance, for lack
        // of a better choice.
        if (callerClass == null) {
            callerClass = OctopusEngine.class;
        }
        // Now, there are basically two choices: the classloader that loaded the
        // caller class, or the context classloader. If there is a
        // parent-relation, choose the child.
        ClassLoader callerLoader = callerClass.getClassLoader();
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader result;

        if (isChild(contextLoader, callerLoader)) {
            result = callerLoader;
        } else if (isChild(callerLoader, contextLoader)) {
            result = contextLoader;
        } else {
            // Apparently there is no relation. The following may not be right,
            // but then, there is no "right".
            result = contextLoader;
        }

        ClassLoader systemLoader = ClassLoader.getSystemClassLoader();

        // If the system classloader is a child of the result found so far, use
        // the system classloader instead.
        if (isChild(result, systemLoader)) {
            result = systemLoader;
        }

        return result;
    }
    
    private static String fixName(String name) {
        if (name.length() == 1) { 
            return name.substring(0, 1).toUpperCase();
        }
        
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }

    private static Adaptor newAdaptor(ClassLoader loader, String name, OctopusProperties properties, OctopusEngine octopusEngine)
            throws OctopusException {
        try {
            Thread.currentThread().setContextClassLoader(loader);
            Class<?> clazz =
                    loader.loadClass("nl.esciencecenter.octopus.adaptors." + name + "." + fixName(name) + "Adaptor");

            Constructor<?> constructor = clazz.getConstructor(new Class[] { OctopusProperties.class, OctopusEngine.class });

            Adaptor result = (Adaptor) constructor.newInstance(new Object[] { properties, octopusEngine });

            return result;
        } catch (Throwable t) {
            // throw new OctopusException("failed to load adaptor " + name, t);
            logger.error("failed to load adaptor " + name, t);
            return null;
        }
    }

    private static final Attributes.Name ADAPTOR_ATTRIBUTE_NAME = new Attributes.Name("Octopus-Adaptors");

    public static final String ADAPTOR_DIR_PROPERTY = "octopus.adaptor.dir";

    private static final Logger logger = LoggerFactory.getLogger(AdaptorLoader.class);

    static Adaptor[] loadAdaptors(OctopusProperties properties, OctopusEngine octopusEngine) throws OctopusException {
                
        ArrayList<File> candidateFiles = new ArrayList<File>();

        // find jar files that potentially contain adaptors
        if (properties.getProperty(ADAPTOR_DIR_PROPERTY) == null) {
            logger.info(ADAPTOR_DIR_PROPERTY + " not set, loading adaptors from classpath");

            String classPath = System.getProperty("java.class.path");

            if (classPath == null) {
                throw new OctopusException("AdaptorLoader", "Failed to load adaptors. Cannot get classpath, and " + ADAPTOR_DIR_PROPERTY
                        + " not set");
            }

            String[] pathElements = classPath.split(java.io.File.pathSeparator);

            for (String path : pathElements) {
                if (path.endsWith(".jar")) {
                    candidateFiles.add(new File(path));
                }
            }
        } else {
            java.io.File adaptorRoot = new java.io.File(properties.getProperty(ADAPTOR_DIR_PROPERTY));

            if (!adaptorRoot.isDirectory()) {
                throw new OctopusException(ADAPTOR_DIR_PROPERTY + " (" + adaptorRoot + ") is not a directory", null, null);
            }

            File[] files = adaptorRoot.listFiles();

            if (files == null) {
                throw new OctopusException("AdaptorLoader", "cannot list files in " + adaptorRoot);
            }

            // sort files
            Arrays.sort(files, new FileComparator());

            for (File file : files) {
                if (file.getName().endsWith(".jar")) {
                    candidateFiles.add(file);
                }
            }
        }

        // filter out jar files that contain adaptors, create JarFileSystems
        HashSet<JarFileSystem> fileSystems = new HashSet<JarFileSystem>();

        HashSet<String> adaptorNames = new HashSet<String>();

        for (File file : candidateFiles) {
            // logger.debug("next file: " + file);
            if (!file.isFile()) {
                continue;
            }

            try {
                JarFile jarFile = new JarFile(file);

                Manifest manifest = jarFile.getManifest();

                if (manifest == null || manifest.getMainAttributes() == null
                        || !manifest.getMainAttributes().containsKey(ADAPTOR_ATTRIBUTE_NAME)
                        || manifest.getMainAttributes().getValue(ADAPTOR_ATTRIBUTE_NAME) == null
                        || manifest.getMainAttributes().getValue(ADAPTOR_ATTRIBUTE_NAME).isEmpty()) {
                    jarFile.close();
                    continue;
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("Loading adaptor set: " + manifest.getMainAttributes().getValue(ADAPTOR_ATTRIBUTE_NAME));
                }

                JarFileSystem fileSystem = new JarFileSystem(jarFile);

                fileSystems.add(fileSystem);
                adaptorNames.addAll(fileSystem.getAdaptorNames());

            } catch (IOException e) {
                logger.error("failed to load adaptor from jar: " + file, e);
            }
        }

        ClassLoader parentLoader = getParentClassLoader();

        // get loader for shared classes (if there are shared libraries)
        ClassLoader sharedLoader = parentLoader;

        if (adaptorNames.contains("Shared")) {
            sharedLoader = new JarFsClassLoader(fileSystems, "Shared", parentLoader);
            adaptorNames.remove("Shared");
        }

        // load and initialize all adaptors
        ArrayList<Adaptor> adaptors = new ArrayList<Adaptor>();

        String [] adaptorsToLoad = properties.getStringList(OctopusEngine.LOAD);
        
        for (String adaptorName : adaptorNames) {
            
            if (checkAdaptorName(adaptorName, adaptorsToLoad)) { 
                ClassLoader adaptorClassLoader = new JarFsClassLoader(fileSystems, adaptorName, sharedLoader);
                Adaptor adaptor = newAdaptor(adaptorClassLoader, adaptorName, properties, octopusEngine);

                if (adaptor != null) {
                    adaptors.add(adaptor);
                }
            }
        }

        if (adaptors.size() == 0) {
            throw new OctopusException("Octopus: No adaptors could be loaded", null, null);
        }

        return adaptors.toArray(new Adaptor[adaptors.size()]);
    }
    
    /** 
     * Check if the adaptor name is present in the list. 
     * 
     * @param adaptorName adaptor name to check for. 
     * @param adaptorsToLoad list of adaptor names. 
     * 
     * @return if the adaptor name is present in the list.
     */
    private static boolean checkAdaptorName(String adaptorName, String [] adaptorsToLoad) { 
        
        if (adaptorsToLoad == null || adaptorsToLoad.length == 0) { 
            return true;
        }
        
        for (int i=0;i<adaptorsToLoad.length;i++) { 
            if (adaptorName.equals(adaptorsToLoad[i])) { 
                return true;
            }
        }
        
        return false;
    }
}

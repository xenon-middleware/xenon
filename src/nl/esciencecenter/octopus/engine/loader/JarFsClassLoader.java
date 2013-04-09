package nl.esciencecenter.octopus.engine.loader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Classloader which loads classes from jar files (and other files) within a jar.
 * 
 * @author Niels Drost
 * 
 */
public class JarFsClassLoader extends ClassLoader {

    private static final Logger logger = LoggerFactory.getLogger(JarFsClassLoader.class);

    private JarFileSystem[] fileSystems;

    private String adaptorName;

    private static JarFileSystem[] filterFileSystems(Set<JarFileSystem> allFileSystems, String adaptorName) {
        ArrayList<JarFileSystem> result = new ArrayList<JarFileSystem>();

        for (JarFileSystem fileSystem : allFileSystems) {
            if (fileSystem.containsAdaptor(adaptorName)) {
                result.add(fileSystem);
            }
        }

        return result.toArray(new JarFileSystem[result.size()]);
    }

    public JarFsClassLoader(Set<JarFileSystem> fileSystems, String adaptorName) {
        super();
        this.fileSystems = filterFileSystems(fileSystems, adaptorName);
        this.adaptorName = adaptorName;

        logger.debug("Loading classes for " + adaptorName + " from " + Arrays.toString(this.fileSystems));
    }

    public JarFsClassLoader(Set<JarFileSystem> fileSystems, String adaptorName, ClassLoader parent) {
        super(parent);
        this.fileSystems = filterFileSystems(fileSystems, adaptorName);
        this.adaptorName = adaptorName;

        logger.debug("Loading classes for " + adaptorName + " from " + Arrays.toString(this.fileSystems));
    }

    @Override
    protected Class<?> findClass(String fullyQualifiedClassName) throws ClassNotFoundException {
        // file we are looking for
        String classFilename = fullyQualifiedClassName.replace(".", "/") + ".class";

        if (logger.isDebugEnabled()) {
            logger.debug(this + " looking for class " + fullyQualifiedClassName + " in file " + classFilename);
        }

        try {
            for (JarFileSystem fileSystem : fileSystems) {
                JarFsFile jarFsFile = fileSystem.findFile(adaptorName, classFilename, true);

                if (jarFsFile == null) {
                    continue;
                }

                // logger.debug("found " + jarFsFile);

                addPackage(fullyQualifiedClassName, classFilename, jarFsFile.getJarEntry().getName(), jarFsFile.getManifest());
                return super.defineClass(fullyQualifiedClassName, jarFsFile.getBytes().array(), jarFsFile.getBytes().position(), jarFsFile
                        .getBytes().remaining());
            }
        } catch (IOException e) {
            throw new ClassNotFoundException("could not load class", e);
        }
        throw new ClassNotFoundException("class not found in " + Arrays.toString(fileSystems));
    }

    private void addPackage(String className, String classFilename, String subJarFilename, Manifest manifest) {
        String packageName = className.substring(0, className.lastIndexOf("."));

        if (super.getPackage(packageName) != null) {
            // already defined
            return;
        }

        // start with empty attributes
        Attributes attributes = new Attributes();

        // add main attributes
        Attributes mainAttributes = manifest.getMainAttributes();
        if (mainAttributes != null) {
            attributes.putAll(mainAttributes);
        }

        // add entry attributes
        Attributes entryAttributes = manifest.getAttributes(classFilename);
        if (entryAttributes != null) {
            attributes.putAll(entryAttributes);
        }

        // get resulting values from attributes
        String specTitle = attributes.getValue(Attributes.Name.SPECIFICATION_TITLE);
        String specVersion = attributes.getValue(Attributes.Name.SPECIFICATION_VERSION);
        String specVendor = attributes.getValue(Attributes.Name.SPECIFICATION_VENDOR);
        String implTitle = attributes.getValue(Attributes.Name.IMPLEMENTATION_TITLE);
        String implVersion = attributes.getValue(Attributes.Name.IMPLEMENTATION_VERSION);
        String implVendor = attributes.getValue(Attributes.Name.IMPLEMENTATION_VENDOR);

        // figure out if this package is sealed, and the base of the seal
        String sealed = attributes.getValue(Attributes.Name.SEALED);
        URL sealBase = null;
        if (sealed != null && sealed.equalsIgnoreCase("true")) {
            try {
                sealBase = new URL(subJarFilename);
            } catch (MalformedURLException e) {
                throw new Error("cannot create url from " + classFilename);
            }
        }

        super.definePackage(packageName, specTitle, specVersion, specVendor, implTitle, implVersion, implVendor, sealBase);
    }

    @Override
    protected URL findResource(String name) {
        return null;
    }

    @Override
    protected Enumeration<URL> findResources(String name) throws IOException {
        return null;
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        // logger.debug("looking for resource: " + name);

        try {
            for (JarFileSystem fileSystem : fileSystems) {

                JarFsFile jarFsFile = fileSystem.findFile(adaptorName, name, true);

                if (jarFsFile != null) {
                    return new ByteArrayInputStream(jarFsFile.getBytes().array(), jarFsFile.getBytes().position(), jarFsFile
                            .getBytes().remaining());
                }
            }
        } catch (IOException e) {
            logger.error("could not open resources", e);
            return null;
        }
        logger.error("resource not found in " + Arrays.toString(fileSystems));
        return null;
    }

    public String toString() {
        return "JarFsClassLoader for " + adaptorName;
    }

}

package nl.esciencecenter.octopus.engine.loader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import nl.esciencecenter.octopus.exceptions.OctopusException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JarFileSystem {

    public static final String ADAPTOR_DIR = "octopus-adaptors";

    private static final Logger logger = LoggerFactory.getLogger(JarFileSystem.class);

    private final JarFile mainJarFile;

    // index for each adaptor
    private final HashMap<String, Index> indexes;

    private final HashSet<String> mainEntries;

    public JarFileSystem(JarFile file) throws OctopusException {
        this.mainJarFile = file;
        this.indexes = new HashMap<String, Index>();
        this.mainEntries = new HashSet<String>();

        Enumeration<JarEntry> entries = mainJarFile.entries();

        int firstSlash = ADAPTOR_DIR.length() + 1;

        long now = System.currentTimeMillis();

        // create all indexes
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();

            // logger.debug("next entry: " + entry.getName());

            String name = entry.getName();
            if (!name.startsWith(ADAPTOR_DIR)) {
                continue;
            }

            int secondSlashIndex = name.indexOf('/', firstSlash + 1);

            if (secondSlashIndex == -1) {
                // second slash not found, skip this entry
                continue;
            }

            String adaptorName = name.substring(firstSlash, secondSlashIndex);

            // logger.debug("adaptor name = " + adaptorName);

            if (adaptorName.isEmpty()) {
                continue;
            }

            Index index = indexes.get(adaptorName);
            if (index == null) {
                index = new Index();
                indexes.put(adaptorName, index);
            }

            if (name.endsWith(".jar")) {
                // process content of entry
                index.add(entry, mainJarFile);
            }

            mainEntries.add(entry.getName());
        }
        if (logger.isDebugEnabled()) {
            logger.debug(this + " contains adaptors: " + indexes.keySet().toString() + ". Indexing " + mainJarFile.size()
                    + " entries took " + (System.currentTimeMillis() - now) + " milliseconds");
        }

    }

    private static ByteBuffer readToBuffer(InputStream in) throws IOException {
        ByteBuffer result = ByteBuffer.allocate(1024);

        while (true) {
            if (!result.hasRemaining()) {
                ByteBuffer newResult = ByteBuffer.allocate(result.capacity() * 2);

                result.flip();
                newResult.put(result);

                result = newResult;
            }

            int read = in.read(result.array(), result.position(), result.remaining());

            if (read == -1) {
                result.flip();
                return result;
            } else {
                result.position(result.position() + read);
            }
        }
    }

    public synchronized JarFsFile findFile(String adaptorName, String filename, boolean loadData) throws IOException {
        // search for the asked for file in the main jar file.
        // DO NOT SEARCH FOR CLASS FILES IN MAIN JAR FILE
        if (!filename.endsWith(".class")) {
            String mainEntryName = ADAPTOR_DIR + "/" + adaptorName + "/" + filename;

            if (mainEntries.contains(mainEntryName)) {
                JarFsFile result = new JarFsFile(adaptorName, filename, mainJarFile.getJarEntry(mainEntryName), true);

                if (loadData) {
                    loadFileData(result);
                }

                return result;
            }
        }

        Index index = indexes.get(adaptorName);

        if (index == null) {
            throw new IOException("Cannot find index for adaptor " + adaptorName);
        }

        JarEntry entry = index.findEntryFor(filename);

        if (entry == null) {
            return null;
        }

        JarFsFile result = new JarFsFile(adaptorName, filename, entry, false);

        if (loadData) {
            loadFileData(result);
        }

        return result;
    }

    public synchronized void loadFileData(JarFsFile file) throws IOException {
        if (file.inMainJar()) {
            try (InputStream in = mainJarFile.getInputStream(mainJarFile.getEntry(file.getJarEntry().getName()))) {
                ByteBuffer byteBuffer = readToBuffer(in);
                Manifest manifest = mainJarFile.getManifest();

                file.setBytes(byteBuffer);
                file.setManifest(manifest);

                in.close();

                return;
            }
        }

        // in sub jar
        try (JarInputStream subJarStream = new JarInputStream(mainJarFile.getInputStream(file.getJarEntry()))) {
            while (true) {
                JarEntry subEntry = subJarStream.getNextJarEntry();

                if (subEntry == null) {
                    throw new IOException("could not load file data, although index said it should be in this jar file");
                } else if (subEntry.getName().equals(file.getFilename())) {
                    ByteBuffer byteBuffer = readToBuffer(subJarStream);
                    Manifest manifest = subJarStream.getManifest();

                    file.setBytes(byteBuffer);
                    file.setManifest(manifest);

                    return;
                }
            }
        }
    }

    public synchronized boolean containsAdaptor(String adaptorName) {
        return indexes.containsKey(adaptorName);
    }

    public String toString() {
        return "JarFS on \"" + mainJarFile.getName() + "\"";
    }

    public synchronized Set<String> getAdaptorNames() {
        return indexes.keySet();
    }

}

package nl.esciencecenter.octopus.files;

import java.util.Iterator;

import nl.esciencecenter.octopus.exceptions.OctopusException;

public interface Path {

    public FileSystem getFileSystem();

    public String getFileName();

    public Path getParent();

    public int getNameCount();

    public String [] getNames();
    
    public String getName(int index);

    public String subpath(int beginIndex, int endIndex);

    public boolean startsWith(String other);

    public boolean endsWith(String other);
    
    public Path normalize();
    
    public Path resolve(String other);

    public Path resolveSibling(String other) throws OctopusException;

    public String relativize(Path other) throws OctopusException;

    public Iterator<Path> iterator();

    public String getPath();
}

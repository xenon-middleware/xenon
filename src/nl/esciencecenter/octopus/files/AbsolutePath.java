package nl.esciencecenter.octopus.files;

import java.util.Iterator;

import nl.esciencecenter.octopus.exceptions.OctopusException;

public interface AbsolutePath {

    public FileSystem getFileSystem();

    public RelativePath getRelativePath();
    
    public boolean isLocal();
    
    public String getFileName();

    public AbsolutePath getParent();

    public int getNameCount();

    public String [] getNames();
    
    public String getName(int index);

    public AbsolutePath subpath(int beginIndex, int endIndex);

    public boolean startsWith(RelativePath other);

    public boolean endsWith(RelativePath other);

    public AbsolutePath resolve(RelativePath other);

    public AbsolutePath normalize();

    public AbsolutePath resolveSibling(RelativePath other) throws OctopusException;

    public AbsolutePath relativize(RelativePath other) throws OctopusException;

    public Iterator<AbsolutePath> iterator();

    public String getPath();
   
}

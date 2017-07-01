package nl.esciencecenter.xenon.files;

public class CopyDescription {

	private FileSystem sourceFileSystem;
	private Path sourcePath;
	
	private Path destinationPath;
	
	private CopyMode option = CopyMode.CREATE;
		
	private boolean recursive;

	/**
	 * Perform a (optionally recursive) copy of from a different filesystem to the given destination path.
	 * 
	 * The provided {@link CopyMode} determines the course of action if the target path exists.   
     *
	 * A recursive copy will only be performed if <code>recursive</code> is set to <code>true</code>. 
	 * If it is set to <code>false</code> only files will be accepted as source and destination paths.
	 * 
	 * @param sourceFS
	 * 		the filesystem to copy from (may not be null).
	 * @param sourcePath
	 * 		the source file to copy from 
	 * @param destinationPath
	 * 		the destination file to copy to 
	 * @param option
	 * 		the {@link CopyMode} which determines the course of action if the target path exists. 
	 * @param recursive
	 * 		if the copy must be done recursively
	 */
	public CopyDescription(FileSystem sourceFS, Path sourcePath, Path destinationPath, CopyMode option, boolean recursive) { 
		this.sourceFileSystem = sourceFS;
		this.sourcePath = sourcePath;
		this.destinationPath = destinationPath;
		this.option = option;
		this.recursive = recursive;
	}

	/**
	 * Perform a (optionally recursive) copy of from a different filesystem to the given destination path.
	 * 
	 * The destination file or directory must not exist yet. 
     *
	 * A recursive copy will only be performed if <code>recursive</code> is set to <code>true</code>. 
	 * If it is set to <code>false</code> only files will be accepted as source and destination paths.
	 * 
	 * @param sourceFS
	 * 		the filesystem to copy from (may not be null).
	 * @param sourcePath
	 * 		the source file to copy from 
	 * @param destinationPath
	 * 		the destination file to copy to 
	 * @param recursive
	 * 		if the copy must be done recursively
	 */
	public CopyDescription(FileSystem sourceFS, Path sourcePath, Path destinationPath, boolean recursive) { 
		this(sourceFS, sourcePath, destinationPath, CopyMode.CREATE, recursive);
	}
	
	/**
	 * Copy a file from a different filesystem to the given destination path.
	 * 
	 * The destination file must not exist yet. 
	 * 
	 * @param sourceFS
	 * 		the filesystem to copy from (may not be null).
	 * @param sourcePath
	 * 		the source file to copy from 
	 * @param destinationPath
	 * 		the destination file to copy to 
	 */
	public CopyDescription(FileSystem sourceFS, Path sourcePath, Path destinationPath) { 
		this(sourceFS, sourcePath, destinationPath, CopyMode.CREATE, false);
	}

	/**
	 * Perform a (optionally recursive) copy of a file or directory within a single {@link FileSystem}.
	 * 
	 * The destination file or directory must not exist yet. 
	 *  
	 * A recursive copy will only be performed if <code>recursive</code> is set to <code>true</code>. 
	 * If it is set to <code>false</code> only files will be accepted as source and destination paths.
	 *  
	 * @param sourcePath
	 * 		the source file or directory to copy from
	 * @param destinationPath
	 * 		the destination file directory to copy to 
	 * @param recursive
	 * 		if the copy must be done recursively
	 */
	public CopyDescription(Path sourcePath, Path destinationPath, boolean recursive) { 
		this(null, sourcePath, destinationPath,CopyMode.CREATE, true);
	}

	/**
	 * Perform a copy of two files within a single {@link FileSystem}.
	 * 
	 * The destination file must not exist yet. 
	 *  
	 * @param sourcePath
	 * 		the source file to copy from 
	 * @param destinationPath
	 * 		the destination file to copy to 
	 */
	public CopyDescription(Path sourcePath, Path destinationPath) { 
		this(null, sourcePath, destinationPath,CopyMode.CREATE, false);
	}
	
	public FileSystem getSourceFileSystem() {
		return sourceFileSystem;
	}

	public Path getSourcePath() {
		return sourcePath;
	}

	public Path getDestinationPath() {
		return destinationPath;
	}

	public CopyMode getOption() {
		return option;
	}

	public boolean isRecursive() {
		return recursive;
	}

	
}

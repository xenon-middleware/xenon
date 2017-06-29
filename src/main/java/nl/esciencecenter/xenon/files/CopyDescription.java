package nl.esciencecenter.xenon.files;

public class CopyDescription {

	private FileSystem sourceFileSystem;
	private Path sourcePath;
	
	private FileSystem destinationFileSystem;
	private Path destinationPath;
	
	private CopyOption option = CopyOption.CREATE;
		
	public CopyDescription(FileSystem sourceFS, Path sourcePath, FileSystem destinationFS, Path destinationPath, CopyOption option) { 
		this.sourceFileSystem = sourceFS;
		this.sourcePath = sourcePath;
		this.destinationFileSystem = destinationFS;
		this.destinationPath = destinationPath;
		this.option = option;
	}
	
	public CopyDescription(FileSystem sourceFS, Path sourcePath, FileSystem destinationFS, Path destinationPath) { 
		this(sourceFS, sourcePath, destinationFS, destinationPath, CopyOption.CREATE);
	}
		
	public CopyDescription(FileSystem filesystem, Path sourcePath, Path destinationPath, CopyOption option) { 
		this(filesystem, sourcePath, filesystem, destinationPath, option);
	}

	public CopyDescription(FileSystem filesystem, Path sourcePath, Path destinationPath) { 
		this(filesystem, sourcePath, filesystem, destinationPath);
	}
	
	public FileSystem getSourceFileSystem() {
		return sourceFileSystem;
	}

	public Path getSourcePath() {
		return sourcePath;
	}

	public FileSystem getDestinationFileSystem() {
		return destinationFileSystem;
	}

	public Path getDestinationPath() {
		return destinationPath;
	}

	public CopyOption getOption() {
		return option;
	}

}

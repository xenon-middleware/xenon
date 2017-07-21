/**
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
package nl.esciencecenter.xenon.filesystems;

import java.util.Set;


/**
 * FileAttributes represents a set of attributes of a path.
 */
public class PathAttributes {
	
	/** The path these attributes belong to */
	private Path path;
	
	 /** Is this a directory ? */
    private boolean isDirectory;

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (creationTime ^ (creationTime >>> 32));
		result = prime * result + (executable ? 1231 : 1237);
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		result = prime * result + (hidden ? 1231 : 1237);
		result = prime * result + (isDirectory ? 1231 : 1237);
		result = prime * result + (isOther ? 1231 : 1237);
		result = prime * result + (isRegular ? 1231 : 1237);
		result = prime * result + (isSymbolicLink ? 1231 : 1237);
		result = prime * result
				+ (int) (lastAccessTime ^ (lastAccessTime >>> 32));
		result = prime * result
				+ (int) (lastModifiedTime ^ (lastModifiedTime >>> 32));
		result = prime * result + ((owner == null) ? 0 : owner.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result
				+ ((permissions == null) ? 0 : permissions.hashCode());
		result = prime * result + (readable ? 1231 : 1237);
		result = prime * result + (int) (size ^ (size >>> 32));
		result = prime * result + (writable ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PathAttributes other = (PathAttributes) obj;
		if (creationTime != other.creationTime)
			return false;
		if (executable != other.executable)
			return false;
		if (group == null) {
			if (other.group != null)
				return false;
		} else if (!group.equals(other.group))
			return false;
		if (hidden != other.hidden)
			return false;
		if (isDirectory != other.isDirectory)
			return false;
		if (isOther != other.isOther)
			return false;
		if (isRegular != other.isRegular)
			return false;
		if (isSymbolicLink != other.isSymbolicLink)
			return false;
		if (lastAccessTime != other.lastAccessTime)
			return false;
		if (lastModifiedTime != other.lastModifiedTime)
			return false;
		if (owner == null) {
			if (other.owner != null)
				return false;
		} else if (!owner.equals(other.owner))
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		if (permissions == null) {
			if (other.permissions != null)
				return false;
		} else if (!permissions.equals(other.permissions))
			return false;
		if (readable != other.readable)
			return false;
		if (size != other.size)
			return false;
		if (writable != other.writable)
			return false;
		return true;
	}

	/** Is this a regular file ? */
    private boolean isRegular;

    /** Is this a symbolic link ? */
    private boolean isSymbolicLink;
    
    /** Is this an other type of file ? */
    private boolean isOther;
    
    /** Is the file executable ? */
    private boolean executable;

    /** Is the file readable ? */
    private boolean readable;

    /** Is the file writable ? */
    private boolean writable;

    /** Is the file hidden ? */
    private boolean hidden;

    /** The creation time of this file */
    private long creationTime;
    
    /** The last access time of this file */
    private long lastAccessTime;
    
    /** The last modified time of this file */
    private long lastModifiedTime;

    /** The size of this file */
    private long size;
    
    /** The owner of this file */
    private String owner;
    
    /** The group of this file */
    private String group;
    
    /** The permissions of this file (POSIX only) */
    private Set<PosixFilePermission> permissions;
    
    public PathAttributes() { 
    	// EMPTY
    }

    /**
     * Get the path these attributes belong to.
     * 
     * @return
     *   	the path these attributes belong to.
     */
    public Path getPath() {  
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}

    /**
     * Does the path refer to a directory ?
     * 
     * @return
     *          if the path refers to a directory.
     */
    public boolean isDirectory() {
		return isDirectory;
	}

	public void setDirectory(boolean isDirectory) {
		this.isDirectory = isDirectory;
	}

	/**
     * Does the path refer to a regular file ?
     * 
     * @return 
     *          if the path refers to a regular file.
     */
	public boolean isRegular() {
		return isRegular;
	}

	public void setRegular(boolean isRegular) {
		this.isRegular = isRegular;
	}


    /**
     * Does the path refer to a symbolic link ?
     * 
     * @return 
     *          if the path refers to a symbolic link.
     */
	public boolean isSymbolicLink() {
		return isSymbolicLink;
	}

	public void setSymbolicLink(boolean isSymbolicLink) {
		this.isSymbolicLink = isSymbolicLink;
	}

    /**
     * Is the path not a file, link or directory ?
     * 
     * @return 
     *          if the path does not refer to a file, link or directory.
     */
	public boolean isOther() {
		return isOther;
	}

	public void setOther(boolean isOther) {
		this.isOther = isOther;
	}
	
    /**
     * Does the path refer to an executable file ?
     * 
     * @return 
     *          if the path refers an executable file ?
     */
	public boolean isExecutable() {
		return executable;
	}

	public void setExecutable(boolean executable) {
		this.executable = executable;
	}

	
	   /**
     * Does the path refer to an readable file ?
     * 
     * @return 
     *          if the path refers an readable file ?
     */
	public boolean isReadable() {
		return readable;
	}

	public void setReadable(boolean readable) {
		this.readable = readable;
	}

    /**
     * Does the path refer to a writable file ?
     * 
     * @return 
     *          if the path refers a writable file ?
     */
	public boolean isWritable() {
		return writable;
	}

	public void setWritable(boolean writable) {
		this.writable = writable;
	}

    /**
     * Does the path refer to an hidden file ?
     * 
     * @return 
     *          if the path refers an hidden file ?
     */
	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	
	/**
     * Get the creation time for this file.
     * 
     * If creationTime is not supported by the adaptor, {@link #getLastModifiedTime()} will be returned instead.
     * 
     * @return 
     *          the creation time for this file.
     */
	public long getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
	}

    /**
     * Get the last access time for this file.
     * 
     * If lastAccessTime is not supported by the adaptor, use {@link #getLastModifiedTime()} will be returned instead.
     *
     * @return 
     *          the last access time for this file.
     */
	public long getLastAccessTime() {
		return lastAccessTime;
	}

	public void setLastAccessTime(long lastAccessTime) {
		this.lastAccessTime = lastAccessTime;
	}

    /**
     * Get the last modified time for this file.
     * 
     * If lastModifiedTime is not supported by the adaptor, <code>0</code> will be returned instead.
     *
     * @return 
     *          the last modified time for this file.
     */
	public long getLastModifiedTime() {
		return lastModifiedTime;
	}

	public void setLastModifiedTime(long lastModifiedTime) {
		this.lastModifiedTime = lastModifiedTime;
	}

	
    /**
     * Get the size of this file in bytes.
     * 
     * If the file is not a regular file, <code>0</code> will be returned. 
     * 
     * @return 
     *          the size of this file.
     */
	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	
    /**
     * Get the owner of this file.
     * 
     * @return 
     *          the owner of this file.
     * 
     * @throws AttributeNotSupportedException
     *          If the attribute is not supported by the adaptor.
     */
	public String getOwner() throws AttributeNotSupportedException {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	 /**
     * Get the group of this file.
     * 
     * @return 
     *          the group of this file.
     * 
     * @throws AttributeNotSupportedException
     *          If the attribute is not supported by the adaptor.
     */
	public String getGroup() throws AttributeNotSupportedException {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}


    /**
     * Get the permissions of this file.
     * 
     * @return 
     *          the permissions of this file.
     * 
     * @throws AttributeNotSupportedException
     *          If the attribute is not supported by the adaptor.
     */
	public Set<PosixFilePermission> getPermissions() throws AttributeNotSupportedException {
		return permissions;
	}

	public void setPermissions(Set<PosixFilePermission> permissions) {
		this.permissions = permissions;
	}   
	
	public String toString() { 
		return path.getAbsolutePath();
	}
}

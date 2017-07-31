/*
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.XenonProperties;
import nl.esciencecenter.xenon.adaptors.filesystems.PathAttributesImplementation;

public class MockFileSystem extends FileSystem {

	private boolean close = false;
	
	public class Callback extends FileSystem.CopyCallback {

		boolean initial = true;
		long maxBytes;

		Callback(boolean initial, long maxBytes){
			this.initial = initial;
			this.maxBytes = maxBytes;
		}

		Callback(long maxBytes){ 
			this.maxBytes = maxBytes;
		}

		@Override
		public boolean isCancelled() {
			return (bytesCopied >= maxBytes);
		} 
	}
	
	public Callback createCallback(boolean initial, long maxBytes){
		return new Callback(initial, maxBytes);
	}
	
	public Callback createCallback(long maxBytes){
		return new Callback(maxBytes);
	}
	
	abstract class Entry { 
		String name;
		PathAttributes attributes;
		
		Entry(String name, PathAttributes attributes) { 
			this.name = name;
			this.attributes = attributes;
		}
		
		void setAttributes(PathAttributes attributes){ 
			this.attributes = attributes;
		}
		
		PathAttributes getAttributes(){
			return attributes;
		}
		
		abstract boolean isDirectory(); 
		abstract boolean isFile(); 
	}
	
	class FileEntryOutputStream extends OutputStream {
		
		FileEntry entry; 
		ByteArrayOutputStream out;
		
		FileEntryOutputStream(FileEntry entry) { 
			this.entry = entry;
			this.out = new ByteArrayOutputStream();
		}
		
		@Override
		public void write(int b) throws IOException {
			out.write(b);
		} 

		@Override
		public void close() throws IOException { 
			out.close();
			entry.setData(out.toByteArray());
		}

		@Override
		public void flush() throws IOException { 
			out.flush();
		}
		
		@Override
		public void write(byte[] b) throws IOException { 
			out.write(b);
		}
		
		@Override
		public void	write(byte[] b, int off, int len) throws IOException {
			out.write(b, off, len);
		}		
	}
	
	class FileEntry extends Entry { 

		InputStream in;
		OutputStream out;
		byte [] data;
		
		public FileEntry(String name, PathAttributes attributes) { 
			super(name,attributes);
		}

		void setInput(InputStream in){ 
			this.in = in;
		}
		
		InputStream getInput() throws XenonException{
			if (in == null) {
				if (data == null ){ 
					throw new XenonException("TEST", "InputStream not set");
				}
				return new ByteArrayInputStream(data);				
			}
			return in;
		}

		void setOutput(OutputStream out){ 
			this.out = out;
		}
		
		OutputStream getOutput() throws XenonException {
			if (out == null) { 
				out = new FileEntryOutputStream(this);
			}
			
			return out;
		}

		@Override
		boolean isDirectory() {
			return false;
		}

		@Override
		boolean isFile() {
			return true;
		}

		void setData(byte[] data) {
			this.data = data;
		}
		
		byte [] getData() {
			return data;
		}
	} 
	
	class DirEntry extends Entry { 
		
		Map<String, Entry> entries = new HashMap<>();

		public DirEntry(String name, PathAttributes attributes) { 
			super(name, attributes);
		}

		@Override
		boolean isDirectory() {
			return true;
		}

		@Override
		boolean isFile() {
			return false;
		}
		
		FileEntry addFile(String name, PathAttributes attributes) throws XenonException { 
			if (entries.containsKey(name)) { 
				throw new PathAlreadyExistsException("TEST", "Path already exists: " + name);
			}

			System.err.println("Add file: " + attributes.getPath().toString() + " " + name);
			
			FileEntry e = new FileEntry(name, attributes);
			entries.put(name, e);
			return e;
		}
		
		DirEntry addDir(String name, PathAttributes attributes) throws XenonException { 
			if (entries.containsKey(name)) { 
				throw new PathAlreadyExistsException("TEST", "Directory already exists: " + name);
			}
			
			System.err.println("Add dir: " + attributes.getPath().toString() + " " + name);
			
			DirEntry e = new DirEntry(name, attributes);
			entries.put(name, e);
			return e;
		}
		
		FileEntry ensureFile(String name, PathAttributes attributes) throws XenonException { 
			
			Entry e = entries.get(name);
			
			if (e == null) { 
				e = new FileEntry(name, attributes);
				entries.put(name, e);
			} else { 
				if (!e.isFile()) { 
					throw new InvalidPathException("TEST", "Path is not a file: " + name);
				}
			}
			
			return (FileEntry)e;
		}
		
		DirEntry ensureDir(String name, PathAttributes attributes) throws XenonException { 
			
			Entry e = entries.get(name);
			
			if (e == null) { 
				e = new DirEntry(name, attributes);
				entries.put(name, e);
			} else { 
				if (!e.isDirectory()) { 
					throw new InvalidPathException("TEST", "Path is not a directory: " + name);
				}
			}
			
			return (DirEntry)e;
		}

		void deleteFile(String name) throws XenonException {
			
			Entry e = entries.get(name);
			
			if (e == null) { 
				throw new NoSuchPathException("TEST", "File not found: " + name);
			}  
			
			if (!e.isFile()) { 
				throw new InvalidPathException("TEST", "Path is not a file: " + name);
			}
			
			entries.remove(name);
		}

		void deleteDir(String name) throws XenonException {

			System.err.println("deleting " + name);
			
			Entry e = entries.get(name);
			
			if (e == null) { 
				throw new NoSuchPathException("TEST", "Directory not found: " + name);
			}  
			
			if (!e.isDirectory()) { 
				throw new InvalidPathException("TEST", "Path is not a directory: " + name);
			}
			
			System.err.println("Checking " + name);
			
			DirEntry dir = (DirEntry)e;
			
			if (!dir.entries.isEmpty()) {
				
				System.err.println("not empty " + name + " " + dir.entries.size());
				
				// If we have exaclty 2 entries "." and ".." it is fine
				if (dir.entries.size() == 2) { 
					for (Entry se : dir.entries.values()) { 
						
						System.err.println("not empty " + name + " " + se.name);
						
						if (!(se.name.equals(".") || se.name.equals(".."))) { 
							throw new DirectoryNotEmptyException("TEST", "Directory not empty: " + name + "/" + se.name);
						}
					}
				} else { 
					throw new DirectoryNotEmptyException("TEST", "Directory not empty: " + name);
				}
			}

			entries.remove(name);
		}

		List<PathAttributes> getList() {
			
			ArrayList<PathAttributes> result = new ArrayList<>();
			
			if (!entries.isEmpty()) {
				for (Entry e: entries.values()) { 
					result.add(e.attributes);
				}
			}
			
			System.out.println("List of " + attributes.getPath().getAbsolutePath() + " " + result);
			
			return result;
		}
	}
	
	private DirEntry root;

	public MockFileSystem(String uniqueID, String name, String location, Path entryPath, XenonProperties p) throws XenonException { 
		super(uniqueID, name, location, entryPath, p);
		root = new DirEntry("", getDirAttributes(new Path("/")));
		ensureDirectories(entryPath);		
	}

	
	public MockFileSystem(String uniqueID, String name, String location, Path entryPath) throws XenonException {
		this(uniqueID,name,location, entryPath, null);
	}
		
	public PathAttributes getDirAttributes(Path path) { 
		PathAttributesImplementation a = new PathAttributesImplementation();
		a.setPath(path);
		a.setDirectory(true);
		return a;
	}

	public PathAttributes getFileAttributes(Path path) { 
		PathAttributesImplementation a = new PathAttributesImplementation();
		a.setPath(path);
		a.setRegular(true);
		return a;
	}
	
	public synchronized boolean getClose() { 
		return close;
	}

	public synchronized Entry getEntry(Path path) throws XenonException { 
		
		//System.err.println("getEntry: " + path);
		
		if (path == null || path.isEmpty()) {
			//System.err.println("entry is root");
			return root;
		}

		//System.err.println("get sub entry " + path.getParent() + "  : " + path.getFileNameAsString());
		
		if ("..".equals(path.getFileNameAsString())) { 
			return (DirEntry) getEntry(path.getParent().getParent());
		}
		
		DirEntry current = (DirEntry) getEntry(path.getParent());
		
		if (".".equals(path.getFileNameAsString())) { 
			return current;
		}

		Entry e = current.entries.get(path.getFileNameAsString());
		
		if (e == null) { 
			throw new NoSuchPathException("TEST", "Cannot retrieve path: " + path);
		}
		
		return e;
	}
	
//	public Entry getEntry(Path path) throws XenonException { 
//		
//		if (path == null) { 
//			throw new InvalidPathException("TEST", "Cannot retrieve path: null");
//		}
//	
//		DirEntry current = root; 
//		
//		Path parent = path.getParent();
//		
//		if (parent != null) { 
//			for (Path p : path) { 
//			
//				String name = p.getFileNameAsString();
//			
//				Entry e = current.entries.get(name);
//
//				if (e == null) { 
//					throw new NoSuchPathException("TEST", "Cannot retrieve directory: " + name);
//				}
//				
//				if (!e.isDirectory()) { 
//					throw new InvalidPathException("TEST", "Path is not a directory: " + name);
//				}
//				
//				current = (DirEntry)e;
//			}
//		}
//
//		Entry e = current.entries.get(path.getFileNameAsString());
//
//		if (e == null) { 
//			throw new NoSuchPathException("TEST", "Cannot retrieve path: " + path);
//		}
//		
//		return e;
//	}

	public synchronized DirEntry getDirEntry(Path path) throws XenonException { 
		
		//System.err.println("getdDirEntry: " + path);
		
		Entry e = getEntry(path);
		
		if (!e.isDirectory()) { 
			throw new InvalidPathException("TEST", "Path is not a directory: " + path);
		}

		return (DirEntry) e;
	}
	
	public synchronized FileEntry getFileEntry(Path path) throws XenonException { 
		
		Entry e = getEntry(path);
		
		if (!e.isFile()) { 
			throw new InvalidPathException("TEST", "Path is not a file: " + path);
		}

		return (FileEntry) e;
	}

	public synchronized byte [] getFileContent(Path path) throws XenonException {
		
		FileEntry e = getFileEntry(path);
		
		if (e.out == null) { 
			throw new XenonException("TEST", "File does not have content: " + path);
		}
		
		return ((ByteArrayOutputStream) e.out).toByteArray();
	}
		
	
	public synchronized DirEntry ensureDirectories(Path path) throws XenonException { 
		
		if (path == null || path.isEmpty()) { 
			return root;
		}

		DirEntry current = root; 
		
		Path tmp = new Path("/");
		
		for (Path p : path) { 
			tmp = tmp.resolve(p);
			current = current.ensureDir(p.getFileNameAsString(), getDirAttributes(tmp));
		}
		
		return current;
	}
	
	public synchronized FileEntry ensureFile(Path path) throws XenonException { 
		
		if (path == null) { 
			throw new InvalidPathException("TEST", "Path is null");
		}

		return ensureDirectories(path.getParent()).ensureFile(path.getFileNameAsString(), getFileAttributes(path));
	}		
		
	public synchronized void addAttributes(Path path, PathAttributes att) throws XenonException { 
		ensureFile(path).setAttributes(att);
	}

	public synchronized void addData(Path path, byte [] data) throws XenonException { 
		getFileEntry(path).setData(data);
	}
	
	public synchronized byte [] getData(Path path) throws XenonException { 
		return getFileEntry(path).getData();
	}
	
	public synchronized void addInputStream(Path path, InputStream in) throws XenonException { 
		ensureFile(path).setInput(in);
	}

	public synchronized void addOutputStream(Path path, OutputStream out) throws XenonException { 
		ensureFile(path).setOutput(out);
	}

	@Override
	public synchronized void close() throws XenonException {
		this.close = true;
		super.close();
	}

	@Override
	public synchronized boolean isOpen() throws XenonException {
		return !close;
	}

	@Override
	public void rename(Path source, Path target) throws XenonException {
		throw new XenonException("TEST", "Not implememnted");
	}

	@Override
	public synchronized void createDirectory(Path dir) throws XenonException {
		//System.err.println("Create dir: " + dir);
		getDirEntry(dir.getParent()).addDir(dir.getFileNameAsString(), getDirAttributes(dir));
	}

	@Override
	public synchronized void createFile(Path file) throws XenonException {
		//System.err.println("Create file: " + file);
		getDirEntry(file.getParent()).addFile(file.getFileNameAsString(), getFileAttributes(file));
	}

	@Override
	public void createSymbolicLink(Path link, Path path) throws XenonException {
		throw new XenonException("TEST", "Not implemented");
	}

	@Override
	public boolean exists(Path path) throws XenonException {
		try { 
			getEntry(path);
			return true;
		} catch (XenonException e) {
			return false;
		}
	}

	@Override
	public synchronized InputStream readFromFile(Path file) throws XenonException {
		return getFileEntry(file).getInput();
	}

	@Override
	public synchronized OutputStream writeToFile(Path file, long size) throws XenonException {
		
		System.out.println("FileSystem(" + getAdaptorName() + ").writeToFile " + file); 
		
		if (!exists(file)){ 
			createFile(file);
		}
		
		return getFileEntry(file).getOutput();
	}

	@Override
	public OutputStream writeToFile(Path file) throws XenonException {
		return writeToFile(file, 0);
	}

	@Override
	public OutputStream appendToFile(Path file) throws XenonException {
		throw new XenonException("TEST", "Not implemented");
	}

	@Override
	public synchronized PathAttributes getAttributes(Path path) throws XenonException {
		return getEntry(path).getAttributes();
	}

	@Override
	public Path readSymbolicLink(Path link) throws XenonException {
		throw new XenonException("TEST", "Not implemented");
	}

	@Override
	public void setPosixFilePermissions(Path path, Set<PosixFilePermission> permissions) throws XenonException {
		throw new XenonException("TEST", "Not implemented");
	}

	@Override
	protected synchronized void deleteFile(Path file) throws XenonException {
		getDirEntry(file.getParent()).deleteFile(file.getFileNameAsString());
	}

	@Override
	protected synchronized void deleteDirectory(Path dir) throws XenonException {
		getDirEntry(dir.getParent()).deleteDir(dir.getFileNameAsString());
	}

	@Override
	protected synchronized List<PathAttributes> listDirectory(Path dir) throws XenonException {
		return getDirEntry(dir).getList();
	}
}

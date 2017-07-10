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

/**
 * Copy represents an asynchronous copy operation.
 * <p>
 * A <code>CopyHandle</code> is returned as the result of the {@link FileSystem#copy(Path, FileSystem, Path, CopyMode, boolean) FileSystem.copy} 
 * This <code>CopyHandle</code> can then 
 * be used to retrieve the status of the copy operation using {@link FileSystem#getStatus(CopyHandle) FileSystem.getStatus} or cancel it 
 * using {@link FileSystem#cancel(CopyHandle) Files.cancel}.
 * </p>
 */
public interface CopyHandle {

	/**
	 * Returns the {@link FileSystem} this copy is using as a source.
	 * 
	 * @return
	 * 		the {@link FileSystem} this copy is using as a source.
	 */
	public FileSystem getSourceFileSystem();
	
	/**
	 * Returns the {@link Path} this copy is using as a source.
	 * 
	 * @return
	 * 		the {@link Path} this copy is using as a source.
	 */
    public Path getSourcePath();

	/**
	 * Returns the {@link FileSystem} this copy is using as a destination.
	 * 
	 * @return
	 * 		the {@link FileSystem} this copy is using as a destination.
	 */
    public FileSystem getDestinatonFileSystem();

	/**
	 * Returns the {@link Path} this copy is using as a destination.
	 * 
	 * @return
	 * 		the {@link Path} this copy is using as a destination.
	 */
    public Path getDestinationPath();
	
	/**
	 * Returns the {@link CopyMode} that determines what to do if the destination exists.
	 * 
	 * @return
	 * 		the {@link CopyMode} that determines what to do if the destination exists.		
	 */
	public CopyMode getMode();
	
	/**
	 * Returns if this this copy is recursive.
	 * 	  
	 * @return
	 * 		if this copy is recursive.
	 */
	public boolean isRecursive();
}

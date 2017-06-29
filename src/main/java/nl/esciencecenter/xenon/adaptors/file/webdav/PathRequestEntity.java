package nl.esciencecenter.xenon.adaptors.file.webdav;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.httpclient.methods.RequestEntity;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.Path;

public class PathRequestEntity implements RequestEntity {

	private final FileSystem filesystem;
	private final Path file;
	
	public PathRequestEntity(FileSystem filesystem, Path file) {
		super();
		this.filesystem = filesystem;
		this.file = file;
	}

	@Override
	public boolean isRepeatable() {
		return true;
	}

	@Override
	public void writeRequest(OutputStream out) throws IOException {
		
		byte[] tmp = new byte[4096];
		int i = 0;

		InputStream in;
		
		try {
			in = filesystem.newInputStream(file);
		} catch (XenonException e) {
			throw new IOException("Failed to open file", e);
		}

		try {
			while ((i = in.read(tmp)) >= 0) {
				out.write(tmp, 0, i);
			}        
		} finally {
			in.close();
		}
	}

	@Override
	public long getContentLength() {
		try {
			return filesystem.getAttributes(file).size();
		} catch (XenonException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public String getContentType() {
		return null;
	}

}

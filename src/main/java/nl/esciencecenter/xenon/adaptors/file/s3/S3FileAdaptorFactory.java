package nl.esciencecenter.xenon.adaptors.file.s3;

import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.engine.files.FileAdaptor;
import nl.esciencecenter.xenon.engine.files.FileAdaptorFactory;
import nl.esciencecenter.xenon.engine.files.FilesEngine;

public class S3FileAdaptorFactory implements FileAdaptorFactory {

	@Override
	public String getPropertyPrefix() {
        return S3Properties.PREFIX;
	}

	@Override
	public XenonPropertyDescription[] getSupportedProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FileAdaptor createAdaptor(FilesEngine engine,
			Map<String, String> properties) throws XenonException {
		// TODO Auto-generated method stub
		return null;
	}

}

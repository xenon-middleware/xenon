package nl.esciencecenter.xenon.adaptors;

import nl.esciencecenter.xenon.XenonPropertyDescription;

public class MockAdaptor extends Adaptor {

    public MockAdaptor(String name, String description, String[] locations, XenonPropertyDescription[] properties) {
        super(name, description, locations, properties);
    }

    @Override
    public Class[] getSupportedCredentials() {
        return null;
    }
}

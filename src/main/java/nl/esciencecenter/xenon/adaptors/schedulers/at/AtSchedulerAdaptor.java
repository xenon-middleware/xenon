package nl.esciencecenter.xenon.adaptors.schedulers.at;

import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.adaptors.schedulers.ScriptingSchedulerAdaptor;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.schedulers.Scheduler;

public class AtSchedulerAdaptor extends ScriptingSchedulerAdaptor {

    public AtSchedulerAdaptor(String name, String description, String[] locations, XenonPropertyDescription[] properties) throws XenonException {
        super(name, description, locations, properties);
        // TODO Auto-generated constructor stub

    }

    @Override
    public Scheduler createScheduler(String location, Credential credential, Map<String, String> properties) throws XenonException {
        // TODO Auto-generated method stub

        // Use "at -V" to check if at exists on target machine
        return null;
    }

}

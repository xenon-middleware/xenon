package nl.esciencecenter.octopus.jobs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JavaJobDescription extends JobDescription {

    private List<String> javaOptions = new ArrayList<String>();

    private Map<String, String> javaSystemProperties = new HashMap<String, String>();

    private String javaMain;

    private List<String> javaArguments;

    private String javaClassPath;

    /**
     * Create a {@link JavaJobDescription}, which describes the java
     * application.
     */
    public JavaJobDescription() {
        super();
    }

    /**
     * Returns the jvm options.
     * 
     * @return the jvm options.
     */
    public List<String> getJavaOptions() {
        return javaOptions;
    }

    /**
     * Sets the jvm options.
     * 
     * @param options
     *            the jvm options.
     */
    public void setJavaOptions(String... options) {
        this.javaOptions.clear();
        this.javaOptions.addAll(Arrays.asList(options));
    }

    /**
     * Returns the java system properties.
     * 
     * @return the java system properties.
     */
    public Map<String, String> getJavaSystemProperties() {
        return javaSystemProperties;
    }

    /**
     * Sets the system properties. A system property should be passed as a key
     * value pair <"a", "b">, not as <"-Da", "b">, Octopus will add the -D to
     * the property.
     * 
     * @param systemProperties
     *            the system properties.
     */
    public void setJavaSystemProperties(Map<String, String> systemProperties) {
        this.javaSystemProperties = systemProperties;
    }

    /**
     * Adds a system property to the current set of system properties. The key
     * of the system property should not start with "-D".
     * 
     * @param key
     *            the key of the system property to be added
     * @param value
     *            the value belonging to the key of the system property to be
     *            added
     */
    public void addJavaSystemProperty(String key, String value) {
        if (javaSystemProperties == null) {
            javaSystemProperties = new HashMap<String, String>();
        }
        javaSystemProperties.put(key, value);
    }

    /**
     * Returns the main class of the java application.
     * 
     * @return the main class.
     */
    public String getJavaMain() {
        return javaMain;
    }

    /**
     * Sets the main class.
     * 
     * @param main
     *            the main class.
     */
    public void setJavaMain(String main) {
        this.javaMain = main;
    }

    /**
     * Returns the arguments for the main class.
     * 
     * @return the arguments for the main class
     */
    public List<String> getJavaArguments() {
        return javaArguments;
    }

    /**
     * Sets the arguments of the java main class.
     * 
     * @param javaArguments
     *            the arguments of the java main class.
     */
    public void setJavaArguments(String... javaArguments) {
        this.javaArguments.clear();
        this.javaArguments.addAll(Arrays.asList(javaArguments));
    }

    /**
     * <b>This method should not be used</b>. This method will ignore all
     * arguments. The methods {@link #setJavaClassPath(String)},
     * {@link #setJavaOptions(String[])}, {@link #setJavaSystemProperties(Map)},
     * {@link #setJavaMain(String)} and {@link #setJavaArguments(String[])}
     * should be used to construct the command line arguments.
     * 
     * @param arguments
     */
    public void setArguments(String... arguments) {
    }

    /**
     * Constructs the command line arguments from the class path, the jvm
     * options, the system properties, the main and the java arguments.
     * 
     * @return the command line arguments
     */
    public ArrayList<String> getArguments() {
        ArrayList<String> result = new ArrayList<String>();
        if (getJavaOptions() != null) {
            for (String option : getJavaOptions()) {
                result.add(option);
            }
        }
        if (getJavaClassPath() != null) {
            result.add("-classpath");
            result.add(getJavaClassPath());
        }

        if (getJavaSystemProperties() != null) {
            Map<String, String> properties = getJavaSystemProperties();
            for (String key : properties.keySet()) {
                // null values ignored
                if (properties.get(key) != null) {
                    result.add("-D" + key + "=" + properties.get(key));
                }
            }
        }
        if (getJavaMain() != null) {
            result.add(getJavaMain());
        } else {
            return null;
        }
        if (getJavaArguments() != null) {
            for (String javaArgument : getJavaArguments()) {
                result.add(javaArgument);
            }
        }
        return result;
    }

    /**
     * Returns the executable. If no executable is set the default executable
     * will be "java".
     * 
     * @return Returns the executable.
     */
    public String getExecutable() {
        if (super.getExecutable() == null) {
            return "java";
        } else {
            return super.getExecutable();
        }
    }

    /**
     * Returns the java class path.
     * 
     * @return the java class path.
     */
    public String getJavaClassPath() {
        return javaClassPath;
    }

    /**
     * Sets the java class path.
     * 
     * @param javaClassPath
     *            the class path to be set.
     */
    public void setJavaClassPath(String javaClassPath) {
        this.javaClassPath = javaClassPath;
    }

}
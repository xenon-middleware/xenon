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
package nl.esciencecenter.xenon.utils;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.FileSystemAdaptorDescription;
import nl.esciencecenter.xenon.schedulers.Scheduler;
import nl.esciencecenter.xenon.schedulers.SchedulerAdaptorDescription;

/**
 * Generates html snippet with options of all adaptors.
 */
public class AdaptorDocGenerator {
    public static void main(String[] args) throws FileNotFoundException {
        if (args.length != 1) {
            System.err.println("Name of output file is required!");
            System.exit(1);
        }

        AdaptorDocGenerator generator = new AdaptorDocGenerator();
        generator.toFile(args[0]);
    }

    private void toFile(String filename) {
        PrintWriter out = null;
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
        } catch (IOException e) {
            System.err.println("Failed to open output file: " + filename + " " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }

        try {
            generate(out);
        } catch (XenonException e) {
            System.err.println("Failed to generate adaptor documentation: " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }

        out.flush();
        out.close();
    }

    public void generate(PrintWriter out) throws XenonException {
        out.println("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>Xenon Javadoc overview</title></head><body>");
        out.println("A middleware abstraction library that provides a simple programming interface to various compute and storage resources.");
        out.println("<p>The main entry points are<ul>");
        out.println("<li><a href=\"nl/esciencecenter/xenon/schedulers/Scheduler.html\">nl.esciencecenter.xenon.schedulers.Scheduler</a></li>");
        out.println("<li><a href=\"nl/esciencecenter/xenon/filesystems/FileSystem.html\">nl.esciencecenter.xenon.filesystems.FileSystem</a></li>");
        out.println("</ul></p>");
        out.println("<h1>Adaptor Documentation</h1>");
        out.println("<p>This section contains the adaptor documentation which is generated "
                + "from the information provided by the adaptors themselves.</p>");

        generateTOC(out);
        generateSchedulers(out);
        generateFileSystems(out);

        out.println("</body></html>");
    }

    private void generateTOC(PrintWriter out) {
        out.println("<ul>");
        out.println("<li><a href=\"#schedulers\">Schedulers</a><ul>");
        for (String name: Scheduler.getAdaptorNames()) {
            out.println(String.format("<li><a href=\"#%s\">%s</a></li>", name, name));
        }
        out.println("</ul></li>");
        out.println("<li><a href=\"#filesystems\">File systems</a><ul>");
        for (String name: FileSystem.getAdaptorNames()) {
            out.println(String.format("<li><a href=\"#%s\">%s</a></li>", name, name));
        }
        out.println("</ul></li>");
        out.println("</ul>");
    }

    private void generateFileSystems(PrintWriter out) throws XenonException {
        out.println("<h2><a name=\"filesystems\">File systems<a/></h2>");
        for (FileSystemAdaptorDescription description : FileSystem.getAdaptorDescriptions()) {
            generateFileSystem(description, out);
        }
    }

    private void generateFileSystem(FileSystemAdaptorDescription description, PrintWriter out) {
        out.println(String.format("<h2><a name=\"%s\">%s</a></h2>", description.getName(), description.getName()));
        out.println(String.format("<p>%s</p>", description.getDescription()));
        out.println("<h4>Supported locations:</h4>");
        out.println("Supported locations for <i>FileSystem.create(type, location, credential, properties)</i> method.<il>");
        for (String supportedLocation : description.getSupportedLocations()) {
            out.println(String.format("<li>%s</li>", supportedLocation));
        }
        out.println("</ul>");
        out.println("<h4>Supported properties:</h4>");
        if (description.getSupportedProperties().length > 0) {
            out.println("Supported properties for <i>FileSystem.create(type, location, credential, properties)</i> method.");
            out.println("<table border=1><tr><th>Name</th><th>Description</th><th>Expected type</th><th>Default</th></tr>");
            for (XenonPropertyDescription prop : description.getSupportedProperties()) {
                generateSupportedProperty(prop, out);
            }
            out.println("</table>");
        } else {
            out.println("No properties.");
        }
        out.println("<h4>Supported features</h4><ul>");
        out.println(String.format("<li>Read symbolic links: %s</li>", description.canReadSymboliclinks()));
        out.println(String.format("<li>Create symbolic links: %s</li>", description.canCreateSymboliclinks()));
        out.println(String.format("<li>Third party copy: %s</li>", description.supportsThirdPartyCopy()));
        out.println("</ul>");
    }

    private void generateSupportedProperty(XenonPropertyDescription prop, PrintWriter out) {
        out.println("<tr>");
        out.println(String.format("<td><pre>%s</pre></td>", prop.getName()));
        out.println(String.format("<td>%s</td>", prop.getDescription()));
        out.println(String.format("<td>%s</td>", prop.getType()));
        out.println(String.format("<td>%s</td>", prop.getDefaultValue()));
        out.println("</tr>");
    }

    private void generateSchedulers(PrintWriter out) throws XenonException {
        out.println("<h2><a name=\"schedulers\">Schedulers</a></h2>");
        for (SchedulerAdaptorDescription description : Scheduler.getAdaptorDescriptions()) {
            generateScheduler(description, out);
        }
    }

    private void generateScheduler(SchedulerAdaptorDescription description, PrintWriter out) {
        out.println(String.format("<h2><a name=\"%s\">%s</a></h2>", description.getName(), description.getName()));
        out.println(String.format("<p>%s</p>", description.getDescription()));
        out.println("<h4>Supported locations:</h4>");
        out.println("Supported locations for <i>Scheduler.create(type, location, credential, properties)</i> method.<ul>");
        for (String supportedLocation : description.getSupportedLocations()) {
            out.println(String.format("<li>%s</li>", supportedLocation));
        }
        out.println("</ul>");
        out.println("<h4>Supported properties:</h4>");
        if (description.getSupportedProperties().length > 0) {
            out.println("Supported properties for <i>Scheduler.create(type, location, credential, properties)</i> method.");
            out.println("<table border=1><tr><th>Name</th><th>Description</th><th>Expected type</th><th>Default</th></tr>");
            for (XenonPropertyDescription prop : description.getSupportedProperties()) {
                generateSupportedProperty(prop, out);
            }
            out.println("</table>");
        } else {
            out.println("No properties.");
        }
        out.println("<h4>Supported features</h4><ul>");
        out.println(String.format("<li>Batch: %s</li>", description.supportsBatch()));
        out.println(String.format("<li>Interactive: %s</li>", description.supportsInteractive()));
        out.println(String.format("<li>Online: %s</li>", description.isOnline()));
        out.println("</ul>");
    }
}

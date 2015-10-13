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

package nl.esciencecenter.xenon.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import nl.esciencecenter.xenon.AdaptorStatus;
import nl.esciencecenter.xenon.Xenon;
import nl.esciencecenter.xenon.XenonFactory;
import nl.esciencecenter.xenon.XenonPropertyDescription;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 *
 */
public class AdaptorDocGenerator {

    private static PrintWriter out = null;

    private static void printPropertyDescription(XenonPropertyDescription d) {

        out.println("<tr>");
        out.println("<td>" + "<pre>" + d.getName() + "</pre></td>");
        out.println("<td>" + d.getDescription() + "</td>");
        out.println("<td>" + d.getType() + "</td>");
        out.println("<td>" +  d.getDefaultValue() + "</td>");
        out.println("<td>" + d.getLevels() + "</td>");
        out.println("</tr>");
    }

    private static void printAdaptorDoc(AdaptorStatus a) {

        out.println("<h2><a name=\"" + a.getName()  + "\">Adaptor: " + a.getName() + "</a></h2>");


        out.println("<p>");
        out.println(a.getDescription());
        out.println("</p");

        out.println("<h4>Supported schemes:</h4><ul>");

        String[] schemes = a.getSupportedSchemes();

        for (int i = 0; i < schemes.length; i++) {
            out.print("<li>" + schemes[i] + "</li>");
        }

        out.println("</ul>");

        out.println("<h4> Supported locations:</h4><ul>");

        String[] locations = a.getSupportedLocations();

        for (int i = 0; i < locations.length; i++) {
            out.print("<li>" + locations[i] + "</li>");
        }

        out.println("</ul>");

        out.println("<h4> Supported properties: </h4>");

        XenonPropertyDescription[] properties = a.getSupportedProperties();

        out.println("<table border=1><tr><th>Name</th><th>Description</th><th>Expected type</th><th>Default</th><th>Valid for</th></tr>");
        for (XenonPropertyDescription d : properties) {
            printPropertyDescription(d);
        }

        out.println("</table>");
    }

    public static void main(String[] args) {

        if (args.length != 1) {
            System.err.println("Name of output file is required!");
            System.exit(1);
        }

        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(args[0])));
        } catch (IOException e) {
            System.err.println("Failed to open output file: " + args[0] + " " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }

        try {
            Xenon xenon = XenonFactory.newXenon(null);
            AdaptorStatus[] adaptors = xenon.getAdaptorStatuses();

            out.println("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>Insert title here</title></head><body>");
            out.println("A middleware abstraction library that provides a simple programming interface to various compute and storage resources.");
            out.println("<h1>Adaptor Documentation</h1>");
            out.println("<p>This section contains the adaptor documentation which is generated "
                    + "from the information provided by the adaptors themselves.</p>");

            out.print("Xenon currently supports " + adaptors.length + " adaptors: <ul>");

            for (AdaptorStatus a : adaptors) {
                out.println("<li><a href=\"#" + a.getName() + "\">" + a.getName() + "</a></li>");
            }

            out.println("</ul>");
            out.println("");

            for (AdaptorStatus a : adaptors) {
                printAdaptorDoc(a);
            }

            XenonFactory.endAll();

            out.println("</body></html>");

        } catch (Exception e) {
            System.err.println("Failed to generate adaptor documentation: " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }

        out.flush();
        out.close();
    }
}

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
package nl.esciencecenter.xenon.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import nl.esciencecenter.xenon.FileSystem;

public class AdaptorDocGenerator {
    static public void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Name of output file is required!");
            System.exit(1);
        }
        String filename = args[0];

        try (PrintWriter out=new PrintWriter(new BufferedWriter(new FileWriter(filename)))) {
            out.println("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>Xenon Javadoc overview</title></head><body>");

            out.println("A middleware abstraction library that provides a simple programming interface to various compute and storage resources.");
            out.println("<p>The main entry points are<ul>");
            out.println("<li><a href=\"nl/esciencecenter/xenon/schedulers/Scheduler.html\">nl.esciencecenter.xenon.schedulers.Scheduler</a></li>");
            out.println("<li><a href=\"nl/esciencecenter/xenon/filesystems/FileSystem.html\">nl.esciencecenter.xenon.filesystems.FileSystem</a></li>");
            out.println("</ul></p>");
            out.println("<h1>Adaptor Documentation</h1>");
            out.println("<p>This section contains the adaptor documentation which is generated "
                + "from the information provided by the adaptors themselves.</p>");

            out.println("</body></html>");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.err.println("Running doc generator");
        new FileSystem();
    }
}

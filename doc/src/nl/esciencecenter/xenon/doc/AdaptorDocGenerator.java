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

package nl.esciencecenter.xenon.doc;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import nl.esciencecenter.xenon.AdaptorStatus;
import nl.esciencecenter.xenon.Cobalt;
import nl.esciencecenter.xenon.CobaltFactory;
import nl.esciencecenter.xenon.CobaltPropertyDescription;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
public class AdaptorDocGenerator {
    
    private static PrintWriter out = null;
    
    private static void printPropertyDescription(CobaltPropertyDescription d) {

        out.println("__`" + d.getName() + "`__\n");
        out.println(d.getDescription() + "\n");
        out.println("- Expected type: " + d.getType() + "\n");
        out.println("- Default value: " + d.getDefaultValue() + "\n");
        out.println("- Valid for: " + d.getLevels() + "\n\n");
    }

    private static void printAdaptorDoc(AdaptorStatus a) {

        out.println("Adaptor: " + a.getName());
        out.println("--------");

        out.println();
        out.println(a.getDescription());
        out.println();

        out.println("#### Supported schemes: ####");

        String[] schemes = a.getSupportedSchemes();

        String comma = "";

        for (int i = 0; i < schemes.length; i++) {
            out.print(comma + schemes[i]);
            comma = ", ";
        }

        out.println();
        out.println();

        out.println("#### Supported locations: ####");

        String[] locations = a.getSupportedLocations();

        comma = "";

        for (int i = 0; i < locations.length; i++) {
            out.print(comma + locations[i]);
            comma = ", ";
        }

        out.println();
        out.println();
        
        out.println("#### Supported properties: ####\n\n");

        CobaltPropertyDescription[] properties = a.getSupportedProperties();

        for (CobaltPropertyDescription d : properties) {
            printPropertyDescription(d);
        }

        out.println();
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
            Cobalt cobalt = CobaltFactory.newCobalt(null);
            AdaptorStatus[] adaptors = cobalt.getAdaptorStatuses();

            out.println("Appendix A: Adaptor Documentation");
            out.println("---------------------------------");
            out.println("");
            out.println("This section contains the adaptor documentation which is generated "
                    + "from the information provided by the adaptors themselves.");
            out.println("");
            out.print("Cobalt currently supports " + adaptors.length + " adaptors: ");

            String comma = "";

            for (AdaptorStatus a : adaptors) {
                out.print(comma + a.getName());
                comma = ", ";
            }

            out.println(".");
            out.println();

            for (AdaptorStatus a : adaptors) {
                printAdaptorDoc(a);
            }

            CobaltFactory.endAll();

        } catch (Exception e) {
            System.err.println("Failed to generate adaptor documentation: " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
        
        out.flush();
        out.close();
    }
}

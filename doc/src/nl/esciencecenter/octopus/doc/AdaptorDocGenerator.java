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

package nl.esciencecenter.octopus.doc;

import nl.esciencecenter.octopus.AdaptorStatus;
import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.OctopusPropertyDescription;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
public class AdaptorDocGenerator {

    private static void printPropertyDescription(OctopusPropertyDescription d) {

        System.out.println("### `" + d.getName() + "` ###\n");
        System.out.println(d.getDescription() + "\n");
        System.out.println("- Expected type: " + d.getType() + "\n");
        System.out.println("- Default value: " + d.getDefaultValue() + "\n");
        System.out.println("- Valid for: " + d.getLevels() + "\n\n");
    }

    private static void printAdaptorDoc(AdaptorStatus a) {

        System.out.println("Adaptor: " + a.getName());
        System.out.println("--------");

        System.out.println();
        System.out.println(a.getDescription());
        System.out.println();

        System.out.println("### Supported schemes: ###");

        String[] schemes = a.getSupportedSchemes();

        String comma = "";

        for (int i = 0; i < schemes.length; i++) {
            System.out.print(comma + schemes[i]);
            comma = ", ";
        }

        System.out.println();
        System.out.println();

        System.out.println("### Supported properties: ###\n\n");

        OctopusPropertyDescription[] properties = a.getSupportedProperties();

        for (OctopusPropertyDescription d : properties) {
            printPropertyDescription(d);
        }

        System.out.println();
    }

    public static void main(String[] args) {

        try {
            Octopus octopus = OctopusFactory.newOctopus(null);
            AdaptorStatus[] adaptors = octopus.getAdaptorStatuses();

            System.out.println("Octopus Adaptor Documentation");
            System.out.println("=============================");
            System.out.println("");
            System.out.println("This document contains the adaptor documentation. This documentation is generated "
                    + "from the information provided by the adaptors themselves.");
            System.out.println("");
            System.out.print("Octopus currently supports " + adaptors.length + " adaptors: ");

            String comma = "";

            for (AdaptorStatus a : adaptors) {
                System.out.print(comma + a.getName());
                comma = ", ";
            }

            System.out.println(".");
            System.out.println();

            for (AdaptorStatus a : adaptors) {
                printAdaptorDoc(a);
            }

            OctopusFactory.endAll();

        } catch (Exception e) {
            System.err.println("Failed to generate adaptor documentation: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }
}

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

package nl.esciencecenter.xenon.adaptors.gftp;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Set;
import java.util.TimeZone;

import nl.esciencecenter.xenon.engine.util.PosixFileUtils;
import nl.esciencecenter.xenon.files.PosixFilePermission;
import nl.esciencecenter.xenon.files.RelativePath;

import org.globus.ftp.FeatureList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Various Globus and Other Grid FTP util methods.<br>
 * @author Piter T. de Boer
 */
public class GftpUtil {

    public static final Logger logger = LoggerFactory.getLogger(GftpUtil.class);

    public static final String GSIFTP_SCHEME = "gsiftp";

    public static final String GFTP_SCHEME = "gftp";

    public static boolean isXDir(String dirName) {

        // Current Dir
        if (dirName.compareTo(".") == 0) {
            return true;
        }
        // Parent Dir
        if (dirName.compareTo("..") == 0) {
            return true;
        }

        return false;
    }

    /**
     * Convert (Grid) FTP Time String "YYYYMMDDhhmmss[.zzz]" to millis since Epoch.
     */
    public static long timeStringToMillis(String val) {
        return timeStringtoCalender(val).getTimeInMillis();
    }

    /**
     * Convert (Grid) FTP Time String "YYYYMMDDhhmmss[.zzz]" to Java Date.
     */
    public static java.util.Date timeStringToDate(String val) {
        return timeStringtoCalender(val).getTime();
    }

    /**
     * Convert (Grid) FTP Time String "YYYYMMDDhhmmss[.zzz]" to java Calendar.
     */
    public static java.util.Calendar timeStringtoCalender(String val) {
        // FTP date value is in YYYYMMDDhhmmss
        int YYYY = Integer.valueOf(val.substring(0, 4));
        int MM = Integer.valueOf(val.substring(4, 6));
        int DD = Integer.valueOf(val.substring(6, 8));
        int hh = Integer.valueOf(val.substring(8, 10));
        int mm = Integer.valueOf(val.substring(10, 12));
        int ss = Integer.valueOf(val.substring(12, 14));

        // todo: optional milli seconds after seconds.  

        // GMT TIMEZONE:
        TimeZone gmtTZ = TimeZone.getTimeZone("GMT-0:00");
        Calendar cal = new GregorianCalendar(gmtTZ);

        // O-be-1-kenobi: month number in GregorianCalendar is zero-based
        cal.set(YYYY, MM - 1, DD, hh, mm, ss);

        return cal;
    }

    public static String basename(String filepath) {
        return new RelativePath(filepath).getFileNameAsString();
    }

    public static String dirname(String filepath) {
        return new RelativePath(filepath).getParent().getAbsolutePath();
    }

    public static Set<PosixFilePermission> unixModeToPosixFilePermissions(int mode) {
        return PosixFileUtils.bitsToPermissions(mode);
    }

    public static String toString(FeatureList features) {

        String str = "FeatureList:[";
        boolean first = true;

        // Use reflection to check String Constants as FeatureList doesn't support them:
        Field[] fields = FeatureList.class.getFields();
        for (Field field : fields) {

            // skip private parts 
            if (field.isAccessible() == false) {
                //continue; 
            }

            String name = field.getName();
            if (features.contains(name)) {
                if (first) {
                    str += name;
                    first = false;
                } else {
                    str += "," + name;
                }
            }
        }

        return str + "]";
    }
}

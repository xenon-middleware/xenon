package nl.esciencecenter.octopus.adaptors.local;

import java.nio.file.LinkOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import nl.esciencecenter.octopus.exceptions.OctopusRuntimeException;
import nl.esciencecenter.octopus.files.CopyOption;
import nl.esciencecenter.octopus.files.OpenOption;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.PosixFilePermission;

class LocalUtils {

    //TODO: test this function
    static boolean exists(String path) {

        if (path.startsWith("/~")) {
            path = System.getProperty("user.home") + "/" + path.substring(2);
        }
    
        java.nio.file.Path tmp = java.nio.file.FileSystems.getDefault().getPath(path);
        
        return java.nio.file.Files.exists(tmp, LinkOption.NOFOLLOW_LINKS);
    }
    
    static java.nio.file.Path javaPath(AbsolutePath path) {
        String string = path.getPath();
        if (string.startsWith("/~")) {
            string = System.getProperty("user.home") + "/" + string.substring(2);
        }
        return java.nio.file.FileSystems.getDefault().getPath(string);
    }

    static java.nio.file.CopyOption[] javaCopyOptions(CopyOption... options) {
        ArrayList<java.nio.file.CopyOption> result = new ArrayList<java.nio.file.CopyOption>();

        for (int i = 0; i < options.length; i++) {
            try {
                result.add(java.nio.file.StandardCopyOption.valueOf(options[i].toString()));
            } catch (IllegalArgumentException e) {
                throw new UnsupportedOperationException("Option " + options[i] + " not recognized by Local adaptor");
            }

        }
        return result.toArray(new java.nio.file.CopyOption[0]);
    }

    static FileAttribute<Set<java.nio.file.attribute.PosixFilePermission>> javaPermissionAttribute(
            Set<PosixFilePermission> permissions) {
        return PosixFilePermissions.asFileAttribute(javaPermissions(permissions));
    }

    static Set<java.nio.file.attribute.PosixFilePermission> javaPermissions(Set<PosixFilePermission> permissions)
             {
        Set<java.nio.file.attribute.PosixFilePermission> result = new HashSet<java.nio.file.attribute.PosixFilePermission>();

        if (permissions == null) {
            return result;
        }

        for (PosixFilePermission permission : permissions) {
            try {
                result.add(java.nio.file.attribute.PosixFilePermission.valueOf(permission.toString()));
            } catch (IllegalArgumentException e) {
                throw new UnsupportedOperationException("Posix permission " + permission + " not recognized by Local adaptor");
            }
        }

        return result;
    }

    static Set<PosixFilePermission> octopusPermissions(Set<java.nio.file.attribute.PosixFilePermission> permissions) {
        if (permissions == null) {
            return null;
        }

        Set<PosixFilePermission> result = new HashSet<PosixFilePermission>();

        for (java.nio.file.attribute.PosixFilePermission permission : permissions) {
            try {
                result.add(PosixFilePermission.valueOf(permission.toString()));
            } catch (IllegalArgumentException e) {
                throw new OctopusRuntimeException("Posix permission " + permission + " not recognized by Local adaptor", "local",
                        null);
            }
        }

        return result;
    }

    static java.nio.file.OpenOption[] javaOpenOptions(OpenOption[] options) {
        ArrayList<java.nio.file.OpenOption> result = new ArrayList<java.nio.file.OpenOption>();

        for (int i = 0; i < options.length; i++) {
            try {
                result.add(java.nio.file.StandardOpenOption.valueOf(options[i].toString()));
            } catch (IllegalArgumentException e) {
                throw new UnsupportedOperationException("Option " + options[i] + " not recognized by Local adaptor");
            }

        }
        return result.toArray(new java.nio.file.OpenOption[0]);
    }

    static Set<? extends java.nio.file.OpenOption> javaOpenOptionsSet(OpenOption[] options) {
        HashSet<java.nio.file.OpenOption> result = new HashSet<java.nio.file.OpenOption>();

        for (int i = 0; i < options.length; i++) {
            try {
                result.add(java.nio.file.StandardOpenOption.valueOf(options[i].toString()));
            } catch (IllegalArgumentException e) {
                throw new UnsupportedOperationException("Option " + options[i] + " not recognized by Local adaptor");
            }

        }
        return result;
    }
    
    
    
}

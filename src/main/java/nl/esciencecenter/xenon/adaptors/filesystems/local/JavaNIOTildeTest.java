package nl.esciencecenter.xenon.adaptors.filesystems.local;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

/**
 * Created by atze on 3-8-17.
 */
public class JavaNIOTildeTest {

    public static void main(String[] argv){
        Path p = FileSystems.getDefault().getPath("~/xenonbla");
        try {
            Files.createFile(p);
        } catch (Exception e){
            e.printStackTrace();;
        }
    }
}

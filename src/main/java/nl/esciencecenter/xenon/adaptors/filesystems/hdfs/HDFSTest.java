package nl.esciencecenter.xenon.adaptors.filesystems.hdfs;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import nl.esciencecenter.xenon.credentials.DefaultCredential;
import nl.esciencecenter.xenon.filesystems.*;


public class HDFSTest {
    public static void main(String[] argv){
        try {
            FileSystem fs = new HDFSFileAdaptor().createFileSystem("localhost:8020", new DefaultCredential(), null);
            fs.createDirectory(new Path("jada/bla"));
            System.out.println(fs.exists(new Path("jada")));
            OutputStream out = fs.writeToFile(new Path("jip.jpg"));
            InputStream jip = new FileInputStream(new File("jip.jpg"));
            int read ;
            byte[] data = new byte[1024];
            while ((read = jip.read(data)) != -1) {
                out.write(data);
            }
            out.close();
            InputStream is = fs.readFromFile(new Path("jip.jpg"));
            OutputStream os = new FileOutputStream(new File("jip2.jpg"));
            while ((read = is.read(data)) != -1) {
                os.write(data);
            }
            os.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
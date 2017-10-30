package nl.esciencecenter.xenon.adaptors.filesystems.hdfs;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import nl.esciencecenter.xenon.credentials.DefaultCredential;
import nl.esciencecenter.xenon.credentials.KeytabCredential;
import nl.esciencecenter.xenon.credentials.PasswordCredential;
import nl.esciencecenter.xenon.filesystems.*;
import nl.esciencecenter.xenon.utils.OutputReader;


public class HDFSTest {

    static protected void copySync(FileSystem fileSystem, Path source, Path target, CopyMode mode, boolean recursive) throws Throwable {
        String s = fileSystem.copy(source, fileSystem, target, mode, recursive);
        CopyStatus status = fileSystem.waitUntilDone(s, 1000);

        // For some adaptors (like webdav) it may take a few moments for the
        // copy to fully arrive at the server.
        // To prevent the next operation from overtaking this copy, we sleep for
        // a second to let the target settle.
        Thread.sleep(1000);

        if (status.hasException()) {
            throw status.getException();
        }
    }


    public static void main(String[] argv){

        System.setProperty("java.security.krb5.conf", "src/integrationTest/resources/kerberos/krb5.conf");

//        conf.set("dfs.datanode.kerberos.principal",  "hdfs/localhost@esciencecenter.nl");
//        conf.set("dfs.namenode.kerberos.http.principal","HTTP/localhost@esciencecenter.nl");
//        conf.set("dfs.namenode.kerberos.principal", "hdfs/localhost@esciencecenter.nl");
////        conf.set("dfs.namenode.kerberos.internal.spnego.principal","HTTP/localhost@esciencecenter.nl");
////        conf.set("hadoop.http.authentication.kerberos.principal", "HTTP/localhost@esciencecenter.nl");
//        conf.set("dfs.block.access.token.enable", "true");
//        conf.set("dfs.data.transfer.protection","integrity");
        try {
            Map<String,String> properties = new HashMap();
            properties.put(HDFSFileAdaptor.AUTHENTICATION,"kerberos");
            properties.put(HDFSFileAdaptor.DFS_NAMENODE_KERBEROS_PRINCIPAL,"hdfs/localhost@esciencecenter.nl");
            properties.put(HDFSFileAdaptor.BLOCK_ACCESS_TOKEN, "true");
            properties.put(HDFSFileAdaptor.TRANSFER_PROTECTION, "integrity");
//            properties.put(HDFSFileAdaptor.DATA_NODE_ADRESS, "localhost:50016");
            KeytabCredential kt = new KeytabCredential("xenon@esciencecenter.nl","src/integrationTest/resources/kerberos/xenon.keytab");
            FileSystem fs = new HDFSFileAdaptor().createFileSystem("localhost:8020", kt, properties);
            fs.setWorkingDirectory(new Path("/filesystem-test-fixture"));

//            fs.createDirectory(new Path("jada"));
//
            byte[] data = "Hello World!".getBytes();


//            fs.createFile(new Path("bla201"));
            String deep = "";
            for(int i = 0 ; i < 20; i++){
                deep += "blayasca" + i + "/";
                for(int j = 0 ; j < 20 ; j++) {
                    OutputStream s = fs.writeToFile(new Path(deep + "jadaab" + j));

                    s.write(data);
                    s.close();
                }
            }
            //String p = "jada/bla100";
            Iterator<PathAttributes> ps = fs.list(new Path("/filesystem-test-fixture"),true).iterator();
            System.out.println(ps.hasNext());
            while(ps.hasNext()){
                System.out.println(ps.next().getPath().toString());
            }

           // copySync(fs, new Path(p), new Path("bla3"), CopyMode.CREATE, false);


            //fs.setWorkingDirectory(new Path("/filesystem-test-fixture"));
            //fs.createDirectories(new Path("jada/bla"));
//            fs.createDirectory(new Path("xenon_test"));
//            for(int i = 0 ; i < 1 ; i++) {
//                 OutputStream out = fs.writeToFile(new Path("xenon_test/jip" + i + ".jpg"));
//
//                InputStream jip = new FileInputStream(new File("/home/atze/jip.jpg"));
//                int read;
//                byte[] data = new byte[1024];
//                while ((read = jip.read(data)) != -1) {
//                    out.write(data);
//                }
//                out.close();
//            }
//
//            String s = fs.copy(new Path("xenon_test/jip" + 0 + ".jpg"), fs, new Path("xenon_test/jip" + 3006 + ".jpg"),CopyMode.CREATE, false);
//            CopyStatus cs = fs.waitUntilDone(s,50000);
//            cs.maybeThrowException();
//            System.out.println(cs.isDone());
////                InputStream is = fs.readFromFile(new Path();
////                OutputStream os = new FileOutputStream(new File("jip2.jpg"));
////                while ((read = is.read(data)) != -1) {
////                    os.write(data);
////                }
////                os.close();

        } catch (Throwable e) {
            e.printStackTrace();
        }

    }
}
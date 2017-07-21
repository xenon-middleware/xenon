package nl.esciencecenter.xenon.adaptors.filesystems.jclouds;

import nl.esciencecenter.xenon.credentials.PasswordCredential;
import nl.esciencecenter.xenon.filesystems.Path;
import nl.esciencecenter.xenon.filesystems.PathAttributes;

import nl.esciencecenter.xenon.adaptors.filesystems.s3.S3FileAdaptor;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.StorageType;
import org.jclouds.blobstore.options.ListContainerOptions;

import java.io.*;

public class JCloudsTest {

    public static void main(String[] argv){
        try {
            System.setProperty("jclouds.relax-hostname","true");
            JCloudsFileSytem fs = (JCloudsFileSytem)new JCloudsFileAdaptor().createFileSystem("http://localhost:9000/filesystem-test-fixture",
                    new PasswordCredential("xenon", "javagat01".toCharArray()), null);
           // fs.createDirectory(new Path("bla"));
            //Path testDir = new Path("test04_createDirectory");
//            fs.createDirectory(new Path("links"));
/*
            InputStream s = new FileInputStream(new File("jip.jpg"));
            OutputStream read = fs.writeToFile(new Path("links/file0"),s.available());

            byte[] bytes = new byte[1024];
            int bs = 0;
            while ((bs = s.read(bytes)) != -1) {
                read.write(bytes, 0, bs);
            }
            read.close();
            */
            //fs.delete(new Path("links"),true);

            //fs.createDirectory(testDir);
            Iterable<PathAttributes> p = fs.list(new Path(""), true);
            for(PathAttributes pa : p){
            	System.out.println("bla" + pa.getPath());
            }
            /*
            System.out.println(fs.getAttributes(new Path("links/file0")).getSize());
            System.out.println(fs.exists(new Path("links")));
            System.out.println(fs.exists(new Path("links/file0")));
            System.out.println(fs.exists(new Path("jada")));
            System.out.println("\n\n\n");
            PageSet<? extends StorageMetadata> ps =
                    fs.context.getBlobStore().list("filesystem-test-fixture", new ListContainerOptions().prefix("bla").delimiter("/"));
            for(StorageMetadata m : ps) {
                System.out.println(m.getName());
                System.out.println(m.getType() );
            }
            */
            //fs.delete(new Path("links"), true);


            //byte[] msg = "test bla".getBytes();
            //OutputStream os = fs.newOutputStream(new Path("test3"),msg.length*2);
            //os.write(msg);
            //os.write(msg);
            //os.close();

            //fs.copySync(new CopyDescription(fs,new Path("jip.jpg"), fs, new Path("jip2.jpg"), CopyOption.CREATE));

            /*
            for (PathAttributesPair p : fs.list(new Path(""), true)) {
                System.out.println(p.path().getRelativePath());
            }
            */


            //System.out.println(fs.getAttributes(new Path("jip.jpg")).size());
/*
            InputStream read = fs.newInputStream(new Path("jip.jpg"));
            OutputStream s = new FileOutputStream(new File("jip.jpg"));
            byte[] bytes = new byte[1024];
            int bs = 0;
            while ((bs = read.read(bytes)) != -1) {
                s.write(bytes, 0, bs);
            }
            s.close();
            */
            /*
            Thread.sleep(1000);
            InputStream s = fs.newInputStream(new Path("test3"));
            s.read(msg);
            System.out.println(new String(msg));
            */


        } catch (Exception e){
            e.printStackTrace();
        }
    }
}


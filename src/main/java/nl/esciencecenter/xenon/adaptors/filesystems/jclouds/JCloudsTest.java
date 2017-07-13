package nl.esciencecenter.xenon.adaptors.filesystems.jclouds;

import nl.esciencecenter.xenon.credentials.PasswordCredential;
import nl.esciencecenter.xenon.filesystems.Path;


public class JCloudsTest {

    public static void main(String[] argv){
        try {
            System.setProperty("jclouds.relax-hostname","true");
            JCloudsFileSytem fs = (JCloudsFileSytem)new JCloudsFileAdaptor().createFileSystem("http://localhost:9000/aapjeemmer",
                    new PasswordCredential("X0XAO695ATIYL94AO8R7", "Dcajx9mm/RUQZnHepBEcVv42urKxy0s+y1nKvBJb".toCharArray()), null);

            //byte[] msg = "test bla".getBytes();
            //OutputStream os = fs.newOutputStream(new Path("test3"),msg.length*2);
            //os.write(msg);
            //os.write(msg);
            //os.close();
            for(int i = 0 ; i < 10000 ; i++){
                fs.delete(new Path("NogEenAapje" + i), false);
            }
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

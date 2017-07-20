package nl.esciencecenter.xenon.adaptors.filesystems.s3;


import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import nl.esciencecenter.xenon.adaptors.filesystems.s3.S3FileAdaptor;
import nl.esciencecenter.xenon.credentials.PasswordCredential;
import nl.esciencecenter.xenon.filesystems.Path;

public class S3Test {

    public static void main(String[] argv){
        try {
            //System.setProperty("jclouds.relax-hostname","true");

            AWSCredentials credentials = new BasicAWSCredentials("xenon", "javagat01");
            ClientConfiguration clientConfig = new ClientConfiguration();
            clientConfig.setProtocol(Protocol.HTTP);
            AwsClientBuilder.EndpointConfiguration endpointConfiguration = new AwsClientBuilder.EndpointConfiguration(
                    "http://localhost:9000","");
            AmazonS3 client = AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .withClientConfiguration(clientConfig)
                    .withEndpointConfiguration(endpointConfiguration)
                    .withRegion("").enablePathStyleAccess()
                    .build();
            client.putObject("aapjesemmer", "jada", "bla");
            ObjectListing list =client.listObjects(new ListObjectsRequest().withBucketName("filesystem-test-fixture"));
            for(S3ObjectSummary sum : list.getObjectSummaries()){
                System.out.println(sum.getKey());
            }
            for(String s : list.getCommonPrefixes()){
                System.out.println(s);
            }
            System.out.println(client.doesObjectExist("aapjesemmer", "links"));
            /*


            S3FileSystem fs = (S3FileSystem)new S3FileAdaptor().createFileSystem("http://localhost:9000/filesystem-test-fixture",
                    new PasswordCredential("xenon", "javagat01".toCharArray()), null);
            // fs.createDirectory(new Path("bla"));
            Path testDir = new Path("test04_createDirectory");
            //fs.createDirectory(testDir);

            System.out.print(fs.exists(new Path("links")));
            System.out.print(fs.exists(new Path("links/file0")));
            System.out.print(fs.exists(new Path("jada")));




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


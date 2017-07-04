package nl.esciencecenter.xenon.adaptors.filesystems.jclouds;

import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;

import java.util.Iterator;

/**
 * Created by atze on 30-6-17.
 */
public class Gedoe {


    public static void main(String[] argv) {
        // get a context with amazon that offers the portable BlobStore API
        BlobStoreContext context = ContextBuilder.newBuilder("s3").endpoint("http://localhost:9000")
                .credentials("X0XAO695ATIYL94AO8R7", "Dcajx9mm/RUQZnHepBEcVv42urKxy0s+y1nKvBJb")
                .buildView(BlobStoreContext.class);

        // create a container in the default location
        BlobStore blobStore = context.getBlobStore();

        blobStore.createContainerInLocation(null, "bucket");

        // add blob
        Blob blob = blobStore.blobBuilder("test").build();
        blob.setPayload("test data");
        blobStore.putBlob("bucket", blob);

        PageSet<? extends StorageMetadata> ps = blobStore.list("aapjeemmer");
        Iterator<? extends StorageMetadata> it = ps.iterator();
        while(it.hasNext()){
            System.out.println(it.next().getName());
        }



        context.close();
    }
}

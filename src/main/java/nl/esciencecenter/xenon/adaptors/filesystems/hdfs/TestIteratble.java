package nl.esciencecenter.xenon.adaptors.filesystems.hdfs;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by atze on 7-7-17.
 */
public class TestIteratble {

    public static void main(String[] argv){
        ArrayList<Integer> x = new ArrayList<>();

        x.add(3);
        x.add(2);
        x.add(1);
        Iterable<Integer> i = x;
        Iterator<Integer> it = i.iterator();
        Iterator<Integer> it2 = i.iterator();
        while(it.hasNext()){
            System.out.println(it.next());
        }
        while(it2.hasNext()){
            System.out.println(it2.next());
        }
    }
}

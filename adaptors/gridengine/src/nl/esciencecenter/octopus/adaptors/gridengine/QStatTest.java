package nl.esciencecenter.octopus.adaptors.gridengine;

import java.io.File;
import java.io.FileInputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

public class QStatTest {

    public static void main(String[] args) throws Exception {
        //File schemaFile = new File("/home/niels/workspace/octopus/adaptors/gridengine/doc/schemas/sge6.2u5p2/qstat/qstat.xsd"); 
        
  //      File schemaFile = new File(args[0]);
        
        File jobFile = new File(args[0]);
        
//        //File jobFile = new File("/home/niels/workspace/octopus/adaptors/gridengine/jobs.xml");
//        
//        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
//        
//        Schema schema = factory.newSchema(schemaFile);
//        
//        Source xmlFile = new StreamSource(jobFile);
//        
//        Validator validator = schema.newValidator();
//        
//        validator.validate(xmlFile);
//        
        // Create a JAXB context
        JAXBContext jc = JAXBContext.newInstance("com.sun.grid.xml.qstat");

        // Use the context to create an Unmarshaller
        Unmarshaller u = jc.createUnmarshaller();
        
//        u.setSchema(schema);

        
        //remote job fetch
        //Process p = Runtime.getRuntime ().exec ("ssh fs0.das4.cs.vu.nl qstat -xml");
        //JobInfo ji = (JobInfo)u.unmarshal (p.getInputStream ());

        //local job read
        FileInputStream in = new FileInputStream(jobFile);
//        JobInfo ji = (JobInfo) u.unmarshal(in);
        
        
//        List list = ((JobInfoT) ji.getJobInfo().get(0)).getJobList();
//        Iterator i = list.iterator();
//
//        while (i.hasNext()) {
//            JobListT jlt = (JobListT) i.next();
//
//            System.out.println(jlt.getJBJobNumber() + ": " + jlt.getJBName());
//        }
        
//        for (QueueInfoT queueInfo: ji.getQueueInfo()) {
//            for(Object object: queueInfo.getQueueListAndJobList()) {
//                if (object instanceof JobListT) {
//                    JobListT jobInfo = (JobListT) object;
//                    
//                    System.out.println("found a job: " + jobInfo.getJBJobNumber());
//                    System.out.println("\towned by: " + jobInfo.getJBOwner());
//                    System.out.println("\tusing slots: " + jobInfo.getSlots());
//                    
//                }
//            }
//        }
        
    }
}

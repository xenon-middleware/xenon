package nl.esciencecenter.xenon.adaptors.job;

import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.jobs.IncompleteJobDescriptionException;
import nl.esciencecenter.xenon.jobs.InvalidJobDescriptionException;
import nl.esciencecenter.xenon.jobs.Job;
import nl.esciencecenter.xenon.jobs.JobDescription;
//import nl.esciencecenter.xenon.util.Utils;

public class ScriptingUtils {

	//  protected static boolean supportsScheme(String scheme, String[] supportedSchemes) {
	//      for (String validScheme : supportedSchemes) {
	//          if (validScheme.equalsIgnoreCase(scheme)) {
	//              return true;
	//          }
	//      }
	//
	//      return false;
	//  }
	//
	/**
	 * Do some checks on a job description.
	 * 
	 * @param description
	 *            the job description to check
	 * @param adaptorName
	 *            the name of the adaptor. Used when an exception is thrown
	 * @throws IncompleteJobDescriptionException
	 *             if the description is missing a mandatory value.
	 * @throws InvalidJobDescriptionException
	 *             if the description contains illegal values.
	 */
	public static void verifyJobDescription(JobDescription description, String adaptorName) throws XenonException {
		String executable = description.getExecutable();

		if (executable == null) {
			throw new IncompleteJobDescriptionException(adaptorName, "Executable missing in JobDescription!");
		}

		int nodeCount = description.getNodeCount();

		if (nodeCount < 1) {
			throw new InvalidJobDescriptionException(adaptorName, "Illegal node count: " + nodeCount);
		}

		int processesPerNode = description.getProcessesPerNode();

		if (processesPerNode < 1) {
			throw new InvalidJobDescriptionException(adaptorName, "Illegal processes per node count: " + processesPerNode);
		}

		int maxTime = description.getMaxTime();

		if (maxTime <= 0) {
			throw new InvalidJobDescriptionException(adaptorName, "Illegal maximum runtime: " + maxTime);
		}
	}

	public static void verifyJobOptions(Map<String, String> options, String[] validOptions, String adaptorName)
			throws InvalidJobDescriptionException {

		//check if all given job options are valid
		for (String option : options.keySet()) {
			boolean found = false;
			for (String validOption : validOptions) {
				if (validOption.equals(option)) {
					found = true;
				}
			}
			if (!found) {
				throw new InvalidJobDescriptionException(adaptorName, "Given Job option \"" + option + "\" not supported");
			}
		}
	}

	/**
	 * Check if the info map for a job exists, contains the expected job ID, and contains the given additional fields
	 * 
	 * @param jobInfo
	 *            the map the job info should be .
	 * @param job
	 *            the job to check the presence for.
	 * @param adaptorName
	 *            name of the current adaptor for error reporting.
	 * @param jobIDField
	 *            the field which contains the job id.
	 * @param additionalFields
	 *            any additional fields to check the presence of.
	 * @throws XenonException
	 *             if any fields are missing or incorrect
	 */
	public static void verifyJobInfo(Map<String, String> jobInfo, Job job, String adaptorName, String jobIDField,
			String... additionalFields) throws XenonException {
		if (jobInfo == null) {
			//redundant check, calling functions usually already check for this and return null.
			throw new XenonException(adaptorName, "Job " + job.getIdentifier() + " not found in job info");
		}

		String jobID = jobInfo.get(jobIDField);

		if (jobID == null) {
			throw new XenonException(adaptorName, "Invalid job info. Info does not contain job id");
		}

		if (!jobID.equals(job.getIdentifier())) {
			throw new XenonException(adaptorName, "Invalid job info. Found job id \"" + jobID + "\" does not match "
					+ job.getIdentifier());
		}

		for (String field : additionalFields) {
			if (!jobInfo.containsKey(field)) {
				throw new XenonException(adaptorName, "Invalid job info. Info does not contain mandatory field \"" + field + "\"");
			}
		}
	}

	

}

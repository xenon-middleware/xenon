package nl.esciencecenter.xenon.adaptors.schedulers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class JobSeenMap {

    private final long expireTime;

    /**
     * Map with the last seen time of jobs. There is a delay between jobs disappearing from the qstat queue output, and information about this job appearing in
     * the qacct output. Instead of throwing an exception, we allow for a certain grace time. Jobs will report the status "pending" during this time. Typical
     * delays are in the order of seconds.
     */
    private final Map<String, Long> lastSeenMap = new HashMap<>();

    // list of jobs that where killed before they even started.
    private final Set<String> deletedJobs = new HashSet<>();

    public JobSeenMap() {
        this(60000);
    }

    public JobSeenMap(long expireTime) {
        this.expireTime = expireTime <= 0 ? 0 : expireTime;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public synchronized void updateRecentlySeen(Set<String> identifiers) {
        long currentTime = System.currentTimeMillis();

        for (String identifier : identifiers) {
            lastSeenMap.put(identifier, currentTime);
        }

        if (expireTime == 0) {
            return;
        }

        long expiredTime = currentTime + expireTime;

        Iterator<Entry<String, Long>> iterator = lastSeenMap.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<String, Long> entry = iterator.next();

            if (entry.getValue() > expiredTime) {
                iterator.remove();
            }
        }
    }

    public synchronized boolean haveRecentlySeen(String identifier) {
        if (!lastSeenMap.containsKey(identifier)) {
            return false;
        }

        if (expireTime == 0) {
            return true;
        } else {
            return (lastSeenMap.get(identifier) + expireTime) > System.currentTimeMillis();
        }
    }

    public synchronized void addDeletedJob(String jobIdentifier) {
        deletedJobs.add(jobIdentifier);
    }

    public synchronized boolean jobWasDeleted(String jobIdentifier) {
        // optimization of common case
        if (deletedJobs.isEmpty()) {
            return false;
        }
        return deletedJobs.remove(jobIdentifier);
    }
}

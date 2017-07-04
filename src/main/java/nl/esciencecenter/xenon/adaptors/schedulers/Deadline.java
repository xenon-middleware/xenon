package nl.esciencecenter.xenon.adaptors.schedulers;

public class Deadline {
	
    /**
     * Calculate the epoch timestamp when a timeout will expire. 
     * 
     * This deadline is computed by adding the <code>timeout</code> to <code>System.currentTimeMillis()</code>. This computation 
     * is protected against overflow, that is, the deadline will never exceed <code>Long.MAX_VALUE</code>.  
     * 
     * This allows the user to simply test if the deadline has passed by performing a check against the current epoch time:
     * 
     *    <code>if (deadline &lt;= System.currentTimeMillis()) { // deadline has passed }</code>
     * 
     * @param timeout
     *          the timeout to compute the deadline with. Must be &gt;= 0 or an IllegalArgumentException will be thrown.
     * @return
     *          the timestamp at which the timeout will expire, or <code>Long.MAX_VALUE</code> if the timeout causes an overflow.
     */
    public static long getDeadline(long timeout) {

        long deadline;

        if (timeout > 0) { 

            long time = System.currentTimeMillis(); 

            deadline = time + timeout;

            if (deadline < time) { 
                // Timeout overflow. Partial fix by setting timeout to end of epoch.
                deadline = Long.MAX_VALUE;
            }            
        } else if (timeout == 0) { 
            deadline = Long.MAX_VALUE;
        } else { 
            throw new IllegalArgumentException("Illegal timeout " + timeout);
        }

        return deadline;
    }
}

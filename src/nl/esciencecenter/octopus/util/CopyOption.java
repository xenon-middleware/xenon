package nl.esciencecenter.octopus.util;

public enum CopyOption {

    /**
     * Copy attributes to the new file.
     */
    COPY_ATTRIBUTES,

    /**
     * Replace an existing file if it exists.
     */
    REPLACE_EXISTING, ;

    public static boolean contains(CopyOption[] options, CopyOption option) {
        for (CopyOption oneOption : options) {
            if (oneOption == option) {
                return true;
            }
        }
        return false;
    }
}

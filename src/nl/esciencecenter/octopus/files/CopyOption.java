package nl.esciencecenter.octopus.files;

public enum CopyOption {

    /**
     * Copy attributes to the new file.
     */
    COPY_ATTRIBUTES,

    /**
     * Replace an existing file if it exists.
     */
    REPLACE_EXISTING,

    /**
     * Copy directories recursively. By default only the directory itself is
     * copied, not its contents. Will not follow symlinks, if symlinks are
     * detectable by the adaptor.
     */
    RECURSIVE,
    
    ;

    public static boolean contains(CopyOption[] options, CopyOption option) {
        for(CopyOption oneOption: options) {
            if (oneOption == option) {
                return true;
            }
        }
        return false;
    }
}

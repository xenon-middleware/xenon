package nl.esciencecenter.octopus.files;

public enum DeleteOption {

    /**
     * Recursively delete directory contents
     */
    RECURSIVE,

    /**
     * First overwrite file with 0's, then delete
     */
    WIPE;

    public static boolean contains(DeleteOption[] options, DeleteOption option) {
        for (DeleteOption oneOption : options) {
            if (oneOption == option) {
                return true;
            }
        }
        return false;
    }
}

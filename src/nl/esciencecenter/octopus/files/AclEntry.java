package nl.esciencecenter.octopus.files;

import java.util.Set;

public class AclEntry {

    public static final class Builder {

        /**
         * Constructs an AclEntry from the components of this builder.
         */
        AclEntry build() {
            return null;
        }

        /**
         * Sets the flags component of this builder.
         */
        AclEntry.Builder setFlags(AclEntryFlag... flags) {
            return null;
        }

        /**
         * Sets the flags component of this builder.
         */
        AclEntry.Builder setFlags(Set<AclEntryFlag> flags) {
            return null;
        }

        /**
         * Sets the permissions component of this builder.
         */
        AclEntry.Builder setPermissions(AclEntryPermission... perms) {
            return null;
        }

        /**
         * Sets the permissions component of this builder.
         */
        AclEntry.Builder setPermissions(Set<AclEntryPermission> perms) {
            return null;
        }

        /**
         * Sets the principal component of this builder.
         */
        AclEntry.Builder setPrincipal(String who) {
            return null;
        }

        /**
         * Sets the type component of this builder.
         */
        AclEntry.Builder setType(AclEntryType type) {
            return null;
        }
    }

    /**
     * Compares the specified object with this ACL entry for equality.
     */
    public boolean equals(Object ob) {
        return false;
    }

    /**
     * Returns a copy of the flags component.
     */
    public Set<AclEntryFlag> flags() {
        return null;
    }

    /**
     * Returns the hash-code value for this ACL entry.
     */
    public int hashCode() {
        return 0;
    }

    /**
     * Constructs a new builder.
     */
    public static AclEntry.Builder newBuilder() {
        return null;
    }

    /**
     * Constructs a new builder with the components of an existing ACL entry.
     */
    public static AclEntry.Builder newBuilder(AclEntry entry) {
        return null;
    }

    /**
     * Returns a copy of the permissions component.
     */
    public Set<AclEntryPermission> permissions() {
        return null;
    }

    /**
     * Returns the principal component.
     */
    public String principal() {
        return null;
    }

    /**
     * Returns the string representation of this ACL entry.
     */
    public String toString() {
        return null;
    }

    /**
     * Returns the ACL entry type.
     */
    public AclEntryType type() {
        return null;
    }
}

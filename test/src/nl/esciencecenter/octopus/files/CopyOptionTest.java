package nl.esciencecenter.octopus.files;

import static org.junit.Assert.*;

import org.junit.Test;

public class CopyOptionTest {

    @Test
    public void testContains_empty_doesNotContainIt() {
        CopyOption[] options = new CopyOption[0];
        assertFalse(CopyOption.contains(options, CopyOption.CREATE));
    }

    @Test
    public void testContains_filled_doesContainIt() {
        CopyOption[] options = new CopyOption[] { CopyOption.ASYNCHRONOUS, CopyOption.CREATE };
        assertTrue(CopyOption.contains(options, CopyOption.CREATE));
    }

    @Test
    public void testContains_filled_doesNotContainIt() {
        CopyOption[] options = new CopyOption[] { CopyOption.REPLACE };
        assertFalse(CopyOption.contains(options, CopyOption.CREATE));
    }

    @Test
    public void testContains_optionsNull_doesNotContainIt() {
        assertFalse(CopyOption.contains(null, CopyOption.CREATE));
    }

    @Test
    public void testContains_optionNull_doesNotContainIt() {
        CopyOption[] options = new CopyOption[] { CopyOption.ASYNCHRONOUS, CopyOption.CREATE };
        assertFalse(CopyOption.contains(options, null));
    }

    @Test
    public void testContains_optionsFilledNull_doesNotContainIt() {
        CopyOption[] options = new CopyOption[] { null };
        assertFalse(CopyOption.contains(options, CopyOption.CREATE));
    }

}

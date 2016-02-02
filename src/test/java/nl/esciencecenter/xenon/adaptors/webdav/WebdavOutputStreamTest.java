package nl.esciencecenter.xenon.adaptors.webdav;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.files.RelativePath;

public class WebdavOutputStreamTest {
    private Path path;
    private WebdavFiles files;
    private WebdavOutputStream webdavOutputStream;

    @Before
    public void setUp() {
        path = getPathMock();
        files = mock(WebdavFiles.class);
        webdavOutputStream = new WebdavOutputStream(path, files);
    }

    @Test
    public void close_deleteExistingFile() throws IOException, XenonException {
        when(files.exists(Mockito.isA(Path.class))).thenReturn(true);
        webdavOutputStream.close();
        verify(files, atLeastOnce()).delete(Mockito.isA(Path.class));
    }

    @Test
    public void close_dontDeleteNonexistingFile() throws IOException, XenonException {
        when(files.exists(Mockito.isA(Path.class))).thenReturn(false);
        webdavOutputStream.close();
        verify(files, never()).delete(Mockito.isA(Path.class));
    }

    private Path getPathMock() {
        return new Path() {

            @Override
            public RelativePath getRelativePath() {
                return null;
            }

            @Override
            public FileSystem getFileSystem() {
                return null;
            }
        };
    }
}

package org._10ne.smarchive;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

/**
 * @author Noam Y. Tenne
 */
public class SmarchiveInputStream extends ArchiveInputStream {

    private InputStream is;
    private ArchiveInputStream actual;

    public SmarchiveInputStream(InputStream is) {
        this.is = is;
    }

    public void realize() throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException,
            IllegalAccessException {
        BufferedInputStream originalStream = new BufferedInputStream(is);
        originalStream = addGzFilterIfNeeded(originalStream);
        actual = wrapStreamWithSpecificArchiveImplementation(originalStream);
    }

    @Override
    public int read() throws IOException {
        return actual.read();
    }

    @Override
    public long getBytesRead() {
        return actual.getBytesRead();
    }

    @Override
    public boolean canReadEntryData(ArchiveEntry archiveEntry) {
        return actual.canReadEntryData(archiveEntry);
    }

    @Override
    public ArchiveEntry getNextEntry() throws IOException {
        return actual.getNextEntry();
    }

    @Override
    public synchronized void reset() throws IOException {
        actual.reset();
    }

    @Override
    public boolean markSupported() {
        return actual.markSupported();
    }

    @Override
    public synchronized void mark(int readlimit) {
        actual.mark(readlimit);
    }

    @Override
    public void close() throws IOException {
        actual.close();
    }

    @Override
    public int available() throws IOException {
        return actual.available();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return actual.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return actual.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return actual.skip(n);
    }

    private BufferedInputStream addGzFilterIfNeeded(BufferedInputStream buffered) throws IOException {
        if (streamBeginsWithHeader(new byte[]{31,-117}, 0, buffered)) {
            return new BufferedInputStream(new GZIPInputStream(buffered));
        }
        return buffered;
    }

    private ArchiveInputStream wrapStreamWithSpecificArchiveImplementation(BufferedInputStream buffered) throws IOException,
            IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        for (MagicHeader magicHeader : MagicHeader.values()) {
            if (streamBeginsWithHeader(magicHeader, buffered)) {
                return constructActualArchiveStream(magicHeader, buffered);
            }
        }

        throw new IllegalArgumentException("Given input stream could be recognized as an archive stream");
    }

    private ArchiveInputStream constructActualArchiveStream(MagicHeader magicHeader, BufferedInputStream buffered) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Class<? extends ArchiveInputStream> archiveType = magicHeader.getArchiveType();
        Constructor<? extends ArchiveInputStream> constructor = archiveType.getConstructor(InputStream.class);
        return constructor.newInstance(buffered);
    }

    private boolean streamBeginsWithHeader(MagicHeader magicHeader, BufferedInputStream buffered) throws IOException {
        byte[] headerValue = magicHeader.getHeader();
        return streamBeginsWithHeader(headerValue, magicHeader.getOffset(), buffered);
    }

    private boolean streamBeginsWithHeader(byte[] headerValue, int offset, BufferedInputStream buffered) throws IOException {
        buffered.mark(offset + headerValue.length + 1);
        byte[] streamHeader = new byte[headerValue.length];
        buffered.skip(offset);
        int streamBytesRead = buffered.read(streamHeader);
        if (streamBytesRead == 0) {
            throw new IOException("Unable to read bytes for magic header detection");
        }
        buffered.reset();
        return Arrays.equals(headerValue, streamHeader);
    }
}

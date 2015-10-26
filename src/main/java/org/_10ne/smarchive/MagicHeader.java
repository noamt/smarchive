package org._10ne.smarchive;

import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ar.ArArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

/**
 * @author Noam Y. Tenne
 */
public enum MagicHeader {

    TAR(new byte[]{117, 115, 116, 97, 114}, 257, TarArchiveInputStream.class),
    AR(new byte[]{33, 60, 97, 114, 99, 104, 62}, 0, ArArchiveInputStream.class),
    ZIP(new byte[]{80, 75}, 0, ZipArchiveInputStream.class);

    private byte[] header;
    private int offset;
    private Class<? extends ArchiveInputStream> archiveType;

    MagicHeader(byte[] header, int offset, Class archiveType) {
        this.header = header;
        this.offset = offset;
        this.archiveType = archiveType;
    }

    public byte[] getHeader() {
        return header;
    }

    public int getOffset() {
        return offset;
    }

    public Class<? extends ArchiveInputStream> getArchiveType() {
        return archiveType;
    }
}

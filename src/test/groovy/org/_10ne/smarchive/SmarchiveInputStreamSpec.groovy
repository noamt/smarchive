package org._10ne.smarchive

import org.apache.commons.compress.archivers.ArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.GZIPOutputStream

/**
 * @author Noam Y. Tenne
 */
class SmarchiveInputStreamSpec extends Specification {

    def 'Detect tar-gz'() {
        setup:
        def tempFile = Files.createTempFile('temp', 'tar.gz')
        tempFile.withDataOutputStream { OutputStream tempFileStream ->
            def gzStream = new GZIPOutputStream(tempFileStream)
            gzStream.withStream { OutputStream gzOutStream ->
                def tarStream = new TarArchiveOutputStream(gzOutStream)
                tarStream.withStream { TarArchiveOutputStream tarOutStream ->
                    def archiveEntry = new TarArchiveEntry('testentry')
                    putEntry(archiveEntry, tarOutStream)
                }
                gzStream.finish()
            }
        }
        def rawFileStream = new FileInputStream(tempFile.toFile())

        when:
        def smarchive = SmarchiveInputStream.realize(rawFileStream)

        then:
        smarchive.actual instanceof TarArchiveInputStream
        archiveEntryIsValid(smarchive)

        cleanup:
        rawFileStream.close()
    }

    def 'Detect tar'() {
        setup:
        def tempFile = Files.createTempFile('temp', 'tar')
        tempFile.withDataOutputStream { OutputStream tempFileStream ->
            def tarStream = new TarArchiveOutputStream(tempFileStream)
            tarStream.withStream { TarArchiveOutputStream tarOutStream ->
                def archiveEntry = new TarArchiveEntry('testentry')
                putEntry(archiveEntry, tarOutStream)
            }
        }
        def rawFileStream = new FileInputStream(tempFile.toFile())

        when:
        def smarchive = SmarchiveInputStream.realize(rawFileStream)

        then:
        smarchive.actual instanceof TarArchiveInputStream
        archiveEntryIsValid(smarchive)

        cleanup:
        rawFileStream.close()
    }

    def 'Detect zip'() {
        setup:
        def tempFile = Files.createTempFile('temp', 'zip')
        tempFile.withDataOutputStream { OutputStream tempFileStream ->
            def zipStream = new ZipArchiveOutputStream(tempFileStream)
            zipStream.withStream { ZipArchiveOutputStream zipOutStream ->
                def archiveEntry = new ZipArchiveEntry('testentry')
                putEntry(archiveEntry, zipOutStream)
            }
        }
        def rawFileStream = new FileInputStream(tempFile.toFile())

        when:
        def smarchive = SmarchiveInputStream.realize(rawFileStream)

        then:
        smarchive.actual instanceof ZipArchiveInputStream
        archiveEntryIsValid(smarchive)

        cleanup:
        rawFileStream.close()
    }

    private void archiveEntryIsValid(ArchiveInputStream smarchive) {
        def entry = smarchive.nextEntry
        assert entry.name == 'testentry'
        assert smarchive.canReadEntryData(entry)

        byte[] entryContent = [0, 0, 0, 0] as byte[]
        smarchive.read(entryContent)
        assert new String(entryContent) == 'test'
    }

    private Path tempEntryFile() {
        Path file = Files.createTempFile('temp', 'example')
        file.withDataOutputStream {
            it << 'test'
        }
        file
    }

    private void putEntry(def archiveEntry, def outStream) {
        Path entryFile = tempEntryFile()
        archiveEntry.setSize(entryFile.size())
        outStream.putArchiveEntry(archiveEntry);
        outStream.write(entryFile.bytes)
        outStream.closeArchiveEntry()
        outStream.finish()
    }
}

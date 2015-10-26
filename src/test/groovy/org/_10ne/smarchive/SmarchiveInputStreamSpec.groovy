package org._10ne.smarchive

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
                    def entryFile = tempEntryFile()
                    def archiveEntry = new TarArchiveEntry('testentry')
                    archiveEntry.setSize(entryFile.size())
                    tarOutStream.putArchiveEntry(archiveEntry);
                    tarOutStream.write(entryFile.bytes)
                    tarOutStream.closeArchiveEntry()
                    tarOutStream.finish()
                }
                gzStream.finish()
            }
        }
        def rawFileStream = new FileInputStream(tempFile.toFile())

        when:
        def smarchive = new SmarchiveInputStream(rawFileStream)
        smarchive.realize()

        then:
        smarchive.actual instanceof TarArchiveInputStream

        cleanup:
        rawFileStream.close()
    }

    def 'Detect tar'() {
        setup:
        def tempFile = Files.createTempFile('temp', 'tar')
        tempFile.withDataOutputStream { OutputStream tempFileStream ->
            def tarStream = new TarArchiveOutputStream(tempFileStream)
            tarStream.withStream { TarArchiveOutputStream tarOutStream ->
                def entryFile = tempEntryFile()
                def archiveEntry = new TarArchiveEntry('testentry')
                archiveEntry.setSize(entryFile.size())
                tarOutStream.putArchiveEntry(archiveEntry);
                tarOutStream.write(entryFile.bytes)
                tarOutStream.closeArchiveEntry()
                tarOutStream.finish()
            }
        }
        def rawFileStream = new FileInputStream(tempFile.toFile())

        when:
        def smarchive = new SmarchiveInputStream(rawFileStream)
        smarchive.realize()

        then:
        smarchive.actual instanceof TarArchiveInputStream

        cleanup:
        rawFileStream.close()
    }

    def 'Detect zip'() {
        setup:
        def tempFile = Files.createTempFile('temp', 'zip')
        tempFile.withDataOutputStream { OutputStream tempFileStream ->
            def zipStream = new ZipArchiveOutputStream(tempFileStream)
            zipStream.withStream { ZipArchiveOutputStream zipOutStream ->
                def entryFile = tempEntryFile()
                def archiveEntry = new ZipArchiveEntry('testentry')
                archiveEntry.setSize(entryFile.size())
                zipOutStream.putArchiveEntry(archiveEntry);
                zipOutStream.write(entryFile.bytes)
                zipOutStream.closeArchiveEntry()
                zipOutStream.finish()
            }
        }
        def rawFileStream = new FileInputStream(tempFile.toFile())

        when:
        def smarchive = new SmarchiveInputStream(rawFileStream)
        smarchive.realize()

        then:
        smarchive.actual instanceof ZipArchiveInputStream

        cleanup:
        rawFileStream.close()
    }

    private Path tempEntryFile() {
        Path file = Files.createTempFile('temp', 'example')
        file.withDataOutputStream {
            it << 'test'
        }
        file
    }
}

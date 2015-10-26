package org._10ne.smarchive

import org.apache.commons.compress.archivers.ar.ArArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import spock.lang.Specification

/**
 * @author Noam Y. Tenne
 */
class MagicHeadersSpec extends Specification {

    def 'Interpret the magic headers'() {
        expect:
        header.header == magicHeader as byte[]
        header.archiveType == type

        where:
        header          | magicHeader                    | type
        MagicHeader.AR  | [33, 60, 97, 114, 99, 104, 62] | ArArchiveInputStream
        MagicHeader.TAR | [117, 115, 116, 97, 114]       | TarArchiveInputStream
    }
}

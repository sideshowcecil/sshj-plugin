package com.plugin.sshjplugin.util

import spock.lang.Specification

class DelegateOutputStreamSpec extends Specification {
    def "When Delegate OutputStream closed it should not close the target OutputStream"() {
        given:
        ByteArrayOutputStream bytes = new ByteArrayOutputStream()
        DelegateOutputStream outputStream = new DelegateOutputStream(bytes)
        Boolean isStreamClosed = false

        String str1 = "This is a string"
        String str2 = "This is another string"
        String str3 = "This string should be write to the ByteArrayOutputStream"

        when:
        try {
            outputStream.write(str1.getBytes("UTF-8"))
        } finally {
            outputStream.close()
        }

        try {
            outputStream.write(str2.getBytes("UTF-8"))
        } catch(IOException e) {
            isStreamClosed = e.getMessage() == "Delegated OutputStream has been closed."
        }

        try {
            bytes.write(str3.getBytes("UTF-8"))
        } finally {
            bytes.close()
        }

        then:
        isStreamClosed == true
        bytes.toString() == str1 + str3
    }
}

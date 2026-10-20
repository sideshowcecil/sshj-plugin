package com.plugin.sshjplugin.model

import com.dtolabs.rundeck.plugins.PluginLogger
import com.plugin.sshjplugin.SSHJBuilder
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.common.Buffer
import net.schmizz.sshj.transport.verification.HostKeyVerifier
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Path
import java.security.KeyPairGenerator
import java.security.PublicKey

class SSHJBaseHostKeySpec extends Specification {

    @TempDir
    Path tempDir

    static PublicKey newHostKey() {
        def generator = KeyPairGenerator.getInstance('RSA')
        generator.initialize(2048)
        generator.generateKeyPair().public
    }

    static String knownHostsLine(String host, PublicKey key) {
        String encoded = Base64.encoder.encodeToString(new Buffer.PlainBuffer().putPublicKey(key).compactData)
        "${host} ssh-rsa ${encoded}\n"
    }

    SSHJBase baseWith(boolean strict, String knownHostsFile) {
        def base = new SSHJBase()
        base.pluginLogger = Mock(PluginLogger)
        base.pluginName = 'test-plugin'
        base.sshjConnection = Mock(SSHJConnection) {
            isStrictHostKeyChecking() >> strict
            getKnownHostsFile() >> knownHostsFile
        }
        base
    }

    /**
     * Run configureHostKeyVerification on a real client and return the verifiers it installed
     */
    List<HostKeyVerifier> installedVerifiers(SSHJBase base) {
        List<HostKeyVerifier> verifiers = []
        SSHClient ssh = Spy(SSHClient) {
            addHostKeyVerifier(_ as HostKeyVerifier) >> { HostKeyVerifier v -> verifiers << v }
        }
        base.configureHostKeyVerification(ssh)
        verifiers
    }

    static boolean accepted(List<HostKeyVerifier> verifiers, String host, int port, PublicKey key) {
        verifiers.any { it.verify(host, port, key) }
    }

    def "default mode accepts any host key"() {
        when:
        def verifiers = installedVerifiers(baseWith(false, null))

        then:
        verifiers.any { it instanceof PromiscuousVerifier }
        accepted(verifiers, 'node1.example.com', 22, newHostKey())
    }

    def "strict mode accepts a host key listed in the known hosts file"() {
        given:
        def key = newHostKey()
        def file = tempDir.resolve('known_hosts').toFile()
        file.text = knownHostsLine('node1.example.com', key) + knownHostsLine('[10.0.0.5]:2222', key)

        when:
        def verifiers = installedVerifiers(baseWith(true, file.absolutePath))

        then:
        !verifiers.any { it instanceof PromiscuousVerifier }
        accepted(verifiers, 'node1.example.com', 22, key)
        accepted(verifiers, '10.0.0.5', 2222, key)
    }

    def "strict mode rejects a changed host key"() {
        given:
        def file = tempDir.resolve('known_hosts').toFile()
        file.text = knownHostsLine('node1.example.com', newHostKey())

        when:
        def verifiers = installedVerifiers(baseWith(true, file.absolutePath))

        then:
        !accepted(verifiers, 'node1.example.com', 22, newHostKey())
    }

    def "strict mode rejects a host that is not in the known hosts file"() {
        given:
        def key = newHostKey()
        def file = tempDir.resolve('known_hosts').toFile()
        file.text = knownHostsLine('node1.example.com', key)

        when:
        def verifiers = installedVerifiers(baseWith(true, file.absolutePath))

        then:
        !accepted(verifiers, 'node2.example.com', 22, key)
        !accepted(verifiers, 'node1.example.com', 2222, key)
    }

    def "strict mode fails with a clear error when the known hosts file is missing"() {
        given:
        def missing = tempDir.resolve('does-not-exist').toString()

        when:
        installedVerifiers(baseWith(true, missing))

        then:
        def e = thrown(SSHJBuilder.BuilderException)
        e.message.contains('known hosts file cannot be read')
        e.message.contains(missing)
    }

    def "strict mode without a file uses the default known hosts location"() {
        given:
        SSHClient ssh = Mock(SSHClient)
        def base = baseWith(true, null)

        when:
        base.configureHostKeyVerification(ssh)

        then:
        1 * ssh.loadKnownHosts()
        0 * ssh.addHostKeyVerifier(_ as PromiscuousVerifier)
    }

    def "strict mode reports a clear error when the default known hosts cannot be loaded"() {
        given:
        SSHClient ssh = Mock(SSHClient) {
            loadKnownHosts() >> { throw new IOException('Could not load known_hosts') }
        }

        when:
        baseWith(true, null).configureHostKeyVerification(ssh)

        then:
        def e = thrown(SSHJBuilder.BuilderException)
        e.message.contains('known hosts file could not be loaded')
        0 * ssh.addHostKeyVerifier(_ as PromiscuousVerifier)
    }
}

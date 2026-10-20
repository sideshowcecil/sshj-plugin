package com.plugin.sshjplugin

import com.dtolabs.rundeck.core.execution.ExecutionListener
import com.dtolabs.rundeck.plugins.PluginLogger
import com.plugin.sshjplugin.model.SSHJBase
import com.plugin.sshjplugin.model.SSHJConnection
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.transport.verification.HostKeyVerifier
import net.schmizz.sshj.transport.verification.OpenSSHKnownHosts
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Path

/**
 * Each SSH operation must use its own client, so host key verifiers from one node or execution
 * never apply to another.
 */
class SSHJClientPerOperationSpec extends Specification {

    @TempDir
    Path tempDir

    static List<HostKeyVerifier> verifiersOf(SSHClient client) {
        new ArrayList<HostKeyVerifier>(client.transport.@kexer.@hostVerifiers)
    }

    void configure(SSHClient client, boolean strict, String knownHostsFile) {
        def base = new SSHJBase()
        base.pluginLogger = Mock(PluginLogger)
        base.pluginName = 'test-plugin'
        base.sshjConnection = Mock(SSHJConnection) {
            isStrictHostKeyChecking() >> strict
            getKnownHostsFile() >> knownHostsFile
        }
        base.configureHostKeyVerification(client)
    }

    String knownHostsFile(String name) {
        def file = tempDir.resolve(name).toFile()
        file.text = ''
        file.absolutePath
    }

    def "node executor creates a new client for every command"() {
        given:
        def plugin = new SSHJNodeExecutorPlugin()
        def listener = Mock(ExecutionListener)

        when:
        def first = plugin.createSshClient(listener)
        def second = plugin.createSshClient(listener)

        then:
        !first.is(second)
        plugin.@sshClient == null
    }

    def "file copier creates a new client for every copy"() {
        given:
        def plugin = new SSHJFileCopierPlugin()

        when:
        def first = plugin.createSshClient()
        def second = plugin.createSshClient()

        then:
        !first.is(second)
        plugin.@sshClient == null
    }

    def "a permissive operation does not weaken a later strict operation"() {
        given:
        def plugin = new SSHJNodeExecutorPlugin()
        def listener = Mock(ExecutionListener)
        def trustFile = knownHostsFile('known_hosts')

        when: "a permissive node is followed by a strict node"
        def permissiveClient = plugin.createSshClient(listener)
        configure(permissiveClient, false, null)
        def strictClient = plugin.createSshClient(listener)
        configure(strictClient, true, trustFile)

        then: "the strict client has only the known hosts verifier"
        verifiersOf(permissiveClient).any { it instanceof PromiscuousVerifier }
        def strictVerifiers = verifiersOf(strictClient)
        strictVerifiers.size() == 1
        strictVerifiers[0] instanceof OpenSSHKnownHosts
        ((OpenSSHKnownHosts) strictVerifiers[0]).file.absolutePath == trustFile
    }

    def "switching known hosts files does not keep trusting the earlier file"() {
        given:
        def plugin = new SSHJFileCopierPlugin()
        def fileA = knownHostsFile('known_hosts_a')
        def fileB = knownHostsFile('known_hosts_b')

        when:
        def clientA = plugin.createSshClient()
        configure(clientA, true, fileA)
        def clientB = plugin.createSshClient()
        configure(clientB, true, fileB)

        then:
        def verifiers = verifiersOf(clientB)
        verifiers.size() == 1
        ((OpenSSHKnownHosts) verifiers[0]).file.absolutePath == fileB
    }
}

package com.plugin.sshjplugin.model

import com.dtolabs.rundeck.plugins.PluginLogger
import com.plugin.sshjplugin.SSHJBuilder
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.transport.Transport
import net.schmizz.sshj.transport.TransportException
import spock.lang.Specification

class SSHJBaseRetrySpec extends Specification {

    SSHJBase baseWith(boolean retry, int retryCount) {
        def base = new SSHJBase()
        base.pluginLogger = Mock(PluginLogger)
        base.pluginName = 'test-plugin'
        base.hostname = 'node1'
        base.sshjConnection = Mock(SSHJConnection) {
            isRetryEnabled() >> retry
            getRetryCounter() >> retryCount
            getUsername() >> 'user'
            getAuthenticationType() >> SSHJConnection.AuthenticationType.password
            getPassword(_) >> 'pw'
        }
        base
    }

    SSHClient client() {
        Mock(SSHClient) {
            getTransport() >> Mock(Transport) { getConfig() >> Mock(net.schmizz.sshj.Config) }
            isConnected() >> true
        }
    }

    def "successful first attempt connects once when retry is enabled"() {
        given:
        def ssh = client()

        when:
        baseWith(true, 3).connect(ssh)

        then:
        1 * ssh.connect('node1')
    }

    def "a failed attempt is retried and stops once connected"() {
        given:
        def ssh = client()
        int attempts = 0
        ssh.connect('node1') >> { if (++attempts == 1) { throw new TransportException('connection reset') } }

        when:
        baseWith(true, 3).connect(ssh)

        then:
        noExceptionThrown()
        attempts == 2
    }

    def "retry disabled fails on the first transport error"() {
        given:
        def ssh = client()
        int attempts = 0
        ssh.connect('node1') >> { attempts++; throw new TransportException('connection reset') }

        when:
        baseWith(false, 3).connect(ssh)

        then:
        thrown(SSHJBuilder.BuilderException)
        attempts == 1
    }

    def "all attempts failing with retry enabled throws instead of returning unconnected"() {
        given:
        def ssh = client()
        int attempts = 0
        ssh.connect('node1') >> { attempts++; throw new TransportException('connection reset') }

        when:
        baseWith(true, 2).connect(ssh)

        then:
        def e = thrown(SSHJBuilder.BuilderException)
        e.message.contains('connection reset')
        attempts == 3
    }
}

package com.plugin.sshjplugin

import com.dtolabs.rundeck.core.common.IRundeckProject
import com.dtolabs.rundeck.core.common.NodeEntryImpl
import com.dtolabs.rundeck.core.execution.ExecutionListener
import com.dtolabs.rundeck.core.execution.ExecutionListenerOverride
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.sftp.SFTPClient
import net.schmizz.sshj.transport.Transport

class SSHJFileCopierPluginSpec extends SSHJBaseTest {

    def "test copy file with sftp"(){
        given:
        def source = File.createTempFile("test", ".txt")
        def destination = "/tmp/test.txt"

        ExecutionListener logger = Mock(ExecutionListener) {
            createOverride() >> Mock(ExecutionListenerOverride)
        }

        IRundeckProject rundeckFramework = Mock(IRundeckProject){
            hasProperty("project.use-sftp") >> true
            getProperty("project.use-sftp") >> true
        }

        def properties = new Properties()
        properties.setProperty("project.use-sftp","true")

        def context = getContext(properties, rundeckFramework, logger)

        SSHClient client = Mock(SSHClient){
            getTransport()>>Mock(Transport){
                getConfig()>>SSHJDefaultConfig.init().getConfig()
            }
        }

        def plugin = new SSHJFileCopierPlugin()
        plugin.sshClient = client

        def node = new NodeEntryImpl("test")
        node.setAttributes(["username":"test",
                            "osFamily":"linux",
                            "hostname":"localhost",
                            "ssh-connect-timeout":"3",
                            "ssh-command-timeout":"3",
                            "ssh-authentication":"password",
                            "ssh-password-storage-path":"keys/password",
                            "sudo-password-storage-path":"keys/password",
                            "sudo-command-enabled":"true"
        ])


        when:
        plugin.copyFile(context, source, node, destination)

        then:
        1 * client.newSFTPClient()>>Mock(SFTPClient){
            1 * put(_,_) >> { args ->
                args[0].getFile() == source
                args[1] == destination
            }
        }
    }
}

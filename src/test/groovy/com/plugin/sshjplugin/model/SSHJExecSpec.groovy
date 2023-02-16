package com.plugin.sshjplugin.model

import com.dtolabs.rundeck.core.common.Framework
import com.dtolabs.rundeck.core.common.IRundeckProject
import com.dtolabs.rundeck.core.common.NodeEntryImpl
import com.dtolabs.rundeck.core.common.ProjectManager
import com.dtolabs.rundeck.core.execution.ExecutionContextImpl
import com.dtolabs.rundeck.core.utils.PropertyLookup
import com.dtolabs.rundeck.plugins.PluginLogger
import com.plugin.sshjplugin.SSHJBuilder
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.connection.channel.direct.Session
import spock.lang.Specification

class SSHJExecSpec extends Specification{


    def "run command successfully"(){
        given:
        String commandString = "ls"

        def logger =  Mock(PluginLogger)

        def node = new NodeEntryImpl("test")
        node.setAttributes(["username":"test",
                            "osFamily":"linux",
                            "hostname":"test",
                            "ssh-connect-timeout":"3",
                            "ssh-command-timeout":"3",
                            "ssh-authentication":"password",
                            "ssh-password-storage-path":"keys/password",
                            "sudo-password-storage-path":"keys/password",
                            "sudo-command-enabled":"true"
        ])

        def properties = new Properties()
        properties.setProperty("fwkprop","fwkvalue")
        def framework = Mock(Framework){
            getFrameworkProjectMgr() >> Mock(ProjectManager){
                getFrameworkProject(_)>>Mock(IRundeckProject)
            }
            getPropertyLookup() >> PropertyLookup.create(properties)
        }

        def context = ExecutionContextImpl.builder().framework(framework).build()


        SSHJExec sshjExec = new SSHJExec()
        sshjExec.command = commandString
        sshjExec.pluginLogger =logger
        sshjExec.pluginName = "sshj-ssh"
        sshjExec.sshjConnection = new SSHJConnectionParameters(node, context)

        Session session = Mock(Session)
        SSHClient client = Mock(SSHClient)
        Session.Command command = Mock(Session.Command)

        when:
        sshjExec.execute(client)

        then:
        1 * client.startSession()>>session
        1 * session.exec(commandString) >> command
        _ * command.getInputStream()
        _ * command.getErrorStream()
        1 * command.join()
        1 * command.getExitStatus()>>0
        1 * logger.log(3, "[sshj-ssh] exit status: 0")

    }


    def "run command with error"(){
        given:
        String commandString = "ls"

        def logger =  Mock(PluginLogger)

        def node = new NodeEntryImpl("test")
        node.setAttributes(["username":"test",
                            "osFamily":"linux",
                            "hostname":"test",
                            "ssh-connect-timeout":"3",
                            "ssh-command-timeout":"3",
                            "ssh-authentication":"password",
                            "ssh-password-storage-path":"keys/password",
                            "sudo-password-storage-path":"keys/password",
                            "sudo-command-enabled":"true"
        ])

        def properties = new Properties()
        properties.setProperty("fwkprop","fwkvalue")
        def framework = Mock(Framework){
            getFrameworkProjectMgr() >> Mock(ProjectManager){
                getFrameworkProject(_)>>Mock(IRundeckProject)
            }
            getPropertyLookup() >> PropertyLookup.create(properties)
        }

        def context = ExecutionContextImpl.builder().framework(framework).build()


        SSHJExec sshjExec = new SSHJExec()
        sshjExec.command = commandString
        sshjExec.pluginLogger =logger
        sshjExec.pluginName = "sshj-ssh"
        sshjExec.sshjConnection = new SSHJConnectionParameters(node, context)

        Session session = Mock(Session)
        SSHClient client = Mock(SSHClient)
        Session.Command command = Mock(Session.Command)

        when:
        sshjExec.execute(client)

        then:
        1 * client.startSession()>>session
        1 * session.exec(commandString) >> command
        _ * command.getInputStream()
        _ * command.getErrorStream()
        1 * command.join()
        1 * command.getExitStatus()>>1
        1 * logger.log(3, "[sshj-ssh] exit status: 1")

        thrown SSHJBuilder.BuilderException

    }
}

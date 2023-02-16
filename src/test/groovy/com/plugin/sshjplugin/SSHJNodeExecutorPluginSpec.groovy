package com.plugin.sshjplugin

import com.dtolabs.rundeck.core.common.NodeEntryImpl
import com.dtolabs.rundeck.core.data.BaseDataContext
import com.dtolabs.rundeck.core.execution.ExecutionContextImpl
import com.dtolabs.rundeck.core.execution.ExecutionListener
import com.dtolabs.rundeck.core.execution.ExecutionListenerOverride
import com.dtolabs.rundeck.core.common.Framework
import com.dtolabs.rundeck.core.storage.ResourceMeta
import com.dtolabs.rundeck.core.storage.StorageTree
import com.dtolabs.rundeck.core.utils.PropertyLookup
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.transport.Transport
import org.rundeck.storage.api.Resource
import spock.lang.Specification
import com.dtolabs.rundeck.core.common.ProjectManager
import com.dtolabs.rundeck.core.common.IRundeckProject

class SSHJNodeExecutorPluginSpec extends Specification {

    def getContext(Properties properties,def rundeckFramework,  def logger) {


        def dataContext = [
                config: ["RD_TEST": "Value"]
        ]

        def storage = Mock(StorageTree) {
            getResource('keys/password') >> Mock(Resource) {
                getContents() >> Mock(ResourceMeta) {
                    writeContent(_) >> { args ->
                        args[0].write('test.'.bytes)
                        7L
                    }
                }
            }

            getResource('keys/node.key') >> Mock(Resource) {
                getContents() >> Mock(ResourceMeta) {
                    writeContent(_) >> { args ->
                        args[0].write('test.'.bytes)
                        7L
                    }
                }
            }
        }

        def framework = Mock(Framework) {
            getFrameworkProjectMgr() >> Mock(ProjectManager) {
                getFrameworkProject(_) >> rundeckFramework
            }
            getPropertyLookup() >> PropertyLookup.create(properties)
            getProjectManager() >> Mock(ProjectManager) {
                getFrameworkProject(_) >> rundeckFramework
            }
        }

        ExecutionContextImpl.builder()
                .framework(framework)
                .executionListener(logger)
                .storageTree(storage)
                .dataContext(new BaseDataContext(dataContext))
                .frameworkProject("test")
                .build()
    }


    def "authenticate using node password"(){

        given:

        String[] command = ["ls -lrt"]

        def logger = Mock(ExecutionListener) {
            createOverride() >> Mock(ExecutionListenerOverride)
        }

        def rundeckFramework = Mock(IRundeckProject)
        def properties = new Properties()
        properties.setProperty("fwkprop","fwkvalue")
        def context = getContext(properties, rundeckFramework, logger)

        SSHClient client = Mock(SSHClient){
            getTransport()>>Mock(Transport){
                getConfig()>>SSHJDefaultConfig.init().getConfig()
            }
        }

        def plugin = new SSHJNodeExecutorPlugin()
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
        plugin.executeCommand(context, command, node)

        then:
        1 * logger.log(3, "Authenticating using password: keys/password")

    }

    def "authenticate using node key"(){

        given:

        String[] command = ["ls -lrt"]

        def logger = Mock(ExecutionListener) {
            createOverride() >> Mock(ExecutionListenerOverride)
        }

        def rundeckFramework = Mock(IRundeckProject)
        def properties = new Properties()
        properties.setProperty("fwkprop","fwkvalue")
        def context = getContext(properties,rundeckFramework, logger)

        SSHClient client = Mock(SSHClient){
            getTransport()>>Mock(Transport){
                getConfig()>>SSHJDefaultConfig.init().getConfig()
            }
        }

        def plugin = new SSHJNodeExecutorPlugin()
        plugin.sshClient = client

        def node = new NodeEntryImpl("test")
        node.setAttributes(["username":"test",
                            "osFamily":"linux",
                            "hostname":"localhost",
                            "ssh-connect-timeout":"3",
                            "ssh-command-timeout":"3",
                            "ssh-authentication":"privateKey",
                            "ssh-key-storage-path":"keys/node.key",
                            "sudo-command-enabled":"true"
        ])

        when:
        plugin.executeCommand(context, command, node)

        then:
        1 * logger.log(3, "Authenticating using private key")

    }


    def "authenticate using password project level"(){

        given:

        String[] command = ["ls -lrt"]

        def logger = Mock(ExecutionListener) {
            createOverride() >> Mock(ExecutionListenerOverride)
        }

        def rundeckFramework = Mock(IRundeckProject) {
            hasProperty('project.ssh-authentication') >> true
            getProperty('project.ssh-authentication') >> "password"
            hasProperty('project.ssh-password-storage-path') >> true
            getProperty('project.ssh-password-storage-path')>> "keys/password"
        }

        def properties = new Properties()
        properties.setProperty("fwkprop","fwkvalue")
        def context = getContext(properties,rundeckFramework, logger)

        SSHClient client = Mock(SSHClient){
            getTransport()>>Mock(Transport){
                getConfig()>>SSHJDefaultConfig.init().getConfig()
            }
        }

        def plugin = new SSHJNodeExecutorPlugin()
        plugin.sshClient = client

        def node = new NodeEntryImpl("test")
        node.setAttributes(["username":"test",
                            "osFamily":"linux",
                            "hostname":"localhost"
        ])

        when:
        plugin.executeCommand(context, command, node)

        then:
        1 * logger.log(3, "Authenticating using password: keys/password")

    }

    def "authenticate using key project level"(){

        given:

        String[] command = ["ls -lrt"]

        def logger = Mock(ExecutionListener) {
            createOverride() >> Mock(ExecutionListenerOverride)
        }

        def rundeckFramework = Mock(IRundeckProject) {
            hasProperty('project.ssh-authentication') >> true
            getProperty('project.ssh-authentication') >> "privateKey"
            hasProperty('project.ssh-key-storage-path') >> true
            getProperty('project.ssh-key-storage-path')>> "keys/node.key"
        }
        def properties = new Properties()
        properties.setProperty("fwkprop","fwkvalue")
        def context = getContext(properties,rundeckFramework, logger)

        SSHClient client = Mock(SSHClient){
            getTransport()>>Mock(Transport){
                getConfig()>>SSHJDefaultConfig.init().getConfig()
            }
        }

        def plugin = new SSHJNodeExecutorPlugin()
        plugin.sshClient = client

        def node = new NodeEntryImpl("test")
        node.setAttributes(["username":"test",
                            "osFamily":"linux",
                            "hostname":"localhost"
        ])

        when:
        plugin.executeCommand(context, command, node)

        then:
        1 * logger.log(3, "Authenticating using private key")

    }

}

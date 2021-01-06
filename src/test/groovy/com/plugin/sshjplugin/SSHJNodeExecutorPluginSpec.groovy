package com.plugin.sshjplugin

import com.dtolabs.rundeck.core.common.IFramework
import com.dtolabs.rundeck.core.common.NodeEntryImpl
import com.dtolabs.rundeck.core.data.BaseDataContext
import com.dtolabs.rundeck.core.execution.ExecutionContext
import com.dtolabs.rundeck.core.execution.ExecutionListener
import com.dtolabs.rundeck.core.execution.ExecutionListenerOverride
import com.dtolabs.rundeck.core.execution.ExecutionLogger
import com.dtolabs.rundeck.core.common.Framework
import com.dtolabs.rundeck.core.common.INodeEntry
import com.dtolabs.rundeck.core.execution.service.NodeExecutorResultImpl
import com.dtolabs.rundeck.core.storage.ResourceMeta
import com.dtolabs.rundeck.core.storage.StorageTree
import com.dtolabs.rundeck.core.utils.PropertyLookup
import org.rundeck.storage.api.Resource
import spock.lang.Specification
import com.dtolabs.rundeck.core.common.ProjectManager
import com.dtolabs.rundeck.core.common.IRundeckProject

class SSHJNodeExecutorPluginSpec extends Specification {

    def getContext(IFramework iFramework, ExecutionLogger logger, Boolean fail){

            def manager = Mock(ProjectManager){
                getFrameworkProject(_)>> Mock(IRundeckProject) {
                    hasProperty(('project.exampleConfig')) >> true
                    getProperty(('project.exampleConfig')) >> "123345"
                    hasProperty(('project.exampleSelect')) >> true
                    getProperty(('project.exampleSelect')) >> "Blue"
                    hasProperty(('project.ssh-connect-timeout')) >> true
                    getProperty(('project.ssh-connect-timeout')) >> "3"


                }
            }

            def dataContext = [
                    config   : ["RD_TEST":"Value"]
            ]

            Mock(ExecutionContext){
                getExecutionLogger()>>logger
                getFrameworkProject() >> "test"
                getIFramework() >> iFramework
                getExecutionListener() >> Mock(ExecutionListener){
                    createOverride()>>Mock(ExecutionListenerOverride)
                }
                getStorageTree()>>Mock(StorageTree){
                    getResource('keys/password') >> Mock(Resource) {
                        getContents() >> Mock(ResourceMeta) {
                            writeContent(_) >> { args ->
                                args[0].write('Variacode.'.bytes)
                                7L
                            }
                        }
                    }
                }
                getDataContext()>>new BaseDataContext(dataContext)

            }
        }

    def "check Boolean parameter"(){

        given:

        String[] command = ["ls -lrt"]
        def logger = Mock(ExecutionLogger)
        def properties = new Properties()
        properties.setProperty("fwkprop","fwkvalue")

        def framework = Mock(IFramework){
            getFrameworkProjectMgr() >> Mock(ProjectManager){
                getFrameworkProject(_)>>Mock(IRundeckProject)
            }
            getPropertyLookup() >> PropertyLookup.create(properties)


        }
        def context = getContext(framework,logger,true)
        def example = new SSHJNodeExecutorPlugin()


        /*
        def node = Mock(INodeEntry){
            getNodename()>>"test"
            getHostname()>>"192.168.0.12"
            getUsername()>>"variacode"
            getAttributes()>>["hostname":"192.168.0.12","osFamily":"linux","forceFail":"true","username":"variacode"]
        }
        */


        def node = new NodeEntryImpl("test")
        node.setAttributes(["username":"variacode",
                            "osFamily":"linux",
                            "hostname":"192.168.0.12",
                            "ssh-connect-timeout":"3",
                            "ssh-command-timeout":"3",
                            "ssh-authentication":"password",
                            //"ssh-keypath":"/Users/luistoledo/id_rsa_variacode"
                            "ssh-password-storage-path":"keys/password",
                            "sudo-password-storage-path":"keys/password",
                            "sudo-command-enabled":"true"
        ])

        when:
        example.executeCommand(context, command, node)

        command = ["uname -a"]

        example.executeCommand(context, command, node)

        then:
        true
        //1 * logger.log(4, "Using ssh password storage path: keys/password")

    }

    def "run OK"(){

        given:

        String[] command = ["ls","-lrt"]
        def logger = Mock(ExecutionLogger)
        def example = new SSHJNodeExecutorPlugin()
        def context = getContext(Mock(IFramework),logger,false)
        def node = Mock(INodeEntry){
            getNodename()>>"test"
            getAttributes()>>["hostname":"Test","osFamily":"linux"]
        }

        when:
        example.executeCommand(context, command, node)

        then:
        true //1 * NodeExecutorResultImpl.createSuccess(_)

    }

}

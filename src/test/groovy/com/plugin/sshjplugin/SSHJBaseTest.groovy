package com.plugin.sshjplugin

import com.dtolabs.rundeck.core.common.Framework
import com.dtolabs.rundeck.core.common.IRundeckProject
import com.dtolabs.rundeck.core.common.ProjectManager
import com.dtolabs.rundeck.core.data.BaseDataContext
import com.dtolabs.rundeck.core.execution.ExecutionContextImpl
import com.dtolabs.rundeck.core.execution.ExecutionListener
import com.dtolabs.rundeck.core.storage.ResourceMeta
import com.dtolabs.rundeck.core.storage.StorageTree
import com.dtolabs.rundeck.core.utils.PropertyLookup
import org.rundeck.storage.api.Resource
import spock.lang.Specification

class SSHJBaseTest extends Specification{

    def getContext(Properties properties, IRundeckProject rundeckFramework, ExecutionListener logger) {

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
                        args[0].write('-----BEGIN OPENSSH PRIVATE KEY-----'.bytes)
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
}

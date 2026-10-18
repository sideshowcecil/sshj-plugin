package com.plugin.sshjplugin.model;

import com.dtolabs.rundeck.plugins.PluginLogger;
import com.plugin.sshjplugin.SSHJBuilder;
import com.plugin.sshjplugin.SSHJPluginLoggerFactory;
import net.schmizz.keepalive.KeepAliveRunner;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

public class SSHJBase {

    String pluginName;
    PluginLogger pluginLogger;
    int connectionNumber=0;
    int retryConnections=0;
    SSHJConnection sshjConnection;
    String hostname;
    Integer port;

    public String getPluginName() {
        return pluginName;
    }

    public SSHJConnection getSshjConnection() {
        return sshjConnection;
    }

    public void setSshjConnection(SSHJConnection sshjConnection) {
        this.sshjConnection = sshjConnection;
    }

    public PluginLogger getPluginLogger() {
        return pluginLogger;
    }

    public void setPluginLogger(PluginLogger pluginLogger) {
        this.pluginLogger = pluginLogger;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void connect(SSHClient ssh){
        pluginLogger.log(3, "["+ getPluginName()+"] init SSHJDefaultConfig" );
        int connectTimeout = sshjConnection.getConnectTimeout();
        int commandTimeout = sshjConnection.getCommandTimeout();
        int keepAliveInterval = sshjConnection.getKeepAliveInterval();
        int keepAliveCount = sshjConnection.getKeepAliveMaxAlive();
        boolean retry = sshjConnection.isRetryEnabled();
        int retryCount = sshjConnection.getRetryCounter();
        boolean useSftp = sshjConnection.useSftp();

        SSHJAuthentication authentication = new SSHJAuthentication(sshjConnection, pluginLogger);

        pluginLogger.log(3, "["+getPluginName()+"] setting timeouts" );
        pluginLogger.log(3, "["+getPluginName()+"] getConnectTimeout timeout: " + connectTimeout);
        pluginLogger.log(3, "["+getPluginName()+"] getTimeout timeout: " + commandTimeout);
        pluginLogger.log(3, "["+getPluginName()+"] keepAliveInterval: " + keepAliveInterval);
        pluginLogger.log(3, "["+getPluginName()+"] keepAliveMaxCount: " + keepAliveCount);
        pluginLogger.log(3, "["+getPluginName()+"] retry: " + retry);
        pluginLogger.log(3, "["+getPluginName()+"] retryCount: " + retryCount);
        pluginLogger.log(3, "["+getPluginName()+"] useSftp: " + useSftp);

        ssh.getTransport().getConfig().setLoggerFactory(new SSHJPluginLoggerFactory(pluginLogger));
        ssh.setConnectTimeout(connectTimeout);
        ssh.setTimeout(commandTimeout);
        ssh.getTransport().setTimeoutMs(connectTimeout);

        configureHostKeyVerification(ssh);

        int count=0;
        if(!retry){
            retryCount=0;
        }

        if (keepAliveInterval != 0) {
            KeepAliveRunner keepAlive = (KeepAliveRunner)ssh.getConnection().getKeepAlive();
            keepAlive.setKeepAliveInterval(keepAliveInterval);

            if(keepAliveCount != 0){
                keepAlive.setMaxAliveCount(keepAliveCount);
            }
        }



        while(count <= retryCount) {
            try {
                pluginLogger.log(3, "["+getPluginName()+"] open connection");

                if (port != null) {
                    ssh.connect(hostname, port.intValue());
                } else {
                    ssh.connect(hostname);
                }
                pluginLogger.log(3, "["+getPluginName()+"] connection done");

                authentication.authenticate(ssh);

                pluginLogger.log(3, "["+getPluginName()+"]  authentication set");


                if (ssh.isConnected()) {
                    pluginLogger.log(3, "["+getPluginName()+"] connection done");
                    connectionNumber++;
                }

            } catch (TransportException e) {
                pluginLogger.log(2, "["+getPluginName()+"] TransportException: " + e.getMessage() + 
                              " (" + e.getClass().getSimpleName() + ")");
                if(retry && count<=retryCount){
                    retryConnections++;
                    pluginLogger.log(2, "["+getPluginName()+"]  trying again");
                    pluginLogger.log(2, "["+getPluginName()+"]  total connections " + connectionNumber);
                    pluginLogger.log(2, "["+getPluginName()+"]  total retries " + retryConnections);
                }else{
                    throw new SSHJBuilder.BuilderException("Transport error: " + e.getMessage(), e);
                }
            } catch (UnknownHostException e) {
                pluginLogger.log(2, "["+getPluginName()+"] DNS resolution FAILED: hostname=" + hostname + 
                                  " - Unable to resolve hostname to IP address. Check DNS configuration and network connectivity.");
                throw new SSHJBuilder.BuilderException("DNS resolution failed for hostname: " + hostname, e);
            } catch (IOException e) {
                pluginLogger.log(2, "["+getPluginName()+"] Connection failed: " + e.getMessage() + 
                              " (" + e.getClass().getSimpleName() + ")");
                throw new SSHJBuilder.BuilderException("Connection failed: " + e.getMessage(), e);
            }

            count++;

        }
    }

    /**
     * Configure how the server host key is verified.
     * <p>
     * With strict host key checking enabled, only host keys listed in the known hosts file are accepted,
     * so connections to unknown hosts or hosts whose key changed fail. Otherwise any host key is accepted.
     *
     * @param ssh client to configure
     * @throws SSHJBuilder.BuilderException if strict checking is enabled and the configured known hosts file
     *                                      cannot be read
     */
    void configureHostKeyVerification(SSHClient ssh) {
        if (sshjConnection.isStrictHostKeyChecking()) {
            String knownHostsFile = sshjConnection.getKnownHostsFile();
            try {
                if (knownHostsFile != null) {
                    File file = new File(knownHostsFile);
                    if (!file.isFile() || !file.canRead()) {
                        throw new SSHJBuilder.BuilderException(
                                "Strict host key checking is enabled but the known hosts file cannot be read: "
                                + knownHostsFile
                        );
                    }
                    pluginLogger.log(3, "["+getPluginName()+"] strict host key checking, known hosts file: " + knownHostsFile);
                    ssh.loadKnownHosts(file);
                } else {
                    pluginLogger.log(3, "["+getPluginName()+"] strict host key checking, using default known hosts file");
                    ssh.loadKnownHosts();
                }
            } catch (IOException e) {
                throw new SSHJBuilder.BuilderException(
                        "Strict host key checking is enabled but the known hosts file could not be loaded: "
                        + e.getMessage(),
                        e
                );
            }
            return;
        }

        ssh.addHostKeyVerifier(new PromiscuousVerifier());

        pluginLogger.log(3, "["+getPluginName()+"] adding loadKnownHosts" );

        try {
            ssh.loadKnownHosts();
        } catch (IOException e) {
            pluginLogger.log(3, "["+getPluginName()+"] Warning: Could not load known hosts: " + e.getMessage() +
                          " (" + e.getClass().getSimpleName() + ")");
        }
    }

}

package com.plugin.sshjplugin.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public interface SSHJConnection{

    static enum AuthenticationType {
        privateKey,
        password
    }

    String getUsername();

    AuthenticationType getAuthenticationType();

    String getPrivateKeyPath() throws IOException;

    String getPrivateKeyStoragePath() throws IOException;

    InputStream getPrivateKeyStorageData(String path);

    String getPasswordStoragePath();

    String getPassword(String path) throws IOException;

    String getSudoPasswordStoragePath();

    String getSudoPassword(String path) throws IOException;

    String getPrivateKeyPassphraseStoragePath() throws IOException;

    String getPrivateKeyPassphrase(String path) throws IOException;

    Boolean isSudoEnabled();

    String getSudoCommandPattern();

    String getSudoPromptPattern();

    boolean matchesCommandPattern(final String command);

    int getCommandTimeout();

    int getConnectTimeout();

    int getKeepAliveInterval();

    int getRetryCounter();

    boolean isRetryEnabled();

    Boolean getLocalSSHAgent();

    Integer getTtlSSHAgent();

    Map<String, String> getSshConfig();

    String getBindAddress();
}

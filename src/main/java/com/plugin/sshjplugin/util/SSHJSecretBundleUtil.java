package com.plugin.sshjplugin.util;

import com.dtolabs.rundeck.core.common.INodeEntry;
import com.dtolabs.rundeck.core.execution.ExecutionContext;
import com.dtolabs.rundeck.core.execution.proxy.DefaultSecretBundle;
import com.dtolabs.rundeck.core.execution.proxy.SecretBundle;
import com.dtolabs.utils.Streams;
import com.plugin.sshjplugin.model.SSHJConnectionParameters;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SSHJSecretBundleUtil {

    public static SecretBundle createBundle(final ExecutionContext context, final INodeEntry node)  {
        try {
            DefaultSecretBundle secretBundle = new DefaultSecretBundle();
            final SSHJConnectionParameters nodeAuthentication = new SSHJConnectionParameters(node, context.getFramework(),
                    context
            );
            if(nodeAuthentication.getPasswordStoragePath() != null) {
                secretBundle.addSecret(
                        nodeAuthentication.getPasswordStoragePath(),
                        nodeAuthentication.getPassword(nodeAuthentication.getPasswordStoragePath()).getBytes()
                );
            }
            if(nodeAuthentication.getPrivateKeyPassphraseStoragePath() != null) {
                secretBundle.addSecret(
                        nodeAuthentication.getPrivateKeyPassphraseStoragePath(),
                        nodeAuthentication.getPrivateKeyPassphrase(nodeAuthentication.getPrivateKeyPassphraseStoragePath()).getBytes()
                );
            }
            if(nodeAuthentication.getPrivateKeyStoragePath() != null) {
                ByteArrayOutputStream pkData = new ByteArrayOutputStream();
                Streams.copyStream(nodeAuthentication.getPrivateKeyStorageData(nodeAuthentication.getPrivateKeyStoragePath()), pkData);
                secretBundle.addSecret(
                        nodeAuthentication.getPrivateKeyStoragePath(),
                        pkData.toByteArray()
                );
            }

            if(nodeAuthentication.getSudoPasswordStoragePath() != null) {
                secretBundle.addSecret(
                        nodeAuthentication.getSudoPasswordStoragePath(),
                        nodeAuthentication.getSudoPassword(nodeAuthentication.getSudoPasswordStoragePath()).getBytes()
                );
            }


            return secretBundle;
        } catch(IOException iex) {
            throw new RuntimeException("Unable to prepare secret bundle", iex);
        }
    }

}

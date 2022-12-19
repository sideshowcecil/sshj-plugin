package com.plugin.sshjplugin.model;
import com.dtolabs.rundeck.plugins.PluginLogger;
import com.plugin.sshjplugin.SSHJBuilder;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import java.io.File;
import java.io.IOException;

public class SSHJAuthentication {

    SSHJConnection.AuthenticationType authenticationType;
    String username;
    String password;
    String privateKeyFile;
    String passphrase;
    PluginLogger logger;
    SSHJConnection connectionParameters;

    public SSHJAuthentication(SSHJConnection connectionParameters, PluginLogger logger) {
        this.logger = logger;
        this.connectionParameters = connectionParameters;

        this.username = connectionParameters.getUsername();
        this.authenticationType = connectionParameters.getAuthenticationType();
    }

    void authenticate(final SSHClient ssh) throws IOException {

        switch (authenticationType) {
            case privateKey:
                try{
                    privateKeyFile = connectionParameters.getPrivateKeyPath();
                } catch (IOException e) {
                    logger.log(0, "Failed to get SSH key: " + e.getMessage());
                }

                String passphrasePath = connectionParameters.getPrivateKeyPassphraseStoragePath();
                try{
                    passphrase = connectionParameters.getPrivateKeyPassphrase(passphrasePath);
                } catch (IOException e) {
                    logger.log(0, "Failed to read SSH Passphrase stored at path: " + passphrasePath);
                }

                KeyProvider key = null;
                if (null != privateKeyFile && !"".equals(privateKeyFile)) {
                    if (!new File(privateKeyFile).exists()) {
                        throw new SSHJBuilder.BuilderException("SSH Keyfile does not exist: " + privateKeyFile);
                    }
                    logger.log(3, "[sshj-debug] Using ssh keyfile: " + privateKeyFile);
                }

                if (passphrase == null) {
                    key = ssh.loadKeys(privateKeyFile);
                } else {
                    key = ssh.loadKeys(privateKeyFile, passphrase);
                }
                ssh.authPublickey(username, key);
                break;
            case password:
                String passwordPath = connectionParameters.getPasswordStoragePath();
                if(passwordPath!=null){
                    logger.log(3, "Authenticating using password: " + passwordPath);
                }
                try{
                    password = connectionParameters.getPassword(passwordPath);
                } catch (IOException e) {
                    logger.log(0, "Failed to read SSH Password stored at path: " + passwordPath);
                }

                if (password != null) {
                    ssh.authPassword(username, password);
                }else{
                    throw new SSHJBuilder.BuilderException("SSH password wasn't set, please define a password");
                }
                break;
        }
    }
}

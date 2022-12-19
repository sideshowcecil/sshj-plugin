package com.plugin.sshjplugin.model;
import com.dtolabs.rundeck.plugins.PluginLogger;
import com.plugin.sshjplugin.SSHJBuilder;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.Factory;
import net.schmizz.sshj.userauth.keyprovider.FileKeyProvider;
import net.schmizz.sshj.userauth.keyprovider.KeyFormat;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.userauth.keyprovider.KeyProviderUtil;
import net.schmizz.sshj.userauth.password.PasswordUtils;

import java.io.*;

public class SSHJAuthentication {

    SSHJConnection.AuthenticationType authenticationType;
    String username;
    String password;
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

                String privateKeyFile = connectionParameters.getPrivateKeyfilePath();
                String privateKeyStoragePath = connectionParameters.getPrivateKeyStoragePath();
                String passphrasePath = connectionParameters.getPrivateKeyPassphraseStoragePath();

                KeyProvider key = null;

                try{
                    passphrase = connectionParameters.getPrivateKeyPassphrase(passphrasePath);
                } catch (IOException e) {
                    logger.log(0, "Failed to read SSH Passphrase stored at path: " + passphrasePath);
                }

                if(privateKeyStoragePath!=null && !privateKeyStoragePath.isEmpty()){

                    try (InputStream privateKey = connectionParameters.getPrivateKeyStorageData(privateKeyStoragePath);
                         InputStreamReader privateKeyReader = new InputStreamReader(privateKey)){

                        KeyFormat format = KeyProviderUtil.detectKeyFileFormat(privateKeyReader,true);
                        FileKeyProvider privateKeyProvider = Factory.Named.Util.create(ssh.getTransport().getConfig().getFileKeyProviderFactories(), format.toString());

                        if(passphrase == null){
                            privateKeyProvider.init(new InputStreamReader(privateKey), null);
                        }else{
                            privateKeyProvider.init(new InputStreamReader(privateKey), PasswordUtils.createOneOff(passphrase.toCharArray()));
                        }
                        key = (KeyProvider) privateKey;
                    } catch (Exception e) {
                        logger.log(0, "Failed to get SSH key: " + e.getMessage());
                    }
                }

                if (null != privateKeyFile && !"".equals(privateKeyFile)) {
                    if (!new File(privateKeyFile).exists()) {
                        throw new SSHJBuilder.BuilderException("SSH Keyfile does not exist: " + privateKeyFile);
                    }
                    logger.log(3, "[sshj-debug] Using ssh keyfile: " + privateKeyFile);
                    if (passphrase == null) {
                        key = ssh.loadKeys(privateKeyFile);
                    } else {
                        key = ssh.loadKeys(privateKeyFile, passphrase);
                    }
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

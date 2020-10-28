package com.plugin.sshjplugin;

import net.schmizz.sshj.DefaultConfig;

public class SSHJDefaultConfig {

    private static SSHJDefaultConfig instance = null;
    private DefaultConfig config;

    private SSHJDefaultConfig() {
        this.config = new DefaultConfig();
    }

    public DefaultConfig getConfig() {
        return config;
    }

    public static SSHJDefaultConfig init(){
         if(instance==null){
             instance= new SSHJDefaultConfig();
         }

         return instance;
    }
}

package com.plugin.sshjplugin;

import com.dtolabs.rundeck.plugins.PluginLogger;
import net.schmizz.sshj.common.LoggerFactory;
import org.slf4j.Logger;

public class SSHJPluginLoggerFactory implements LoggerFactory {

    private PluginLogger pluginLogger;

    public SSHJPluginLoggerFactory(PluginLogger pluginLogger) {
        this.pluginLogger = pluginLogger;
    }

    @Override
    public Logger getLogger(String name) {
        SSHJPluginLogger logger = new SSHJPluginLogger(name, pluginLogger);
        logger.setPrintClassName(true);
        return logger;
    }

    @Override
    public Logger getLogger(Class<?> clazz) {
        SSHJPluginLogger logger = new SSHJPluginLogger(clazz.getName(), pluginLogger);
        logger.setPrintClassName(true);
        return logger;
    }


}

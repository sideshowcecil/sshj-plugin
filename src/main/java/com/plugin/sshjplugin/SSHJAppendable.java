package com.plugin.sshjplugin;

import com.dtolabs.rundeck.plugins.PluginLogger;

import java.io.IOException;

public class SSHJAppendable implements Appendable {

    PluginLogger logger;
    int level;

    public SSHJAppendable(PluginLogger logger, int level) {
        this.logger = logger;
        this.level = level;
    }

    @Override
    public Appendable append(CharSequence csq) throws IOException {
        logger.log(level, String.valueOf(csq));
        return this;
    }

    @Override
    public Appendable append(CharSequence csq, int start, int end) throws IOException {
        return this;
    }

    @Override
    public Appendable append(char c) throws IOException {
        return this;
    }
}

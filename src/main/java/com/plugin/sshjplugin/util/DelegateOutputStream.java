package com.plugin.sshjplugin.util;

import java.io.IOException;
import java.io.OutputStream;

public class DelegateOutputStream extends OutputStream {

    private OutputStream targetOutput;


    public DelegateOutputStream(OutputStream targetOutput) {
        this.targetOutput = targetOutput;
    }

    @Override
    public void write(int b) throws IOException {
        this.targetOutput.write(b);
    }

    @Override
    public void flush() throws IOException {
        this.targetOutput.flush();
        super.flush();
    }

    @Override
    public void close() throws IOException {
        this.targetOutput = null;   // Don't close the target OutputStream but dereference to it.
        super.close();
    }
}

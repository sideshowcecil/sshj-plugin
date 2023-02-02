package com.plugin.sshjplugin.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This is a custom OutputStream which delegate a target OutputStream passed into the constructor at creation time.
 *
 * We need this delegate to pass it into StreamCopier because StreamCopier will close the OutputStream if it reaches the end of the InputStream.
 * But we need to keep the OutputStream open the whole lifecycle of the plugin, so we can receive output from the following executions instead of only the first execution.
 *
 * This implementation won't close the OutputStream it delegates and leave the lifecycle management to its caller.
 * You are not supposed to use this class in other occasions, but if you want to use it please remember to close the target OutputStream properly.
 *
 */
public class DelegateOutputStream extends OutputStream {

    private OutputStream targetOutput;

    public DelegateOutputStream(OutputStream targetOutput) {
        this.targetOutput = targetOutput;
    }

    @Override
    public void write(int b) throws IOException {
        if(this.targetOutput == null) throw new IOException("Delegated OutputStream has been closed.");
        this.targetOutput.write(b);
    }

    @Override
    public void flush() throws IOException {
        this.targetOutput.flush();
        super.flush();
    }

    @Override
    public void close() throws IOException {
        // Don't close the target OutputStream but dereference to it.
        this.targetOutput = null;
        super.close();
    }
}

package com.plugin.sshjplugin;

import com.dtolabs.rundeck.plugins.PluginLogger;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.MessageFormatter;

public class SSHJPluginLogger implements Logger {

    private PluginLogger pluginLogger;
    private String name;
    private Boolean printClassName = false;

    public SSHJPluginLogger(String name, PluginLogger pluginLogger) {
        this.name = name;
        this.pluginLogger = pluginLogger;
    }

    public PluginLogger getPluginLogger() {
        return pluginLogger;
    }

    public void setPluginLogger(PluginLogger pluginLogger) {
        this.pluginLogger = pluginLogger;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getPrintClassName() {
        return printClassName;
    }

    public void setPrintClassName(Boolean printClassName) {
        this.printClassName = printClassName;
    }

    void processLog(Level level, String message, Object[] args, Throwable throwable) {
        String formatMessage = MessageFormatter.arrayFormat(message, args).getMessage();

        if (printClassName) {
            formatMessage = "[" + name + "] " + formatMessage;
        }
        if (level.equals(Level.DEBUG)) {
            pluginLogger.log(5, formatMessage);
        }
        if (level.equals(Level.ERROR)) {
            pluginLogger.log(0, formatMessage);
        }
        if (level.equals(Level.INFO)) {
            pluginLogger.log(2, formatMessage);
        }

        if (level.equals(Level.WARN)) {
            pluginLogger.log(1, formatMessage);
        }
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public void trace(String msg) {

    }

    @Override
    public void trace(String format, Object arg) {

    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {

    }

    @Override
    public void trace(String format, Object... arguments) {

    }

    @Override
    public void trace(String msg, Throwable t) {

    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return false;
    }

    @Override
    public void trace(Marker marker, String msg) {

    }

    @Override
    public void trace(Marker marker, String format, Object arg) {

    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {

    }

    @Override
    public void trace(Marker marker, String format, Object... argArray) {

    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {

    }

    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    @Override
    public void debug(String msg) {
        processLog(Level.DEBUG, msg, null, null);
    }

    @Override
    public void debug(String format, Object arg) {
        processLog(Level.DEBUG, format, new Object[]{arg}, null);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        processLog(Level.DEBUG, format, new Object[]{arg1, arg2}, null);
    }

    @Override
    public void debug(String format, Object... arguments) {
        processLog(Level.DEBUG, format, arguments, null);
    }

    @Override
    public void debug(String msg, Throwable t) {
        processLog(Level.DEBUG, msg, null, t);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return false;
    }

    @Override
    public void debug(Marker marker, String msg) {

    }

    @Override
    public void debug(Marker marker, String format, Object arg) {

    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {

    }

    @Override
    public void debug(Marker marker, String format, Object... arguments) {

    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {

    }

    @Override
    public boolean isInfoEnabled() {
        return false;
    }

    @Override
    public void info(String msg) {
        processLog(Level.DEBUG, msg, null, null);

    }

    @Override
    public void info(String format, Object arg) {
        processLog(Level.DEBUG, format, new Object[]{arg}, null);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        processLog(Level.DEBUG, format, new Object[]{arg1, arg2}, null);
    }

    @Override
    public void info(String format, Object... arguments) {
        processLog(Level.DEBUG, format, arguments, null);
    }

    @Override
    public void info(String msg, Throwable t) {
        processLog(Level.DEBUG, msg, null, t);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return false;
    }

    @Override
    public void info(Marker marker, String msg) {

    }

    @Override
    public void info(Marker marker, String format, Object arg) {

    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {

    }

    @Override
    public void info(Marker marker, String format, Object... arguments) {

    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {

    }

    @Override
    public boolean isWarnEnabled() {
        return false;
    }

    @Override
    public void warn(String msg) {
        processLog(Level.DEBUG, msg, null, null);
    }

    @Override
    public void warn(String format, Object arg) {
        processLog(Level.DEBUG, format, new Object[]{arg}, null);
    }

    @Override
    public void warn(String format, Object... arguments) {
        processLog(Level.DEBUG, format, arguments, null);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        processLog(Level.DEBUG, format, new Object[]{arg1, arg2}, null);
    }

    @Override
    public void warn(String msg, Throwable t) {
        processLog(Level.DEBUG, msg, null, t);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return false;
    }

    @Override
    public void warn(Marker marker, String msg) {

    }

    @Override
    public void warn(Marker marker, String format, Object arg) {

    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {

    }

    @Override
    public void warn(Marker marker, String format, Object... arguments) {

    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {

    }

    @Override
    public boolean isErrorEnabled() {
        return false;
    }

    @Override
    public void error(String msg) {
        processLog(Level.DEBUG, msg, null, null);
    }

    @Override
    public void error(String format, Object arg) {
        processLog(Level.DEBUG, format, new Object[]{arg}, null);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        processLog(Level.DEBUG, format, new Object[]{arg1, arg2}, null);
    }

    @Override
    public void error(String format, Object... arguments) {
        processLog(Level.DEBUG, format, arguments, null);
    }

    @Override
    public void error(String msg, Throwable t) {
        processLog(Level.DEBUG, msg, null, t);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return false;
    }

    @Override
    public void error(Marker marker, String msg) {

    }

    @Override
    public void error(Marker marker, String format, Object arg) {

    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {

    }

    @Override
    public void error(Marker marker, String format, Object... arguments) {

    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {

    }
}

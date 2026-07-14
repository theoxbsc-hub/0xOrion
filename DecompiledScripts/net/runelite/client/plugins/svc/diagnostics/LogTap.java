/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  ch.qos.logback.classic.Level
 *  ch.qos.logback.classic.Logger
 *  ch.qos.logback.classic.spi.ILoggingEvent
 *  ch.qos.logback.classic.spi.IThrowableProxy
 *  ch.qos.logback.classic.spi.ThrowableProxyUtil
 *  ch.qos.logback.core.Appender
 *  ch.qos.logback.core.AppenderBase
 *  ch.qos.logback.core.Context
 *  org.slf4j.LoggerFactory
 */
package net.runelite.client.plugins.svc.diagnostics;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Context;
import org.slf4j.LoggerFactory;

class LogTap
extends AppenderBase<ILoggingEvent> {
    private final Sink sink;
    private final int thresholdInt;
    private Logger root;

    private LogTap(Sink sink, int n) {
        this.sink = sink;
        this.thresholdInt = n;
    }

    static LogTap attach(Sink sink, boolean bl) {
        try {
            Logger logger = (Logger)LoggerFactory.getLogger((String)"ROOT");
            int n = (bl ? Level.INFO : Level.WARN).toInt();
            LogTap logTap = new LogTap(sink, n);
            logTap.root = logger;
            logTap.setName("x64-diagnostics");
            logTap.setContext((Context)logger.getLoggerContext());
            logTap.start();
            logger.addAppender((Appender)logTap);
            return logTap;
        }
        catch (Throwable throwable) {
            return null;
        }
    }

    void detach() {
        try {
            if (this.root != null) {
                this.root.detachAppender((Appender)this);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            this.stop();
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        this.root = null;
    }

    protected void append(ILoggingEvent iLoggingEvent) {
        try {
            if (iLoggingEvent == null || iLoggingEvent.getLevel() == null) {
                return;
            }
            int n = iLoggingEvent.getLevel().toInt();
            if (n < this.thresholdInt) {
                return;
            }
            String string = null;
            IThrowableProxy iThrowableProxy = iLoggingEvent.getThrowableProxy();
            if (iThrowableProxy != null) {
                string = ThrowableProxyUtil.asString((IThrowableProxy)iThrowableProxy);
            }
            this.sink.onLog(n, iLoggingEvent.getLevel().toString(), iLoggingEvent.getLoggerName(), iLoggingEvent.getFormattedMessage(), string);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    static interface Sink {
        public void onLog(int var1, String var2, String var3, String var4, String var5);
    }
}


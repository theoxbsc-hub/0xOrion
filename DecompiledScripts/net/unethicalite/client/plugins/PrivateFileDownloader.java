/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  okhttp3.OkHttpClient
 *  okhttp3.Request
 *  okhttp3.Request$Builder
 *  okhttp3.Response
 *  okio.BufferedSink
 *  okio.BufferedSource
 *  okio.Okio
 *  okio.Sink
 *  okio.Source
 *  org.pf4j.update.SimpleFileDownloader
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.unethicalite.client.plugins;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import okio.Sink;
import okio.Source;
import org.pf4j.update.SimpleFileDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrivateFileDownloader
extends SimpleFileDownloader {
    private static final Logger log = LoggerFactory.getLogger(PrivateFileDownloader.class);
    private final String token;

    public PrivateFileDownloader(String token) {
        this.token = token;
    }

    protected Path downloadFileHttp(URL fileUrl) throws IOException {
        Path path;
        block16: {
            Path destination = Files.createTempDirectory("pf4j-update-downloader", new FileAttribute[0]);
            destination.toFile().deleteOnExit();
            String path2 = fileUrl.getPath();
            String fileName = path2.substring(path2.lastIndexOf(47) + 1);
            Path file = destination.resolve(fileName);
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder().addHeader("Authorization", "Bearer " + this.token).url(fileUrl).get().build();
            Response response = okHttpClient.newCall(request).execute();
            try {
                long lastModified = System.currentTimeMillis();
                if (response.headers().getDate("Last-Modified") != null) {
                    lastModified = response.headers().getDate("Last-Modified").toInstant().toEpochMilli();
                }
                try (BufferedSource bufferedSource = response.body().source();){
                    BufferedSink bufferedSink = Okio.buffer((Sink)Okio.sink((Path)file, (OpenOption[])new OpenOption[0]));
                    bufferedSink.writeAll((Source)bufferedSource);
                    bufferedSink.close();
                }
                log.debug("Set last modified of '{}' to '{}'", (Object)file, (Object)lastModified);
                Files.setLastModifiedTime(file, FileTime.fromMillis(lastModified));
                path = file;
                if (response == null) break block16;
            }
            catch (Throwable throwable) {
                try {
                    if (response != null) {
                        try {
                            response.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (Exception e) {
                    log.error("Exception", (Throwable)e);
                    throw new IOException(e);
                }
            }
            response.close();
        }
        return path;
    }
}


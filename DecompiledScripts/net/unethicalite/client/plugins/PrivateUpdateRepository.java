/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  net.runelite.client.plugins.OPRSUpdateRepository
 *  okhttp3.OkHttpClient
 *  okhttp3.Request
 *  okhttp3.Request$Builder
 *  okhttp3.Response
 *  org.pf4j.update.DefaultUpdateRepository
 *  org.pf4j.update.FileDownloader
 *  org.pf4j.update.PluginInfo
 *  org.pf4j.update.PluginInfo$PluginRelease
 *  org.pf4j.update.util.LenientDateTypeAdapter
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.unethicalite.client.plugins;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import net.runelite.client.plugins.OPRSUpdateRepository;
import net.unethicalite.client.plugins.PrivateFileDownloader;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.pf4j.update.DefaultUpdateRepository;
import org.pf4j.update.FileDownloader;
import org.pf4j.update.PluginInfo;
import org.pf4j.update.util.LenientDateTypeAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrivateUpdateRepository
extends OPRSUpdateRepository {
    private static final String DEFAULT_PLUGINS_JSON_FILENAME = "plugins.json";
    private static final Logger log = LoggerFactory.getLogger(DefaultUpdateRepository.class);
    private final String token;

    public PrivateUpdateRepository(String id, URL url, String token) {
        this(id, url, DEFAULT_PLUGINS_JSON_FILENAME, token);
    }

    public PrivateUpdateRepository(String id, URL url, String pluginsJsonFileName, String token) {
        super(id, url, pluginsJsonFileName);
        this.token = token;
    }

    public void initPlugins() {
        try {
            URL pluginsUrl = new URL(this.getUrl(), this.getPluginsJsonFileName());
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder().addHeader("Authorization", "Bearer " + this.token).url(pluginsUrl).get().build();
            log.debug("Read plugins of '{}' repository from '{}'", (Object)this.getId(), (Object)pluginsUrl);
            try (Response response = okHttpClient.newCall(request).execute();){
                InputStreamReader pluginsJsonReader = new InputStreamReader(Objects.requireNonNull(response.body()).byteStream());
                Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, (Object)new LenientDateTypeAdapter()).create();
                PluginInfo[] items = (PluginInfo[])gson.fromJson((Reader)pluginsJsonReader, PluginInfo[].class);
                this.plugins = new HashMap(items.length);
                for (PluginInfo p : items) {
                    for (PluginInfo.PluginRelease r : p.releases) {
                        try {
                            r.url = new URL(this.getUrl(), r.url).toString();
                            if (r.date.getTime() != 0L) continue;
                            log.warn("Illegal release date when parsing {}@{}, setting to epoch", (Object)p.id, (Object)r.version);
                        }
                        catch (MalformedURLException e) {
                            log.warn("Skipping release {} of plugin {} due to failure to build valid absolute URL. Url was {}{}", new Object[]{r.version, p.id, this.getUrl(), r.url});
                        }
                    }
                    p.setRepositoryId(this.getId());
                    this.plugins.put(p.id, p);
                }
                log.debug("Found {} plugins in repository '{}'", (Object)this.plugins.size(), (Object)this.getId());
            }
        }
        catch (Exception e) {
            log.error(e.getMessage(), (Throwable)e);
            this.plugins = Collections.emptyMap();
        }
    }

    public FileDownloader getFileDownloader() {
        return new PrivateFileDownloader(this.token);
    }

    public String getToken() {
        return this.token;
    }
}


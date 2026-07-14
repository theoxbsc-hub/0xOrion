/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  javax.inject.Inject
 *  javax.inject.Named
 *  javax.inject.Singleton
 *  okhttp3.OkHttpClient
 *  okhttp3.Request
 *  okhttp3.Request$Builder
 *  okhttp3.Response
 *  okhttp3.ResponseBody
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.unethicalite.client.managers;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class PluginRepoManager {
    private static final Logger log = LoggerFactory.getLogger(PluginRepoManager.class);
    private List<String> pluginRepoUrls = new ArrayList<String>();

    @Inject
    PluginRepoManager(@Named(value="unethicalite.api.url") String apiUrl, OkHttpClient okHttpClient) {
        String endPoint = apiUrl + "/malicious-repos";
        Request request = new Request.Builder().get().url(endPoint).build();
        try (Response response = okHttpClient.newCall(request).execute();){
            int code = response.code();
            if (code != 200) {
                log.error("Request unsuccessful: {}", (Object)code);
                return;
            }
            ResponseBody body = response.body();
            if (body == null) {
                log.error("Response body is null");
                return;
            }
            Gson gson = new Gson();
            this.pluginRepoUrls = Arrays.asList((String[])gson.fromJson(body.string(), String[].class));
        }
        catch (Exception e) {
            log.error("Failed to GET", (Throwable)e);
        }
    }

    public boolean isRepoMalicious(String owner) {
        return this.pluginRepoUrls.contains(owner);
    }
}


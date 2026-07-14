/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.client.plugins.Plugin
 *  net.runelite.client.plugins.PluginDescriptor
 */
package net.unethicalite.client.minimal.plugins;

import java.util.jar.Manifest;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.plugins.Script;

public final class PluginEntry {
    private final Class<? extends Plugin> scriptClass;
    private final PluginDescriptor meta;
    private final Manifest manifest;

    public String toString() {
        return this.meta.name();
    }

    public boolean isScript() {
        return Script.class.isAssignableFrom(this.scriptClass);
    }

    public PluginEntry(Class<? extends Plugin> scriptClass, PluginDescriptor meta, Manifest manifest) {
        this.scriptClass = scriptClass;
        this.meta = meta;
        this.manifest = manifest;
    }

    public Class<? extends Plugin> getScriptClass() {
        return this.scriptClass;
    }

    public PluginDescriptor getMeta() {
        return this.meta;
    }

    public Manifest getManifest() {
        return this.manifest;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof PluginEntry)) {
            return false;
        }
        PluginEntry other = (PluginEntry)o;
        Class<? extends Plugin> this$scriptClass = this.getScriptClass();
        Class<? extends Plugin> other$scriptClass = other.getScriptClass();
        if (this$scriptClass == null ? other$scriptClass != null : !this$scriptClass.equals(other$scriptClass)) {
            return false;
        }
        PluginDescriptor this$meta = this.getMeta();
        PluginDescriptor other$meta = other.getMeta();
        if (this$meta == null ? other$meta != null : !this$meta.equals(other$meta)) {
            return false;
        }
        Manifest this$manifest = this.getManifest();
        Manifest other$manifest = other.getManifest();
        return !(this$manifest == null ? other$manifest != null : !((Object)this$manifest).equals(other$manifest));
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Class<? extends Plugin> $scriptClass = this.getScriptClass();
        result = result * 59 + ($scriptClass == null ? 43 : $scriptClass.hashCode());
        PluginDescriptor $meta = this.getMeta();
        result = result * 59 + ($meta == null ? 43 : $meta.hashCode());
        Manifest $manifest = this.getManifest();
        result = result * 59 + ($manifest == null ? 43 : ((Object)$manifest).hashCode());
        return result;
    }
}


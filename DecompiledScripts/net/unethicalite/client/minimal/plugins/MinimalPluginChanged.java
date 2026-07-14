/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.client.plugins.Plugin
 */
package net.unethicalite.client.minimal.plugins;

import net.runelite.client.plugins.Plugin;
import net.unethicalite.client.minimal.plugins.MinimalPluginState;

public final class MinimalPluginChanged {
    private final Plugin plugin;
    private final MinimalPluginState state;

    public MinimalPluginChanged(Plugin plugin, MinimalPluginState state) {
        this.plugin = plugin;
        this.state = state;
    }

    public Plugin getPlugin() {
        return this.plugin;
    }

    public MinimalPluginState getState() {
        return this.state;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof MinimalPluginChanged)) {
            return false;
        }
        MinimalPluginChanged other = (MinimalPluginChanged)o;
        Plugin this$plugin = this.getPlugin();
        Plugin other$plugin = other.getPlugin();
        if (this$plugin == null ? other$plugin != null : !this$plugin.equals(other$plugin)) {
            return false;
        }
        MinimalPluginState this$state = this.getState();
        MinimalPluginState other$state = other.getState();
        return !(this$state == null ? other$state != null : !((Object)((Object)this$state)).equals((Object)other$state));
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Plugin $plugin = this.getPlugin();
        result = result * 59 + ($plugin == null ? 43 : $plugin.hashCode());
        MinimalPluginState $state = this.getState();
        result = result * 59 + ($state == null ? 43 : ((Object)((Object)$state)).hashCode());
        return result;
    }

    public String toString() {
        return "MinimalPluginChanged(plugin=" + String.valueOf(this.getPlugin()) + ", state=" + String.valueOf((Object)this.getState()) + ")";
    }
}


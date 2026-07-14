/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.client.config.ConfigDescriptor
 */
package net.unethicalite.client.minimal.config;

import net.runelite.client.config.ConfigDescriptor;

public final class ConfigurationDescriptor {
    private final String name;
    private final String description;
    private final ConfigDescriptor configDescriptor;

    public boolean hasConfigurables() {
        return this.configDescriptor != null && !this.configDescriptor.getItems().stream().allMatch(x -> x.getItem().hidden());
    }

    public ConfigurationDescriptor(String name, String description, ConfigDescriptor configDescriptor) {
        this.name = name;
        this.description = description;
        this.configDescriptor = configDescriptor;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public ConfigDescriptor getConfigDescriptor() {
        return this.configDescriptor;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ConfigurationDescriptor)) {
            return false;
        }
        ConfigurationDescriptor other = (ConfigurationDescriptor)o;
        String this$name = this.getName();
        String other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) {
            return false;
        }
        String this$description = this.getDescription();
        String other$description = other.getDescription();
        if (this$description == null ? other$description != null : !this$description.equals(other$description)) {
            return false;
        }
        ConfigDescriptor this$configDescriptor = this.getConfigDescriptor();
        ConfigDescriptor other$configDescriptor = other.getConfigDescriptor();
        return !(this$configDescriptor == null ? other$configDescriptor != null : !this$configDescriptor.equals(other$configDescriptor));
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        String $name = this.getName();
        result = result * 59 + ($name == null ? 43 : $name.hashCode());
        String $description = this.getDescription();
        result = result * 59 + ($description == null ? 43 : $description.hashCode());
        ConfigDescriptor $configDescriptor = this.getConfigDescriptor();
        result = result * 59 + ($configDescriptor == null ? 43 : $configDescriptor.hashCode());
        return result;
    }

    public String toString() {
        return "ConfigurationDescriptor(name=" + this.getName() + ", description=" + this.getDescription() + ", configDescriptor=" + String.valueOf(this.getConfigDescriptor()) + ")";
    }
}


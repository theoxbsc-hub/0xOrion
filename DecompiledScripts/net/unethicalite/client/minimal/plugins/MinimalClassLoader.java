/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.client.util.ReflectUtil
 *  net.runelite.client.util.ReflectUtil$PrivateLookupableClassLoader
 */
package net.unethicalite.client.minimal.plugins;

import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.net.URLClassLoader;
import net.runelite.client.util.ReflectUtil;

public class MinimalClassLoader
extends URLClassLoader
implements ReflectUtil.PrivateLookupableClassLoader {
    private MethodHandles.Lookup lookup;

    public MinimalClassLoader(URL[] urls) {
        super(urls, MinimalClassLoader.class.getClassLoader());
        ReflectUtil.installLookupHelper((ReflectUtil.PrivateLookupableClassLoader)this);
    }

    public Class<?> defineClass0(String name, byte[] b, int off, int len) throws ClassFormatError {
        return super.defineClass(name, b, off, len);
    }

    public MethodHandles.Lookup getLookup() {
        return this.lookup;
    }

    public void setLookup(MethodHandles.Lookup lookup) {
        this.lookup = lookup;
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.client.config.Config
 *  net.runelite.client.config.ConfigGroup
 *  net.runelite.client.config.ConfigItem
 *  net.runelite.client.config.ConfigSection
 *  net.runelite.client.config.Range
 */
package net.runelite.client.plugins.svc.autologin;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.plugins.svc.autologin.ProfileSlot;

@ConfigGroup(value="autoLogin")
public interface AutoLoginConfig
extends Config {
    @ConfigSection(name="Accounts", description="Enter your account credentials and the proxy each account is assigned to. Saved automatically.", position=2)
    public static final String accountsSection = "accounts";
    @ConfigSection(name="Behaviour", description="Login timing", position=3)
    public static final String behaviourSection = "behaviour";

    @ConfigItem(keyName="activeProfile", name="Active Profile", description="Profile to log in with when no launch-proxy binding applies (manual / fallback)", position=0)
    default public ProfileSlot activeProfile() {
        return ProfileSlot.PROFILE_1;
    }

    @ConfigItem(keyName="bindProfileToProxy", name="Bind profile to launch proxy", description="On launch, auto-select the profile assigned to the proxy you chose in the launcher", position=1)
    default public boolean bindProfileToProxy() {
        return true;
    }

    @ConfigItem(keyName="profile1Username", name="Profile 1 Username", description="Login email/username for Profile 1", section="accounts", position=0)
    default public String profile1Username() {
        return "";
    }

    @ConfigItem(keyName="profile1Password", name="Profile 1 Password", description="Password for Profile 1", section="accounts", position=1, secret=true)
    default public String profile1Password() {
        return "";
    }

    @ConfigItem(keyName="profile1Proxy", name="Profile 1 Proxy", description="Proxy assigned to Profile 1: the proxies.txt label (e.g. webshare-us-1) or raw host:port. Launch on this proxy and Auto Login picks this profile.", section="accounts", position=2)
    default public String profile1Proxy() {
        return "";
    }

    @ConfigItem(keyName="profile2Username", name="Profile 2 Username", description="Login email/username for Profile 2", section="accounts", position=3)
    default public String profile2Username() {
        return "";
    }

    @ConfigItem(keyName="profile2Password", name="Profile 2 Password", description="Password for Profile 2", section="accounts", position=4, secret=true)
    default public String profile2Password() {
        return "";
    }

    @ConfigItem(keyName="profile2Proxy", name="Profile 2 Proxy", description="Proxy assigned to Profile 2: the proxies.txt label (e.g. webshare-us-1) or raw host:port. Launch on this proxy and Auto Login picks this profile.", section="accounts", position=5)
    default public String profile2Proxy() {
        return "";
    }

    @ConfigItem(keyName="profile3Username", name="Profile 3 Username", description="Login email/username for Profile 3", section="accounts", position=6)
    default public String profile3Username() {
        return "";
    }

    @ConfigItem(keyName="profile3Password", name="Profile 3 Password", description="Password for Profile 3", section="accounts", position=7, secret=true)
    default public String profile3Password() {
        return "";
    }

    @ConfigItem(keyName="profile3Proxy", name="Profile 3 Proxy", description="Proxy assigned to Profile 3: the proxies.txt label (e.g. webshare-us-1) or raw host:port. Launch on this proxy and Auto Login picks this profile.", section="accounts", position=8)
    default public String profile3Proxy() {
        return "";
    }

    @ConfigItem(keyName="profile4Username", name="Profile 4 Username", description="Login email/username for Profile 4", section="accounts", position=9)
    default public String profile4Username() {
        return "";
    }

    @ConfigItem(keyName="profile4Password", name="Profile 4 Password", description="Password for Profile 4", section="accounts", position=10, secret=true)
    default public String profile4Password() {
        return "";
    }

    @ConfigItem(keyName="profile4Proxy", name="Profile 4 Proxy", description="Proxy assigned to Profile 4: the proxies.txt label (e.g. webshare-us-1) or raw host:port. Launch on this proxy and Auto Login picks this profile.", section="accounts", position=11)
    default public String profile4Proxy() {
        return "";
    }

    @ConfigItem(keyName="profile5Username", name="Profile 5 Username", description="Login email/username for Profile 5", section="accounts", position=12)
    default public String profile5Username() {
        return "";
    }

    @ConfigItem(keyName="profile5Password", name="Profile 5 Password", description="Password for Profile 5", section="accounts", position=13, secret=true)
    default public String profile5Password() {
        return "";
    }

    @ConfigItem(keyName="profile5Proxy", name="Profile 5 Proxy", description="Proxy assigned to Profile 5: the proxies.txt label (e.g. webshare-us-1) or raw host:port. Launch on this proxy and Auto Login picks this profile.", section="accounts", position=14)
    default public String profile5Proxy() {
        return "";
    }

    @ConfigItem(keyName="profile6Username", name="Profile 6 Username", description="Login email/username for Profile 6", section="accounts", position=15)
    default public String profile6Username() {
        return "";
    }

    @ConfigItem(keyName="profile6Password", name="Profile 6 Password", description="Password for Profile 6", section="accounts", position=16, secret=true)
    default public String profile6Password() {
        return "";
    }

    @ConfigItem(keyName="profile6Proxy", name="Profile 6 Proxy", description="Proxy assigned to Profile 6: the proxies.txt label (e.g. webshare-us-1) or raw host:port. Launch on this proxy and Auto Login picks this profile.", section="accounts", position=17)
    default public String profile6Proxy() {
        return "";
    }

    @ConfigItem(keyName="profile7Username", name="Profile 7 Username", description="Login email/username for Profile 7", section="accounts", position=18)
    default public String profile7Username() {
        return "";
    }

    @ConfigItem(keyName="profile7Password", name="Profile 7 Password", description="Password for Profile 7", section="accounts", position=19, secret=true)
    default public String profile7Password() {
        return "";
    }

    @ConfigItem(keyName="profile7Proxy", name="Profile 7 Proxy", description="Proxy assigned to Profile 7: the proxies.txt label (e.g. webshare-us-1) or raw host:port. Launch on this proxy and Auto Login picks this profile.", section="accounts", position=20)
    default public String profile7Proxy() {
        return "";
    }

    @ConfigItem(keyName="profile8Username", name="Profile 8 Username", description="Login email/username for Profile 8", section="accounts", position=21)
    default public String profile8Username() {
        return "";
    }

    @ConfigItem(keyName="profile8Password", name="Profile 8 Password", description="Password for Profile 8", section="accounts", position=22, secret=true)
    default public String profile8Password() {
        return "";
    }

    @ConfigItem(keyName="profile8Proxy", name="Profile 8 Proxy", description="Proxy assigned to Profile 8: the proxies.txt label (e.g. webshare-us-1) or raw host:port. Launch on this proxy and Auto Login picks this profile.", section="accounts", position=23)
    default public String profile8Proxy() {
        return "";
    }

    @ConfigItem(keyName="profile9Username", name="Profile 9 Username", description="Login email/username for Profile 9", section="accounts", position=24)
    default public String profile9Username() {
        return "";
    }

    @ConfigItem(keyName="profile9Password", name="Profile 9 Password", description="Password for Profile 9", section="accounts", position=25, secret=true)
    default public String profile9Password() {
        return "";
    }

    @ConfigItem(keyName="profile9Proxy", name="Profile 9 Proxy", description="Proxy assigned to Profile 9: the proxies.txt label (e.g. webshare-us-1) or raw host:port. Launch on this proxy and Auto Login picks this profile.", section="accounts", position=26)
    default public String profile9Proxy() {
        return "";
    }

    @ConfigItem(keyName="profile10Username", name="Profile 10 Username", description="Login email/username for Profile 10", section="accounts", position=27)
    default public String profile10Username() {
        return "";
    }

    @ConfigItem(keyName="profile10Password", name="Profile 10 Password", description="Password for Profile 10", section="accounts", position=28, secret=true)
    default public String profile10Password() {
        return "";
    }

    @ConfigItem(keyName="profile10Proxy", name="Profile 10 Proxy", description="Proxy assigned to Profile 10: the proxies.txt label (e.g. webshare-us-1) or raw host:port. Launch on this proxy and Auto Login picks this profile.", section="accounts", position=29)
    default public String profile10Proxy() {
        return "";
    }

    @ConfigItem(keyName="profile11Username", name="Profile 11 Username", description="Login email/username for Profile 11", section="accounts", position=30)
    default public String profile11Username() {
        return "";
    }

    @ConfigItem(keyName="profile11Password", name="Profile 11 Password", description="Password for Profile 11", section="accounts", position=31, secret=true)
    default public String profile11Password() {
        return "";
    }

    @ConfigItem(keyName="profile11Proxy", name="Profile 11 Proxy", description="Proxy assigned to Profile 11: the proxies.txt label (e.g. webshare-us-1) or raw host:port. Launch on this proxy and Auto Login picks this profile.", section="accounts", position=32)
    default public String profile11Proxy() {
        return "";
    }

    @ConfigItem(keyName="profile12Username", name="Profile 12 Username", description="Login email/username for Profile 12", section="accounts", position=33)
    default public String profile12Username() {
        return "";
    }

    @ConfigItem(keyName="profile12Password", name="Profile 12 Password", description="Password for Profile 12", section="accounts", position=34, secret=true)
    default public String profile12Password() {
        return "";
    }

    @ConfigItem(keyName="profile12Proxy", name="Profile 12 Proxy", description="Proxy assigned to Profile 12: the proxies.txt label (e.g. webshare-us-1) or raw host:port. Launch on this proxy and Auto Login picks this profile.", section="accounts", position=35)
    default public String profile12Proxy() {
        return "";
    }

    @ConfigItem(keyName="profile13Username", name="Profile 13 Username", description="Login email/username for Profile 13", section="accounts", position=36)
    default public String profile13Username() {
        return "";
    }

    @ConfigItem(keyName="profile13Password", name="Profile 13 Password", description="Password for Profile 13", section="accounts", position=37, secret=true)
    default public String profile13Password() {
        return "";
    }

    @ConfigItem(keyName="profile13Proxy", name="Profile 13 Proxy", description="Proxy assigned to Profile 13: the proxies.txt label (e.g. webshare-us-1) or raw host:port. Launch on this proxy and Auto Login picks this profile.", section="accounts", position=38)
    default public String profile13Proxy() {
        return "";
    }

    @ConfigItem(keyName="profile14Username", name="Profile 14 Username", description="Login email/username for Profile 14", section="accounts", position=39)
    default public String profile14Username() {
        return "";
    }

    @ConfigItem(keyName="profile14Password", name="Profile 14 Password", description="Password for Profile 14", section="accounts", position=40, secret=true)
    default public String profile14Password() {
        return "";
    }

    @ConfigItem(keyName="profile14Proxy", name="Profile 14 Proxy", description="Proxy assigned to Profile 14: the proxies.txt label (e.g. webshare-us-1) or raw host:port. Launch on this proxy and Auto Login picks this profile.", section="accounts", position=41)
    default public String profile14Proxy() {
        return "";
    }

    @ConfigItem(keyName="profile15Username", name="Profile 15 Username", description="Login email/username for Profile 15", section="accounts", position=42)
    default public String profile15Username() {
        return "";
    }

    @ConfigItem(keyName="profile15Password", name="Profile 15 Password", description="Password for Profile 15", section="accounts", position=43, secret=true)
    default public String profile15Password() {
        return "";
    }

    @ConfigItem(keyName="profile15Proxy", name="Profile 15 Proxy", description="Proxy assigned to Profile 15: the proxies.txt label (e.g. webshare-us-1) or raw host:port. Launch on this proxy and Auto Login picks this profile.", section="accounts", position=44)
    default public String profile15Proxy() {
        return "";
    }

    @ConfigItem(keyName="profile16Username", name="Profile 16 Username", description="Login email/username for Profile 16", section="accounts", position=45)
    default public String profile16Username() {
        return "";
    }

    @ConfigItem(keyName="profile16Password", name="Profile 16 Password", description="Password for Profile 16", section="accounts", position=46, secret=true)
    default public String profile16Password() {
        return "";
    }

    @ConfigItem(keyName="profile16Proxy", name="Profile 16 Proxy", description="Proxy assigned to Profile 16: the proxies.txt label (e.g. webshare-us-1) or raw host:port. Launch on this proxy and Auto Login picks this profile.", section="accounts", position=47)
    default public String profile16Proxy() {
        return "";
    }

    @ConfigItem(keyName="profile17Username", name="Profile 17 Username", description="Login email/username for Profile 17", section="accounts", position=48)
    default public String profile17Username() {
        return "";
    }

    @ConfigItem(keyName="profile17Password", name="Profile 17 Password", description="Password for Profile 17", section="accounts", position=49, secret=true)
    default public String profile17Password() {
        return "";
    }

    @ConfigItem(keyName="profile17Proxy", name="Profile 17 Proxy", description="Proxy assigned to Profile 17: the proxies.txt label (e.g. webshare-us-1) or raw host:port. Launch on this proxy and Auto Login picks this profile.", section="accounts", position=50)
    default public String profile17Proxy() {
        return "";
    }

    @ConfigItem(keyName="profile18Username", name="Profile 18 Username", description="Login email/username for Profile 18", section="accounts", position=51)
    default public String profile18Username() {
        return "";
    }

    @ConfigItem(keyName="profile18Password", name="Profile 18 Password", description="Password for Profile 18", section="accounts", position=52, secret=true)
    default public String profile18Password() {
        return "";
    }

    @ConfigItem(keyName="profile18Proxy", name="Profile 18 Proxy", description="Proxy assigned to Profile 18: the proxies.txt label (e.g. webshare-us-1) or raw host:port. Launch on this proxy and Auto Login picks this profile.", section="accounts", position=53)
    default public String profile18Proxy() {
        return "";
    }

    @ConfigItem(keyName="profile19Username", name="Profile 19 Username", description="Login email/username for Profile 19", section="accounts", position=54)
    default public String profile19Username() {
        return "";
    }

    @ConfigItem(keyName="profile19Password", name="Profile 19 Password", description="Password for Profile 19", section="accounts", position=55, secret=true)
    default public String profile19Password() {
        return "";
    }

    @ConfigItem(keyName="profile19Proxy", name="Profile 19 Proxy", description="Proxy assigned to Profile 19: the proxies.txt label (e.g. webshare-us-1) or raw host:port. Launch on this proxy and Auto Login picks this profile.", section="accounts", position=56)
    default public String profile19Proxy() {
        return "";
    }

    @ConfigItem(keyName="profile20Username", name="Profile 20 Username", description="Login email/username for Profile 20", section="accounts", position=57)
    default public String profile20Username() {
        return "";
    }

    @ConfigItem(keyName="profile20Password", name="Profile 20 Password", description="Password for Profile 20", section="accounts", position=58, secret=true)
    default public String profile20Password() {
        return "";
    }

    @ConfigItem(keyName="profile20Proxy", name="Profile 20 Proxy", description="Proxy assigned to Profile 20: the proxies.txt label (e.g. webshare-us-1) or raw host:port. Launch on this proxy and Auto Login picks this profile.", section="accounts", position=59)
    default public String profile20Proxy() {
        return "";
    }

    @ConfigItem(keyName="loginDelay", name="Login Delay (seconds)", description="Seconds to wait on the login screen before attempting to log in", section="behaviour", position=0)
    @Range(min=1, max=60)
    default public int loginDelay() {
        return 2;
    }
}


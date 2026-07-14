/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.api.GameState
 *  org.jboss.aerogear.security.otp.Totp
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.unethicalite.api.script.blocking_events;

import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.unethicalite.api.account.GameAccount;
import net.unethicalite.api.game.Game;
import net.unethicalite.api.game.Worlds;
import net.unethicalite.api.input.Keyboard;
import net.unethicalite.api.script.blocking_events.BlockingEvent;
import net.unethicalite.api.script.blocking_events.BlockingEventManager;
import net.unethicalite.client.Static;
import org.jboss.aerogear.security.otp.Totp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginEvent
extends BlockingEvent {
    private static final Logger log = LoggerFactory.getLogger(LoginEvent.class);
    private final BlockingEventManager blockingEventManager;
    private Totp totp = null;
    private long timer = 0L;
    private long validateTime = -1L;
    private long maxLoginScreenTime = 1800000L;

    @Override
    public boolean validate() {
        boolean validate;
        boolean bl = validate = !Game.isLoggedIn() && Game.getGameAccount() != null;
        if (validate && this.validateTime < 0L) {
            this.validateTime = System.currentTimeMillis();
        }
        if (!validate) {
            this.validateTime = -1L;
        }
        return validate;
    }

    @Override
    public int loop() {
        String loginMessage = Static.getClient().getLoginMessage();
        Client client = Static.getClient();
        if (this.validateTime > -1L && System.currentTimeMillis() - this.validateTime >= this.maxLoginScreenTime) {
            log.info("Spent longer than 10 mins in login screen, exiting client.");
            System.exit(0);
        }
        if (client.getWorldList() == null) {
            Worlds.loadWorlds();
            return 1000;
        }
        if (client.isWorldSelectOpen()) {
            client.setWorldSelectOpen(false);
            return 1000;
        }
        if (loginMessage.toLowerCase(Locale.ROOT).contains("update")) {
            log.info("Game was updated, exiting client.");
            System.exit(0);
        }
        for (Map.Entry<Response, Function<Response, Integer>> entry : this.blockingEventManager.getLoginMessageHandlers().entrySet()) {
            Response response = entry.getKey();
            Function<Response, Integer> handler = entry.getValue();
            if (!loginMessage.contains(response.line1)) continue;
            return handler.apply(response);
        }
        return this.login();
    }

    public int login() {
        Client client = Static.getClient();
        int loginState = client.getLoginIndex();
        GameAccount gameAccount = Game.getGameAccount();
        if (gameAccount == null) {
            log.warn("No account set, stopping login event.");
            return -1000;
        }
        switch (loginState) {
            case 4: {
                if (gameAccount.getAuth() == null) {
                    log.error("We are on auth screen, but auth code was not set.");
                    return -1000;
                }
                this.totp = new Totp(gameAccount.getAuth());
                client.setOtp(this.totp.now());
                Keyboard.sendEnter();
                return 1000;
            }
            case 2: {
                if (Game.getState() == GameState.LOGGING_IN) {
                    this.timer = 0L;
                    return 1000;
                }
                client.setUsername(gameAccount.getUsername());
                client.setPassword(gameAccount.getPassword());
                Keyboard.sendEnter();
                Keyboard.sendEnter();
                return 1000;
            }
            case 0: 
            case 24: {
                client.setLoginIndex(2);
                return 1000;
            }
            case 3: 
            case 14: {
                log.warn("Invalid account set.");
                return 5000;
            }
        }
        log.warn("Cannot handle login state {}", (Object)loginState);
        return 1000;
    }

    public LoginEvent(BlockingEventManager blockingEventManager) {
        this.blockingEventManager = blockingEventManager;
    }

    public static enum Response {
        CONNECTION_TIMED_OUT(-3, "Connection timed out.", "Please try using a different world.", ""),
        ERROR_CONNECTING(-2, "", "Error connecting to server.", "Please try using a different world."),
        NO_SERVER_RESPONSE(-1, "No response from server.", "Please try using a different world.", ""),
        INVALID_CREDENTIALS(3, "Invalid credentials.", "", ""),
        ACCOUNT_DISABLED(4, "Your account has been disabled.", "Please visit the support page for assistance.", ""),
        ACCOUNT_NOT_LOGGED_OUT(5, "Your account has not logged out from its last", "session or the server is too busy right now.", "Please try again in a few minutes."),
        RUNESCAPE_UPDATE(6, "RuneScape has been updated!", "Please reload this page.", ""),
        WORLD_FULL(7, "This world is full.", "Please use a different world.", ""),
        LOGIN_SERVER_OFFLINE(8, "Unable to connect.", "Login server offline.", ""),
        LOGIN_LIMIT(9, "Login limit exceeded.", "Too many connections from your address.", ""),
        BAD_SESSION_ID(10, "Unable to connect.", "Bad session id.", ""),
        ACCOUNT_STOLEN(11, "We suspect someone knows your password.", "Press 'change your password' on front page.", ""),
        MEMBERSHIP_REQUIRED(12, "You need a members account to login to this world.", "Please subscribe, or use a different world.", ""),
        COULD_NOT_COMPLETE(13, "Could not complete login.", "Please try using a different world.", ""),
        SERVER_BEING_UPDATED(14, "The server is being updated.", "Please wait 1 minute and try again.", ""),
        TOO_MANY_ATTEMPTS(16, "Too many login attempts.", "Please wait a few minutes before trying again.", ""),
        MEMBERS_ONLY_AREA(17, "You are standing in a members-only area.", "To play on this world move to a free area first", ""),
        ACCOUNT_LOCKED(18, "Account locked as we suspect it has been stolen.", "Please visit the support page for assistance.", ""),
        WORLD_CLOSED_BETA(19, "This world is running a closed Beta.", "Sorry invited players only.", "Please use a different world."),
        INVALID_LOGIN_SERVER(20, "Invalid loginserver requested.", "Please try using a different world.", ""),
        MALFORMED_LOGIN_PACKET(22, "Malformed login packet.", "Please try again.", ""),
        NO_SERVER_REPLY(23, "No reply from loginserver.", "Please wait 1 minute and try again.", ""),
        ERROR_LOADING_PROFILE(24, "Error loading your profile.", "Please contact customer support.", ""),
        UNEXPECTED_SERVER_RESPONSE(25, "Unexpected loginserver response.", "Please try using a different world.", ""),
        COMPUTER_ADDRESS_BLOCKED(26, "This computers address has been blocked", "as it was used to break our rules.", ""),
        SERVICE_UNAVAILABLE(27, "", "Service unavailable.", ""),
        NO_DISPLAY_NAME_SET(31, "Your account must have a displayname set", "in order to play the game.  Please set it", "via the website, or the main game."),
        UNSUCCESSFUL_ACCOUNT_LOGIN_ATTEMPT(32, "Your attempt to log into your account was", "unsuccessful.  Don't worry, you can sort", "this out by visiting the billing system."),
        ACCOUNT_INACCESSIBLE(37, "Your account is currently inaccessible.", "Please try again in a few minutes.", ""),
        VOTE_REQUIRED(38, "You need to vote to play!", "Visit runescape.com and vote,", "and then come back here!"),
        ENTER_AUTH(56, "Enter the 6-digit code generated by your", "authenticator app.", ""),
        INCORRECT_AUTH_CODE(57, "The code you entered was incorrect.", "Please try again.", ""),
        RUNESCAPE_UPDATE_2(68, "RuneScape has been updated!", "Please reload this page.", "");

        private final int code;
        private final String line1;
        private final String line2;
        private final String line3;

        public String getMessage() {
            return this.line1 + " " + this.line2 + " " + this.line3;
        }

        public static Response forCode(int code) {
            for (Response value : Response.values()) {
                if (value.code != code) continue;
                return value;
            }
            return null;
        }

        public static Response forMessage(String contained) {
            for (Response value : Response.values()) {
                if (!value.getMessage().toLowerCase(Locale.ROOT).contains(contained.toLowerCase(Locale.ROOT))) continue;
                return value;
            }
            return null;
        }

        private Response(int code, String line1, String line2, String line3) {
            this.code = code;
            this.line1 = line1;
            this.line2 = line2;
            this.line3 = line3;
        }

        public int getCode() {
            return this.code;
        }

        public String getLine1() {
            return this.line1;
        }

        public String getLine2() {
            return this.line2;
        }

        public String getLine3() {
            return this.line3;
        }
    }

    public static interface State {
        public static final int MAIN_MENU = 0;
        public static final int BETA_WORLD = 1;
        public static final int ENTER_CREDENTIALS = 2;
        public static final int INVALID_CREDENTIALS = 3;
        public static final int AUTHENTICATOR = 4;
        public static final int DISABLED = 14;
        public static final int BEEN_DISCONNECTED = 24;
    }
}


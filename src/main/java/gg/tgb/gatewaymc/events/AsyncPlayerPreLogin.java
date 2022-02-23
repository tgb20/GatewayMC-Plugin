package gg.tgb.gatewaymc.events;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.bukkit.Bukkit.getServer;

public class AsyncPlayerPreLogin implements Listener {

    private Logger log;
    private String code;

    public AsyncPlayerPreLogin(String code, Logger logger) {
        this.code = code;
        this.log = logger;
    }

    @EventHandler
    public void onAsyncPlayerPreLoginEvent(AsyncPlayerPreLoginEvent event) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(event.getUniqueId());

        if(player.isOp()) {
            event.allow();
        } else if (Bukkit.getOnlinePlayers().size() >= getServer().getMaxPlayers()) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_FULL, "The server is full");
        } else if (player.isBanned()) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, "You are banned");
        } else if (!player.isWhitelisted() && Bukkit.hasWhitelist()) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, "You are not whitelisted on this server");
        } else {
            String uuid = player.getUniqueId().toString().replace("-", "");
            try {
                String urlString = "https://gatewaymc.gg/api/check?uuid=" + uuid + "&gatewayCode=" + this.code;
                URL url = new URL(urlString);
                URLConnection conn = url.openConnection();
                InputStream is = conn.getInputStream();

                JSONParser jsonParser = new JSONParser();
                JSONObject jsonObject = (JSONObject) jsonParser.parse(new InputStreamReader(is, "UTF-8"));

                boolean success = (boolean) jsonObject.getOrDefault("success", false);

                if(success) {
                    event.allow();
                } else {
                    String error = (String) jsonObject.get("error");
                    ERROR errorType = ERROR.valueOf(error);

                    switch (errorType) {
                        case NO_USER:
                            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "No GatewayMC user found");
                            break;
                        case NO_SERVER:
                            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "No GatewayMC server found");
                            break;
                        case NO_TWITCH:
                            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "This server requires a Twitch account");
                            break;
                        case NO_FOLLOW:
                            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "This server requires you to be a follower");
                            break;
                        case NO_SUB:
                            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "This server requires you to be a subscriber");
                            break;
                        case NO_HOURS:
                            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "This server requires you to have a certain watch time");
                            break;
                        default:
                            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "This server requires an additional connection");
                            break;
                    }

                }

            } catch(Exception e) {
                log.log(Level.SEVERE, e.getMessage());
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "There was a problem with GatewayMC");
            }
        }
    }

    enum ERROR {
        NO_USER,
        NO_SERVER,
        NO_TWITCH,
        NO_FOLLOW,
        NO_SUB,
        NO_HOURS
    }
}

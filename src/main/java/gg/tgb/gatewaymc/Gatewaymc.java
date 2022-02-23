package gg.tgb.gatewaymc;

import de.leonhard.storage.Config;
import gg.tgb.gatewaymc.events.AsyncPlayerPreLogin;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Gatewaymc extends JavaPlugin {

    private Logger log = getLogger();;
    private int pluginId = 14437;
    private int apiVersion = 1;
    private int pluginVersion = 1;

    private boolean badAPI = false;

    Config config = new Config("config", "plugins/gatewaymc");

    @Override
    public void onEnable() {
        // Plugin startup logic

        String gatewayCode = config.getOrSetDefault("gatewaycode", "");
        config.setHeader(" Server code from creator settings");

        Metrics metrics = new Metrics(this, pluginId);
        try {
            String urlString = "https://gatewaymc.gg/api/version";
            URL url = new URL(urlString);
            URLConnection conn = url.openConnection();
            InputStream is = conn.getInputStream();

            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(new InputStreamReader(is, "UTF-8"));

            long api = (long) jsonObject.get("api");
            long plugin = (long) jsonObject.get("plugin");

            if(plugin > pluginVersion) {
                log.log(Level.WARNING, "---------------------");
                log.log(Level.WARNING, "New Version Available");
                log.log(Level.WARNING, "---------------------");
            }

            if(api > apiVersion) {
                log.log(Level.WARNING, "------------------------");
                log.log(Level.WARNING, "Incompatible API version");
                log.log(Level.WARNING, "------------------------");
                badAPI = true;
            }

        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
        }

        AsyncPlayerPreLogin loginEvent = new AsyncPlayerPreLogin(gatewayCode, log);
        getServer().getPluginManager().registerEvents(loginEvent, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}

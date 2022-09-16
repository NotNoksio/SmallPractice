package io.noks.smallpractice.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import org.bukkit.plugin.java.JavaPlugin;

import io.noks.smallpractice.interfaces.WebCallback;
import lombok.experimental.UtilityClass;

@UtilityClass
public class WebUtil {
	
    public static void getResponse(JavaPlugin plugin, String url, WebCallback callback) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
                callback.callback(reader.readLine());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }
}
package io.noks.smallpractice.objects.managers;

import io.noks.smallpractice.Main;

public class ConfigManager {
	public String serverDomainName = "noks.io";
	
	public ConfigManager(Main main) {
		serverDomainName = main.getConfig().getString("server-domain-name");
	}
}

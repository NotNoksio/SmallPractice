package io.noks.smallpractice.objects.managers;

import io.noks.smallpractice.Main;

public class ConfigManager {
	public String serverDomainName = "noks.io";
	public boolean sendJoinAndQuitMessageToOP = false;
	public String tabHeader = serverDomainName;
	public String tabFooter;
	
	public ConfigManager(Main main) {
		serverDomainName = main.getConfig().getString("server-domain-name", serverDomainName);
		sendJoinAndQuitMessageToOP = main.getConfig().getBoolean("send-join-and-quit-message-to-op", sendJoinAndQuitMessageToOP);
		tabHeader = main.getConfig().getString("tab.header", tabHeader);
		tabFooter = main.getConfig().getString("tab.footer", main.getDescription().getWebsite());
	}
}

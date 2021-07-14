package us.noks.smallpractice.objects.managers;

import us.noks.smallpractice.Main;

public class ConfigManager {
	
	public boolean clearChatOnJoin = true;
	public String serverDomainName = "devmc.noks.io";
	
	public ConfigManager(Main main) {
		clearChatOnJoin = main.getConfig().getBoolean("clear-chat-on-join");
		serverDomainName = main.getConfig().getString("server-domain-name");
	}
}

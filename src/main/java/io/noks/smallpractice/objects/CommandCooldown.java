package io.noks.smallpractice.objects;

import java.util.Map;
import java.util.WeakHashMap;

public class CommandCooldown {
	private Map<String, Long> cooldowns = new WeakHashMap<String, Long>();
	
	public void add(String name) {
		this.cooldowns.put(name, System.currentTimeMillis());
	}
	
	public long getTime(String name) {
		return this.cooldowns.get(name);
	}
	
	public boolean isActive(String name) {
		return this.cooldowns.containsKey(name);
	}
}

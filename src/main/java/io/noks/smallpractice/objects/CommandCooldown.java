package io.noks.smallpractice.objects;

import java.util.Map;
import java.util.WeakHashMap;

public class CommandCooldown {
	private Map<String, Long> cooldown = new WeakHashMap<String, Long>();
	
	public void addCooldown(String name, Long time) {
		this.cooldown.put(name, time);
	}
	
	public long getCooldownTime(String name) {
		return this.cooldown.get(name);
	}
	
	public boolean hasCooldown(String name) {
		return this.cooldown.containsKey(name);
	}
}

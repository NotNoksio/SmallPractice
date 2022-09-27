package io.noks.smallpractice.objects;

import java.util.Map;
import java.util.WeakHashMap;

public class Cooldown {
	private Map<String, Long> cooldown = new WeakHashMap<String, Long>();
	
	public void add(String name, Long time) {
		this.cooldown.put(name, time);
	}
	
	public long getTime(String name) {
		return this.cooldown.get(name);
	}
	
	public boolean isActive(String name) {
		return this.cooldown.containsKey(name);
	}
}

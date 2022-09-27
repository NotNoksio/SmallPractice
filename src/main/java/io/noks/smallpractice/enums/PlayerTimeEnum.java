package io.noks.smallpractice.enums;

public enum PlayerTimeEnum {
	SUNRISE("Sunrise", 500L),
	DAY("Day", 6000L),
	SUNSET("Sunset", 12000L),
	NIGHT("Night", 18000L);
	
	private String name;
	private long time;
	
	PlayerTimeEnum(String name, long time) {
		this.name = name;
		this.time = time;
	}
	
	public String getName() {
		return this.name;
	}
	
	public long getTime() {
		return this.time;
	}
	
	public static PlayerTimeEnum getEnumByName(String name) {
		for (PlayerTimeEnum pt : values()) {
			if (pt.getName().toLowerCase().equals(name)) {
				return pt;
			}
		}
		return null;
	}
}

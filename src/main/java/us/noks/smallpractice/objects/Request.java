package us.noks.smallpractice.objects;

import us.noks.smallpractice.arena.Arena.Arenas;
import us.noks.smallpractice.enums.Ladders;

public class Request {
	private Arenas arena;
	private Ladders ladder;
	
	public Request(Arenas arena, Ladders ladder) {
		this.arena = arena;
		this.ladder = ladder;
	}
	
	public Arenas getArena() {
		return this.arena;
	}
	
	public Ladders getLadder() {
		return this.ladder;
	}
}

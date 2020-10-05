package us.noks.smallpractice.objects;

import java.util.UUID;

import us.noks.smallpractice.arena.Arena.Arenas;
import us.noks.smallpractice.enums.Ladders;

public class Request {
	private UUID requestedUUID;
	private Arenas arena;
	private Ladders ladder;
	
	public Request(UUID requestedUUID, Arenas arena, Ladders ladder) {
		this.requestedUUID = requestedUUID;
		this.arena = arena;
		this.ladder = ladder;
	}
	
	public UUID getRequestedUUID() {
		return this.requestedUUID;
	}
	
	public Arenas getArenas() {
		return this.arena;
	}
	
	public Ladders getLadder() {
		return this.ladder;
	}
}

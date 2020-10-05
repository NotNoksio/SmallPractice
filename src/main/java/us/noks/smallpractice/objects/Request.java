package us.noks.smallpractice.objects;

import java.util.UUID;

import us.noks.smallpractice.arena.Arena.Arenas;

public class Request {
	private UUID requestedUUID;
	private Arenas arena;
	
	public Request(UUID requestedUUID, Arenas arena) {
		this.requestedUUID = requestedUUID;
		this.arena = arena;
	}
	
	public UUID getRequestedUUID() {
		return this.requestedUUID;
	}
	
	public Arenas getArenas() {
		return this.arena;
	}
}

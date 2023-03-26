package io.noks.smallpractice.objects;

import java.util.UUID;

import io.noks.smallpractice.arena.Arena;
import io.noks.smallpractice.enums.Ladders;

public class Request {
	private UUID requestedUUID;
	private Ladders ladder;
	private Arena arena;
	
	public Request(Ladders ladder, Arena arena) {
		this.requestedUUID = null;
		this.ladder = ladder;
		this.arena = arena;
	}
	
	public Request(UUID requestedUUID, Ladders ladder, Arena arena) {
		this.requestedUUID = requestedUUID;
		this.ladder = ladder;
		this.arena = arena;
	}
	
	public UUID getRequestedUUID() {
		return this.requestedUUID;
	}
	
	public Ladders getLadder() {
		return this.ladder;
	}
	
	public void setLadder(Ladders ladder) {
		this.ladder = ladder;
	}
	
	public Arena getArena() {
		return this.arena;
	}
	
	public void setArena(Arena arena) {
		this.arena = arena;
	}
}

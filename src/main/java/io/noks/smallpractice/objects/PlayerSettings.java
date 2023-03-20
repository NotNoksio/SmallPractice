package io.noks.smallpractice.objects;

public class PlayerSettings {
	private int queuePingDiff;
	private boolean privateMessage, partyInvite, duelRequest;
	
	public PlayerSettings(int pingDiff, boolean privateMessage, boolean partyInvite, boolean duelRequest) {
		this.queuePingDiff = pingDiff;
		this.privateMessage = privateMessage;
		this.partyInvite = partyInvite;
		this.duelRequest = duelRequest;
	}
	public PlayerSettings() {
		this.queuePingDiff = 300;
		this.privateMessage = true;
		this.partyInvite = true;
		this.duelRequest = true;
	}
	
	public int getQueuePingDiff() {
		return this.queuePingDiff;
	}
	
	public void updatePingDiff() {
		if (this.queuePingDiff == 300) {
			this.queuePingDiff = 50;
			return;
		}
		this.queuePingDiff += 50;
	}
	
	public boolean isPrivateMessageToggled() {
		return this.privateMessage;
	}
	
	public void updatePrivateMessage() {
		this.privateMessage = !this.privateMessage;
	}
	
	public boolean isPartyInviteToggled() {
		return this.partyInvite;
	}
	
	public void updatePartyInvite() {
		this.partyInvite = !this.partyInvite;
	}
	
	public boolean isDuelRequestToggled() {
		return this.duelRequest;
	}
	
	public void updateDuelRequest() {
		this.duelRequest = !this.duelRequest;
	}
}

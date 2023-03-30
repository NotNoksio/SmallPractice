package io.noks.smallpractice.objects;

public class PlayerSettings {
	private int queuePingDiff, secondsBeforeRerequest;
	private boolean privateMessage, partyInvite, duelRequest;
	
	public PlayerSettings(int pingDiff, boolean privateMessage, boolean partyInvite, boolean duelRequest, int seconds) {
		this.queuePingDiff = pingDiff;
		this.privateMessage = privateMessage;
		this.partyInvite = partyInvite;
		this.duelRequest = duelRequest;
		this.secondsBeforeRerequest = seconds;
	}
	public PlayerSettings() {
		this.queuePingDiff = 300;
		this.privateMessage = true;
		this.partyInvite = true;
		this.duelRequest = true;
		this.secondsBeforeRerequest = 5;
	}
	
	public int getQueuePingDiff() {
		return this.queuePingDiff;
	}
	
	public void updatePingDiff() {
		if (this.queuePingDiff >= 300) {
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
	
	public int getSecondsBeforeRerequest() {
		return this.secondsBeforeRerequest;
	}
	
	public void updateSecondsBeforeRerequest() {
		if (this.secondsBeforeRerequest >= 60) {
			this.secondsBeforeRerequest = 5;
			return;
		}
		this.secondsBeforeRerequest += 5;
	}
}

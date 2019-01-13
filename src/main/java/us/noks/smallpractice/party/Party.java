package us.noks.smallpractice.party;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

public class Party {
	
	private List<Player> member;
    private Player partyLeader;
    private PartyState partyState;
    private boolean open;
    
    public Party(Player partyLeader) {
        this.member = new ArrayList<Player>();
        this.partyLeader = partyLeader;
        this.partyState = PartyState.LOBBY;
    }
    
    public void addMember(Player player) {
        this.member.add(player);
    }
    
    public void removeMember(Player player) {
        this.member.remove(player);
    }
    
    public void setOpen(boolean open) {
        this.open = open;
    }

    public void setPartyState(PartyState state) {
        this.partyState = state;
    }
    
    public List<Player> getMembers() {
        return this.member;
    }
    
    public Player getLeader() {
        return this.partyLeader;
    }
    
    public boolean isOpen() {
        return this.open;
    }
    
    public PartyState getPartyState() {
        return this.partyState;
    }
    
    public int getSize() {
        return this.getMembers().size() + 1;
    }

    public List<Player> getAllMembersOnline() {
        List<Player> membersOnline = new ArrayList<Player>();

        for(Player member : this.member) {
            if(member != null) {
                membersOnline.add(member);
            }
        }
        membersOnline.add(partyLeader);
        return membersOnline;
    }
}

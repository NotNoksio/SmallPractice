package us.noks.smallpractice.party;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.minecraft.util.com.google.common.collect.Lists;

public class Party {
	
	private List<UUID> memberUUIDs;
    private UUID partyLeader;
    private String leaderName;
    private PartyState partyState;
    private boolean open;
    
    public Party(UUID partyLeader, String leaderName) {
        this.memberUUIDs = Lists.newArrayList();
        this.partyLeader = partyLeader;
        this.leaderName = leaderName;
        this.partyState = PartyState.LOBBY;
    }
    
    public void addMember(UUID uuid) {
        this.memberUUIDs.add(uuid);
    }
    
    public void removeMember(UUID uuid) {
        this.memberUUIDs.remove(uuid);
    }
    
    public void setOpen(boolean open) {
        this.open = open;
    }

    public void setPartyState(PartyState state) {
        this.partyState = state;
    }
    
    public List<UUID> getMembers() {
        return this.memberUUIDs;
    }
    
    public UUID getLeader() {
        return this.partyLeader;
    }
    
    public String getLeaderName() {
        return this.leaderName;
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

    public List<UUID> getAllMembersOnline() {
        List<UUID> membersOnline = Lists.newArrayList();

        for(UUID memberUUID : this.memberUUIDs) {
            Player member = Bukkit.getPlayer(memberUUID);

            if(member != null) {
                membersOnline.add(member.getUniqueId());
            }
        }
        membersOnline.add(partyLeader);
        return membersOnline;
    }
}

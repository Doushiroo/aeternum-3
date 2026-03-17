package com.aeternum.systems.clans;

import java.util.*;

public class ClanData {

    public enum Rank {
        LEADER(5), HIGH_COMMAND(4), GENERAL(3), OFFICER(2), SOLDIER(1), RECRUIT(0);

        private final int level;
        Rank(int level) { this.level = level; }
        public int getLevel() { return level; }
        public boolean canKick()        { return level >= OFFICER.level; }
        public boolean canInvite()      { return level >= OFFICER.level; }
        public boolean canDeclareWar()  { return level >= HIGH_COMMAND.level; }
        public boolean canProposePeace(){ return level >= HIGH_COMMAND.level; }
        public boolean canPromote()     { return level >= GENERAL.level; }
        public boolean canTransfer()    { return level >= LEADER.level; }
    }

    private final String id; // UUID as string
    private String name;
    private String tag;
    private String description;
    private String leaderUUID;
    private long bankBalance;
    private long createdAt;
    private boolean isPublic;
    private int warPoints;
    private int maxMembers;

    private final Map<String, Rank> members        = new LinkedHashMap<>();
    private final Map<String, String> memberNames  = new HashMap<>();
    private final Set<String> alliedClanIds        = new HashSet<>();
    private final Set<String> atWarWith            = new HashSet<>();
    private final Set<String> pendingWarsWith      = new HashSet<>(); // wars pending 24h notice

    public static final long CLAN_CREATE_COST   = 50_000L;
    public static final int  BASE_MAX_MEMBERS   = 20;

    public ClanData(String id, String name, String tag, String leaderUUID, String leaderName) {
        this.id          = id;
        this.name        = name;
        this.tag         = tag;
        this.leaderUUID  = leaderUUID;
        this.createdAt   = System.currentTimeMillis();
        this.isPublic    = false;
        this.warPoints   = 0;
        this.maxMembers  = BASE_MAX_MEMBERS;
        this.bankBalance = 0;
        this.description = "A new clan in Aeternum.";
        members.put(leaderUUID, Rank.LEADER);
        memberNames.put(leaderUUID, leaderName);
    }

    // ── MEMBERS ───────────────────────────────────────────────────────────────

    public boolean addMember(String uuid, String name) {
        if (members.size() >= maxMembers) return false;
        members.put(uuid, Rank.RECRUIT);
        memberNames.put(uuid, name);
        return true;
    }

    public boolean removeMember(String uuid) {
        if (uuid.equals(leaderUUID)) return false;
        members.remove(uuid);
        memberNames.remove(uuid);
        return true;
    }

    public boolean promoteMember(String promoterUUID, String targetUUID) {
        Rank promoterRank = members.get(promoterUUID);
        Rank targetRank   = members.get(targetUUID);
        if (promoterRank == null || targetRank == null) return false;
        if (!promoterRank.canPromote()) return false;

        Rank[] ranks = Rank.values();
        int idx = targetRank.ordinal();
        if (idx <= 0) return false; // already max
        Rank newRank = ranks[idx - 1]; // higher rank = lower ordinal
        if (newRank.getLevel() >= promoterRank.getLevel()) return false;

        members.put(targetUUID, newRank);
        return true;
    }

    public boolean transferLeadership(String currentLeader, String newLeader) {
        if (!currentLeader.equals(leaderUUID)) return false;
        if (!members.containsKey(newLeader)) return false;
        members.put(currentLeader, Rank.HIGH_COMMAND);
        members.put(newLeader, Rank.LEADER);
        this.leaderUUID = newLeader;
        return true;
    }

    public boolean isMember(String uuid)   { return members.containsKey(uuid); }
    public boolean isLeader(String uuid)   { return uuid.equals(leaderUUID); }
    public Rank getRank(String uuid)       { return members.getOrDefault(uuid, Rank.RECRUIT); }
    public int getMemberCount()            { return members.size(); }
    public Map<String, Rank> getMembers()  { return Collections.unmodifiableMap(members); }
    public Map<String, String> getMemberNames() { return Collections.unmodifiableMap(memberNames); }

    // ── WARS & ALLIANCES ──────────────────────────────────────────────────────

    public boolean isAlliedWith(String clanId)  { return alliedClanIds.contains(clanId); }
    public boolean isAtWarWith(String clanId)   { return atWarWith.contains(clanId); }
    public boolean hasPendingWar(String clanId) { return pendingWarsWith.contains(clanId); }

    public void addAlly(String clanId)          { alliedClanIds.add(clanId); }
    public void removeAlly(String clanId)       { alliedClanIds.remove(clanId); }
    public void declarePendingWar(String clanId){ pendingWarsWith.add(clanId); }
    public void activateWar(String clanId)      { pendingWarsWith.remove(clanId); atWarWith.add(clanId); }
    public void endWar(String clanId)           { atWarWith.remove(clanId); pendingWarsWith.remove(clanId); }

    public Set<String> getAllies()   { return Collections.unmodifiableSet(alliedClanIds); }
    public Set<String> getEnemies() { return Collections.unmodifiableSet(atWarWith); }

    // ── BANK ──────────────────────────────────────────────────────────────────

    public long getBankBalance()       { return bankBalance; }
    public void addToBankBalance(long a){ bankBalance += a; }

    // ── GETTERS / SETTERS ─────────────────────────────────────────────────────

    public String getId()          { return id; }
    public String getName()        { return name; }
    public void setName(String n)  { this.name = n; }
    public String getTag()         { return tag; }
    public String getDescription() { return description; }
    public void setDescription(String d) { this.description = d; }
    public String getLeaderUUID()  { return leaderUUID; }
    public long getCreatedAt()     { return createdAt; }
    public boolean isPublic()      { return isPublic; }
    public void setPublic(boolean p){ this.isPublic = p; }
    public int getWarPoints()      { return warPoints; }
    public void addWarPoints(int p){ this.warPoints += p; }
    public int getMaxMembers()     { return maxMembers; }
    public void setMaxMembers(int m){ this.maxMembers = m; }
}

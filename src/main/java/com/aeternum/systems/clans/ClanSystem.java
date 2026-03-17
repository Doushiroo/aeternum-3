package com.aeternum.systems.clans;

import com.aeternum.data.PlayerData;
import com.aeternum.registry.ModAttachments;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public class ClanSystem {

    // In-memory clan storage (persisted to world SavedData in full implementation)
    private static final Map<String, ClanData> CLANS = new LinkedHashMap<>();

    // ── CREATE ────────────────────────────────────────────────────────────────

    public static boolean createClan(ServerPlayer player, String name, String tag) {
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA.get());

        if (data.isInClan()) {
            player.sendSystemMessage(Component.literal("§cYou are already in a clan. Leave first."));
            return false;
        }
        if (name.length() < 3 || name.length() > 20) {
            player.sendSystemMessage(Component.literal("§cClan name must be 3-20 characters."));
            return false;
        }
        if (tag.length() < 2 || tag.length() > 5) {
            player.sendSystemMessage(Component.literal("§cClan tag must be 2-5 characters."));
            return false;
        }
        for (ClanData existing : CLANS.values()) {
            if (existing.getName().equalsIgnoreCase(name)) {
                player.sendSystemMessage(Component.literal("§cThat clan name is already taken."));
                return false;
            }
            if (existing.getTag().equalsIgnoreCase(tag)) {
                player.sendSystemMessage(Component.literal("§cThat clan tag is already taken."));
                return false;
            }
        }
        if (!data.payFromWallet(ClanData.CLAN_CREATE_COST)) {
            player.sendSystemMessage(Component.literal(
                "§cCreating a clan costs §e" + ClanData.CLAN_CREATE_COST +
                " AU§c. You have §e" + data.getWalletBalance() + " AU§c."));
            return false;
        }

        String clanId = UUID.randomUUID().toString();
        ClanData clan = new ClanData(clanId, name, tag,
            player.getUUID().toString(), player.getName().getString());
        CLANS.put(clanId, clan);

        data.setClanId(clanId);
        data.setClanRank("LEADER");

        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§6§l  ╔══════════════════════════════╗"));
        player.sendSystemMessage(Component.literal("§6§l  ║   CLAN CREATED!              ║"));
        player.sendSystemMessage(Component.literal("§e§l  ║   [" + tag + "] " + name));
        player.sendSystemMessage(Component.literal("§6§l  ╚══════════════════════════════╝"));
        return true;
    }

    // ── INVITE / JOIN ─────────────────────────────────────────────────────────

    public static boolean invitePlayer(ServerPlayer inviter, ServerPlayer target) {
        PlayerData inviterData = inviter.getData(ModAttachments.PLAYER_DATA.get());
        PlayerData targetData  = target.getData(ModAttachments.PLAYER_DATA.get());

        if (!inviterData.isInClan()) {
            inviter.sendSystemMessage(Component.literal("§cYou are not in a clan."));
            return false;
        }
        if (targetData.isInClan()) {
            inviter.sendSystemMessage(Component.literal("§c" + target.getName().getString() + " is already in a clan."));
            return false;
        }

        ClanData clan = CLANS.get(inviterData.getClanId());
        if (clan == null) { inviter.sendSystemMessage(Component.literal("§cClan data error.")); return false; }

        ClanData.Rank rank = ClanData.Rank.valueOf(inviterData.getClanRank());
        if (!rank.canInvite()) {
            inviter.sendSystemMessage(Component.literal("§cYou don't have permission to invite players."));
            return false;
        }
        if (!clan.addMember(target.getUUID().toString(), target.getName().getString())) {
            inviter.sendSystemMessage(Component.literal("§cClan is full! Max members: " + clan.getMaxMembers()));
            return false;
        }

        targetData.setClanId(inviterData.getClanId());
        targetData.setClanRank("RECRUIT");

        inviter.sendSystemMessage(Component.literal("§a" + target.getName().getString() + " joined [" + clan.getTag() + "] " + clan.getName() + "!"));
        target.sendSystemMessage(Component.literal("§aYou joined clan §e[" + clan.getTag() + "] " + clan.getName() + "§a! Welcome!"));
        broadcastToClan(clan, "§e" + target.getName().getString() + " §ajoined the clan!", inviter.getServer());
        return true;
    }

    // ── LEAVE ─────────────────────────────────────────────────────────────────

    public static boolean leaveClan(ServerPlayer player) {
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA.get());
        if (!data.isInClan()) {
            player.sendSystemMessage(Component.literal("§cYou are not in a clan."));
            return false;
        }

        ClanData clan = CLANS.get(data.getClanId());
        String playerUUID = player.getUUID().toString();

        if (data.getClanRank().equals("LEADER")) {
            player.sendSystemMessage(Component.literal("§cLeaders cannot leave. Transfer leadership first with §e/clan leader <player>§c."));
            return false;
        }

        if (clan != null) clan.removeMember(playerUUID);
        data.setClanId("");
        data.setClanRank("RECRUIT");

        player.sendSystemMessage(Component.literal("§aYou have left the clan."));
        if (clan != null) broadcastToClan(clan, "§e" + player.getName().getString() + " §cleft the clan.", player.getServer());
        return true;
    }

    // ── KICK ──────────────────────────────────────────────────────────────────

    public static boolean kickMember(ServerPlayer kicker, ServerPlayer target) {
        PlayerData kickerData = kicker.getData(ModAttachments.PLAYER_DATA.get());
        PlayerData targetData = target.getData(ModAttachments.PLAYER_DATA.get());

        if (!kickerData.isInClan() || !kickerData.getClanId().equals(targetData.getClanId())) {
            kicker.sendSystemMessage(Component.literal("§cYou are not in the same clan."));
            return false;
        }
        ClanData.Rank kickerRank = ClanData.Rank.valueOf(kickerData.getClanRank());
        ClanData.Rank targetRank = ClanData.Rank.valueOf(targetData.getClanRank());

        if (!kickerRank.canKick() || kickerRank.getLevel() <= targetRank.getLevel()) {
            kicker.sendSystemMessage(Component.literal("§cYou don't have permission to kick this player."));
            return false;
        }

        ClanData clan = CLANS.get(kickerData.getClanId());
        if (clan != null) clan.removeMember(target.getUUID().toString());
        targetData.setClanId("");
        targetData.setClanRank("RECRUIT");

        kicker.sendSystemMessage(Component.literal("§aKicked §e" + target.getName().getString() + " §afrom the clan."));
        target.sendSystemMessage(Component.literal("§cYou were kicked from the clan."));
        return true;
    }

    // ── WAR ───────────────────────────────────────────────────────────────────

    public static boolean declareWar(ServerPlayer player, String targetClanName, MinecraftServer server) {
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA.get());
        if (!data.isInClan()) {
            player.sendSystemMessage(Component.literal("§cYou are not in a clan."));
            return false;
        }

        ClanData.Rank rank = ClanData.Rank.valueOf(data.getClanRank());
        if (!rank.canDeclareWar()) {
            player.sendSystemMessage(Component.literal("§cOnly High Command or above can declare war."));
            return false;
        }

        ClanData myClan = CLANS.get(data.getClanId());
        ClanData targetClan = CLANS.values().stream()
            .filter(c -> c.getName().equalsIgnoreCase(targetClanName))
            .findFirst().orElse(null);

        if (targetClan == null) {
            player.sendSystemMessage(Component.literal("§cClan '§e" + targetClanName + "§c' not found."));
            return false;
        }
        if (myClan.isAlliedWith(targetClan.getId())) {
            player.sendSystemMessage(Component.literal("§cYou cannot declare war on an allied clan!"));
            return false;
        }
        if (myClan.isAtWarWith(targetClan.getId())) {
            player.sendSystemMessage(Component.literal("§cYou are already at war with " + targetClanName + "."));
            return false;
        }

        myClan.declarePendingWar(targetClan.getId());

        // Notify target clan
        broadcastToClan(targetClan,
            "§4⚠ WAR DECLARATION ⚠ §c[" + myClan.getTag() + "] " + myClan.getName() +
            " has declared war on your clan! War begins in 24 hours.", server);
        player.sendSystemMessage(Component.literal(
            "§eWar declared on [" + targetClan.getTag() + "] " + targetClan.getName() +
            ". War starts in 24 hours."));
        server.getPlayerList().broadcastSystemMessage(
            Component.literal("§4[WAR] §c[" + myClan.getTag() + "] " + myClan.getName() +
                " has declared war on [" + targetClan.getTag() + "] " + targetClan.getName() + "!"), false);
        return true;
    }

    // ── INFO ──────────────────────────────────────────────────────────────────

    public static void showClanInfo(ServerPlayer player) {
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA.get());
        if (!data.isInClan()) {
            player.sendSystemMessage(Component.literal("§cYou are not in a clan. Create one with §e/clan create <name> <tag>"));
            return;
        }

        ClanData clan = CLANS.get(data.getClanId());
        if (clan == null) { player.sendSystemMessage(Component.literal("§cClan data error.")); return; }

        player.sendSystemMessage(Component.literal("§6=== [" + clan.getTag() + "] " + clan.getName() + " ==="));
        player.sendSystemMessage(Component.literal("§7Description: §f" + clan.getDescription()));
        player.sendSystemMessage(Component.literal("§7Members: §e" + clan.getMemberCount() + "/" + clan.getMaxMembers()));
        player.sendSystemMessage(Component.literal("§7Your Rank: §e" + data.getClanRank()));
        player.sendSystemMessage(Component.literal("§7War Points: §e" + clan.getWarPoints()));
        player.sendSystemMessage(Component.literal("§7Bank: §e" + clan.getBankBalance() + " AU"));

        if (!clan.getEnemies().isEmpty()) {
            player.sendSystemMessage(Component.literal("§c§lAt War With: §r" + formatClanIds(clan.getEnemies())));
        }
        if (!clan.getAllies().isEmpty()) {
            player.sendSystemMessage(Component.literal("§a§lAllies: §r" + formatClanIds(clan.getAllies())));
        }

        player.sendSystemMessage(Component.literal("§6=== MEMBERS ==="));
        clan.getMembers().forEach((uuid, rank) -> {
            String name = clan.getMemberNames().getOrDefault(uuid, "Unknown");
            player.sendSystemMessage(Component.literal("§7  [" + rank.name() + "] §f" + name));
        });
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    public static boolean transferLeadership(ServerPlayer currentLeader, ServerPlayer newLeader) {
        PlayerData leaderData = currentLeader.getData(ModAttachments.PLAYER_DATA.get());
        PlayerData newLeaderData = newLeader.getData(ModAttachments.PLAYER_DATA.get());

        if (!leaderData.isInClan()) {
            currentLeader.sendSystemMessage(Component.literal("§cYou are not in a clan."));
            return false;
        }
        if (!leaderData.getClanId().equals(newLeaderData.getClanId())) {
            currentLeader.sendSystemMessage(Component.literal("§c" + newLeader.getName().getString() + " is not in your clan."));
            return false;
        }
        if (!leaderData.getClanRank().equals("LEADER")) {
            currentLeader.sendSystemMessage(Component.literal("§cOnly the clan leader can transfer leadership."));
            return false;
        }

        ClanData clan = CLANS.get(leaderData.getClanId());
        if (clan == null) return false;

        boolean success = clan.transferLeadership(
            currentLeader.getUUID().toString(),
            newLeader.getUUID().toString());

        if (success) {
            leaderData.setClanRank("HIGH_COMMAND");
            newLeaderData.setClanRank("LEADER");
            currentLeader.sendSystemMessage(Component.literal(
                "§aLeadership transferred to §e" + newLeader.getName().getString() + "§a."));
            newLeader.sendSystemMessage(Component.literal(
                "§aYou are now the leader of clan §e[" + clan.getTag() + "] " + clan.getName() + "§a!"));
            broadcastToClan(clan, "§e" + newLeader.getName().getString() + " §ais the new clan leader!", currentLeader.getServer());
        }
        return success;
    }

    public static ClanData getClanByPlayerId(String playerUUID) {
        for (ClanData clan : CLANS.values()) {
            if (clan.isMember(playerUUID)) return clan;
        }
        return null;
    }

    public static ClanData getClanById(String clanId) {
        return CLANS.get(clanId);
    }

    public static Collection<ClanData> getAllClans() {
        return Collections.unmodifiableCollection(CLANS.values());
    }

    public static boolean areAtWar(String clanId1, String clanId2) {
        ClanData c1 = CLANS.get(clanId1);
        return c1 != null && c1.isAtWarWith(clanId2);
    }

    private static void broadcastToClan(ClanData clan, String message, MinecraftServer server) {
        if (server == null) return;
        clan.getMembers().keySet().forEach(uuid -> {
            try {
                ServerPlayer p = server.getPlayerList().getPlayer(UUID.fromString(uuid));
                if (p != null) p.sendSystemMessage(Component.literal(message));
            } catch (Exception ignored) {}
        });
    }

    private static String formatClanIds(Set<String> clanIds) {
        StringBuilder sb = new StringBuilder();
        for (String id : clanIds) {
            ClanData c = CLANS.get(id);
            if (c != null) sb.append("[").append(c.getTag()).append("] ").append(c.getName()).append(", ");
        }
        return sb.length() > 2 ? sb.substring(0, sb.length() - 2) : "None";
    }
}

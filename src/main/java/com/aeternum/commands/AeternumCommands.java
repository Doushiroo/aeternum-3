package com.aeternum.commands;

import com.aeternum.data.PlayerData;
import com.aeternum.registry.ModAttachments;
import com.aeternum.systems.classes.ClassSystem;
import com.aeternum.systems.classes.PlayerClass;
import com.aeternum.systems.clans.ClanData;
import com.aeternum.systems.clans.ClanSystem;
import com.aeternum.systems.economy.EconomySystem;
import com.aeternum.systems.skills.SkillSystem;
import com.aeternum.systems.titles.TitleSystem;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class AeternumCommands {

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();

        registerAeternum(d);
        registerClass(d);
        registerSkills(d);
        registerBank(d);
        registerKarma(d);
        registerClan(d);
        registerTitle(d);
        registerStats(d);
    }

    // ── /aeternum ─────────────────────────────────────────────────────────────

    private void registerAeternum(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("aeternum")
            .executes(ctx -> {
                ServerPlayer p = getPlayer(ctx);
                if (p == null) return 0;
                PlayerData data = p.getData(ModAttachments.PLAYER_DATA.get());
                p.sendSystemMessage(Component.literal(""));
                p.sendSystemMessage(Component.literal("§6§l══════ AETERNUM ══════"));
                p.sendSystemMessage(Component.literal("§eLevel: §f" + data.getLevel() + " §7(XP: " + data.getExperience() + "/" + data.getXpForNextLevel() + ")"));
                p.sendSystemMessage(Component.literal("§eClass: §f" + data.getPlayerClass() + " Lv." + data.getClassLevel()));
                p.sendSystemMessage(Component.literal("§eKarma: §f" + data.getKarma() + " §7[" + data.getKarmaLevel().name() + "]"));
                p.sendSystemMessage(Component.literal("§eTemp:  §f" + String.format("%.1f", data.getBodyTemperature()) + "°C §7[" + data.getTemperatureStatus().name() + "]"));
                p.sendSystemMessage(Component.literal("§eHP:    §f" + (int)data.getCurrentHealth() + "/" + (int)data.getMaxHealth()));
                p.sendSystemMessage(Component.literal("§eEN:    §f" + (int)data.getCurrentEnergy() + "/" + (int)data.getMaxEnergy()));
                p.sendSystemMessage(Component.literal("§eGold:  §f" + data.getWalletBalance() + " AU (wallet) | " + data.getBankBalance() + " AU (bank)"));
                if (data.isInClan()) {
                    ClanData clan = ClanSystem.getClanById(data.getClanId());
                    String clanDisplay = clan != null
                        ? "[" + clan.getTag() + "] " + clan.getName()
                        : "Unknown Clan";
                    p.sendSystemMessage(Component.literal("§eClan:  §f" + clanDisplay + " §7[" + data.getClanRank() + "]"));
                }
                p.sendSystemMessage(Component.literal(""));
                p.sendSystemMessage(Component.literal("§7Commands: /class /skills /bank /karma /clan /title /stats"));
                p.sendSystemMessage(Component.literal("§6§l══════════════════════"));
                return 1;
            })
        );
    }

    // ── /class ────────────────────────────────────────────────────────────────

    private void registerClass(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("class")
            .then(Commands.literal("info")
                .executes(ctx -> {
                    ServerPlayer p = getPlayer(ctx);
                    if (p == null) return 0;
                    ClassSystem.showClassInfo(p);
                    return 1;
                })
            )
            .then(Commands.literal("list")
                .executes(ctx -> {
                    ServerPlayer p = getPlayer(ctx);
                    if (p == null) return 0;
                    PlayerData data = p.getData(ModAttachments.PLAYER_DATA.get());
                    p.sendSystemMessage(Component.literal("§6=== AVAILABLE CLASSES ==="));
                    for (PlayerClass cls : ClassSystem.getAvailableClasses(data)) {
                        String karmaInfo = cls.getKarmaRequirement() != 0 ?
                            " §8(Karma: " + cls.getKarmaRequirement() + ")" : "";
                        p.sendSystemMessage(Component.literal(
                            "§e" + cls.getDisplayName() + " §8[" + cls.getType().name() + "]" + karmaInfo));
                        p.sendSystemMessage(Component.literal("  §7" + cls.getDescription()));
                    }
                    return 1;
                })
            )
            .then(Commands.literal("choose")
                .then(Commands.argument("classname", StringArgumentType.string())
                    .executes(ctx -> {
                        ServerPlayer p = getPlayer(ctx);
                        if (p == null) return 0;
                        String className = StringArgumentType.getString(ctx, "classname").toUpperCase();
                        try {
                            PlayerClass cls = PlayerClass.valueOf(className);
                            ClassSystem.chooseClass(p, cls);
                        } catch (IllegalArgumentException e) {
                            p.sendSystemMessage(Component.literal(
                                "§cUnknown class: §e" + className + "§c. Use §e/class list §cto see options."));
                        }
                        return 1;
                    })
                )
            )
            .then(Commands.literal("attribute")
                .then(Commands.argument("attr", StringArgumentType.word())
                    .then(Commands.argument("points", LongArgumentType.longArg(1, 10))
                        .executes(ctx -> {
                            ServerPlayer p = getPlayer(ctx);
                            if (p == null) return 0;
                            PlayerData data = p.getData(ModAttachments.PLAYER_DATA.get());
                            String attr = StringArgumentType.getString(ctx, "attr").toUpperCase();
                            int pts = (int) LongArgumentType.getLong(ctx, "points");
                            if (data.allocateAttribute(attr, pts)) {
                                p.sendSystemMessage(Component.literal("§aAllocated §e" + pts + " points §ato §e" + attr + "§a."));
                            } else {
                                p.sendSystemMessage(Component.literal("§cNot enough skill points or invalid attribute. Valid: STR AGI INT VIT WIS LUK"));
                            }
                            return 1;
                        })
                    )
                )
            )
        );
    }

    // ── /skills ───────────────────────────────────────────────────────────────

    private void registerSkills(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("skills")
            .executes(ctx -> {
                ServerPlayer p = getPlayer(ctx);
                if (p == null) return 0;
                SkillSystem.showSkillList(p);
                return 1;
            })
            .then(Commands.literal("learn")
                .then(Commands.argument("skillid", StringArgumentType.word())
                    .executes(ctx -> {
                        ServerPlayer p = getPlayer(ctx);
                        if (p == null) return 0;
                        String id = StringArgumentType.getString(ctx, "skillid");
                        SkillSystem.learnSkill(p, id);
                        return 1;
                    })
                )
            )
            .then(Commands.literal("use")
                .then(Commands.argument("skillid", StringArgumentType.word())
                    .executes(ctx -> {
                        ServerPlayer p = getPlayer(ctx);
                        if (p == null) return 0;
                        String id = StringArgumentType.getString(ctx, "skillid");
                        SkillSystem.useSkill(p, id);
                        return 1;
                    })
                )
            )
        );
    }

    // ── /bank ─────────────────────────────────────────────────────────────────

    private void registerBank(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("bank")
            .executes(ctx -> {
                ServerPlayer p = getPlayer(ctx);
                if (p == null) return 0;
                EconomySystem.showBalance(p);
                return 1;
            })
            .then(Commands.literal("deposit")
                .then(Commands.argument("amount", LongArgumentType.longArg(1))
                    .executes(ctx -> {
                        ServerPlayer p = getPlayer(ctx);
                        if (p == null) return 0;
                        long amount = LongArgumentType.getLong(ctx, "amount");
                        EconomySystem.deposit(p, amount);
                        return 1;
                    })
                )
            )
            .then(Commands.literal("withdraw")
                .then(Commands.argument("amount", LongArgumentType.longArg(1))
                    .executes(ctx -> {
                        ServerPlayer p = getPlayer(ctx);
                        if (p == null) return 0;
                        long amount = LongArgumentType.getLong(ctx, "amount");
                        EconomySystem.withdraw(p, amount);
                        return 1;
                    })
                )
            )
            .then(Commands.literal("transfer")
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("amount", LongArgumentType.longArg(1))
                        .executes(ctx -> {
                            ServerPlayer from = getPlayer(ctx);
                            if (from == null) return 0;
                            ServerPlayer to = EntityArgument.getPlayer(ctx, "player");
                            long amount = LongArgumentType.getLong(ctx, "amount");
                            EconomySystem.transfer(from, to, amount);
                            return 1;
                        })
                    )
                )
            )
            .then(Commands.literal("market")
                .executes(ctx -> {
                    ServerPlayer p = getPlayer(ctx);
                    if (p == null) return 0;
                    EconomySystem.showMarket(p);
                    return 1;
                })
            )
        );
    }

    // ── /karma ────────────────────────────────────────────────────────────────

    private void registerKarma(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("karma")
            .executes(ctx -> {
                ServerPlayer p = getPlayer(ctx);
                if (p == null) return 0;
                PlayerData data = p.getData(ModAttachments.PLAYER_DATA.get());
                PlayerData.KarmaLevel lvl = data.getKarmaLevel();
                String color = switch (lvl) {
                    case DIVINE, HOLY, VIRTUOUS, GOOD -> "§a";
                    case NEUTRAL -> "§7";
                    case SHADY, WICKED -> "§c";
                    case CORRUPT, ABYSSAL -> "§4";
                };
                p.sendSystemMessage(Component.literal("§6=== YOUR KARMA ==="));
                p.sendSystemMessage(Component.literal("§7Value: " + color + data.getKarma() + " §8/ ±10000"));
                p.sendSystemMessage(Component.literal("§7Level: " + color + lvl.name()));
                p.sendSystemMessage(Component.literal(""));
                p.sendSystemMessage(Component.literal("§7Effects of your karma level:"));
                switch (lvl) {
                    case DIVINE -> {
                        p.sendSystemMessage(Component.literal("§a  • Angels protect you passively"));
                        p.sendSystemMessage(Component.literal("§a  • Regeneration + Resistance aura"));
                        p.sendSystemMessage(Component.literal("§a  • Villagers give you major discounts"));
                        p.sendSystemMessage(Component.literal("§a  • Divine events may occur"));
                    }
                    case HOLY -> {
                        p.sendSystemMessage(Component.literal("§a  • Angels are friendly"));
                        p.sendSystemMessage(Component.literal("§a  • Passive Regeneration aura"));
                        p.sendSystemMessage(Component.literal("§a  • Villagers trust you"));
                    }
                    case ABYSSAL -> {
                        p.sendSystemMessage(Component.literal("§4  • Demons treat you as an ally"));
                        p.sendSystemMessage(Component.literal("§4  • Angels attack you on sight"));
                        p.sendSystemMessage(Component.literal("§4  • Villagers flee from you"));
                        p.sendSystemMessage(Component.literal("§4  • Darkness bonus: Strength + Night Vision"));
                        p.sendSystemMessage(Component.literal("§4  • Demonic events may empower you"));
                    }
                    case WICKED, CORRUPT -> {
                        p.sendSystemMessage(Component.literal("§c  • Villagers refuse to trade with you"));
                        p.sendSystemMessage(Component.literal("§c  • Pillagers consider you neutral"));
                        p.sendSystemMessage(Component.literal("§c  • Darkness aura visible"));
                    }
                    default -> p.sendSystemMessage(Component.literal("§7  • Standard world interactions"));
                }
                return 1;
            })
        );
    }

    // ── /clan ─────────────────────────────────────────────────────────────────

    private void registerClan(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("clan")
            .executes(ctx -> {
                ServerPlayer p = getPlayer(ctx);
                if (p == null) return 0;
                ClanSystem.showClanInfo(p);
                return 1;
            })
            .then(Commands.literal("create")
                .then(Commands.argument("name", StringArgumentType.string())
                    .then(Commands.argument("tag", StringArgumentType.word())
                        .executes(ctx -> {
                            ServerPlayer p = getPlayer(ctx);
                            if (p == null) return 0;
                            String name = StringArgumentType.getString(ctx, "name");
                            String tag  = StringArgumentType.getString(ctx, "tag");
                            ClanSystem.createClan(p, name, tag);
                            return 1;
                        })
                    )
                )
            )
            .then(Commands.literal("invite")
                .then(Commands.argument("player", EntityArgument.player())
                    .executes(ctx -> {
                        ServerPlayer p = getPlayer(ctx);
                        if (p == null) return 0;
                        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                        ClanSystem.invitePlayer(p, target);
                        return 1;
                    })
                )
            )
            .then(Commands.literal("kick")
                .then(Commands.argument("player", EntityArgument.player())
                    .executes(ctx -> {
                        ServerPlayer p = getPlayer(ctx);
                        if (p == null) return 0;
                        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                        ClanSystem.kickMember(p, target);
                        return 1;
                    })
                )
            )
            .then(Commands.literal("leave")
                .executes(ctx -> {
                    ServerPlayer p = getPlayer(ctx);
                    if (p == null) return 0;
                    ClanSystem.leaveClan(p);
                    return 1;
                })
            )
            .then(Commands.literal("war")
                .then(Commands.argument("clanname", StringArgumentType.string())
                    .executes(ctx -> {
                        ServerPlayer p = getPlayer(ctx);
                        if (p == null) return 0;
                        String clanName = StringArgumentType.getString(ctx, "clanname");
                        ClanSystem.declareWar(p, clanName, ctx.getSource().getServer());
                        return 1;
                    })
                )
            )
            .then(Commands.literal("leader")
                .then(Commands.argument("player", EntityArgument.player())
                    .executes(ctx -> {
                        ServerPlayer p = getPlayer(ctx);
                        if (p == null) return 0;
                        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                        ClanSystem.transferLeadership(p, target);
                        return 1;
                    })
                )
            )
        );
    }

    // ── /title ────────────────────────────────────────────────────────────────

    private void registerTitle(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("title")
            .executes(ctx -> {
                ServerPlayer p = getPlayer(ctx);
                if (p == null) return 0;
                PlayerData data = p.getData(ModAttachments.PLAYER_DATA.get());
                p.sendSystemMessage(Component.literal("§6=== YOUR TITLES ==="));
                p.sendSystemMessage(Component.literal("§7Active: §e" +
                    (data.getActiveTitle().isEmpty() ? "None" :
                        TitleSystem.ALL_TITLES.getOrDefault(data.getActiveTitle(),
                            new TitleSystem.AeternumTitle("", "Unknown",""))
                            .displayName())));
                p.sendSystemMessage(Component.literal(""));
                if (data.getUnlockedTitles().isEmpty()) {
                    p.sendSystemMessage(Component.literal("§7No titles unlocked yet. Keep playing!"));
                } else {
                    p.sendSystemMessage(Component.literal("§7Unlocked titles:"));
                    for (String tid : data.getUnlockedTitles()) {
                        TitleSystem.AeternumTitle t = TitleSystem.ALL_TITLES.get(tid);
                        if (t != null) {
                            String marker = tid.equals(data.getActiveTitle()) ? "§a►" : "§7 ";
                            p.sendSystemMessage(Component.literal(marker + " " + t.displayName() + " §8(/" + tid + ")"));
                        }
                    }
                }
                return 1;
            })
            .then(Commands.literal("set")
                .then(Commands.argument("titleid", StringArgumentType.word())
                    .executes(ctx -> {
                        ServerPlayer p = getPlayer(ctx);
                        if (p == null) return 0;
                        String id = StringArgumentType.getString(ctx, "titleid");
                        PlayerData data = p.getData(ModAttachments.PLAYER_DATA.get());
                        if (!data.getUnlockedTitles().contains(id)) {
                            p.sendSystemMessage(Component.literal("§cYou haven't earned that title."));
                        } else {
                            data.setActiveTitle(id);
                            TitleSystem.AeternumTitle t = TitleSystem.ALL_TITLES.get(id);
                            if (t != null) p.sendSystemMessage(Component.literal("§aTitle set to: " + t.displayName()));
                        }
                        return 1;
                    })
                )
            )
        );
    }

    // ── /stats ────────────────────────────────────────────────────────────────

    private void registerStats(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("stats")
            .executes(ctx -> {
                ServerPlayer p = getPlayer(ctx);
                if (p == null) return 0;
                PlayerData data = p.getData(ModAttachments.PLAYER_DATA.get());
                p.sendSystemMessage(Component.literal("§6=== COMBAT STATS ==="));
                p.sendSystemMessage(Component.literal("§7Physical ATK: §e" + String.format("%.1f", data.getPhysicalAttack())));
                p.sendSystemMessage(Component.literal("§7Magic ATK:    §e" + String.format("%.1f", data.getMagicAttack())));
                p.sendSystemMessage(Component.literal("§7Physical DEF: §e" + String.format("%.1f", data.getPhysicalDefense())));
                p.sendSystemMessage(Component.literal("§7Magic DEF:    §e" + String.format("%.1f", data.getMagicDefense())));
                p.sendSystemMessage(Component.literal("§7Crit Chance:  §e" + String.format("%.1f", data.getCritChance() * 100) + "%"));
                p.sendSystemMessage(Component.literal("§7Crit Multi:   §e" + String.format("%.1f", data.getCritMultiplier()) + "x"));
                p.sendSystemMessage(Component.literal("§7Dodge:        §e" + String.format("%.1f", data.getDodgeChance() * 100) + "%"));
                p.sendSystemMessage(Component.literal("§6=== ATTRIBUTES ==="));
                p.sendSystemMessage(Component.literal(
                    "§7STR:§e" + data.getSTR() + " §7AGI:§e" + data.getAGI() +
                    " §7INT:§e" + data.getINT() + " §7VIT:§e" + data.getVIT() +
                    " §7WIS:§e" + data.getWIS() + " §7LUK:§e" + data.getLUK()));
                p.sendSystemMessage(Component.literal("§7Unspent SP: §e" + data.getSkillPoints()));
                p.sendSystemMessage(Component.literal("§6=== HISTORY ==="));
                p.sendSystemMessage(Component.literal("§7PvP Kills:    §e" + data.getTotalPlayerKills()));
                p.sendSystemMessage(Component.literal("§7Boss Kills:   §e" + data.getBossesKilled()));
                p.sendSystemMessage(Component.literal("§7Deaths:       §e" + data.getTotalDeaths()));
                p.sendSystemMessage(Component.literal("§7Distance:     §e" + data.getDistanceTraveled() + " blocks"));
                p.sendSystemMessage(Component.literal("§7Total Earned: §e" + data.getTotalEarned() + " AU"));
                p.sendSystemMessage(Component.literal("§7Rebirths:     §e" + data.getRebirthCount()));
                p.sendSystemMessage(Component.literal("§7Noble:        §e" + (data.isNoble() ? "Yes" : "No")));
                p.sendSystemMessage(Component.literal("§7Olympiad OP:  §e" + data.getOlympiadPoints()));
                return 1;
            })
        );
    }

    // ── HELPER ────────────────────────────────────────────────────────────────

    private ServerPlayer getPlayer(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        try {
            return ctx.getSource().getPlayerOrException();
        } catch (Exception e) {
            return null;
        }
    }
}

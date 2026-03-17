package com.aeternum.systems.economy;

import com.aeternum.data.PlayerData;
import com.aeternum.registry.ModAttachments;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public class EconomySystem {

    public static final double TAX_RATE        = 0.05;  // 5% tax on transfers
    public static final double BANK_FEE        = 0.005; // 0.5% bank withdrawal fee
    public static final double MARKET_LIST_FEE = 0.02;  // 2% market listing fee

    // In-memory market listings (in a full implementation, use SavedData)
    private static final Map<UUID, List<MarketListing>> MARKET = new LinkedHashMap<>();

    // ── BANK ──────────────────────────────────────────────────────────────────

    public static boolean deposit(ServerPlayer player, long amount) {
        if (amount <= 0) {
            player.sendSystemMessage(Component.literal("§cAmount must be positive."));
            return false;
        }
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA.get());
        if (!data.payFromWallet(amount)) {
            player.sendSystemMessage(Component.literal(
                "§cNot enough Aurum in wallet. You have §e" + data.getWalletBalance() + " AU§c."));
            return false;
        }
        data.setBankBalance(data.getBankBalance() + amount);
        player.sendSystemMessage(Component.literal(
            "§aDeposited §e" + amount + " AU §ato your bank account."));
        player.sendSystemMessage(Component.literal(
            "§7Bank: §e" + data.getBankBalance() + " AU §7| Wallet: §e" + data.getWalletBalance() + " AU"));
        return true;
    }

    public static boolean withdraw(ServerPlayer player, long amount) {
        if (amount <= 0) {
            player.sendSystemMessage(Component.literal("§cAmount must be positive."));
            return false;
        }
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA.get());
        long fee = Math.max(1, (long)(amount * BANK_FEE));
        long totalCost = amount + fee;

        if (data.getBankBalance() < totalCost) {
            player.sendSystemMessage(Component.literal(
                "§cInsufficient bank balance. Need §e" + totalCost + " AU §c(includes §e" + fee + " AU §cfee)."));
            return false;
        }
        data.setBankBalance(data.getBankBalance() - totalCost);
        data.receiveToWallet(amount);
        player.sendSystemMessage(Component.literal(
            "§aWithdrew §e" + amount + " AU§a. Fee: §e" + fee + " AU"));
        player.sendSystemMessage(Component.literal(
            "§7Bank: §e" + data.getBankBalance() + " AU §7| Wallet: §e" + data.getWalletBalance() + " AU"));
        return true;
    }

    public static void showBalance(ServerPlayer player) {
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA.get());
        player.sendSystemMessage(Component.literal("§6=== YOUR AURUM BALANCE ==="));
        player.sendSystemMessage(Component.literal("§7Wallet:    §e" + data.getWalletBalance() + " AU"));
        player.sendSystemMessage(Component.literal("§7Bank:      §e" + data.getBankBalance() + " AU"));
        player.sendSystemMessage(Component.literal("§7Total:     §e" + (data.getWalletBalance() + data.getBankBalance()) + " AU"));
        player.sendSystemMessage(Component.literal("§8Taxes paid: §7" + data.getTotalTaxesPaid() + " AU"));
    }

    // ── PLAYER-TO-PLAYER TRANSFER ─────────────────────────────────────────────

    public static boolean transfer(ServerPlayer from, ServerPlayer to, long amount) {
        if (amount <= 0) {
            from.sendSystemMessage(Component.literal("§cAmount must be positive."));
            return false;
        }
        if (from.getUUID().equals(to.getUUID())) {
            from.sendSystemMessage(Component.literal("§cYou cannot transfer to yourself."));
            return false;
        }

        PlayerData fromData = from.getData(ModAttachments.PLAYER_DATA.get());
        PlayerData toData   = to.getData(ModAttachments.PLAYER_DATA.get());

        long tax      = (long)(amount * TAX_RATE);
        long totalCost = amount + tax;

        if (fromData.getWalletBalance() < totalCost) {
            from.sendSystemMessage(Component.literal(
                "§cNot enough Aurum. Need §e" + totalCost + " AU §c(includes §e" + tax + " AU §ctax)."));
            return false;
        }

        fromData.setWalletBalance(fromData.getWalletBalance() - totalCost);
        fromData.addTaxPaid(tax);
        toData.receiveToWallet(amount);

        from.sendSystemMessage(Component.literal(
            "§aSent §e" + amount + " AU §ato §b" + to.getName().getString() +
            "§a. Tax: §e" + tax + " AU"));
        to.sendSystemMessage(Component.literal(
            "§aReceived §e" + amount + " AU §afrom §b" + from.getName().getString() + "§a."));
        return true;
    }

    // ── BANK-TO-BANK TRANSFER ─────────────────────────────────────────────────

    public static boolean bankTransfer(ServerPlayer from, ServerPlayer to, long amount) {
        if (amount <= 0) {
            from.sendSystemMessage(Component.literal("§cAmount must be positive."));
            return false;
        }

        PlayerData fromData = from.getData(ModAttachments.PLAYER_DATA.get());
        PlayerData toData   = to.getData(ModAttachments.PLAYER_DATA.get());

        long tax  = (long)(amount * TAX_RATE);
        long fee  = Math.max(1, (long)(amount * BANK_FEE));
        long total = amount + tax + fee;

        if (fromData.getBankBalance() < total) {
            from.sendSystemMessage(Component.literal(
                "§cInsufficient bank balance. Need §e" + total + " AU§c."));
            return false;
        }

        fromData.setBankBalance(fromData.getBankBalance() - total);
        fromData.addTaxPaid(tax);
        toData.setBankBalance(toData.getBankBalance() + amount);

        from.sendSystemMessage(Component.literal(
            "§aBank transfer of §e" + amount + " AU §ato §b" + to.getName().getString() +
            "§a. Tax+Fee: §e" + (tax + fee) + " AU"));
        to.sendSystemMessage(Component.literal(
            "§aReceived §e" + amount + " AU §ain your bank from §b" + from.getName().getString() + "§a."));
        return true;
    }

    // ── REWARD ────────────────────────────────────────────────────────────────

    public static void reward(ServerPlayer player, long amount, String reason) {
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA.get());
        data.receiveToWallet(amount);
        player.sendSystemMessage(Component.literal(
            "§a+§e" + amount + " AU §7(" + reason + ")"));
    }

    // ── NPC PRICE WITH KARMA MODIFIER ─────────────────────────────────────────

    public static long getKarmaPrice(long basePrice, PlayerData data) {
        double modifier = switch (data.getKarmaLevel()) {
            case DIVINE    -> 0.75;  // 25% discount
            case HOLY      -> 0.82;
            case VIRTUOUS  -> 0.88;
            case GOOD      -> 0.93;
            case NEUTRAL   -> 1.00;
            case SHADY     -> 1.10;
            case WICKED    -> 1.20;
            case CORRUPT   -> 1.30;
            case ABYSSAL   -> 1.50; // 50% markup
        };
        return (long)(basePrice * modifier);
    }

    // ── MARKET ────────────────────────────────────────────────────────────────

    public static boolean listOnMarket(ServerPlayer seller, String itemName, long price, int quantity) {
        PlayerData data = seller.getData(ModAttachments.PLAYER_DATA.get());
        long fee = Math.max(10L, (long)(price * quantity * MARKET_LIST_FEE));

        if (!data.payFromWallet(fee)) {
            seller.sendSystemMessage(Component.literal(
                "§cListing fee: §e" + fee + " AU§c. Not enough in wallet."));
            return false;
        }

        MarketListing listing = new MarketListing(
            seller.getUUID(), seller.getName().getString(), itemName, price, quantity);
        MARKET.computeIfAbsent(seller.getUUID(), k -> new ArrayList<>()).add(listing);

        data.incrementTrades();
        seller.sendSystemMessage(Component.literal(
            "§aListed §e" + quantity + "x " + itemName +
            " §afor §e" + price + " AU §aeach. Fee: §e" + fee + " AU"));
        return true;
    }

    public static void showMarket(ServerPlayer player) {
        player.sendSystemMessage(Component.literal("§6=== MARKET LISTINGS ==="));
        int count = 0;
        for (List<MarketListing> listings : MARKET.values()) {
            for (MarketListing listing : listings) {
                player.sendSystemMessage(Component.literal(
                    "§e" + listing.getItemName() + " §7x" + listing.getQuantity() +
                    " - §e" + listing.getPrice() + " AU §8(by " + listing.getSellerName() + ")"));
                count++;
                if (count >= 20) {
                    player.sendSystemMessage(Component.literal("§7... and more."));
                    return;
                }
            }
        }
        if (count == 0) {
            player.sendSystemMessage(Component.literal("§7The market is empty. Be the first to list!"));
        }
    }

    // ── INNER CLASSES ─────────────────────────────────────────────────────────

    public static class MarketListing {
        private final UUID id = UUID.randomUUID();
        private final UUID sellerUUID;
        private final String sellerName;
        private final String itemName;
        private final long price;
        private final int quantity;

        public MarketListing(UUID sellerUUID, String sellerName, String itemName, long price, int quantity) {
            this.sellerUUID = sellerUUID;
            this.sellerName = sellerName;
            this.itemName   = itemName;
            this.price      = price;
            this.quantity   = quantity;
        }

        public UUID getId()          { return id; }
        public UUID getSellerUUID()  { return sellerUUID; }
        public String getSellerName(){ return sellerName; }
        public String getItemName()  { return itemName; }
        public long getPrice()       { return price; }
        public int getQuantity()     { return quantity; }
    }
}

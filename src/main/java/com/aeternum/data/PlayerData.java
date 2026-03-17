package com.aeternum.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.*;

public class PlayerData implements INBTSerializable<CompoundTag> {

    private int level = 1;
    private long experience = 0;
    private int skillPoints = 0;
    private int professionPoints = 0;

    private double maxHealth = 100.0;
    private double currentHealth = 100.0;
    private double maxEnergy = 100.0;
    private double currentEnergy = 100.0;
    private double maxStamina = 100.0;
    private double currentStamina = 100.0;

    private double physicalAttack = 10.0;
    private double magicAttack = 10.0;
    private double physicalDefense = 5.0;
    private double magicDefense = 5.0;
    private double critChance = 0.05;
    private double critMultiplier = 1.5;
    private double dodgeChance = 0.03;

    private String playerClass = "WANDERER";
    private int classLevel = 1;
    private long classExperience = 0;
    private List<String> unlockedSkills = new ArrayList<>();
    private Map<String, Long> skillCooldowns = new HashMap<>();
    private int strAttribute = 0;
    private int agiAttribute = 0;
    private int intAttribute = 0;
    private int vitAttribute = 0;
    private int wisAttribute = 0;
    private int lukAttribute = 0;

    private int karma = 0;
    private int karmaDecayTimer = 0;

    private long bankBalance = 0;
    private long walletBalance = 500;
    private long totalTaxesPaid = 0;
    private long totalEarned = 0;
    private int totalTradesCompleted = 0;

    private String clanId = "";
    private String clanRank = "RECRUIT";

    private String activeTitle = "";
    private List<String> unlockedTitles = new ArrayList<>();

    private float bodyTemperature = 37.0f;
    private float temperatureResistance = 0.0f;

    private int totalPlayerKills = 0;
    private int bossesKilled = 0;
    private int totalDeaths = 0;
    private long distanceTraveled = 0;

    private String primaryProfession = "NONE";
    private Map<String, Integer> professionLevels = new HashMap<>();
    private Map<String, Long> professionXp = new HashMap<>();

    private boolean firstLogin = true;
    private int rebirthCount = 0;
    private int olympiadPoints = 0;
    private boolean isNoble = false;
    private List<String> tamedEntityIds = new ArrayList<>();
    private Set<String> discoveredBiomes = new HashSet<>();

    public PlayerData() {}

    // ── INBTSerializable ──────────────────────────────────────────────────────

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("level", level);
        tag.putLong("experience", experience);
        tag.putInt("skillPoints", skillPoints);
        tag.putInt("professionPoints", professionPoints);
        tag.putDouble("maxHealth", maxHealth);
        tag.putDouble("currentHealth", currentHealth);
        tag.putDouble("maxEnergy", maxEnergy);
        tag.putDouble("currentEnergy", currentEnergy);
        tag.putDouble("maxStamina", maxStamina);
        tag.putDouble("currentStamina", currentStamina);
        tag.putDouble("physicalAttack", physicalAttack);
        tag.putDouble("magicAttack", magicAttack);
        tag.putDouble("physicalDefense", physicalDefense);
        tag.putDouble("magicDefense", magicDefense);
        tag.putDouble("critChance", critChance);
        tag.putDouble("critMultiplier", critMultiplier);
        tag.putDouble("dodgeChance", dodgeChance);
        tag.putString("playerClass", playerClass);
        tag.putInt("classLevel", classLevel);
        tag.putLong("classExperience", classExperience);
        tag.putInt("str", strAttribute);
        tag.putInt("agi", agiAttribute);
        tag.putInt("int_", intAttribute);
        tag.putInt("vit", vitAttribute);
        tag.putInt("wis", wisAttribute);
        tag.putInt("luk", lukAttribute);
        ListTag skillsList = new ListTag();
        for (String s : unlockedSkills) skillsList.add(StringTag.valueOf(s));
        tag.put("unlockedSkills", skillsList);
        CompoundTag cds = new CompoundTag();
        skillCooldowns.forEach((k, v) -> cds.putLong(k, v));
        tag.put("skillCooldowns", cds);
        tag.putInt("karma", karma);
        tag.putInt("karmaDecayTimer", karmaDecayTimer);
        tag.putLong("bankBalance", bankBalance);
        tag.putLong("walletBalance", walletBalance);
        tag.putLong("totalTaxesPaid", totalTaxesPaid);
        tag.putLong("totalEarned", totalEarned);
        tag.putInt("totalTrades", totalTradesCompleted);
        tag.putString("clanId", clanId);
        tag.putString("clanRank", clanRank);
        tag.putString("activeTitle", activeTitle);
        ListTag titleList = new ListTag();
        for (String t : unlockedTitles) titleList.add(StringTag.valueOf(t));
        tag.put("unlockedTitles", titleList);
        tag.putFloat("bodyTemperature", bodyTemperature);
        tag.putFloat("temperatureResistance", temperatureResistance);
        tag.putInt("totalPlayerKills", totalPlayerKills);
        tag.putInt("bossesKilled", bossesKilled);
        tag.putInt("totalDeaths", totalDeaths);
        tag.putLong("distanceTraveled", distanceTraveled);
        tag.putString("primaryProfession", primaryProfession);
        CompoundTag profLevels = new CompoundTag();
        professionLevels.forEach(profLevels::putInt);
        tag.put("professionLevels", profLevels);
        CompoundTag profXp = new CompoundTag();
        professionXp.forEach((k, v) -> profXp.putLong(k, v));
        tag.put("professionXp", profXp);
        tag.putBoolean("firstLogin", firstLogin);
        tag.putInt("rebirthCount", rebirthCount);
        tag.putInt("olympiadPoints", olympiadPoints);
        tag.putBoolean("isNoble", isNoble);
        ListTag tamedList = new ListTag();
        for (String id : tamedEntityIds) tamedList.add(StringTag.valueOf(id));
        tag.put("tamedEntities", tamedList);
        ListTag biomeList = new ListTag();
        for (String b : discoveredBiomes) biomeList.add(StringTag.valueOf(b));
        tag.put("discoveredBiomes", biomeList);
        // Class bonuses
        tag.putDouble("cb0", classBonuses[0]);
        tag.putDouble("cb1", classBonuses[1]);
        tag.putDouble("cb2", classBonuses[2]);
        tag.putDouble("cb3", classBonuses[3]);
        tag.putDouble("cb4", classBonuses[4]);
        tag.putDouble("cb5", classBonuses[5]);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        level = tag.getInt("level");
        if (level < 1) level = 1;
        experience = tag.getLong("experience");
        skillPoints = tag.getInt("skillPoints");
        professionPoints = tag.getInt("professionPoints");
        maxHealth = tag.getDouble("maxHealth");
        if (maxHealth < 10) maxHealth = 100.0;
        currentHealth = tag.getDouble("currentHealth");
        maxEnergy = tag.getDouble("maxEnergy");
        if (maxEnergy < 10) maxEnergy = 100.0;
        currentEnergy = tag.getDouble("currentEnergy");
        maxStamina = tag.getDouble("maxStamina");
        if (maxStamina < 10) maxStamina = 100.0;
        currentStamina = tag.getDouble("currentStamina");
        physicalAttack = tag.getDouble("physicalAttack");
        if (physicalAttack < 1) physicalAttack = 10.0;
        magicAttack = tag.getDouble("magicAttack");
        if (magicAttack < 1) magicAttack = 10.0;
        physicalDefense = tag.getDouble("physicalDefense");
        magicDefense = tag.getDouble("magicDefense");
        critChance = tag.getDouble("critChance");
        if (critChance < 0.01) critChance = 0.05;
        critMultiplier = tag.getDouble("critMultiplier");
        if (critMultiplier < 1) critMultiplier = 1.5;
        dodgeChance = tag.getDouble("dodgeChance");
        playerClass = tag.getString("playerClass");
        if (playerClass.isEmpty()) playerClass = "WANDERER";
        classLevel = tag.getInt("classLevel");
        if (classLevel < 1) classLevel = 1;
        classExperience = tag.getLong("classExperience");
        strAttribute = tag.getInt("str");
        agiAttribute = tag.getInt("agi");
        intAttribute = tag.getInt("int_");
        vitAttribute = tag.getInt("vit");
        wisAttribute = tag.getInt("wis");
        lukAttribute = tag.getInt("luk");
        unlockedSkills = new ArrayList<>();
        ListTag skillsList = tag.getList("unlockedSkills", Tag.TAG_STRING);
        for (int i = 0; i < skillsList.size(); i++) unlockedSkills.add(skillsList.getString(i));
        skillCooldowns = new HashMap<>();
        CompoundTag cds = tag.getCompound("skillCooldowns");
        for (String k : cds.getAllKeys()) skillCooldowns.put(k, cds.getLong(k));
        karma = tag.getInt("karma");
        karmaDecayTimer = tag.getInt("karmaDecayTimer");
        bankBalance = tag.getLong("bankBalance");
        walletBalance = tag.getLong("walletBalance");
        if (walletBalance <= 0 && !tag.contains("walletBalance")) walletBalance = 500;
        totalTaxesPaid = tag.getLong("totalTaxesPaid");
        totalEarned = tag.getLong("totalEarned");
        totalTradesCompleted = tag.getInt("totalTrades");
        clanId = tag.getString("clanId");
        clanRank = tag.getString("clanRank");
        if (clanRank.isEmpty()) clanRank = "RECRUIT";
        activeTitle = tag.getString("activeTitle");
        unlockedTitles = new ArrayList<>();
        ListTag titleList = tag.getList("unlockedTitles", Tag.TAG_STRING);
        for (int i = 0; i < titleList.size(); i++) unlockedTitles.add(titleList.getString(i));
        bodyTemperature = tag.getFloat("bodyTemperature");
        if (bodyTemperature < 1f) bodyTemperature = 37.0f;
        temperatureResistance = tag.getFloat("temperatureResistance");
        totalPlayerKills = tag.getInt("totalPlayerKills");
        bossesKilled = tag.getInt("bossesKilled");
        totalDeaths = tag.getInt("totalDeaths");
        distanceTraveled = tag.getLong("distanceTraveled");
        primaryProfession = tag.getString("primaryProfession");
        if (primaryProfession.isEmpty()) primaryProfession = "NONE";
        professionLevels = new HashMap<>();
        CompoundTag profLevels = tag.getCompound("professionLevels");
        for (String k : profLevels.getAllKeys()) professionLevels.put(k, profLevels.getInt(k));
        professionXp = new HashMap<>();
        CompoundTag profXp = tag.getCompound("professionXp");
        for (String k : profXp.getAllKeys()) professionXp.put(k, profXp.getLong(k));
        firstLogin = tag.getBoolean("firstLogin");
        rebirthCount = tag.getInt("rebirthCount");
        olympiadPoints = tag.getInt("olympiadPoints");
        isNoble = tag.getBoolean("isNoble");
        tamedEntityIds = new ArrayList<>();
        ListTag tamedList = tag.getList("tamedEntities", Tag.TAG_STRING);
        for (int i = 0; i < tamedList.size(); i++) tamedEntityIds.add(tamedList.getString(i));
        discoveredBiomes = new HashSet<>();
        ListTag biomeList = tag.getList("discoveredBiomes", Tag.TAG_STRING);
        for (int i = 0; i < biomeList.size(); i++) discoveredBiomes.add(biomeList.getString(i));
        classBonuses = new double[]{
            tag.getDouble("cb0"), tag.getDouble("cb1"), tag.getDouble("cb2"),
            tag.getDouble("cb3"), tag.getDouble("cb4"), tag.getDouble("cb5")
        };
    }

    // ── GETTERS / SETTERS ─────────────────────────────────────────────────────

    public int getLevel() { return level; }
    public void setLevel(int l) { this.level = Math.max(1, Math.min(100, l)); }
    public long getExperience() { return experience; }
    public void addExperience(long xp) { this.experience += xp; }
    public int getSkillPoints() { return skillPoints; }
    public void addSkillPoints(int p) { skillPoints += p; }
    public void consumeSkillPoint() { if (skillPoints > 0) skillPoints--; }
    public int getProfessionPoints() { return professionPoints; }

    public long getXpForNextLevel() {
        return (long)(150 * Math.pow(level, 1.8) + 200 * level);
    }
    public boolean canLevelUp() { return experience >= getXpForNextLevel() && level < 100; }
    public void levelUp() {
        if (!canLevelUp()) return;
        experience -= getXpForNextLevel();
        level++; skillPoints += 3; professionPoints++;
        // Incremental stat gain - preserves class bonuses already applied
        maxHealth += 15; maxEnergy += 8; maxStamina += 5;
        physicalAttack += 1.5; magicAttack += 1.5;
        physicalDefense += 0.5; magicDefense += 0.4;
        currentHealth = maxHealth; currentEnergy = maxEnergy;
    }

    public double getMaxHealth() { return maxHealth; }
    public void setMaxHealth(double h) { this.maxHealth = h; }
    public double getCurrentHealth() { return currentHealth; }
    public void setCurrentHealth(double h) { this.currentHealth = Math.max(0, Math.min(maxHealth, h)); }
    public void heal(double a) { setCurrentHealth(currentHealth + a); }
    public void damage(double a) { setCurrentHealth(currentHealth - a); }
    public boolean isDead() { return currentHealth <= 0; }
    public double getHealthPercent() { return maxHealth <= 0 ? 0 : currentHealth / maxHealth; }

    public double getMaxEnergy() { return maxEnergy; }
    public void setMaxEnergy(double e) { this.maxEnergy = e; }
    public double getCurrentEnergy() { return currentEnergy; }
    public void setCurrentEnergy(double e) { this.currentEnergy = Math.max(0, Math.min(maxEnergy, e)); }
    public boolean consumeEnergy(double amount) {
        if (currentEnergy < amount) return false;
        currentEnergy -= amount; return true;
    }

    public double getMaxStamina() { return maxStamina; }
    public double getCurrentStamina() { return currentStamina; }
    public void setCurrentStamina(double s) { this.currentStamina = Math.max(0, Math.min(maxStamina, s)); }

    public double getPhysicalAttack() { return physicalAttack; }
    public void setPhysicalAttack(double a) { this.physicalAttack = a; }
    public double getMagicAttack() { return magicAttack; }
    public void setMagicAttack(double a) { this.magicAttack = a; }
    public double getPhysicalDefense() { return physicalDefense; }
    public void setPhysicalDefense(double d) { this.physicalDefense = d; }
    public double getMagicDefense() { return magicDefense; }
    public void setMagicDefense(double d) { this.magicDefense = d; }
    public double getCritChance() { return critChance; }
    public void setCritChance(double c) { this.critChance = c; }
    public double getCritMultiplier() { return critMultiplier; }
    public double getDodgeChance() { return dodgeChance; }
    public void setDodgeChance(double d) { this.dodgeChance = d; }

    public int getSTR() { return strAttribute; }
    public int getAGI() { return agiAttribute; }
    public int getINT() { return intAttribute; }
    public int getVIT() { return vitAttribute; }
    public int getWIS() { return wisAttribute; }
    public int getLUK() { return lukAttribute; }

    public boolean allocateAttribute(String attr, int points) {
        if (skillPoints < points) return false;
        skillPoints -= points;
        switch (attr.toUpperCase()) {
            case "STR" -> strAttribute += points;
            case "AGI" -> agiAttribute += points;
            case "INT" -> intAttribute += points;
            case "VIT" -> vitAttribute += points;
            case "WIS" -> wisAttribute += points;
            case "LUK" -> lukAttribute += points;
            default -> { skillPoints += points; return false; }
        }
        recalcFromAttributes();
        return true;
    }

    // Stores class stat bonuses to be re-applied after attribute recalc
    // Index: [maxHealth, maxEnergy, physAtk, magAtk, physDef, magDef]
    private double[] classBonuses = new double[6];

    /** Called when a class is assigned to preserve bonuses through attribute reallocations */
    public void setClassBonuses(double[] bonuses) {
        if (bonuses != null && bonuses.length == 6)
            this.classBonuses = bonuses.clone();
    }

    public void recalcFromAttributes() {
        // Base formula from attributes + level
        physicalAttack  = 10.0 + (strAttribute * 2.0) + (level * 1.5) + classBonuses[2];
        dodgeChance     = 0.03 + (agiAttribute * 0.002);
        magicAttack     = 10.0 + (intAttribute * 2.5) + (level * 1.5) + classBonuses[3];
        maxHealth       = 100.0 + (vitAttribute * 10.0) + (level * 15.0) + classBonuses[0];
        physicalDefense = 5.0   + (vitAttribute * 0.5)  + (level * 0.3)  + classBonuses[4];
        maxEnergy       = 100.0 + (wisAttribute * 5.0) + classBonuses[1];
        magicDefense    = 5.0   + (wisAttribute * 1.0) + classBonuses[5];
        critChance      = 0.05  + (lukAttribute * 0.003);
    }

    public int getKarma() { return karma; }
    public void addKarma(int a) { this.karma = Math.max(-10000, Math.min(10000, karma + a)); }
    public int getKarmaDecayTimer() { return karmaDecayTimer; }
    public void setKarmaDecayTimer(int t) { this.karmaDecayTimer = t; }

    public KarmaLevel getKarmaLevel() {
        if (karma >= 8000) return KarmaLevel.DIVINE;
        if (karma >= 5000) return KarmaLevel.HOLY;
        if (karma >= 2000) return KarmaLevel.VIRTUOUS;
        if (karma >= 500)  return KarmaLevel.GOOD;
        if (karma > -500)  return KarmaLevel.NEUTRAL;
        if (karma > -2000) return KarmaLevel.SHADY;
        if (karma > -5000) return KarmaLevel.WICKED;
        if (karma > -8000) return KarmaLevel.CORRUPT;
        return KarmaLevel.ABYSSAL;
    }

    public enum KarmaLevel { DIVINE, HOLY, VIRTUOUS, GOOD, NEUTRAL, SHADY, WICKED, CORRUPT, ABYSSAL }

    public enum TemperatureStatus { HEAT_STROKE, HEAT_EXHAUSTION, HOT, NORMAL, COLD, HYPOTHERMIA, FROSTBITE }

    public String getPlayerClass() { return playerClass; }
    public void setPlayerClass(String c) { this.playerClass = c == null ? "WANDERER" : c; }
    public int getClassLevel() { return classLevel; }
    public void setClassLevel(int l) { this.classLevel = l; }
    public long getClassExperience() { return classExperience; }
    public void addClassExperience(long xp) { this.classExperience += xp; }
    public List<String> getUnlockedSkills() { return Collections.unmodifiableList(unlockedSkills); }
    public boolean hasSkill(String id) { return unlockedSkills.contains(id); }
    public void unlockSkill(String id) { if (id != null && !unlockedSkills.contains(id)) unlockedSkills.add(id); }
    public long getSkillCooldownRemaining(String id) {
        Long exp = skillCooldowns.get(id);
        return exp == null ? 0 : Math.max(0, exp - System.currentTimeMillis());
    }
    public void setSkillCooldown(String id, long ms) {
        if (ms <= 0) { skillCooldowns.remove(id); return; }
        skillCooldowns.put(id, System.currentTimeMillis() + ms);
    }
    public void clearExpiredCooldowns() {
        long now = System.currentTimeMillis();
        skillCooldowns.entrySet().removeIf(e -> e.getValue() <= now);
    }

    public long getBankBalance() { return bankBalance; }
    public void setBankBalance(long b) { this.bankBalance = Math.max(0, b); }
    public long getWalletBalance() { return walletBalance; }
    public void setWalletBalance(long w) { this.walletBalance = Math.max(0, w); }
    public long getTotalTaxesPaid() { return totalTaxesPaid; }
    public void addTaxPaid(long a) { totalTaxesPaid += a; }
    public long getTotalEarned() { return totalEarned; }
    public int getTotalTradesCompleted() { return totalTradesCompleted; }
    public void incrementTrades() { totalTradesCompleted++; }
    public boolean payFromWallet(long a) {
        if (walletBalance < a) return false;
        walletBalance -= a; return true;
    }
    public void receiveToWallet(long a) { walletBalance += a; totalEarned += a; }

    public String getClanId() { return clanId; }
    public void setClanId(String id) { this.clanId = id == null ? "" : id; }
    public boolean isInClan() { return !clanId.isEmpty(); }
    public String getClanRank() { return clanRank; }
    public void setClanRank(String r) { this.clanRank = r == null ? "RECRUIT" : r; }

    public String getActiveTitle() { return activeTitle; }
    public void setActiveTitle(String t) { this.activeTitle = t == null ? "" : t; }
    public List<String> getUnlockedTitles() { return Collections.unmodifiableList(unlockedTitles); }
    public void unlockTitle(String t) { if (t != null && !unlockedTitles.contains(t)) unlockedTitles.add(t); }
    public void removeTitle(String t) { unlockedTitles.remove(t); if (t != null && t.equals(activeTitle)) activeTitle = ""; }

    public float getBodyTemperature() { return bodyTemperature; }
    public void setBodyTemperature(float t) { this.bodyTemperature = t; }
    public void adjustBodyTemperature(float d) { this.bodyTemperature += d; }
    public float getTemperatureResistance() { return temperatureResistance; }
    public void setTemperatureResistance(float r) { this.temperatureResistance = r; }
    public TemperatureStatus getTemperatureStatus() {
        if (bodyTemperature >= 42f)   return TemperatureStatus.HEAT_STROKE;
        if (bodyTemperature >= 40f)   return TemperatureStatus.HEAT_EXHAUSTION;
        if (bodyTemperature >= 39f)   return TemperatureStatus.HOT;
        if (bodyTemperature >= 35f)   return TemperatureStatus.NORMAL;
        if (bodyTemperature >= 28f)   return TemperatureStatus.COLD;     // wide cold zone
        if (bodyTemperature >= 22f)   return TemperatureStatus.HYPOTHERMIA; // only cold biomes
        return TemperatureStatus.FROSTBITE;                               // only arctic/tundra
    }

    public int getTotalPlayerKills() { return totalPlayerKills; }
    public void incrementPlayerKills() { totalPlayerKills++; }
    public int getBossesKilled() { return bossesKilled; }
    public void incrementBossesKilled() { bossesKilled++; }
    public int getTotalDeaths() { return totalDeaths; }
    public void incrementDeaths() { totalDeaths++; }
    public long getDistanceTraveled() { return distanceTraveled; }
    public void addDistanceTraveled(long d) { distanceTraveled += d; }
    public Set<String> getDiscoveredBiomes() { return Collections.unmodifiableSet(discoveredBiomes); }
    public void addDiscoveredBiome(String b) { discoveredBiomes.add(b); }

    public String getPrimaryProfession() { return primaryProfession; }
    public void setPrimaryProfession(String p) { this.primaryProfession = p == null ? "NONE" : p; }
    public int getProfessionLevel(String p) { return professionLevels.getOrDefault(p, 1); }
    public void setProfessionLevel(String p, int l) { professionLevels.put(p, l); }
    public long getProfessionXp(String p) { return professionXp.getOrDefault(p, 0L); }
    public void addProfessionXp(String p, long xp) { professionXp.merge(p, xp, Long::sum); }

    public boolean isFirstLogin() { return firstLogin; }
    public void setFirstLogin(boolean b) { this.firstLogin = b; }
    public int getRebirthCount() { return rebirthCount; }
    public void incrementRebirth() { rebirthCount++; }
    public int getOlympiadPoints() { return olympiadPoints; }
    public void addOlympiadPoints(int p) { olympiadPoints += p; }
    public boolean isNoble() { return isNoble; }
    public void setNoble(boolean b) { this.isNoble = b; }
    public List<String> getTamedEntityIds() { return Collections.unmodifiableList(tamedEntityIds); }
    public void addTamedEntityId(String id) { tamedEntityIds.add(id); }
    public void removeTamedEntityId(String id) { tamedEntityIds.remove(id); }
}

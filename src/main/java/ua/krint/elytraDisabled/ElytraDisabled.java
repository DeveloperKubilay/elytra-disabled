package ua.krint.elytraDisabled;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

public class ElytraDisabled extends JavaPlugin {

    private static final long JOIN_DELAY_TICKS = 5L;
    private static final long TELEPORT_DELAY_TICKS = 2L;
    private static final long RESPAWN_DELAY_TICKS = 2L;
    private static final long COOLDOWN_CLEANUP_INTERVAL = 6000L;

    private static final int BSTATS_PLUGIN_ID = 33682;

    private FileConfiguration config;
    private FileConfiguration langConfig;
    private ElytraListener listener;
    private ElytraSafetyNetTask safetyNetTask;
    private UpdateChecker updateChecker;
    private final MessageCooldown messageCooldown = new MessageCooldown();
    private String currentLanguage;

    private String bypassPermission;
    private boolean preventEquip;
    private boolean forceUnequipOnEnter;
    private boolean stopExistingGlide;
    private long messageCooldownMillis;
    private long safetyNetIntervalTicks;

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        saveDefaultConfig();
        config = getConfig();

        setupLanguage();
        cacheConfigValues();

        listener = new ElytraListener(this);
        Bukkit.getPluginManager().registerEvents(listener, this);

        startSafetyNet();

        ElytraCommand command = new ElytraCommand(this);
        getCommand("elytra-disabled").setExecutor(command);
        getCommand("elytra-disabled").setTabCompleter(command);

        startCooldownCleanup();

        if (config.getBoolean("settings.check_updates", true)) {
            checkForUpdates();
        }

        setupMetrics();
        setupPlaceholders();

        getLogger().info("ElytraDisabled enabled successfully!");
        getLogger().info("Language: " + currentLanguage);
        getLogger().info("Protection active in worlds: " + config.getStringList("settings.disable_in_worlds"));
    }

    private void cacheConfigValues() {
        this.bypassPermission = config.getString("permissions.bypass", "elytradisabled.bypass");
        this.preventEquip = config.getBoolean("settings.prevent_equip", true);
        this.forceUnequipOnEnter = config.getBoolean("settings.force_unequip_on_enter", true);
        this.stopExistingGlide = config.getBoolean("settings.stop_existing_glide", true);
        this.messageCooldownMillis = config.getLong("settings.message_cooldown", 3) * 1000L;
        this.safetyNetIntervalTicks = config.getLong("settings.safety_net_interval_ticks", 100);
    }

    private void checkForUpdates() {
        updateChecker = new UpdateChecker(this);
        updateChecker.checkForUpdates(hasUpdate -> {
            if (hasUpdate) {
                Bukkit.getPluginManager().registerEvents(new org.bukkit.event.Listener() {
                    @org.bukkit.event.EventHandler
                    public void onJoin(org.bukkit.event.player.PlayerJoinEvent e) {
                        Player p = e.getPlayer();
                        if (p.hasPermission("elytradisabled.update.notify")) {
                            Bukkit.getScheduler().runTaskLater(ElytraDisabled.this, () -> {
                                updateChecker.notifyPlayer(p);
                            }, 40L);
                        }
                    }
                }, this);
            }
        });
    }

    private void setupMetrics() {
        if (BSTATS_PLUGIN_ID <= 0) {
            getLogger().warning("bStats plugin ID is not configured. Set BSTATS_PLUGIN_ID in ElytraDisabled.java.");
            return;
        }
        new Metrics(this, BSTATS_PLUGIN_ID);
    }

    private void setupPlaceholders() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            return;
        }
        new ElytraPlaceholders(this).register();
        getLogger().info("PlaceholderAPI found, placeholders registered.");
    }

    private void startCooldownCleanup() {
        Bukkit.getScheduler().runTaskTimer(this,
                () -> messageCooldown.cleanUp(messageCooldownMillis),
                COOLDOWN_CLEANUP_INTERVAL, COOLDOWN_CLEANUP_INTERVAL);
    }

    @Override
    public void onDisable() {
        if (safetyNetTask != null) {
            safetyNetTask.stop();
        }
        getLogger().info("ElytraDisabled disabled!");
    }

    private void setupLanguage() {
        File langFolder = new File(getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        saveResourceIfNotExists("lang/ru.yml");
        saveResourceIfNotExists("lang/en.yml");
        saveResourceIfNotExists("lang/ua.yml");
        saveResourceIfNotExists("lang/tr.yml");

        currentLanguage = config.getString("settings.language", "ru");
        File langFile = new File(langFolder, currentLanguage + ".yml");

        if (!langFile.exists()) {
            getLogger().warning("Language file '" + currentLanguage + ".yml' not found! Using Russian language.");
            currentLanguage = "ru";
            langFile = new File(langFolder, "ru.yml");
        }

        langConfig = YamlConfiguration.loadConfiguration(langFile);

        try {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(getResource("lang/" + currentLanguage + ".yml"), StandardCharsets.UTF_8));
            langConfig.setDefaults(defaultConfig);
            langConfig.options().copyDefaults(true);

            langConfig.save(langFile);
        } catch (Exception e) {
            getLogger().warning("Failed to load default values for language: " + currentLanguage);
            e.printStackTrace();
        }
    }

    private void saveResourceIfNotExists(String resourcePath) {
        File file = new File(getDataFolder(), resourcePath);
        if (!file.exists()) {
            saveResource(resourcePath, false);
        }
    }

    public boolean isPluginEnabled() {
        return config.getBoolean("settings.enable_plugin", true);
    }

    public boolean isWorldDisabled(World world) {
        if (world == null) return false;
        if (!isPluginEnabled()) return false;

        List<String> disabledWorlds = config.getStringList("settings.disable_in_worlds");
        String worldName = world.getName();
        World.Environment env = world.getEnvironment();

        for (String name : disabledWorlds) {
            if (worldName.equalsIgnoreCase(name)) return true;
            if (name.equalsIgnoreCase("world_the_end") && env == World.Environment.THE_END) return true;
        }

        return false;
    }

    public boolean hasBypass(Player player) {
        return player.hasPermission(bypassPermission);
    }

    public String getMessage(String key) {
        return getRichMessage(key).toPlainText();
    }

    private List<String> getMessageLines(String key) {
        if (langConfig.isList(key)) {
            List<String> list = langConfig.getStringList(key);
            if (!list.isEmpty()) return list;
        }

        String single = langConfig.getString(key);
        if (single == null || single.isEmpty()) {
            getLogger().warning("Message with key '" + key + "' not found in language file!");
            return Collections.singletonList("[" + key + "]");
        }
        return Collections.singletonList(single);
    }

    private RichMessage getRichMessage(String key) {
        return RichMessage.parse(getMessageLines(key), getLogger());
    }

    private boolean isMessageDisabled(String key) {
        return langConfig.isBoolean(key) && !langConfig.getBoolean(key);
    }

    public void sendBlockedWarning(Player p, String messageKey) {
        if (p == null || !p.isOnline()) return;
        if (isMessageDisabled(messageKey)) return;
        if (!messageCooldown.allow(p, messageCooldownMillis)) return;
        getRichMessage(messageKey).send(p);
    }

    public void sendMessage(Player p, String messageKey) {
        if (p == null || !p.isOnline()) return;
        if (isMessageDisabled(messageKey)) return;
        getRichMessage(messageKey).send(p);
    }

    public void sendCommandMessage(CommandSender sender, String messageKey) {
        if (isMessageDisabled(messageKey)) return;
        sender.sendMessage(getMessage(messageKey));
    }

    public boolean removeElytra(Player p, String messageKey) {
        if (p == null || !p.isOnline()) {
            return false;
        }

        try {
            PlayerInventory inv = p.getInventory();
            ItemStack chest = inv.getChestplate();

            if (chest != null && chest.getType() == Material.ELYTRA) {
                inv.setChestplate(null);

                if (inv.firstEmpty() != -1) {
                    inv.addItem(chest);
                } else {
                    p.getWorld().dropItemNaturally(p.getLocation(), chest);
                }

                if (p.isGliding()) {
                    p.setGliding(false);
                }

                if (messageKey != null && !messageKey.isEmpty()) {
                    sendMessage(p, messageKey);
                }

                return true;
            }
        } catch (Exception e) {
            getLogger().warning("Error while removing elytra from player " + p.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public void reloadPluginConfig() {
        reloadConfig();
        config = getConfig();
        setupLanguage();
        cacheConfigValues();

        if (safetyNetTask != null) {
            safetyNetTask.stop();
            safetyNetTask = null;
        }

        startSafetyNet();
        if (config.getBoolean("settings.check_updates", true)) {
            checkForUpdates();
        }
    }

    private void startSafetyNet() {
        if (safetyNetIntervalTicks > 0) {
            safetyNetTask = new ElytraSafetyNetTask(this);
            safetyNetTask.start(safetyNetIntervalTicks);
        }
    }

    public long getJoinDelayTicks() {
        return JOIN_DELAY_TICKS;
    }

    public long getTeleportDelayTicks() {
        return TELEPORT_DELAY_TICKS;
    }

    public long getRespawnDelayTicks() {
        return RESPAWN_DELAY_TICKS;
    }

    public boolean isPreventEquip() {
        return preventEquip;
    }

    public boolean isForceUnequipOnEnter() {
        return forceUnequipOnEnter;
    }

    public boolean isStopExistingGlide() {
        return stopExistingGlide;
    }
}
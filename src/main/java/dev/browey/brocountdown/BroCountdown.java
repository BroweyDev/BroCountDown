package dev.browey.brocountdown;

import dev.browey.brocountdown.config.*;
import dev.browey.brocountdown.managers.TeleportManager;
import dev.browey.brocountdown.utils.MessageUtils;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.potion.PotionEffectType;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public final class BroCountdown extends JavaPlugin implements Listener {
    private Map<String, CommandConfig> commandConfigs = new HashMap<>();
    private Map<String, TeleportPreset> teleportPresets = new HashMap<>();
    private Map<String, Long> cooldowns = new HashMap<>();
    private LuckPerms luckPerms;
    private TeleportManager teleportManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        
        teleportManager = new TeleportManager(this);
        loadCommandConfigs();
        loadTeleportPresets();
        luckPerms = LuckPermsProvider.get();

        getCommand("brocd").setExecutor(this);
    }

    private void loadCommandConfigs() {
        ConfigurationSection commandsSection = getConfig().getConfigurationSection("command-delays");
        for (String cmd : commandsSection.getKeys(false)) {
            if (cmd.equals("teleport-presets")) continue;
            
            CommandConfig config = new CommandConfig();
            ConfigurationSection cmdSection = commandsSection.getConfigurationSection(cmd);
            
            config.setType(cmdSection.getString("type"));
            config.setDelay(cmdSection.getInt("delay"));
            config.setPreset(cmdSection.getString("preset", "default"));
            
            Map<String, String> messages = new HashMap<>();
            ConfigurationSection messagesSection = cmdSection.getConfigurationSection("messages");
            if (messagesSection != null) {
                for (String key : messagesSection.getKeys(false)) {
                    messages.put(key, messagesSection.getString(key));
                }
            }
            config.setMessages(messages);
            
            Map<String, String> bossbar = new HashMap<>();
            ConfigurationSection bossbarSection = cmdSection.getConfigurationSection("bossbar");
            if (bossbarSection != null) {
                bossbar.put("color", bossbarSection.getString("color"));
                bossbar.put("style", bossbarSection.getString("style"));
            }
            config.setBossbar(bossbar);
            
            Map<String, String> worldPresets = new HashMap<>();
            ConfigurationSection worldPresetsSection = cmdSection.getConfigurationSection("world-specific-presets");
            if (worldPresetsSection != null) {
                for (String world : worldPresetsSection.getKeys(false)) {
                    worldPresets.put(world, worldPresetsSection.getString(world));
                }
            }
            
            Map<String, Boolean> cancelOnDamage = new HashMap<>();
            ConfigurationSection cancelSection = cmdSection.getConfigurationSection("cancel-on-damage");
            if (cancelSection != null) {
                cancelOnDamage.put("mob", cancelSection.getBoolean("mob", false));
                cancelOnDamage.put("player", cancelSection.getBoolean("player", false));
            }
            
            Map<String, Integer> groupDelays = new HashMap<>();
            ConfigurationSection groupDelaysSection = cmdSection.getConfigurationSection("group-delays");
            if (groupDelaysSection != null) {
                for (String group : groupDelaysSection.getKeys(false)) {
                    groupDelays.put(group, groupDelaysSection.getInt(group));
                }
            }
            
            commandConfigs.put("/" + cmd, config);
        }
    }

    private void loadTeleportPresets() {
        ConfigurationSection presets = getConfig().getConfigurationSection("command-delays.teleport-presets");
        for (String presetName : presets.getKeys(false)) {
            ConfigurationSection presetSection = presets.getConfigurationSection(presetName);
            TeleportPreset preset = new TeleportPreset();
            
            preset.setMessages(loadMessages(presetSection.getConfigurationSection("messages")));
            preset.setBossbar(loadBossBarSettings(presetSection.getConfigurationSection("bossbar")));
            preset.setCancelButton(presetSection.getBoolean("cancel-button"));
            preset.setSounds(loadSounds(presetSection.getConfigurationSection("sounds")));
            preset.setCountdownSound(loadCountdownSound(presetSection.getConfigurationSection("sounds")));
            preset.setStartEffects(loadEffects(presetSection.getConfigurationSection("effects.start")));
            preset.setCompleteEffects(loadEffects(presetSection.getConfigurationSection("effects.complete")));
            
            teleportPresets.put(presetName, preset);
        }
    }

    private Map<String, String> loadMessages(ConfigurationSection section) {
        Map<String, String> messages = new HashMap<>();
        if (section == null) return messages;
        
        for (String key : section.getKeys(false)) {
            messages.put(key, section.getString(key));
        }
        return messages;
    }

    private Map<String, String> loadBossBarSettings(ConfigurationSection section) {
        Map<String, String> settings = new HashMap<>();
        if (section == null) return settings;

        if (section.contains("color")) {
            settings.put("color", section.getString("color"));
        }
        if (section.contains("style")) {
            settings.put("style", section.getString("style"));
        }
        return settings;
    }

    private Map<String, SoundEffect> loadSounds(ConfigurationSection section) {
        Map<String, SoundEffect> sounds = new HashMap<>();
        if (section == null) return sounds;

        for (String key : section.getKeys(false)) {
            if (key.equals("countdown")) continue;
            
            ConfigurationSection soundSection = section.getConfigurationSection(key);
            if (soundSection != null) {
                SoundEffect effect = new SoundEffect();
                effect.setSound(Sound.valueOf(soundSection.getString("sound")));
                effect.setVolume((float) soundSection.getDouble("volume", 1.0));
                effect.setPitch((float) soundSection.getDouble("pitch", 1.0));
                sounds.put(key, effect);
            }
        }
        return sounds;
    }

    private CountdownSound loadCountdownSound(ConfigurationSection section) {
        if (section == null || !section.contains("countdown")) return null;

        ConfigurationSection countdownSection = section.getConfigurationSection("countdown");
        if (countdownSection == null) return null;

        CountdownSound countdownSound = new CountdownSound();
        countdownSound.setEnabled(countdownSection.getBoolean("enabled", false));
        countdownSound.setSound(Sound.valueOf(countdownSection.getString("sound")));
        countdownSound.setVolume((float) countdownSection.getDouble("volume", 1.0));
        countdownSound.setPitch((float) countdownSection.getDouble("pitch", 1.0));
        
        return countdownSound;
    }

    private List<PotionEffectData> loadEffects(ConfigurationSection section) {
        List<PotionEffectData> effects = new ArrayList<>();
        if (section == null) return effects;

        for (String key : section.getKeys(false)) {
            ConfigurationSection effectSection = section.getConfigurationSection(key);
            if (effectSection != null) {
                PotionEffectData effect = new PotionEffectData();
                effect.setType(PotionEffectType.getByName(effectSection.getString("type")));
                effect.setDuration(effectSection.getInt("duration"));
                effect.setAmplifier(effectSection.getInt("amplifier"));
                effects.add(effect);
            }
        }
        return effects;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("brocd")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("brocd.reload")) {
                    sender.sendMessage(MessageUtils.formatMessage(getConfig().getString("messages.no-permission")));
                    return true;
                }
                
                reloadConfig();
                loadCommandConfigs();
                loadTeleportPresets();
                sender.sendMessage(MessageUtils.formatMessage(getConfig().getString("messages.reload-success")));
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().split(" ")[0].toLowerCase();
        
        CommandConfig config = commandConfigs.get(command);
        if (config == null) return;

        if (config.getType().equals("TELEPORT")) {
            event.setCancelled(true);
            String fullCommand = event.getMessage().substring(1);
            TeleportPreset preset = teleportPresets.get(config.getPreset());
            teleportManager.startTeleport(player, fullCommand, config, preset);
        } else if (config.getType().equals("COOLDOWN")) {
            handleCooldown(player, command, event, config);
        }
    }

    private void handleCooldown(Player player, String command, PlayerCommandPreprocessEvent event, CommandConfig config) {
        Long lastUsed = cooldowns.get(player.getUniqueId() + command);
        if (lastUsed != null) {
            int delay = getDelayForPlayer(player, config);
            long remainingTime = (lastUsed + (delay * 1000L)) - System.currentTimeMillis();
            if (remainingTime > 0) {
                event.setCancelled(true);
                String timeStr = MessageUtils.formatTime(remainingTime, 
                    getConfig().getString("time-format.format"),
                    getConfig().getString("time-format.hours"),
                    getConfig().getString("time-format.minutes"),
                    getConfig().getString("time-format.seconds"));
                String message = config.getMessages().get("cooldown").replace("{time}", timeStr);
                player.sendMessage(MessageUtils.formatMessage(message));
                return;
            }
        }
        
        cooldowns.put(player.getUniqueId() + command, System.currentTimeMillis());
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        
        if (!getConfig().getBoolean("settings.enable-damage-cancellation")) return;
        
        String command = teleportManager.getPendingCommand(player);
        if (command == null) return;
        
        CommandConfig config = commandConfigs.get("/" + command.split(" ")[0]);
        if (config == null) return;
        
        boolean cancelOnMob = config.getCancelOnDamage().get("mob");
        boolean cancelOnPlayer = config.getCancelOnDamage().get("player");
        
        if ((event.getDamager() instanceof Player && cancelOnPlayer) ||
            (event.getDamager() instanceof Monster && cancelOnMob)) {
            teleportManager.cancelTeleport(player, "damage-cancel");
        }
    }

    private int getDelayForPlayer(Player player, CommandConfig config) {
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user != null) {
            String primaryGroup = user.getPrimaryGroup();
            return config.getGroupDelays().getOrDefault(primaryGroup, config.getDelay());
        }
        return config.getDelay();
    }
}
package dev.browey.brocountdown.managers;

import dev.browey.brocountdown.BroCountdown;
import dev.browey.brocountdown.config.*;
import dev.browey.brocountdown.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class TeleportManager {
    private final BroCountdown plugin;
    private final Map<Player, BossBar> activeBossBars = new HashMap<>();
    private final Map<Player, Integer> activeTaskIds = new HashMap<>();
    private final Map<Player, String> pendingCommands = new HashMap<>();

    public TeleportManager(BroCountdown plugin) {
        this.plugin = plugin;
    }

    public void startTeleport(Player player, String command, CommandConfig config, TeleportPreset preset) {
        pendingCommands.put(player, command);

        applyEffects(player, preset.getStartEffects());

        if (preset.getSounds().containsKey("start")) {
            SoundEffect sound = preset.getSounds().get("start");
            player.playSound(player.getLocation(), sound.getSound(), sound.getVolume(), sound.getPitch());
        }

        String bossBarMessage = config.getMessages().getOrDefault("bossbar", 
            preset.getMessages().get("bossbar"));
        
        BossBar bossBar = Bukkit.createBossBar(
            MessageUtils.formatMessage(bossBarMessage.replace("{time}", String.valueOf(config.getDelay()))),
            BarColor.valueOf(preset.getBossbar().get("color")),
            BarStyle.valueOf(preset.getBossbar().get("style"))
        );
        
        activeBossBars.put(player, bossBar);
        bossBar.addPlayer(player);

        startTimer(player, command, config, preset, bossBar);
    }

    private void startTimer(Player player, String command, CommandConfig config, 
                          TeleportPreset preset, BossBar bossBar) {
        int taskId = new BukkitRunnable() {
            int countdown = config.getDelay();

            @Override
            public void run() {
                bossBar.setProgress((double) countdown / config.getDelay());
                String message = config.getMessages().getOrDefault("bossbar", 
                    preset.getMessages().get("bossbar"));
                bossBar.setTitle(MessageUtils.formatMessage(
                    message.replace("{time}", String.valueOf(countdown))));

                if (preset.getCountdownSound() != null && preset.getCountdownSound().isEnabled()) {
                    player.playSound(
                        player.getLocation(),
                        preset.getCountdownSound().getSound(),
                        preset.getCountdownSound().getVolume(),
                        preset.getCountdownSound().getPitch()
                    );
                }

                if (countdown <= 0) {
                    executeCommand(player, preset);
                    bossBar.removeAll();
                    activeBossBars.remove(player);
                    cancel();
                }
                countdown--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
        
        activeTaskIds.put(player, taskId);
    }

    public void cancelTeleport(Player player, String messageKey) {
        BossBar bossBar = activeBossBars.get(player);
        if (bossBar != null) {
            Integer taskId = activeTaskIds.remove(player);
            if (taskId != null) {
                Bukkit.getScheduler().cancelTask(taskId);
            }
            
            bossBar.removeAll();
            activeBossBars.remove(player);
            pendingCommands.remove(player);
            player.sendMessage(MessageUtils.formatMessage(
                plugin.getConfig().getString("messages." + messageKey)));
        }
    }

    private void executeCommand(Player player, TeleportPreset preset) {
        String command = pendingCommands.get(player);
        if (command != null) {
            if (preset.getSounds().containsKey("complete")) {
                SoundEffect sound = preset.getSounds().get("complete");
                player.playSound(player.getLocation(), sound.getSound(), sound.getVolume(), sound.getPitch());
            }

            applyEffects(player, preset.getCompleteEffects());

            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.dispatchCommand(player, command);
                pendingCommands.remove(player);
            });
        }
    }

    private void applyEffects(Player player, List<PotionEffectData> effects) {
        if (effects == null) return;
        for (PotionEffectData effect : effects) {
            player.addPotionEffect(new PotionEffect(
                effect.getType(),
                effect.getDuration(),
                effect.getAmplifier()
            ));
        }
    }

    public boolean hasPendingTeleport(Player player) {
        return activeBossBars.containsKey(player);
    }

    public String getPendingCommand(Player player) {
        return pendingCommands.get(player);
    }
} 
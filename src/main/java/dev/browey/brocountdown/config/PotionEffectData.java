package dev.browey.brocountdown.config;

import org.bukkit.potion.PotionEffectType;

public class PotionEffectData {
    private PotionEffectType type;
    private int duration;
    private int amplifier;

    public PotionEffectType getType() { return type; }
    public void setType(PotionEffectType type) { this.type = type; }
    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }
    public int getAmplifier() { return amplifier; }
    public void setAmplifier(int amplifier) { this.amplifier = amplifier; }
} 
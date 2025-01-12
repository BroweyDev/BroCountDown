package dev.browey.brocountdown.config;

import org.bukkit.Sound;

public class SoundEffect {
    private Sound sound;
    private float volume;
    private float pitch;

    public Sound getSound() { return sound; }
    public void setSound(Sound sound) { this.sound = sound; }
    public float getVolume() { return volume; }
    public void setVolume(float volume) { this.volume = volume; }
    public float getPitch() { return pitch; }
    public void setPitch(float pitch) { this.pitch = pitch; }
} 
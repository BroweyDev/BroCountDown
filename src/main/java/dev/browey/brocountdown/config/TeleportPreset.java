package dev.browey.brocountdown.config;

import org.bukkit.Sound;
import java.util.List;
import java.util.Map;

public class TeleportPreset {
    private Map<String, String> messages;
    private Map<String, String> bossbar;
    private boolean cancelButton;
    private Map<String, SoundEffect> sounds;
    private List<PotionEffectData> startEffects;
    private List<PotionEffectData> completeEffects;
    private CountdownSound countdownSound;


    public Map<String, String> getMessages() { return messages; }
    public void setMessages(Map<String, String> messages) { this.messages = messages; }
    public Map<String, String> getBossbar() { return bossbar; }
    public void setBossbar(Map<String, String> bossbar) { this.bossbar = bossbar; }
    public boolean isCancelButton() { return cancelButton; }
    public void setCancelButton(boolean cancelButton) { this.cancelButton = cancelButton; }
    public Map<String, SoundEffect> getSounds() { return sounds; }
    public void setSounds(Map<String, SoundEffect> sounds) { this.sounds = sounds; }
    public List<PotionEffectData> getStartEffects() { return startEffects; }
    public void setStartEffects(List<PotionEffectData> startEffects) { this.startEffects = startEffects; }
    public List<PotionEffectData> getCompleteEffects() { return completeEffects; }
    public void setCompleteEffects(List<PotionEffectData> completeEffects) { this.completeEffects = completeEffects; }
    public CountdownSound getCountdownSound() { return countdownSound; }
    public void setCountdownSound(CountdownSound countdownSound) { this.countdownSound = countdownSound; }
} 
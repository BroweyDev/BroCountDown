package dev.browey.brocountdown.config;

import java.util.HashMap;
import java.util.Map;

public class CommandConfig {
    private String type;
    private int delay;
    private Map<String, String> messages;
    private Map<String, String> bossbar;
    private boolean cancelButton;
    private String preset;
    private Map<String, String> worldPresets = new HashMap<>();
    private Map<String, Boolean> cancelOnDamage = new HashMap<>();
    private Map<String, Integer> groupDelays = new HashMap<>();

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getDelay() { return delay; }
    public void setDelay(int delay) { this.delay = delay; }
    public Map<String, String> getMessages() { return messages; }
    public void setMessages(Map<String, String> messages) { this.messages = messages; }
    public Map<String, String> getBossbar() { return bossbar; }
    public void setBossbar(Map<String, String> bossbar) { this.bossbar = bossbar; }
    public boolean isCancelButton() { return cancelButton; }
    public void setCancelButton(boolean cancelButton) { this.cancelButton = cancelButton; }
    public String getPreset() { return preset; }
    public void setPreset(String preset) { this.preset = preset; }
    public Map<String, String> getWorldPresets() { return worldPresets; }
    public Map<String, Boolean> getCancelOnDamage() { return cancelOnDamage; }
    public Map<String, Integer> getGroupDelays() { return groupDelays; }
} 
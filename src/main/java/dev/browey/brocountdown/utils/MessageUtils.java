package dev.browey.brocountdown.utils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class MessageUtils {
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static String formatMessage(String message) {
        if (message == null) return "";
        
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();
        
        while (matcher.find()) {
            String color = matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.of("#" + color).toString());
        }
        matcher.appendTail(buffer);
        
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    public static String formatTime(long milliseconds, String format, String hoursText, 
                                 String minutesText, String secondsText) {
        long seconds = milliseconds / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;

        return format
            .replace("{hours}", String.valueOf(hours))
            .replace("{h}", hoursText)
            .replace("{minutes}", String.valueOf(minutes))
            .replace("{m}", minutesText)
            .replace("{seconds}", String.valueOf(seconds))
            .replace("{s}", secondsText);
    }

    public static void sendCancelButton(Player player, String buttonText, String hoverText) {
        if (buttonText.startsWith("<center>")) {
            buttonText = centerText(buttonText.substring(8));
        }
        
        TextComponent cancelButton = new TextComponent(formatMessage(buttonText));
        cancelButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/canceltp"));
        cancelButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
            new ComponentBuilder(formatMessage(hoverText)).create()));
        
        player.spigot().sendMessage(cancelButton);
    }

    private static String centerText(String text) {
        int chatWidth = 320;
        int buttonWidth = ChatColor.stripColor(text).length() * 6;
        int padding = (chatWidth - buttonWidth) / 2;
        StringBuilder spaces = new StringBuilder();
        for (int i = 0; i < padding / 4; i++) {
            spaces.append(" ");
        }
        return spaces.toString() + text;
    }
} 
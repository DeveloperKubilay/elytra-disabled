package ua.krint.elytraDisabled;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RichMessage {

    private static final Pattern META_ATTR = Pattern.compile("(\\w+)=\"([^\"]*)\"");

    private final String type;
    private final int fadeInTicks;
    private final int stayTicks;
    private final int fadeOutTicks;
    private final Sound sound;
    private final float volume;
    private final float pitch;
    private final List<String> lines;

    private RichMessage(String type, int fadeInTicks, int stayTicks, int fadeOutTicks,
                         Sound sound, float volume, float pitch, List<String> lines) {
        this.type = type;
        this.fadeInTicks = fadeInTicks;
        this.stayTicks = stayTicks;
        this.fadeOutTicks = fadeOutTicks;
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
        this.lines = lines;
    }

    public static RichMessage parse(List<String> rawLines, Logger logger) {
        String type = "chat";
        int fadeIn = 10, stay = 40, fadeOut = 10;
        Sound sound = null;
        float volume = 1.0f;
        float pitch = 1.0f;

        List<String> content = rawLines;

        if (!rawLines.isEmpty() && isMetaLine(rawLines.get(0))) {
            Matcher m = META_ATTR.matcher(rawLines.get(0));
            while (m.find()) {
                String key = m.group(1).toLowerCase(Locale.ROOT);
                String value = m.group(2);

                if (key.equals("type")) {
                    type = value.toLowerCase(Locale.ROOT);
                } else if (key.equals("title_times")) {
                    String[] parts = value.split(":");
                    if (parts.length == 3) {
                        try {
                            fadeIn = Integer.parseInt(parts[0].trim());
                            stay = Integer.parseInt(parts[1].trim());
                            fadeOut = Integer.parseInt(parts[2].trim());
                        } catch (NumberFormatException ignored) {
                        }
                    }
                } else if (key.equals("sound")) {
                    String[] parts = value.split(";");
                    sound = parseSound(parts[0], logger);
                    if (parts.length > 1) volume = parseFloat(parts[1], 1.0f);
                    if (parts.length > 2) pitch = parseFloat(parts[2], 1.0f);
                }
            }
            content = rawLines.subList(1, rawLines.size());
        }

        List<String> colored = new ArrayList<>(content.size());
        for (String line : content) {
            colored.add(line.replace('&', '§'));
        }
        if (colored.isEmpty()) {
            colored = Collections.singletonList("");
        }

        return new RichMessage(type, fadeIn, stay, fadeOut, sound, volume, pitch, colored);
    }

    private static boolean isMetaLine(String line) {
        String trimmed = line.trim();
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) return false;
        return META_ATTR.matcher(trimmed).find();
    }

    private static Sound parseSound(String raw, Logger logger) {
        String cleaned = raw.trim();
        if (cleaned.isEmpty()) return null;

        String candidate = cleaned.replaceFirst("^minecraft:", "").replace('.', '_').toUpperCase(Locale.ROOT);
        try {
            return Sound.valueOf(candidate);
        } catch (IllegalArgumentException e) {
            try {
                return Sound.valueOf(cleaned.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e2) {
                if (logger != null) {
                    logger.warning("Unknown sound in lang file: " + raw);
                }
                return null;
            }
        }
    }

    private static float parseFloat(String raw, float fallback) {
        try {
            return Float.parseFloat(raw.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    @SuppressWarnings("deprecation")
    public void send(Player p) {
        if (p == null || !p.isOnline()) return;

        switch (type) {
            case "actionbar":
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(lines.get(0)));
                break;
            case "title":
                String main = lines.get(0);
                String subtitle = lines.size() > 1 ? lines.get(1) : "";
                p.sendTitle(main, subtitle, fadeInTicks, stayTicks, fadeOutTicks);
                break;
            case "chat":
            default:
                for (String line : lines) {
                    p.sendMessage(line);
                }
                break;
        }

        if (sound != null) {
            p.playSound(p.getLocation(), sound, volume, pitch);
        }
    }

    public String toPlainText() {
        return String.join("\n", lines);
    }
}

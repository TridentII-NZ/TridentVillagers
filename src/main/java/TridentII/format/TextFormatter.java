package TridentII.format;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class TextFormatter {

    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("(?i)&#([a-f0-9]{6})");
    private static final Pattern LEGACY_HEX_PATTERN = Pattern.compile("(?i)&x(&[a-f0-9]){6}");
    private static final Map<Character, String> LEGACY_TAGS = Map.ofEntries(
        Map.entry('0', "black"),
        Map.entry('1', "dark_blue"),
        Map.entry('2', "dark_green"),
        Map.entry('3', "dark_aqua"),
        Map.entry('4', "dark_red"),
        Map.entry('5', "dark_purple"),
        Map.entry('6', "gold"),
        Map.entry('7', "gray"),
        Map.entry('8', "dark_gray"),
        Map.entry('9', "blue"),
        Map.entry('a', "green"),
        Map.entry('b', "aqua"),
        Map.entry('c', "red"),
        Map.entry('d', "light_purple"),
        Map.entry('e', "yellow"),
        Map.entry('f', "white"),
        Map.entry('k', "obfuscated"),
        Map.entry('l', "bold"),
        Map.entry('m', "strikethrough"),
        Map.entry('n', "underlined"),
        Map.entry('o', "italic"),
        Map.entry('r', "reset")
    );

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public Component format(String input) {
        if (input == null || input.isBlank()) {
            return Component.empty();
        }

        return miniMessage.deserialize(legacyToMiniMessage(input));
    }

    public String legacyToMiniMessage(String input) {
        String converted = replaceLegacyHexColors(replaceHexColors(input));
        StringBuilder builder = new StringBuilder(converted.length());

        for (int index = 0; index < converted.length(); index++) {
            char current = converted.charAt(index);
            if (current == '&' && index + 1 < converted.length()) {
                String tag = LEGACY_TAGS.get(Character.toLowerCase(converted.charAt(index + 1)));
                if (tag != null) {
                    builder.append('<').append(tag).append('>');
                    index++;
                    continue;
                }
            }

            builder.append(current);
        }

        return builder.toString();
    }

    private String replaceHexColors(String input) {
        Matcher matcher = HEX_COLOR_PATTERN.matcher(input);
        StringBuilder builder = new StringBuilder(input.length());

        while (matcher.find()) {
            matcher.appendReplacement(builder, "<#" + matcher.group(1) + ">");
        }

        matcher.appendTail(builder);
        return builder.toString();
    }

    private String replaceLegacyHexColors(String input) {
        Matcher matcher = LEGACY_HEX_PATTERN.matcher(input);
        StringBuilder builder = new StringBuilder(input.length());

        while (matcher.find()) {
            String hex = matcher.group().replace("&x", "").replace("&", "");
            matcher.appendReplacement(builder, "<#" + hex + ">");
        }

        matcher.appendTail(builder);
        return builder.toString();
    }
}

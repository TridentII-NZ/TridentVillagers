package TridentII.format;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ChatColour {

    private static final char COLOR_CHAR = '\u00A7';
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("(?i)&#([a-f0-9]{6})");
    private static final Pattern LEGACY_HEX_PATTERN = Pattern.compile("(?i)&x(&[a-f0-9]){6}");
    private static final Pattern ALT_COLOR_PATTERN = Pattern.compile("(?i)&([0-9a-fk-or])");

    private ChatColour() {
    }

    public static String colorize(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }

        return translateLegacyColors(replaceLegacyHexColors(replaceHexColors(input)));
    }

    static String hexColour(String hex) {
        String normalized = hex.startsWith("#") ? hex.substring(1) : hex;
        StringBuilder builder = new StringBuilder(14);
        builder.append(COLOR_CHAR).append('x');

        for (int index = 0; index < normalized.length(); index++) {
            builder.append(COLOR_CHAR).append(normalized.charAt(index));
        }

        return builder.toString();
    }

    private static String replaceHexColors(String input) {
        Matcher matcher = HEX_COLOR_PATTERN.matcher(input);
        StringBuilder builder = new StringBuilder(input.length());

        while (matcher.find()) {
            matcher.appendReplacement(builder, Matcher.quoteReplacement(hexColour(matcher.group(1))));
        }

        matcher.appendTail(builder);
        return builder.toString();
    }

    private static String replaceLegacyHexColors(String input) {
        Matcher matcher = LEGACY_HEX_PATTERN.matcher(input);
        StringBuilder builder = new StringBuilder(input.length());

        while (matcher.find()) {
            String hex = matcher.group().replace("&x", "").replace("&", "");
            matcher.appendReplacement(builder, Matcher.quoteReplacement(hexColour(hex)));
        }

        matcher.appendTail(builder);
        return builder.toString();
    }

    private static String translateLegacyColors(String input) {
        Matcher matcher = ALT_COLOR_PATTERN.matcher(input);
        StringBuilder builder = new StringBuilder(input.length());

        while (matcher.find()) {
            matcher.appendReplacement(builder, Matcher.quoteReplacement(COLOR_CHAR + matcher.group(1).toLowerCase()));
        }

        matcher.appendTail(builder);
        return builder.toString();
    }
}

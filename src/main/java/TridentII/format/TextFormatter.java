package TridentII.format;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextFormatter {

    private static final Pattern SQUARE_GRADIENT_PATTERN = Pattern.compile(
        "(?is)\\[gradient:(#[a-f0-9]{6}):(#[a-f0-9]{6})](.*?)\\[/gradient]"
    );

    public String format(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }

        return ChatColour.colorize(applyGradients(input));
    }

    private String applyGradients(String input) {
        return replaceGradients(input, SQUARE_GRADIENT_PATTERN);
    }

    private String replaceGradients(String input, Pattern pattern) {
        Matcher matcher = pattern.matcher(input);
        StringBuilder builder = new StringBuilder(input.length());

        while (matcher.find()) {
            String replacement = Gradient.apply(matcher.group(3), matcher.group(1), matcher.group(2));
            matcher.appendReplacement(builder, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(builder);
        return builder.toString();
    }
}

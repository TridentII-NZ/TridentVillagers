package TridentII.format;

public final class Gradient {

    private Gradient() {
    }

    public static String apply(String input, String startHex, String endHex) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        int[] start = rgb(startHex);
        int[] end = rgb(endHex);
        int denominator = Math.max(1, input.length() - 1);
        StringBuilder builder = new StringBuilder(input.length() * 15);

        for (int index = 0; index < input.length(); index++) {
            double ratio = (double) index / denominator;
            int red = interpolate(start[0], end[0], ratio);
            int green = interpolate(start[1], end[1], ratio);
            int blue = interpolate(start[2], end[2], ratio);
            builder.append(ChatColour.hexColour(String.format("%02x%02x%02x", red, green, blue)));
            builder.append(input.charAt(index));
        }

        return builder.toString();
    }

    private static int[] rgb(String hex) {
        String normalized = hex.startsWith("#") ? hex.substring(1) : hex;
        return new int[] {
            Integer.parseInt(normalized.substring(0, 2), 16),
            Integer.parseInt(normalized.substring(2, 4), 16),
            Integer.parseInt(normalized.substring(4, 6), 16)
        };
    }

    private static int interpolate(int start, int end, double ratio) {
        return (int) Math.round(start + ((end - start) * ratio));
    }
}

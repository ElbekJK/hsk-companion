package com.example.hskcompanion;

import android.content.Context;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/** Optional, build-time configured public funding link. No credentials or payment SDK. */
public final class DonationLinks {
    private DonationLinks() {}

    public static String validatedUrl(String url) {
        if (url == null) return "";
        String candidate = url.trim();
        if (candidate.length() < 12 || candidate.length() > 500) return "";
        if (candidate.indexOf('\n') >= 0 || candidate.indexOf('\r') >= 0) return "";
        try {
            URI parsed = new URI(candidate);
            if (!"https".equalsIgnoreCase(parsed.getScheme()) || parsed.getHost() == null ||
                parsed.getHost().isEmpty() || parsed.getRawUserInfo() != null ||
                parsed.getPort() != -1) return "";
            return candidate;
        } catch (Exception ignored) {
            return "";
        }
    }

    public static String configuredUrl(Context context) {
        try (InputStream stream = context.getAssets().open("donation_url.txt")) {
            byte[] raw = new byte[1024];
            int read = stream.read(raw);
            if (read <= 0) return "";
            String text = new String(raw, 0, read, StandardCharsets.UTF_8);
            return validatedUrl(text);
        } catch (Exception ignored) {
            return "";
        }
    }
}

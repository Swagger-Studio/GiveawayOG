package com.rocketdev.oggiveaway.utils;



import com.rocketdev.oggiveaway.OGGiveaway;
import com.rocketdev.oggiveaway.config.WebhookConfig;
import org.bukkit.Bukkit;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class DiscordWebhook {

    public static void send(String type, String prizeName, String playerName, int participantCount) {

        if (!WebhookConfig.get().getBoolean("webhook-settings.enabled")) return;

        String urlString = WebhookConfig.get().getString("webhook-settings.url");
        if (urlString == null || urlString.contains("REPLACE")) return;


        String path = "messages." + type + ".";
        String title = WebhookConfig.get().getString(path + "title", "Giveaway Notification");
        String description = WebhookConfig.get().getString(path + "description", "Event updated")
                .replace("%prize%", prizeName != null ? prizeName : "Unknown")
                .replace("%player%", playerName != null ? playerName : "Unknown")
                .replace("\\n", "\n");

        int color = WebhookConfig.get().getInt(path + "color", 5763719);


        Bukkit.getScheduler().runTaskAsynchronously(OGGiveaway.getInstance(), () -> {
            try {

                StringBuilder json = new StringBuilder();
                json.append("{");
                json.append("\"embeds\": [{");


                json.append("\"title\": \"").append(escape(title)).append("\",");
                json.append("\"description\": \"").append(escape(description)).append("\",");
                json.append("\"color\": ").append(color).append(",");
                json.append("\"timestamp\": \"").append(Instant.now().toString()).append("\",");


                json.append("\"footer\": {");
                json.append("\"text\": \"Powered by Swagger Studio\"");
                json.append("},");


                if (playerName != null) {
                    json.append("\"thumbnail\": {");
                    json.append("\"url\": \"https://minotar.net/helm/").append(playerName).append("/100.png\"");
                    json.append("},");
                }


                if (participantCount >= 0) {
                    json.append("\"fields\": [");
                    json.append("{");
                    json.append("\"name\": \"👥 Participants\",");
                    json.append("\"value\": \"").append(participantCount).append("\",");
                    json.append("\"inline\": true");
                    json.append("}");
                    json.append("]");
                }

                json.append("}]");
                json.append("}");


                URL url = new URL(urlString);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setDoOutput(true);

                try (OutputStream os = con.getOutputStream()) {
                    byte[] input = json.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                con.getResponseCode();
                con.disconnect();

            } catch (Exception e) {

                Bukkit.getLogger().warning("[GiveawayOG] Failed to send Webhook: " + e.getMessage());
            }
        });
    }


    private static String escape(String text) {
        return text.replace("\"", "\\\"").replace("\n", "\\n");
    }
}
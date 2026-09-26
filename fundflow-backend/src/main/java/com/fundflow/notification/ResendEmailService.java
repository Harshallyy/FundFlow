package com.fundflow.notification;

import com.fundflow.entity.Campaign;
import com.fundflow.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResendEmailService {

    private final RestClient.Builder restClientBuilder;

    @Value("${app.email.resend-api-key:}")
    private String apiKey;

    @Value("${app.email.from:}")
    private String fromEmail;

    public void sendCampaignPendingReviewEmail(User admin, Campaign campaign) {
        if (apiKey.isBlank() || fromEmail.isBlank()) {
            log.warn("Skipping campaign review email because Resend is not configured");
            return;
        }

        String campaignTitle = campaign.getTitle();
        String organizerName = campaign.getOrganizer().getFullName();
        String organizerEmail = campaign.getOrganizer().getEmail();
        String html = "<h2>FundFlow campaign review</h2>"
                + "<p>A campaign has been submitted and is now pending admin review.</p>"
                + "<p><strong>Campaign:</strong> " + escapeHtml(campaignTitle) + "<br>"
                + "<strong>Organizer:</strong> " + escapeHtml(organizerName) + " ("
                + escapeHtml(organizerEmail) + ")</p>"
                + "<p>Please sign in to FundFlow to review this campaign.</p>";

        try {
            restClientBuilder.build()
                    .post()
                    .uri("https://api.resend.com/emails")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "from", fromEmail,
                            "to", new String[] { admin.getEmail() },
                            "subject", "FundFlow campaign pending review: " + campaignTitle,
                            "html", html))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception exception) {
            log.warn("Could not send campaign review email to {} for campaign {}: {}",
                    admin.getEmail(), campaign.getId(), exception.getMessage());
        }
    }

    private String escapeHtml(String value) {
        return value == null ? ""
                : value.replace("&", "&amp;")
                        .replace("<", "&lt;")
                        .replace(">", "&gt;")
                        .replace("\"", "&quot;")
                        .replace("'", "&#39;");
    }
}

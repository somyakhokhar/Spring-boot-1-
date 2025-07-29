package com.example.javaqualifier;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.MediaType;

@Component
public class StartupService {

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        // Participant details - replace with your actual info
        String name = "John Doe";
        String regNo = "REG12347";
        String email = "john@example.com";

        // Prepare JSON body for the initial API call
        String requestBody = String.format("{\"name\":\"%s\",\"regNo\":\"%s\",\"email\":\"%s\"}", name, regNo, email);

        WebClient client = WebClient.create();

        // 1. Call generateWebhook API to get webhook URL and JWT token
        GenerateWebhookResponse resp = client.post()
            .uri("https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(GenerateWebhookResponse.class)
            .block();

        if (resp == null) {
            throw new RuntimeException("Failed to get webhook response from server.");
        }

        // 2. Determine which SQL question you have to solve
        int lastTwoDigits = Integer.parseInt(regNo.substring(regNo.length() - 2));
        String finalQuery;

        if (lastTwoDigits % 2 == 1) {
            // Odd → solve Question 1
            finalQuery = """
                -- Replace below SQL with your solution for Question 1
                SELECT e.emp_id, e.emp_name, d.dept_name
                FROM employees e
                JOIN departments d ON e.dept_id = d.dept_id
                WHERE e.salary > 50000;
                """;
        } else {
            // Even → solve Question 2
            finalQuery = """
                -- Replace below SQL with your solution for Question 2
                SELECT customer_id, order_date, total_amount
                FROM orders
                WHERE order_status = 'Completed';
                """;
        }

        // 3. Submit the SQL query answer to the webhook endpoint
        // Clean JSON escaping of query:
        String sanitizedQuery = finalQuery.replace("\"", "\\\"").replace("\n", "\\n");

        String submitBody = String.format("{\"finalQuery\": \"%s\"}", sanitizedQuery);

        String result = client.post()
            .uri(resp.getWebhook())
            .header("Authorization", resp.getAccessToken())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(submitBody)
            .retrieve()
            .bodyToMono(String.class)
            .block();

        System.out.println("Submission complete. Server response:");
        System.out.println(result);
    }

    // Class to map the response from generateWebhook API
    static class GenerateWebhookResponse {
        private String webhook;
        private String accessToken;

        public String getWebhook() {
            return webhook;
        }

        public void setWebhook(String webhook) {
            this.webhook = webhook;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }
    }
}

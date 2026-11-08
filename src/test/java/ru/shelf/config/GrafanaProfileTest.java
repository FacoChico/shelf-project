package ru.shelf.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalManagementPort;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        properties = {"management.server.port=0", "management.tracing.export.enabled=false"})
@ActiveProfiles({"test", "grafana"})
class GrafanaProfileTest {
    @LocalServerPort private int applicationPort;
    @LocalManagementPort private int managementPort;

    @Test
    void shouldExposeJvmMetricsWhenScrapedOnManagementPort() {
        final var response = get(managementPort, "/actuator/prometheus");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body())
                .contains("jvm_memory_used_bytes", "application=\"shelf-project\"");
    }

    @Test
    void shouldReportUpWhenHealthIsCheckedOnManagementPort() {
        final var response = get(managementPort, "/actuator/health");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"status\":\"UP\"");
    }

    @Test
    void shouldHideMetricsWhenScrapedOnApplicationPort() {
        final var response = get(applicationPort, "/actuator/prometheus");

        assertThat(response.statusCode()).isNotEqualTo(200);
        assertThat(response.body()).doesNotContain("jvm_memory_used_bytes");
    }

    @Test
    void shouldRedirectToLoginWhenGuestOpensShelf() {
        final var response = get(applicationPort, "/shelf");

        assertThat(response.statusCode()).isEqualTo(302);
        assertThat(response.headers().firstValue("Location"))
                .hasValueSatisfying(location -> assertThat(location).endsWith("/login"));
    }

    private static HttpResponse<String> get(int port, String path) {
        final var request =
                HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path)).build();

        try (final var client = HttpClient.newHttpClient()) {
            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).join();
        }
    }
}

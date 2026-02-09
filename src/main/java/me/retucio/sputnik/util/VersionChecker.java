package me.retucio.sputnik.util;

import me.retucio.sputnik.Sputnik;
import net.fabricmc.loader.api.FabricLoader;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class VersionChecker {

    private static final URI PROPERTIES_URL = getFileURI();

    public static boolean updateAvailable = false;
    public static String latestRemoteVersion = null;
    public static String currentVersion = Sputnik.MOD_VERSION;

    public static boolean shouldShowScreen = false;

    public static void check() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(PROPERTIES_URL)
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String gradleProperties = response.body();
                latestRemoteVersion = extract(gradleProperties);

                if (latestRemoteVersion != null) {
                    String remoteVersion = (latestRemoteVersion);
                    String localVersion = cleanVersion(currentVersion);

                    if (!remoteVersion.equals(localVersion)) {
                        updateAvailable = true;
                        Sputnik.LOGGER.info("actualización disponible. local: {}, más reciente: {}",
                                currentVersion, latestRemoteVersion);
                        shouldShowScreen = true;
                    } else {
                        Sputnik.LOGGER.info("el mod está al día: v{}", currentVersion);
                    }
                }
            } else {
                Sputnik.LOGGER.error("no se pudo comprobar la versión más reciente");
            }
        } catch (Exception e) {
            Sputnik.LOGGER.warn("no se pudo comprobar si el mod está actualizado: {}", e.getMessage());
        }
    }

    public static String extract(String file) {
        try {
            String[] lines = file.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("mod_version")) {
                    String[] parts = line.split("=");
                    if (parts.length > 1) {
                        String version = parts[1].trim();
                        version = version.replace("\"", "");
                        version = version.replace("'", "");
                        return version;
                    }
                }
            }
        } catch (Exception e) {
            Sputnik.LOGGER.error("no se pudo parsear el archivo gradle.properties: {}", e.getMessage());
        }
        return null;
    }

    public static String cleanVersion(String version) {
        if (version == null) return "";
        version = version.replaceFirst("^v", "");
        version = version.replaceFirst("^V", "");
        version = version.split("\\+")[0];
        version = version.split("-")[0];
        return version.trim();
    }


    private static URI getFileURI() {
        try {
            String sources = FabricLoader.getInstance()
                    .getModContainer(Sputnik.MOD_ID)
                    .orElseThrow()
                    .getMetadata()
                    .getContact()
                    .get("sources")
                    .orElse(null);

            if (sources != null && sources.contains("github.com")) {
                String rawUrl = sources
                        .replace("https://github.com", "https://raw.githubusercontent.com")
                        + "/master/gradle.properties";
                return URI.create(rawUrl);
            }
        } catch (Exception e) {
            Sputnik.LOGGER.warn("no se pudo construir gradle.properties desde la metadata", e);
        }
        return null;
    }

    public static String getLatestVersion() {
        return latestRemoteVersion;
    }
}

package dev.eduplay.services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import dev.eduplay.entities.Seance;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.*;

public class GoogleCalendarService {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = List.of(CalendarScopes.CALENDAR);
    private static final String APPLICATION_NAME = "EduPlay Admin Calendar";
    private static final ZoneId ZONE_ID = ZoneId.systemDefault();

    private final File credentialsFile;
    private final File tokensDirectory;

    public GoogleCalendarService(File credentialsFile) {
        this.credentialsFile = Objects.requireNonNull(credentialsFile, "credentialsFile is required");
        this.tokensDirectory = new File(System.getProperty("user.home"), ".eduplay/google-tokens");
    }

    public SyncResult syncSeances(List<Seance> seances, Map<Integer, String> courseTitles) throws Exception {
        Calendar client = buildCalendarClient();

        int created = 0;
        int updated = 0;
        int skipped = 0;

        for (Seance seance : seances) {
            if (seance == null || seance.getStartTime() == null || seance.getEndTime() == null) {
                skipped++;
                continue;
            }
            if (!seance.getEndTime().isAfter(seance.getStartTime())) {
                skipped++;
                continue;
            }

            String eventId = "eduplay-seance-" + seance.getId();
            Event payload = buildEvent(seance, courseTitles);

            Event existing = null;
            try {
                existing = client.events().get("primary", eventId).execute();
            } catch (com.google.api.client.googleapis.json.GoogleJsonResponseException e) {
                if (e.getStatusCode() != 404) {
                    throw e;
                }
            }

            if (existing == null) {
                payload.setId(eventId);
                client.events().insert("primary", payload).execute();
                created++;
            } else {
                payload.setId(eventId);
                client.events().update("primary", eventId, payload).execute();
                updated++;
            }
        }

        return new SyncResult(created, updated, skipped);
    }

    public List<Event> listExistingSeanceEvents() throws Exception {
        Calendar client = buildCalendarClient();
        Events events = client.events()
                .list("primary")
                .setQ("EduPlay")
                .setMaxResults(250)
                .execute();
        if (events.getItems() == null) return List.of();
        return events.getItems();
    }

    private Event buildEvent(Seance seance, Map<Integer, String> courseTitles) {
        String courseTitle = courseTitles != null
                ? courseTitles.getOrDefault(seance.getCourseId(), "Cours #" + seance.getCourseId())
                : "Cours #" + seance.getCourseId();
        String title = seance.getTitle() == null || seance.getTitle().isBlank()
                ? "Séance EduPlay"
                : seance.getTitle();

        Event event = new Event();
        event.setSummary(title + " (" + courseTitle + ")");
        event.setLocation(emptyToNull(seance.getLocation()));
        event.setDescription(buildDescription(seance, courseTitle));

        DateTime start = new DateTime(seance.getStartTime().atZone(ZONE_ID).toInstant().toEpochMilli());
        DateTime end = new DateTime(seance.getEndTime().atZone(ZONE_ID).toInstant().toEpochMilli());
        event.setStart(new EventDateTime().setDateTime(start).setTimeZone(ZONE_ID.getId()));
        event.setEnd(new EventDateTime().setDateTime(end).setTimeZone(ZONE_ID.getId()));

        Map<String, String> privateProps = new HashMap<>();
        privateProps.put("eduplaySeanceId", String.valueOf(seance.getId()));
        privateProps.put("eduplayCourseId", String.valueOf(seance.getCourseId()));
        event.setExtendedProperties(new Event.ExtendedProperties().setPrivate(privateProps));
        return event;
    }

    private static String buildDescription(Seance seance, String courseTitle) {
        StringBuilder sb = new StringBuilder();
        sb.append("Séance synchronisée depuis EduPlay.\n");
        sb.append("Cours: ").append(courseTitle).append('\n');
        sb.append("Statut: ").append(emptyToDefault(seance.getStatus(), "scheduled")).append('\n');
        if (seance.getDescription() != null && !seance.getDescription().isBlank()) {
            sb.append('\n').append(seance.getDescription().trim());
        }
        return sb.toString();
    }

    private Calendar buildCalendarClient() throws Exception {
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = getCredentials(httpTransport);
        return new Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private Credential getCredentials(NetHttpTransport httpTransport) throws Exception {
        if (!credentialsFile.exists() || !credentialsFile.isFile()) {
            throw new IllegalStateException(
                    "Fichier credentials Google introuvable: " + credentialsFile.getAbsolutePath()
            );
        }
        if (!tokensDirectory.exists() && !tokensDirectory.mkdirs()) {
            throw new IllegalStateException(
                    "Impossible de créer le dossier de tokens: " + tokensDirectory.getAbsolutePath()
            );
        }

        GoogleClientSecrets clientSecrets;
        try (InputStreamReader reader = new InputStreamReader(
                new FileInputStream(credentialsFile), StandardCharsets.UTF_8)) {
            clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, reader);
        }

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(tokensDirectory))
                .setAccessType("offline")
                .build();

        // OAuth callback: http://localhost:8888/Callback
        LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                .setHost("localhost")
                .setPort(8888)
                .build();

        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("admin");
    }

    private static String emptyToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private static String emptyToDefault(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value.trim();
    }

    public record SyncResult(int createdCount, int updatedCount, int skippedCount) {}
}

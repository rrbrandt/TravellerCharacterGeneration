package com.javasmyths.highguard;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/naval-records")
public class NavalCareerController {
    private static final int MAX_BYTES = 128 * 1024;
    private static final int MAX_RECORDS = 2000;
    private static final Pattern ID = Pattern.compile("[a-f0-9]{8}(?:-[a-f0-9]{4}){3}-[a-f0-9]{12}");
    private final Path directory;
    private final ObjectMapper mapper;

    public NavalCareerController(@Value("${highguard.storage-dir}") String storage, ObjectMapper mapper) {
        this.directory = Paths.get(storage).toAbsolutePath().normalize().resolve("naval-careers");
        this.mapper = mapper;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, headers = "X-High-Guard-Action=save")
    public synchronized ResponseEntity<Map<String, String>> save(HttpServletRequest request) throws IOException {
        byte[] bytes = request.getInputStream().readNBytes(MAX_BYTES + 1);
        if (bytes.length > MAX_BYTES) throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE);
        validate(bytes);
        Files.createDirectories(directory);
        try (Stream<Path> records = Files.list(directory)) {
            if (records.filter(path -> path.getFileName().toString().endsWith(".json"))
                    .limit(MAX_RECORDS).count() >= MAX_RECORDS) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Naval archive is full.");
            }
        }
        String id = UUID.randomUUID().toString();
        Path pending = Files.createTempFile(directory, ".career-", ".tmp");
        try {
            Files.write(pending, bytes);
            Files.move(pending, directory.resolve(id + ".json"), StandardCopyOption.ATOMIC_MOVE);
        } finally {
            Files.deleteIfExists(pending);
        }
        return ResponseEntity.status(HttpStatus.CREATED).cacheControl(CacheControl.noStore())
            .body(Map.of("id", id));
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> read(@PathVariable String id) throws IOException {
        if (!ID.matcher(id).matches()) return ResponseEntity.notFound().build();
        try {
            byte[] bytes = Files.readAllBytes(directory.resolve(id + ".json"));
            return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .header("X-Content-Type-Options", "nosniff").contentType(MediaType.APPLICATION_JSON).body(bytes);
        } catch (NoSuchFileException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private void validate(byte[] bytes) throws IOException {
        JsonNode record;
        try (JsonParser parser = mapper.getFactory().createParser(bytes)) {
            int depth = 0;
            for (JsonToken token = parser.nextToken(); token != null; token = parser.nextToken()) {
                if (token.isStructStart() && ++depth > 5) throw invalid();
                if (token.isStructEnd()) depth--;
            }
            record = mapper.reader().with(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                .readTree(mapper.getFactory().createParser(bytes));
        } catch (JsonProcessingException e) {
            throw invalid();
        }
        if (record == null || !record.isObject() || !record.path("schemaVersion").isIntegralNumber()
                || record.path("schemaVersion").asInt() != 1 || !"CT-HG1980".equals(record.path("ruleset").asText())
                || !text(record.path("name"), 70) || !text(record.path("status"), 70)
                || !text(record.path("upp"), 40) || !text(record.path("initialUpp"), 6)) throw invalid();
        for (String key : new String[]{"age", "years", "terms", "credits", "pension"}) {
            if (!record.path(key).isIntegralNumber() || !record.path(key).canConvertToInt()) throw invalid();
        }
        for (String key : new String[]{"navy", "branch", "rank"}) {
            JsonNode value = record.path(key);
            if (!value.isNull() && !text(value, 80)) throw invalid();
        }
        JsonNode stats = record.path("stats");
        if (!stats.isArray() || stats.size() != 6) throw invalid();
        for (JsonNode stat : stats) if (!stat.isIntegralNumber() || !stat.canConvertToInt()) throw invalid();
        for (String key : new String[]{"skills", "medals"}) {
            JsonNode counts = record.path(key);
            if (!counts.isObject() || counts.size() > 100) throw invalid();
            for (JsonNode value : counts) if (!value.isIntegralNumber() || !value.canConvertToInt()) throw invalid();
        }
        strings(record.path("benefits"), 40);
        JsonNode log = record.path("log");
        if (!log.isArray() || log.isEmpty() || log.size() > 155) throw invalid();
        for (JsonNode entry : log) {
            if (!text(entry.path("title"), 120)) throw invalid();
            strings(entry.path("notes"), 150);
        }
    }

    private static void strings(JsonNode values, int limit) {
        if (!values.isArray() || values.size() > limit) throw invalid();
        for (JsonNode value : values) if (!text(value, 260)) throw invalid();
    }

    private static boolean text(JsonNode value, int limit) {
        return value.isTextual() && !value.textValue().isBlank() && value.textValue().length() <= limit;
    }

    private static ResponseStatusException invalid() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid High Guard career record.");
    }
}

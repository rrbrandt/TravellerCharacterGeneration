package com.javasmyths.TravellerCharacterGeneration.handlingformsubmission;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javasmyths.TravellerCharacterGeneration.model.*;
import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Versioned snapshots, stored outside the application WAR. No Java object deserialization. */
@Component
public class CharacterStore {
  public static class Saved {
    public int schemaVersion = 1;
    public String id, savedAt;
    public int activeStage;
    public TravellerCharacter character;
    public CareerProgress career;
    public String getName() { return character.getName() == null || character.getName().trim().isEmpty() ? "Unnamed Traveller" : character.getName(); }
    public boolean isComplete() { return career.isGenerationComplete(); }
    public String getSavedLabel() { return java.time.format.DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm 'UTC'")
        .withZone(java.time.ZoneOffset.UTC).format(Instant.parse(savedAt)); }
  }
  private final Path directory;
  private final ObjectMapper mapper = new ObjectMapper()
      .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
      .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
  public CharacterStore(@Value("${traveller.storage-dir:./data/characters}") String directory) {
    this.directory = Paths.get(directory).toAbsolutePath().normalize();
  }
  private Path path(String id) {
    if (id == null || !id.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"))
      throw new IllegalArgumentException("Invalid saved character ID");
    return directory.resolve(id + ".json");
  }
  public synchronized Saved save(String id, TravellerCharacter character, CareerProgress career, int active) throws IOException {
    Files.createDirectories(directory);
    Saved saved = new Saved(); saved.id = id == null ? UUID.randomUUID().toString() : id;
    saved.savedAt = Instant.now().toString(); saved.character = character; saved.career = career; saved.activeStage = active;
    Path destination = path(saved.id), temporary = Files.createTempFile(directory, ".saving-", ".tmp");
    try {
      byte[] bytes = mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(saved);
      try (java.nio.channels.FileChannel channel = java.nio.channels.FileChannel.open(temporary, StandardOpenOption.WRITE)) {
        java.nio.ByteBuffer buffer = java.nio.ByteBuffer.wrap(bytes);
        while (buffer.hasRemaining()) channel.write(buffer);
        channel.force(true);
      }
      try { Files.move(temporary, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
      catch (AtomicMoveNotSupportedException ex) { Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING); }
    } finally { Files.deleteIfExists(temporary); }
    return saved;
  }
  public synchronized Saved load(String id) throws IOException {
    Path file = path(id);
    if (Files.size(file) > 2_000_000) throw new IOException("Saved character exceeds supported size");
    Saved saved = mapper.readValue(file.toFile(), Saved.class);
    if (saved.schemaVersion != 1 || !id.equals(saved.id) || saved.character == null || saved.character.getCharacteristics() == null
        || saved.career == null || saved.activeStage < 0 || saved.activeStage > 5 || saved.savedAt == null)
      throw new IOException("Invalid or unsupported saved character");
    return saved;
  }
  public synchronized List<Saved> list() throws IOException {
    if (!Files.exists(directory)) return Collections.emptyList();
    List<Saved> saves = new ArrayList<>();
    try (java.util.stream.Stream<Path> paths = Files.list(directory)) {
      for (Path file : paths.filter(p -> p.getFileName().toString().matches("[0-9a-f-]{36}\\.json")).collect(Collectors.toList())) {
        String name = file.getFileName().toString(); saves.add(load(name.substring(0, 36)));
      }
    }
    saves.sort(Comparator.comparing((Saved s) -> s.savedAt).reversed());
    return saves;
  }
}

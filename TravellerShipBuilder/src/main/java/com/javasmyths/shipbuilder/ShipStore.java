package com.javasmyths.shipbuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
@Component
public class ShipStore {
 public static class Saved {public int schemaVersion=1;public String savedAt;public ShipDesign design;}
 private final Path directory;private final ObjectMapper mapper=new ObjectMapper();
 public ShipStore(@Value("${shipbuilder.storage-dir:./data/ships}") String path){directory=Paths.get(path).toAbsolutePath().normalize();}
 private Path path(String id){if(id==null||!id.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"))throw new IllegalArgumentException("Invalid ship ID");return directory.resolve(id+".json");}
 public synchronized Saved read(String id)throws IOException{
  Path p=path(id);if(Files.size(p)>1000000)throw new IOException("Ship file is too large");Saved s=mapper.readValue(p.toFile(),Saved.class);
  if(s.schemaVersion!=1||s.design==null||!id.equals(s.design.id)||s.savedAt==null)throw new IOException("Unsupported ship file");return s;
 }
 public synchronized Saved save(ShipDesign design)throws IOException{
  if(design.id==null||design.id.isEmpty())design.id=UUID.randomUUID().toString();else read(design.id);
  Files.createDirectories(directory);Path dest=path(design.id),temp=Files.createTempFile(directory,".saving-",".tmp");Saved s=new Saved();s.design=design;s.savedAt=Instant.now().toString();
  try{mapper.writerWithDefaultPrettyPrinter().writeValue(temp.toFile(),s);try(java.nio.channels.FileChannel f=java.nio.channels.FileChannel.open(temp,StandardOpenOption.WRITE)){f.force(true);}try{Files.move(temp,dest,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}catch(AtomicMoveNotSupportedException e){Files.move(temp,dest,StandardCopyOption.REPLACE_EXISTING);}}finally{Files.deleteIfExists(temp);}return s;
 }
 public synchronized List<Saved> list()throws IOException{
  List<Saved> all=new ArrayList<>();if(!Files.exists(directory))return all;try(java.util.stream.Stream<Path> paths=Files.list(directory)){for(Path p:(Iterable<Path>)paths.filter(f->f.getFileName().toString().matches("[0-9a-f-]{36}\\.json"))::iterator){String n=p.getFileName().toString();all.add(read(n.substring(0,36)));}}all.sort(Comparator.comparing((Saved s)->s.savedAt).reversed());return all;
 }
 public byte[] export(String id)throws IOException{return mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(read(id));}
}

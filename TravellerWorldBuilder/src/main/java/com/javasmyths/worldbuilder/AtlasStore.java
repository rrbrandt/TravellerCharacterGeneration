package com.javasmyths.worldbuilder;
import com.fasterxml.jackson.databind.*;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import java.nio.file.*;
import java.io.*;
import java.util.*;
@Component
public class AtlasStore {
 private final Path root;
 public final ObjectMapper mapper=new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
 public AtlasStore(@Value("${worldbuilder.storage-dir:./data/atlases}")String path)throws IOException{root=Paths.get(path).toAbsolutePath().normalize();Files.createDirectories(root);}
 public void validate(Atlas a){
  if(a==null||a.worlds==null||a.worlds.size()>80)throw new IllegalArgumentException("An atlas holds at most 80 worlds");
  a.name=Book3Rules.clean(a.name,80);a.seed=Book3Rules.clean(a.seed,80);a.notes=Book3Rules.clean(a.notes,8000);if(a.name.isEmpty())throw new IllegalArgumentException("Give the atlas a name");
  if(!Arrays.asList("world","subsector").contains(a.kind)||a.density<1||a.density>6||a.kind.equals("world")&&a.worlds.size()!=1)throw new IllegalArgumentException("Invalid atlas type or density");
  Set<String> hexes=new HashSet<>();for(Atlas.World w:a.worlds){Book3Rules.validate(w);if(!hexes.add(w.hex))throw new IllegalArgumentException("Two worlds cannot share a hex");}
 }
 private Path path(String id){if(id==null||!id.matches("[a-f0-9]{8}(?:-[a-f0-9]{4}){3}-[a-f0-9]{12}"))throw new IllegalArgumentException("Invalid atlas ID");return root.resolve(id+".json");}
 public synchronized Atlas save(Atlas a)throws IOException{
  validate(a);if(a.id==null||a.id.isBlank())a.id=UUID.randomUUID().toString();Path target=path(a.id);byte[] bytes=mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(a);if(bytes.length>1000000)throw new IllegalArgumentException("Atlas exceeds 1 MB");Path temp=Files.createTempFile(root,"atlas-",".tmp");try{Files.write(temp,bytes);try{Files.move(temp,target,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}catch(AtomicMoveNotSupportedException e){Files.move(temp,target,StandardCopyOption.REPLACE_EXISTING);}}finally{Files.deleteIfExists(temp);}return a;
 }
 public Atlas load(String id)throws IOException{Path p=path(id);if(Files.size(p)>1000000)throw new IOException("Atlas exceeds size limit");Atlas a=mapper.readValue(p.toFile(),Atlas.class);validate(a);return a;}
 public List<Map<String,Object>> list()throws IOException{
  List<Map<String,Object>> items=new ArrayList<>();try(DirectoryStream<Path> paths=Files.newDirectoryStream(root,"*.json")){for(Path p:paths){try{Atlas a=load(p.getFileName().toString().replace(".json",""));Map<String,Object> row=new LinkedHashMap<>();row.put("id",a.id);row.put("name",a.name);row.put("kind",a.kind);row.put("count",a.worlds.size());row.put("modified",Files.getLastModifiedTime(p).toString());items.add(row);}catch(IllegalArgumentException|IOException ignored){}}}items.sort(Comparator.comparing(x->x.get("modified").toString(),Comparator.reverseOrder()));return items;
 }
}

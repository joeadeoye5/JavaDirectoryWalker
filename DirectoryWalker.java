/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package directorywalker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Envy Software
 */
public class DirectoryWalker {
    private final String initialDirectory;
    private Path currentDirectory;
    DirectoryWalker(String initialDirectory){
        this.initialDirectory = initialDirectory;
        currentDirectory = Path.of(initialDirectory);
    }
    public String getInitialDirectory(){
        return initialDirectory;
    }
    public String getCurrentDirectory(){
        return currentDirectory.toAbsolutePath().toString();
    }
    public void createDirectory(){
    
    }
    public boolean createDirectory(String name){
        try {
            Files.createDirectory(currentDirectory.resolve(name));
            return true;
        } catch (IOException ex) {
            Logger.getLogger(DirectoryWalker.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    public boolean createDirectories(String... names){
        Path newPath = currentDirectory;
        for(String name : names){
            newPath = newPath.resolve(name);
        }
        try {
            Files.createDirectories(newPath);
            return true;
        } catch (IOException ex) {
            Logger.getLogger(DirectoryWalker.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    public String changeDirectory(String dir){
        if(dir.equals("..")){
            currentDirectory = currentDirectory.getParent();
            return currentDirectory.toAbsolutePath().toString();
        }else{
            currentDirectory = currentDirectory.resolve(dir);
            return currentDirectory.toAbsolutePath().toString();
        }
    }
    public List<String> listContents(){
        try (Stream<Path> contents = Files.walk(currentDirectory, 1)) {
            List<String> fileContents = contents
                    .map((path) -> {
                        if(path.compareTo(currentDirectory) != 0){
                            return path.toAbsolutePath().toString();
                        }
                        return ".";
                    })
                    .collect(Collectors.toList());
            fileContents.remove(".");
            return fileContents;

        } catch (IOException e) {
            return null;
        }
    }
    private List<String> getFilesByExt(String ext, int depth){
        try(Stream<Path> _stream = Files.find(currentDirectory, depth, (path, attr) -> {
            if(Files.isRegularFile(path)){
                if(path.toAbsolutePath().toString().endsWith(ext)){
                    return true;
                }
            }
            return false;
        })){
            return _stream.map((path)-> path.toAbsolutePath().toString())
                    .collect(Collectors.toList());
        }catch(IOException x){
            return null;
        }
    }
    public List<String> getFilesByExtension(String ext){
        return getFilesByExt(ext, 1);
    }
    public List<String> getFilesAllByExtension(String ext){
        return getFilesByExt(ext, Integer.MAX_VALUE);
    }
    public String printDirectory(){
        return printDirectory(false);
    }
    public String printDirectory(boolean returnValue){
        StringBuilder sb = new StringBuilder();
        sb.append("Directory of ")
                .append(currentDirectory.toAbsolutePath().toString())
                .append(System.lineSeparator())
                .append(System.lineSeparator());
        
        List<String> contents = listContents();
        int _files = 0, _dirs = 0;
        long _fileSize = 0;
        if(contents != null){
            for(String cont : contents){
                try{
                    File f = new File(cont);
                    boolean isDir = f.isDirectory();
                    if(isDir){
                        _dirs++;
                    }else{
                        _files++;
                        _fileSize += f.length();
                    }
                    long time_mod = f.lastModified();
                    LocalDateTime ldt = LocalDateTime.ofInstant(new Timestamp(time_mod).toInstant(), ZoneId.systemDefault());
                    String entry = "%tD %tT %5s %16s %s".formatted(ldt, ldt, 
                            isDir ? "<DIR>" : "", isDir ? "" : String.format("%,d",f.length()), f.getName());
                    sb.append(entry).append(System.lineSeparator());
                }catch(Exception x){}
            }
        }
        sb.append(String.format("%16s %s %16s %s", String.format("%,d", _files), "File(s)", 
                String.format("%,d", _fileSize), "bytes")).append(System.lineSeparator());
        sb.append(String.format("%16s %s %16s %s", String.format("%,d", _dirs), "Dir(s)", 
                String.format("%,d", currentDirectory.toFile().getFreeSpace()), "bytes free"));
        System.out.println(sb.toString());
        return returnValue ? sb.toString() : null;
    }
    public List<String> listContentsAll(){
        try(Stream<Path> str = Files.walk(currentDirectory, Integer.MAX_VALUE)){
            List<String> contents = str.map((path) -> {
                if(path.compareTo(currentDirectory) != 0){
                    return path.toAbsolutePath().toString();
                }
                return ".";
            }).collect(Collectors.toList());
            contents.remove(".");
            return contents;
        }catch(IOException e){
            return null;
        }
    }
    public long getSize(){
        try(Stream<Path> str = Files.walk(currentDirectory, Integer.MAX_VALUE)){
            List<Long> size = new ArrayList<>();
            str.map((path) -> {
                if(Files.isRegularFile(path)){
                    try {
                        size.add(Files.size(path));
                    } catch (IOException ex) {
                        
                    }
                }
                return path;
            }).collect(Collectors.toList());
            Iterator<Long> ite = size.iterator();
            long value = 0;
            while(ite.hasNext()){
                value += ite.next();
            }
            return value;
        }catch(IOException e){
            return 0;
        }
    }
    private List<String> getFilesOrFolders(boolean isFile){
        List<String> contents = listContents();
        if(contents == null){
            return null;
        }
        List<String> dirs = new ArrayList<>();
        List<String> fils = new ArrayList<>();
        for (String cont : contents){
            if(new File(cont).isDirectory()){
                dirs.add(cont);
            }else{
                fils.add(cont);
            }
        }
        if(isFile){
            return fils;
        }else{
            return dirs;
        }
    }
    public List<String> getSubdirectories(){
        return getFilesOrFolders(false);
    }
    public List<String> getFiles(){
        return getFilesOrFolders(true);
    }
    public String getParent(){
        return changeDirectory("..");
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        DirectoryWalker cmd = new DirectoryWalker("C:\\Users\\Envy Software\\Downloads");
        cmd.changeDirectory("compressed");
        cmd.printDirectory(false);
        cmd.changeDirectory("..");
        cmd.printDirectory();
        System.out.println(cmd.getSize());
    }
}

/*
 * Copyright Osiris Team
 * All rights reserved.
 *
 * This software is copyrighted work licensed under the terms of the
 * AutoPlug License.  Please consult the file "LICENSE" for details.
 */

package com.osiris.autoplug.client.managers;

import com.osiris.autoplug.client.utils.GD;
import com.osiris.autoplug.core.logger.AL;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Search & find files!
 * (Not efficient code. Rework needed!)
 */
public class FileManager {
    private File queryFile = null;
    private final List<File> queryFiles = new ArrayList<>();

    public void deleteOldPlugin(String pl_name) {
        String searchPattern = "*"+pl_name+"**.jar";
        //Find the file
        findFileInPluginsDir(searchPattern);
        //Delete the file
        if ( !queryFile.delete() ) {
            AL.warn(" [!] Couldn't remove old plugin jar at: " + queryFile.toPath() + " [!] ");
        }

    }

    /**
     * Gets all plugins located in the /plugins folder.
     * @return list of files.
     */
    public List<File> getAllPlugins() {
        String searchPattern = "*.jar";
        //Find the file
        findFilesInPluginsDir(searchPattern);
        return queryFiles;
    }

    //Finds all folders starting with "world" in main working dir
    public List<File> serverWorldsFolders() {
        String searchPattern = "*world*";
        //Find the files
        findFoldersInWorkingDir(searchPattern);
        //Return the results
        return queryFiles;

    }

    /**
     * Finds all files with another name than AutoPlug.jar in main working dir.
     * Only files not folders!
     */
    public List<File> serverFiles() {
        String searchPattern = "*";
        //Find the files
        findFilesInWorkingDir(searchPattern);
        //Return the results
        return queryFiles;

    }

    /**
     * Finds the server jar by its name.
     * @return server jar file.
     */
    public File serverJar(String server_jar_name) {
        String searchPattern = "*"+server_jar_name+"**.jar";
        //Find the file
        findJarFileInWorkingDir(searchPattern);
        //Return the result file
        return queryFile;
    }

    /**
     * Finds the first jar file with another name than AutoPlugLauncher.jar.
     * @return server jar file.
     */
    public File serverJar() {
        String searchPattern = "*.jar";
        //Find the file
        findJarFileInWorkingDir(searchPattern);
        //Return the result file
        return queryFile;
    }

    public List<File> getFilesFrom(String dirPath) throws IOException{
        Objects.requireNonNull(dirPath);
        return getFilesFrom(FileSystems.getDefault().getPath(dirPath));
    }

    public List<File> getFilesFrom(File dir) throws IOException{
        Objects.requireNonNull(dir);
        return getFilesFrom(dir.toPath());
    }

    /**
     * Depth is 1. All folders are ignored.
     * @param dirPath the directory path.
     * @return a list of files in this directory.
     * @throws IOException
     */
    public List<File> getFilesFrom(Path dirPath) throws IOException{
        Objects.requireNonNull(dirPath);
        List<File> list = new ArrayList<>();
        SimpleFileVisitor<Path> visitor = new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.SKIP_SUBTREE;
            }

            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                list.add(new File(path.toString()));
                return FileVisitResult.CONTINUE;
            }
        };
        Files.walkFileTree(dirPath, visitor);
        return list;
    }

    public List<File> getFoldersFrom(String dirPath) throws IOException{
        Objects.requireNonNull(dirPath);
        return getFoldersFrom(FileSystems.getDefault().getPath(dirPath));
    }

    public List<File> getFoldersFrom(File dir) throws IOException{
        Objects.requireNonNull(dir);
        return getFoldersFrom(dir.toPath());
    }

    /**
     * Depth is 1. All sub-folders are ignored.
     * @param dirPath the directory path.
     * @return a list of folders in this directory.
     * @throws IOException
     */
    public List<File> getFoldersFrom(Path dirPath) throws IOException{
        Objects.requireNonNull(dirPath);
        List<File> list = new ArrayList<>();
        SimpleFileVisitor<Path> visitor = new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs) throws IOException {
                list.add(new File(path.toString()));
                return FileVisitResult.SKIP_SUBTREE;
            }
        };
        Files.walkFileTree(dirPath, visitor);
        return list;
    }

    //Walks through files (skips AutoPlug.jar and all other subdirectories) and finds one jar
    private void findJarFileInWorkingDir(String searchPattern) {

        try{
            final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + searchPattern);

            Files.walkFileTree(GD.WORKING_DIR.toPath(), new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path path,
                                                 BasicFileAttributes attrs) throws IOException {

                    //Must match the query name, can't be same name as AutoPlug.jar and can't be a directory
                    final String fileName = path.getFileName().toString();
                    if (pathMatcher.matches(path.getFileName())
                            && !fileName.equals("AutoPlug.jar")
                            && !fileName.equals("AutoPlug-Launcher.jar")
                            && !fileName.equals("AutoPlug-Client.jar")
                            && !fileName.equals("AutoPlug-Plugin.jar")) {

                        AL.debug(this.getClass(), "Found server jar at: " + path);
                        queryFile = new File(path.toString());
                        return FileVisitResult.TERMINATE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {

                    if (!dir.toString().equals(GD.WORKING_DIR.toString()) && attrs.isDirectory()){
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    else{
                        return FileVisitResult.CONTINUE;
                    }


                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc)
                        throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });



        } catch (IOException e) {
            e.printStackTrace();
            AL.warn(" [!] Error: "+ e.getMessage() + " [!]");
        }

    }

    //Walks through files (skips AutoPlug.jar and all other subdirectories) and finds ALL files
    private void findFilesInWorkingDir(String searchPattern) {

        try{
            final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + searchPattern);

            Files.walkFileTree(GD.WORKING_DIR.toPath(), new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path path,
                                                 BasicFileAttributes attrs) throws IOException {

                    //Must match the query name, can't be same name as AutoPlug.jar and can't be a directory
                    final String fileName = path.getFileName().toString();
                    if (pathMatcher.matches(path.getFileName())
                            && !fileName.equals("AutoPlug.jar")
                            && !fileName.equals("AutoPlug-Launcher.jar")
                            && !fileName.equals("AutoPlug-Client.jar")
                            && !fileName.equals("AutoPlug-Plugin.jar")) {

                        AL.debug(this.getClass(), "Found file at: " + path);
                        //Adds files to list to return multiple files
                        queryFiles.add(new File(path.toString()));
                        return FileVisitResult.CONTINUE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {

                    if (!dir.toString().equals(GD.WORKING_DIR.toString()) && attrs.isDirectory()){
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    else{
                        return FileVisitResult.CONTINUE;
                    }


                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc)
                        throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });



        } catch (IOException e) {
            e.printStackTrace();
            AL.warn(" [!] Error: "+ e.getMessage() + " [!]");
        }

    }

    //Walks through files and finds ALL sub-folders in main working dir
    private void findFoldersInWorkingDir(String searchPattern) {

        try{
            final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + searchPattern);

            Files.walkFileTree(GD.WORKING_DIR.toPath(), new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path path,
                                                 BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {

                    //Skip subdirectories of non main worki dirs and non matching dirs
                    if (!dir.toString().equals(GD.WORKING_DIR.toString()) && attrs.isDirectory() && !pathMatcher.matches(dir.getFileName())){
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    else if(!dir.toString().equals(GD.WORKING_DIR.toString())){
                        //Adds folders to list
                        queryFiles.add(new File(dir.toString()));
                        return FileVisitResult.CONTINUE;
                    }
                    else {
                        return FileVisitResult.CONTINUE;
                    }


                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc)
                        throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });



        } catch (IOException e) {
            e.printStackTrace();
            AL.warn(" [!] Error: "+ e.getMessage() + " [!]");
        }

    }

    //Walks through files (skips all Plugin directories) and finds one jar
    private void findFileInPluginsDir(String searchPattern) {

        try{
            final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + searchPattern);

            Files.walkFileTree(GD.PLUGINS_DIR.toPath(), new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path path,
                                                 BasicFileAttributes attrs) throws IOException {

                    if (pathMatcher.matches(path.getFileName())) {

                        AL.debug(this.getClass(), "Found plugin jar at: " + path);
                        queryFile = new File(path.toString());
                        return FileVisitResult.TERMINATE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {

                    if (!dir.toString().equals(GD.PLUGINS_DIR.toString()) && attrs.isDirectory()){
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    else{
                        return FileVisitResult.CONTINUE;
                    }


                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc)
                        throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });



        } catch (IOException e) {
            e.printStackTrace();
            AL.warn(" [!] Error: "+ e.getMessage() + " [!]");
        }

    }

    //Walks through files (skips all Plugin directories)
    private void findFilesInPluginsDir(String searchPattern) {

        try{
            final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + searchPattern);

            Files.walkFileTree(GD.PLUGINS_DIR.toPath(), new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path path,
                                                 BasicFileAttributes attrs) throws IOException {

                    if (pathMatcher.matches(path.getFileName())) {

                        AL.debug(this.getClass(), "Found plugin jar at: " + path);
                        queryFile = new File(path.toString());
                        queryFiles.add(queryFile);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {

                    if (!dir.toString().equals(GD.PLUGINS_DIR.toString()) && attrs.isDirectory()){
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    else{
                        return FileVisitResult.CONTINUE;
                    }


                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc)
                        throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });



        } catch (IOException e) {
            e.printStackTrace();
            AL.warn(" [!] Error: "+ e.getMessage() + " [!]");
        }

    }


}

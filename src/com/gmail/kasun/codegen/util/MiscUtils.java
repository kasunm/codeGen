package com.gmail.kasun.codegen.util;



import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>Title         : ${FILE_NAME}
 * <p>Project       : SpanCodeGenerator
 * <p>Description   :
 *
 * @author Kasun Madurasinghe
 * @version 1.0
 */
public class MiscUtils {

    /**
     * Execute shell command
     * @param command String
     * @return Boolean
     */
    public static Boolean executeShellCommand(String command, String directory){
        ProcessBuilder processBuilder = new ProcessBuilder();
        if(directory != null && directory.length() > 0) processBuilder.directory(new File(directory));
        // Windows
        processBuilder.command("cmd.exe", "/c", command);

        try {

            Process process = processBuilder.start();

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            int exitCode = process.waitFor();
            System.out.println("\nExited with error code : " + exitCode);
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void createAngularProject(String baseDirectory, String name){
        MiscUtils.executeShellCommand("ng new " + name + " --style=scss --routing=true", baseDirectory);
        MiscUtils.executeShellCommand("npm install bootstrap ngx-bootstrap --save" , baseDirectory + File.separator + name);
    }

    public static void addAngularComponent(String baseDirectory, String name){
        MiscUtils.executeShellCommand("ng g c " + name, baseDirectory);
    }

    public static void addAngularService(String baseDirectory, String name, Boolean flat){
        MiscUtils.executeShellCommand("ng g s " + name +  (flat ? " --flat" : ""), baseDirectory);
    }

    /**
     * Create directory structure
     * @param path String
     */
    public static void createDirectories(String path){
        File directory = new File(path);
        directory.getParentFile().mkdirs();
    }

    public static String splitCamelCase(String s, String replacement) {
        return s.replaceAll(
                String.format("%s|%s|%s",
                        "(?<=[A-Z])(?=[A-Z][a-z])",
                        "(?<=[^A-Z])(?=[A-Z])",
                        "(?<=[A-Za-z])(?=[^A-Za-z])"
                ),
                replacement
        );
    }

    /**
     * Based on reflection add all string attributes to map with it's value.put("attribute", attribute)
     * @param tokens
     */
    public static void addClassStringAttributes(Object object, Map<String,String> tokens){
        Class  classType = object.getClass();
        Field[] fields = classType.getDeclaredFields();
        for(int i = 0; i < fields.length; i++){
            Field field = fields[i];
            if(field.getType() == String.class ){
                //public string type
                try {
                    field.setAccessible(true);
                    String value = String.valueOf(field.get(object));
                    //Add value to map if not empty
                    if(!StringUtils.isEmpty(value)) tokens.put(field.getName(), value);
                } catch (IllegalAccessException e) {

                }
            }
        }
    }


}

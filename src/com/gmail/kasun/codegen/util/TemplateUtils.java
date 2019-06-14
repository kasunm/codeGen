package com.gmail.kasun.codegen.util;

import com.gmail.kasun.codegen.model.Settings;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Title         : ${FILE_NAME}
 * <p>Project       : SpanCodeGenerator
 * <p>Description   :
 *
 * @author Kasun Madurasinghe
 * @version 1.0
 */
public class TemplateUtils {
    final String variableStart = "${";
    final String variableEnd = "}";

    private static volatile  TemplateUtils instance = new TemplateUtils();

    private TemplateUtils(){}

    public static TemplateUtils getInstance(){
        return instance;
    }

    public boolean generateFixedFile(Settings settings, String templateName, String path, Map<String,String> variableValues) throws Exception{
        String content = readTemplateFile(templateName , Boolean.FALSE);
        if(StringUtils.isEmpty(content)) return false;
        if(variableValues != null) content = replaceVariables(content, variableValues);
        writeToFile(content,path, templateName, settings);
        return true;
    }

    public boolean writeToFile(String content, String path, String fileName, Settings settings) throws IOException{
        File folder = new File(path);
        if(!folder.exists()) folder.mkdirs();
        File file = new File(folder, fileName);

        if(!file.exists()){
            file.createNewFile();
        }else if(!settings.isValidToOverride(file)){
            //Since not allowed to overwrite
            return false;
        }
        Files.write( Paths.get(file.getAbsolutePath()), content.getBytes());
        return true;

    }

    public boolean replaceVariablesAndWrite(String template, Map<String,String> variableValues, String path, String fileName, Settings settings) throws Exception{
        String content = replaceVariables(template, variableValues);
        if(StringUtils.isEmpty(content)) return false;
        return writeToFile(content, path, fileName, settings);
    }

    /**
     * Replace map of variable value pair map
     * @param template String
     * @param variableValues Map<String,String>
     */
    public String replaceVariables(String template, Map<String,String> variableValues){
        StringBuffer sb = new StringBuffer(template.length() + (variableValues.size() * 40));
        String patternString =  "(" + join(variableValues.keySet().iterator(), "\\$\\{", "\\}", "|") + ")" ;
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(template);

        while(matcher.find()) {
            String variable = matcher.group(1);
            String value = variableValues.get(variable.substring(2, variable.length() - 1));
            if(!StringUtils.isEmpty(value))      matcher.appendReplacement(sb, value);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }


    /**
     * Build a joint pattern string
     * @param iterator
     * @param prefix
     * @param postfix
     * @param separator
     * @return String
     */
    private String join(Iterator<String> iterator, String prefix, String postfix, String separator) {
        if (iterator == null) {
            return null;
        } else if (!iterator.hasNext()) {
            return "";
        } else {
            String first = iterator.next();
            if (!iterator.hasNext()) {
                return prefix +first + postfix;
            } else {
                StringBuilder buf = new StringBuilder(256);
                if (first != null) {
                    buf.append(prefix);
                    buf.append(first);
                    buf.append(postfix);
                }

                while(iterator.hasNext()) {
                    if (separator != null) {
                        buf.append(separator);
                    }

                    String obj = iterator.next();
                    if (obj != null) {
                        buf.append(prefix);
                        buf.append(obj);
                        buf.append(postfix);
                    }
                }

                return buf.toString();
            }
        }
    }

    public String readTemplateFile(String name, Boolean partial)throws Exception{
        File file = new File( "templates" + File.separator + (partial ? "partial": "full") + File.separator + name + ".txt");
        FileInputStream fis = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        fis.read(data);fis.close();

        String str = new String(data, "UTF-8");
        return str;
    }

    public Map<String, String> getVariableMap(Object object, String prefix){
        //@TODO
        return null;
    }
}

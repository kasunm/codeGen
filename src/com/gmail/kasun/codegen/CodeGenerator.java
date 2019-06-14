package com.gmail.kasun.codegen;

import com.gmail.kasun.codegen.model.*;
import com.gmail.kasun.codegen.util.AngularCodeGenerate;
import com.gmail.kasun.codegen.util.JavaCodeGenerator;
import com.gmail.kasun.codegen.util.MiscUtils;
import com.gmail.kasun.codegen.util.TemplateUtils;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Title         : ${FILE_NAME}
 * <p>Project       : SpanCodeGenerator
 * <p>Description   :
 *
 * @author Kasun Madurasinghe
 * @version 1.0
 */
public class CodeGenerator {

    public static void main(String[] args) throws Exception{
        //MiscUtils.createAngularProject("F:/Develpment", "pissu");
        if(args == null || args.length < 1){
            System.out.println("Specify json file as argument");
            return;
        }
        CodeGenerator codeGenerator = new CodeGenerator();

        Settings settings = codeGenerator.loadSettingsFromJsonFile(args[0]);
        if(settings == null){
            System.out.println("Invalid Json file format");
            return;
        }

        //Settings settings = getTestSettings();

        String test = "<attributeName>${javaProjectName}</attributeName>";
        System.out.println( TemplateUtils.getInstance().replaceVariables(test, settings.getProjectSettings()));
        System.out.println( new Gson().toJson(settings));



        //settings.generateProjectsAndDirectoryStructure();

        JavaCodeGenerator.getInstance().generateCode(settings);

        AngularCodeGenerate.getInstance().generateCode(settings);

    }

    public void generate(String filePath) throws Exception{
        loadSettingsFromJsonFile(filePath);
    }

    private Settings loadSettingsFromJsonFile(String filePath) throws IOException {
        if(StringUtils.isEmpty(filePath)) return null;
        File file = new File(filePath);
        if(!file.exists()) return null;
        FileInputStream fis = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        fis.read(data);
        fis.close();
        String str = new String(data, "UTF-8");
        if(StringUtils.isEmpty(str)) return null;
        return new Gson().fromJson(str, Settings.class);
    }

    public static Settings getTestSettings(){
        Settings settings = new Settings("Aggalakan", "A food out let specialized for Aggala", "F:/Develpment/Aggala","com.kasunm.aggala", "AggalaUI", "F:/Develpment/Aggala", Boolean.FALSE, Boolean.FALSE);

        settings.dbName = "users";
        settings.dbUserName = "appartmentadmin";
        settings.dbPassword = "Super";

        settings.classes = new ArrayList<ClassTemplate>();
        ClassTemplate classTemplate = new ClassTemplate("User", "A typical user of the system", "users" , "id");
        List<AttributeTemplate> attributeTemplateList = new ArrayList<AttributeTemplate>(6);
        attributeTemplateList.add(new AttributeTemplate("name", AttributeType.STRING, 3, 20, true, true, true));
        attributeTemplateList.add(new AttributeTemplate("email"));
        attributeTemplateList.add(new AttributeTemplate("password", AttributeType.STRING, 0, 20, false));
        attributeTemplateList.add(new AttributeTemplate("age", AttributeType.INT, 0, 900));
        attributeTemplateList.add(new AttributeTemplate("favouriteColors", AttributeType.STRING, CollectionType.List));
        attributeTemplateList.add(new AttributeTemplate("gender", AttributeType.ENUM, "Gender"));
        classTemplate.attributes = attributeTemplateList;
        settings.classes.add(classTemplate);

        List<EnumTemplate> enums = new ArrayList<>();
        enums.add(new EnumTemplate("Gender", "MALE,FEMALE,SHEMALE,OTHER"));
        settings.enums = enums;
        return settings;
    }

}

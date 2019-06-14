package com.gmail.kasun.codegen;

import com.gmail.kasun.codegen.model.*;
import com.gmail.kasun.codegen.util.AngularCodeGenerate;
import com.gmail.kasun.codegen.util.JavaCodeGenerator;
import com.gmail.kasun.codegen.util.MiscUtils;
import com.gmail.kasun.codegen.util.TemplateUtils;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;

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
        Settings settings = getTestSettings();

        String test = "<attributeName>${javaProjectName}</attributeName>";
        System.out.println( TemplateUtils.getInstance().replaceVariables(test, settings.getProjectSettings()));
        System.out.println( new Gson().toJson(settings));
        ClassTemplate ct = settings.classes.get(0);
        Map<String,String> tags = new HashMap<String, String>();
        MiscUtils.addClassStringAttributes(ct, tags);
        System.out.println(tags.keySet());
        System.out.println(StringUtils.capitalize(MiscUtils.splitCamelCase("userName"," ")));


        //settings.generateProjectsAndDirectoryStructure();

        //JavaCodeGenerator.getInstance().generateCode(settings);

        AngularCodeGenerate.getInstance().generateCode(settings);


    }

    public static Settings getTestSettings(){
        Settings settings = new Settings("Aggala", "A food out let specialized for Aggala", "F:/Develpment/Aggala","com.kasunm.aggala", "AggalaUI", "F:/Develpment/Aggala", Boolean.FALSE, Boolean.FALSE);

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
        classTemplate.attributes = attributeTemplateList;
        settings.classes.add(classTemplate);
        return settings;
    }

}

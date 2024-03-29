package com.gmail.kasun.codegen.model;

import com.gmail.kasun.codegen.util.MiscUtils;
import com.gmail.kasun.codegen.config.GlobalSettings;
import com.gmail.kasun.codegen.util.TemplateUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * <p>Title         : ${FILE_NAME}
 * <p>Project       : SpanCodeGenerator
 * <p>Description   :
 *
 * @author Kasun Madurasinghe
 * @version 1.0
 */
public class Settings {
    public String javaProjectName;
    public String javaPackage;
    public String javaProjectBasePath;
    public String projectDescription;

    public String angularProjectName;
    public String angularProjectBasePath;

    public String dbName;
    public String dbUserName;
    public String dbPassword;


    public boolean generateNewAngularProject;
    public boolean generateNewJavaProject;

    public List<ClassTemplate> classes;
    public List<EnumTemplate> enums;


    public Settings(String javaProjectName, String projectDescription, String javaProjectBasePath, String javaPackage, String angularProjectName, String angularProjectBasePath, boolean generateNewAngularProject, boolean generateNewJavaProject) {
        this.javaProjectName = javaProjectName;
        this.projectDescription = projectDescription;
        this.javaPackage = javaPackage;
        this.angularProjectName = angularProjectName;
        this.javaProjectBasePath = javaProjectBasePath;
        this.angularProjectBasePath = angularProjectBasePath;
        this.generateNewAngularProject = generateNewAngularProject;
        this.generateNewJavaProject = generateNewJavaProject;
    }

    public Map<String, String> getDBSettings(){
        Map dbSettings = new HashMap<String, String>();
        dbSettings.put("dbName", this.dbName);
        dbSettings.put("dbUserName", this.dbUserName);
        dbSettings.put("dbPassword", this.dbPassword);
        return dbSettings;
    }

    public Map<String, String> getProjectSettings(){
        Map projectSettings = new HashMap<String, String>();
        projectSettings.put("javaPackage", this.javaPackage);
        projectSettings.put("javaProjectName", this.javaProjectName);
        projectSettings.put("projectDescription", this.projectDescription);
        return projectSettings;
    }


    /**
     * Generate base directories
     */
    public void generateProjectsAndDirectoryStructure(){
        //Generate Java project structure
        if(generateNewJavaProject){
            MiscUtils.createDirectories(getJavaPackageRoot());
            MiscUtils.createDirectories(javaProjectBasePath + File.separator + "src" + File.separator + "main" +  File.separator + "resources");
            MiscUtils.createDirectories(getJavaPackageRoot() + File.separator + GlobalSettings.JavaDirectory.config);
            MiscUtils.createDirectories(getJavaPackageRoot() + File.separator + GlobalSettings.JavaDirectory.controller);
            MiscUtils.createDirectories(getJavaPackageRoot() );
            MiscUtils.createDirectories(getJavaPackageRoot() + File.separator + GlobalSettings.JavaDirectory.model + File.separator + GlobalSettings.JavaDirectory.dto);
            MiscUtils.createDirectories(getJavaPackageRoot() + File.separator + GlobalSettings.JavaDirectory.model + File.separator + GlobalSettings.JavaDirectory.entity);
            MiscUtils.createDirectories(getJavaPackageRoot() + File.separator + GlobalSettings.JavaDirectory.model + File.separator + GlobalSettings.JavaDirectory.enums);
            MiscUtils.createDirectories(getJavaPackageRoot() + File.separator + GlobalSettings.JavaDirectory.repo);
            MiscUtils.createDirectories(getJavaPackageRoot() + File.separator + GlobalSettings.JavaDirectory.service);


            MiscUtils.createDirectories(angularProjectBasePath);
        }

        //Create new angular project
        if(generateNewAngularProject){
            MiscUtils.createDirectories(angularProjectBasePath);
            MiscUtils.createAngularProject(angularProjectBasePath, angularProjectName);
        }
    }





    public boolean isValidToOverride(File file){
        return true;
    }

    public String getJavaPackageRoot() {
        return javaProjectBasePath + File.separator + javaProjectName + File.separator +  "src" + File.separator + "main" +  File.separator + "java" +  File.separator + javaPackage.replaceAll("\\.", "/");
    }

    public String getJavaTestPackageRoot() {
        return javaProjectBasePath + File.separator + javaProjectName + File.separator +  "src" + File.separator + "test" +  File.separator + javaPackage.replaceAll("\\.", "/") + File.separator +  "test";
    }

    public String getJavaProjecteRoot() {
        return javaProjectBasePath + File.separator+ javaProjectName  ;
    }

    public String getAngularPackageRoot() {
        return angularProjectBasePath + File.separator+ angularProjectName + File.separator + "src" + File.separator + "app";
    }

    public String getRepeatedTemplateReplacedValue(String partialTemplate, String firstItemTemplate, String matchingClasses){
        StringBuilder sb = new StringBuilder(500);
        int count = 0;
        for(ClassTemplate classTemplate: classes){
            if(!StringUtils.isEmpty(matchingClasses) && !matchingClasses.contains(classTemplate.className)) continue;
            Map<String,String> map = getProjectSettings();
            MiscUtils.addClassStringAttributes(classTemplate, map);
            if(count ++ < 1 && !StringUtils.isEmpty(firstItemTemplate)) sb.append(TemplateUtils.getInstance().replaceVariables(firstItemTemplate,map));
            else sb.append(TemplateUtils.getInstance().replaceVariables(partialTemplate,map));
        }
        return sb.toString();
    }

    public String getDeclarationClassNames(){
        StringBuilder sb = new StringBuilder(500);
        int count = 0;
        for(ClassTemplate classTemplate: classes){
            if(count++ > 0) sb.append(",\n");
            sb.append(classTemplate.className + "DetailComponent");
            sb.append(",\n");
            sb.append(classTemplate.className + "ListComponent");
        }
        return sb.toString();
    }

    public String getRandomEnumValue(String enumName){
        for(EnumTemplate enumTemplate: enums){
            if(enumName.equalsIgnoreCase(enumTemplate.enumName)) return enumTemplate.getRandomValue();
        }
        return null;
    }

    public String getMappedAttributeNames( String className, String callingClassName, String mainAttributeName, String expectedAttributeNames) throws Exception{
        for(ClassTemplate classTemplate: classes){
            if(classTemplate.className.equals(className)) return classTemplate.getMappedAttributeNames(this, mainAttributeName, callingClassName, expectedAttributeNames);
        }
        return " ";
    }

    public  ClassTemplate getClass(String className){
        for(ClassTemplate classTemplate: classes){
            if(classTemplate.className.equals(className)) return classTemplate;
        }
        return null;
    }

    public  List<AttributeTemplate> getClassAttributes(String className){
        for(ClassTemplate classTemplate: classes){
            if(classTemplate.className.equals(className)) return classTemplate.attributes;
        }
        return null;
    }

    public String getBeanInstance(String className,  String id, boolean valid){
        for(ClassTemplate classTemplate: classes){
            if(classTemplate.className.equals(className)) return classTemplate.getBeanInstance( this,  id, valid);
        }
        return " ";
    }


    public String getMappedAngularAttributes(String className, String callingClassName, String mainAttributeName, String expectedAttributeNames) {
        for(ClassTemplate classTemplate: classes){
            if(classTemplate.className.equals(className)) return classTemplate.getMappedAngularAttributes( this,    mainAttributeName, expectedAttributeNames);
        }
        return " ";
    }

    public List<AttributeTemplate> getDerivedAttributeNames(String className, String expectedAttributeNames, String prefix){
        for(ClassTemplate classTemplate: classes){
            if(classTemplate.className.equals(className)) return classTemplate.getDerivedAttributeNames( expectedAttributeNames, prefix);
        }
        return null;
    }

    public void loadDerivedAttributes(){
        for(ClassTemplate classTemplate: classes){
            classTemplate.loadDerivedAttributes(this);
        }
    }
}

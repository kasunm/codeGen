package com.gmail.kasun.codegen.util;

import com.gmail.kasun.codegen.config.GlobalSettings;
import com.gmail.kasun.codegen.model.ClassTemplate;
import com.gmail.kasun.codegen.model.EnumTemplate;
import com.gmail.kasun.codegen.model.Settings;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>Title         : ${FILE_NAME}
 * <p>Project       : SpanCodeGenerator
 * <p>Description   :
 *
 * @author Kasun Madurasinghe
 * @version 1.0
 */
public class JavaCodeGenerator {
    private static volatile  JavaCodeGenerator javaCodeGenerator = new JavaCodeGenerator();

    private JavaCodeGenerator(){}

    public static JavaCodeGenerator getInstance(){
        return javaCodeGenerator;
    }

    public void generateCode(Settings settings) throws Exception{
        generateFixedFiles(settings);
        generateDynamicClasses(settings);
    }

    /** ------------- Supportive private methods  ------------- **/


    private void generateFixedFiles(Settings settings) throws Exception{
        TemplateUtils.getInstance().generateFixedFile(settings, "application.properties", settings.getJavaProjecteRoot() +  File.separator + "src" + File.separator + "main" + File.separator + "resources"   , settings.getDBSettings());
        TemplateUtils.getInstance().generateFixedFile(settings, "pom.xml", settings.getJavaProjecteRoot(), settings.getProjectSettings());
        TemplateUtils.getInstance().generateFixedFile(settings, "SwaggerConfig.java", settings.getJavaPackageRoot() +  File.separator + GlobalSettings.JavaDirectory.config  , settings.getProjectSettings());
        TemplateUtils.getInstance().generateFixedFile(settings, "AppConfig.java", settings.getJavaPackageRoot() +  File.separator + GlobalSettings.JavaDirectory.config  , settings.getProjectSettings());
        TemplateUtils.getInstance().generateFixedFile(settings, "MainApplication.java", settings.getJavaPackageRoot()   , settings.getProjectSettings());
        TemplateUtils.getInstance().generateFixedFile(settings, "ErrorDetails.java", settings.getJavaPackageRoot() + File.separator + "error"   , settings.getProjectSettings());
        TemplateUtils.getInstance().generateFixedFile(settings, "CustomizedResponseEntityExceptionHandler.java", settings.getJavaPackageRoot()  + File.separator + "error"   , settings.getProjectSettings());
        TemplateUtils.getInstance().generateFixedFile(settings, "MainAppTest.java", settings.getJavaTestPackageRoot()    , settings.getProjectSettings());
        //TemplateUtils.getInstance().generateFixedFile(settings, "CommonUtils.java", settings.getJavaPackageRoot()  + File.separator + "common"   , settings.getProjectSettings());
    }

    private void generateDynamicClasses(Settings settings) throws Exception {
        //Create enums
        if(settings.enums == null || settings.enums.size() < 1){
            settings.enums = new ArrayList<EnumTemplate>();
        }
        settings.enums.add(new EnumTemplate("ServiceStatus", "SUCCESS, NOT_FOUND, INVALID_PARAMS, DB_ERROR"));
        for(EnumTemplate enumTemplate: settings.enums){
            Map classSettings = new HashMap<String, String>();
            classSettings.put("javaPackage", settings.javaPackage );
            classSettings.put("javaProjectName", settings.javaProjectName );
            classSettings.put("projectDescription", settings.projectDescription );
            classSettings.put("enumName", enumTemplate.enumName);
            classSettings.put("enumValues", enumTemplate.enumValues);
            String templateText = TemplateUtils.getInstance().readTemplateFile("enum.class", false);
            TemplateUtils.getInstance().replaceVariablesAndWrite(templateText, classSettings, settings.getJavaPackageRoot() + File.separator + "model" + File.separator + "enums", enumTemplate.enumName + ".java", settings);

        }

        StringBuilder messageResources = new StringBuilder(2000);
        for(ClassTemplate template: settings.classes){
            Map classSettings = new HashMap<String, String>();
            classSettings.put("javaPackage", settings.javaPackage );
            classSettings.put("javaProjectName", settings.javaProjectName );
            classSettings.put("projectDescription", settings.projectDescription );


            template.addJavaAttributeResources(messageResources);
            String templateText = TemplateUtils.getInstance().readTemplateFile("Entity.java", false);
            template.generateFile(template.className + ".java", settings.getJavaPackageRoot() + File.separator +  "model" + File.separator + "entity", settings, templateText, classSettings, true, true);

            templateText = TemplateUtils.getInstance().readTemplateFile("DTO.java", false);
            template.generateFile(template.className + "DTO.java", settings.getJavaPackageRoot() + File.separator +  "model" + File.separator + "dto", settings, templateText, classSettings, false, true);

            templateText = TemplateUtils.getInstance().readTemplateFile("Repository.java", false);
            template.generateFile(template.className + "Repository.java", settings.getJavaPackageRoot() + File.separator + "repo", settings, templateText, classSettings, false, false);

            templateText = TemplateUtils.getInstance().readTemplateFile("MainService.java", false);
            template.generateFile("Main" + template.className + "Service.java", settings.getJavaPackageRoot() +    File.separator + "service", settings, templateText, classSettings, false, false);
            templateText = TemplateUtils.getInstance().readTemplateFile("Service.java", false);
            template.generateFile(template.className + "Service.java", settings.getJavaPackageRoot() + File.separator +    "service", settings, templateText, classSettings, false, false);

            templateText = TemplateUtils.getInstance().readTemplateFile("Controller.java", false);
            template.generateFile(template.className + "Controller.java", settings.getJavaPackageRoot() + File.separator  + "controller", settings, templateText, classSettings, false, false);

            templateText = TemplateUtils.getInstance().readTemplateFile("ControllerTest.java", false);
            template.generateFile(template.className + "ControllerTest.java", settings.getJavaTestPackageRoot() + File.separator  + "controller", settings, templateText, classSettings, false, false);

            templateText = TemplateUtils.getInstance().readTemplateFile("ServiceTest.java", false);
            template.generateFile(template.className + "ServiceTest.java", settings.getJavaTestPackageRoot() + File.separator  + "service", settings, templateText, classSettings, false, false);

            templateText = TemplateUtils.getInstance().readTemplateFile("RepositoryTest.java", false);
            template.generateFile(template.className + "RepositoryTest.java", settings.getJavaTestPackageRoot() + File.separator  + "repo", settings, templateText, classSettings, true, true);

            templateText = TemplateUtils.getInstance().readTemplateFile("APITest.java", false);
            template.generateFile(template.className + "APITest.java", settings.getJavaTestPackageRoot()  + File.separator  + "api", settings, templateText, classSettings, false, false);




        }
        TemplateUtils.getInstance().writeToFile(messageResources.toString(), settings.getJavaProjecteRoot() +  File.separator + "src" + File.separator + "main" + File.separator + "resources", "messages.properties", settings);
    }
}

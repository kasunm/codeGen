package com.gmail.kasun.codegen.util;

import com.gmail.kasun.codegen.model.ClassTemplate;
import com.gmail.kasun.codegen.model.EnumTemplate;
import com.gmail.kasun.codegen.model.Settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * <p>Title         : ${FILE_NAME}
 * <p>Project       : SpanCodeGenerator
 * <p>Description   :
 *
 * @author Kasun Madurasinghe
 * @version 1.0
 */
public class AngularCodeGenerate {
    private static  volatile AngularCodeGenerate instance = new AngularCodeGenerate();
    private Properties prop = new Properties();

    private AngularCodeGenerate(){
        try {
            InputStream input = new FileInputStream("templates/partial/htmlTags.properties");
            prop.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static AngularCodeGenerate getInstance(){
        return instance;
    }

    public void generateCode(Settings settings) throws Exception{
//        generateFixedFiles(settings);
//        generateDynamicClasses(settings);
    }

    /** ------------- Supportive private methods  ------------- **/

    private String getTag(String key){
        return prop.getProperty(key);
    }

    private void generateFixedFiles(Settings settings) throws Exception {
        Map projectSettings = new HashMap<String, String>();
        projectSettings.put("angularProjectName", settings.angularProjectName);
        projectSettings.put("projectDescription", settings.projectDescription);
        TemplateUtils.getInstance().generateFixedFile(settings, "index.html",   settings.angularProjectBasePath + File.separator + settings.angularProjectName +   File.separator + "src" , projectSettings);
        TemplateUtils.getInstance().generateFixedFile(settings, "styles.scss",   settings.angularProjectBasePath + File.separator + settings.angularProjectName +   File.separator + "src" , null);
        TemplateUtils.getInstance().generateFixedFile(settings, "page-not-found.component.ts",   settings.getAngularPackageRoot() + File.separator + "page-not-found" , null);
        TemplateUtils.getInstance().generateFixedFile(settings, "http-error.interceptor.ts",   settings.getAngularPackageRoot() + File.separator + "interceptors" , null);


        projectSettings.put("app.links", getForEachClass(getTag("app.link"), settings)); //Add All class links

        TemplateUtils.getInstance().generateFixedFile(settings, "app.component.html",   settings.getAngularPackageRoot() , projectSettings);
        projectSettings.put("model.class.import", settings.getRepeatedTemplateReplacedValue(getTag("model.class.import"), "", null));
        projectSettings.put("model.class.names", settings.getDeclarationClassNames());
        TemplateUtils.getInstance().generateFixedFile(settings, "app.module.ts",   settings.getAngularPackageRoot() , projectSettings);
        projectSettings.put("model.class.route", settings.getRepeatedTemplateReplacedValue(getTag("model.class.route"), getTag("model.class.firstRoute"), null));



        TemplateUtils.getInstance().generateFixedFile(settings, "app-routing.module.ts",   settings.getAngularPackageRoot() , projectSettings);


    }

    private void generateDynamicClasses(Settings settings) throws Exception {
        settings.loadDerivedAttributes();
        for(EnumTemplate template: settings.enums){
            Map classSettings = new HashMap<String, String>();
            classSettings.put("angularProjectName", settings.angularProjectName );
            classSettings.put("projectDescription", settings.projectDescription );
            classSettings.put("enumName", template.enumName);
            classSettings.put("enumValues", template.enumValues);
            String templateText = TemplateUtils.getInstance().readTemplateFile("enum.ts", false);
            TemplateUtils.getInstance().replaceVariablesAndWrite(templateText, classSettings, settings.getAngularPackageRoot() + File.separator + "enums", MiscUtils.splitCamelCase(template.enumName, "-").toLowerCase() + ".enum.ts", settings);
        }

        for(ClassTemplate template: settings.classes){
            Map classSettings = new HashMap<String, String>();
            classSettings.put("angularProjectName", settings.angularProjectName );
            classSettings.put("projectDescription", settings.projectDescription );
            classSettings.put("attributesComma", template.getAngularConstructor(settings));
            classSettings.put("attributesToString", template.getToString());
            classSettings.put("enumImports", template.getEnumImports());
            classSettings.put("dependantObjectList", template.getAngularDependantObjectList());
            classSettings.put("dependantServices", template.getAngularDependantServices());
            classSettings.put("dependantListLoad", template.getAngularDependantListLoads());
            classSettings.put("dependantValuesSet", template.getAngularDependantValueSets());
            classSettings.put("detail.class.enumCollection", template.getEnumKeys(getTag("detail.class.enumCollection")));
            if(template.getDependantClassNames().length() > 2)
                classSettings.put("model.dependant.import", settings.getRepeatedTemplateReplacedValue(getTag("model.dependant.import"), "", template.getDependantClassNames()));
            else  classSettings.put("model.dependant.import", " ");


            //Model
            String templateText = TemplateUtils.getInstance().readTemplateFile("class.ts", false);
            template.generateFile(template.getClassFileAngular() + ".ts", settings.getAngularPackageRoot() + File.separator + template.classURL, settings, templateText, classSettings, false, true);

            //Service
            templateText = TemplateUtils.getInstance().readTemplateFile("service.ts", false);
            template.generateFile(template.getClassFileAngular() + ".service.ts", settings.getAngularPackageRoot()+ File.separator + template.classURL, settings, templateText, classSettings, false, false);

            //List generations
            classSettings.put("attribute.th", template.getPopulatesAngularAttributeNames(getTag("attribute.th"),  true));
            classSettings.put("attribute.td", template.getPopulatesAngularAttributeNames(getTag("attribute.td"), getTag("attribute.td.enum"), true));
            template.generateFile(template.getClassFileAngular() + "-list.component.scss", settings.getAngularPackageRoot()+ File.separator + template.classURL, settings, " ", classSettings, false, false);

            templateText = TemplateUtils.getInstance().readTemplateFile("list.component.html", false);
            template.generateFile(template.getClassFileAngular() + "-list.component.html", settings.getAngularPackageRoot() + File.separator+ template.classURL, settings, templateText, classSettings, false, true);
            templateText = TemplateUtils.getInstance().readTemplateFile("list.component.ts", false);
            template.generateFile(template.getClassFileAngular() + "-list.component.ts", settings.getAngularPackageRoot() + File.separator+ template.classURL, settings, templateText, classSettings, false, false);


            //Details
            classSettings.put("attribute.validator.get", template.getPopulatesAngularAttributeNames(getTag("attribute.validator.get"), false));
            classSettings.put("attribute.validation", template.getAngularFormValidators());
            template.generateFile(template.getClassFileAngular() + "-detail.component.scss", settings.getAngularPackageRoot()+ File.separator + template.classURL, settings, " ", classSettings, false, false);

            templateText = TemplateUtils.getInstance().readTemplateFile("detail.component.ts", false);
            template.generateFile(template.getClassFileAngular()+ "-detail.component.ts", settings.getAngularPackageRoot()+ File.separator + template.classURL, settings, templateText, classSettings, false, false);

            templateText = TemplateUtils.getInstance().readTemplateFile("detail.component.html", false);
            classSettings.put("attributeFormElement", template.getAttributeFormElementHTML(prop));
            template.generateFile(template.getClassFileAngular()+ "-detail.component.html", settings.getAngularPackageRoot()+ File.separator + template.classURL, settings, templateText, classSettings, false, false);

        }
    }



    private String getForEachClass(String templateText, Settings settings) {
        StringBuilder allValues = new StringBuilder(100);
        for(ClassTemplate classTemplate: settings.classes){
            allValues.append("\n" + TemplateUtils.getInstance().replaceVariables(templateText, classTemplate.getClassOnlyAttributes()));
        }
        return allValues.toString();
    }

    }

package com.gmail.kasun.codegen.model;

import com.gmail.kasun.codegen.util.MiscUtils;
import com.gmail.kasun.codegen.util.TemplateUtils;
import org.apache.commons.lang3.StringUtils;

import java.beans.Transient;
import java.util.HashMap;
import java.util.List;
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

public class ClassTemplate {
    public String className;

    public String classDescription;

    public String classURL;
    public String idAttributeName;
    public List <AttributeTemplate> attributes;

    //Derived attributes. Populated to map using reflection
    private String classNameDisplay;
    private String classFileAngular;
    private String classVariableName;



    public ClassTemplate(String className, String classDescription, String classURL, String idAttributeName) {
        this.className = className;
        this.classDescription = classDescription;
        this.classURL = classURL;
        this.idAttributeName = idAttributeName;
        initDerivedVariables(className);
    }

    public String getClassNameDisplay() {
        return classNameDisplay;
    }

    public String getClassFileAngular() {
        return classFileAngular;
    }

    private void initDerivedVariables(String className) {
        this.classNameDisplay = MiscUtils.splitCamelCase(className, " ");
        this.classFileAngular = MiscUtils.splitCamelCase(className, "-").toLowerCase();
        this.classVariableName = StringUtils.uncapitalize(className);
    }

    public String getAttributeNames(boolean isEntityClass) throws Exception{
        StringBuilder attributeText = new StringBuilder(1000);
        if(attributes == null) return "";
        for(AttributeTemplate template: attributes){
            attributeText.append(template.getJavaAttribute(isEntityClass));
            attributeText.append("\n");
        }
        return attributeText.toString();
    }

    public boolean generateFile(String fileName, String path,Settings settings, String templateText, Map<String, String> variables, boolean isEntity, boolean addVariables) throws Exception{
        if(addVariables) addClassTemplateVariables(variables, isEntity);
        return TemplateUtils.getInstance().replaceVariablesAndWrite(templateText, variables, path, fileName, settings);
    }

    public void addClassTemplateVariables(Map<String,String> variables, boolean isEntity) throws Exception{
        MiscUtils.addClassStringAttributes(this, variables);
        variables.put("classAttributes", getAttributeNames(isEntity));
    }

    public String getClassVariableName(){
        return classVariableName;
    }

    public Map<String,String> getClassOnlyAttributes(){
        Map<String,String> map = new HashMap<>();
        MiscUtils.addClassStringAttributes(this, map);
        return map;
    }


    public String getPopulatesAngularAttributeNames(String partialTemplateText, boolean showInGrid){
        StringBuilder sb = new StringBuilder(300);
        int count = 0;
        for(AttributeTemplate attribute: attributes){
            if(showInGrid && (attribute.showOnGrid == null || !attribute.showOnGrid)) continue;
            if(count ++ > 0) sb.append("\n");
            sb.append(partialTemplateText.replaceAll("\\$\\{attributeName\\}", attribute.attributeName)
                    .replaceAll("\\$\\{attributeError\\}", attribute.getAngularErrorMessage())
                    .replaceAll("\\$\\{attributeDisplayName\\}", attribute.getDisplayName())
                    .replaceAll("\\$\\{classVariableName\\}", classVariableName)
            );
        }
        return sb.toString();
    }

    public String getAttributeFormElementHTML(Properties props){
        StringBuilder sb = new StringBuilder(3000);
        for(AttributeTemplate attribute: attributes){
            attribute.addAttributeFormElementHTML(props, sb);
        }
        return sb.toString();
    }

    public String getAngularFormValidators(){
        StringBuilder validators = new StringBuilder(2500);
        int count = attributes.size();
        for(AttributeTemplate attributeTemplate: attributes){
            attributeTemplate.addToAngularFormBuilder(validators);
            if(count-- > 1) validators.append(",");
            validators.append("\n");
        }
        return validators.toString();
    }

    public String getAngularConstructor(){
        StringBuilder sb = new StringBuilder(300);
        int count = 0;
        for(AttributeTemplate attribute: attributes){
            if( count++ > 0) sb.append(", ");
            sb.append(attribute.getAngularAttribute());
        }
        return sb.toString();
    }

    public String getAngularAttributeNames(String separator, boolean skipLast){
        StringBuilder sb = new StringBuilder(300);
        int count = 0;
        for(AttributeTemplate attribute: attributes){
            if(skipLast && count++ > 0) sb.append(separator);
            sb.append(attribute);
        }
        return sb.toString();
    }

    public String getToString(){
        StringBuilder sb = new StringBuilder(300);
        int count = 0;
        for(AttributeTemplate attribute: attributes){
            if(count++ > 0) sb.append(" + ', ' + ");
            sb.append("'"+attribute.attributeName+":' + this."+attribute.attributeName+"" );
        }
        return sb.toString();
    }

}

package com.gmail.kasun.codegen.model;

import com.gmail.kasun.codegen.util.MiscUtils;
import com.gmail.kasun.codegen.util.TemplateUtils;
import org.apache.commons.lang3.StringUtils;

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
    public List <SearchOption> searchOptions;


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

    public String getAttributeNames(Settings settings, boolean isEntityClass) throws Exception{
        StringBuilder attributeText = new StringBuilder(1000);
        if(attributes == null) return "";
        for(AttributeTemplate template: attributes){
            attributeText.append(template.getJavaAttribute(settings, isEntityClass, className, "", false));
            attributeText.append("\n");
        }
        return attributeText.toString();
    }

    public void addSearchAttributeNames(Settings settings, String matchingAttributes, StringBuilder attributeDeclarations, StringBuilder attributeParameters, StringBuilder attributeValidations) throws Exception{
        if(attributes == null) return ;
        int count = 0;
        for(AttributeTemplate template: attributes){
            if(matchingAttributes.contains(template.attributeName)){ //@TODO in future support derived attributes
                Map<String,String> attributeVariables = new HashMap<String,String>();
                if(count ++ > 0){
                    attributeDeclarations.append(", ");
                    attributeParameters.append("\n");
                }

                template.getJavaAttribute(settings, false, className, "", true, attributeVariables);
                attributeDeclarations.append(attributeVariables.get("type") + " " + attributeVariables.get("attributeName"));
                attributeParameters.append("     * @param " + attributeVariables.get("attributeName") + " " + attributeVariables.get("type"));
                attributeValidations.append("\t\tAssert.notNull(" +template.attributeName + ", \"Expects a valid "+ template.attributeName +"\");\n");
                if(template.type == AttributeType.INT || template.type == AttributeType.LONG || template.type == AttributeType.DOUBLE){
                    attributeValidations.append( "\t\tAssert.isTrue(" + template.attributeName + " > 0, \"Expects a valid "+ template.attributeName +" > 0\");\n");
                }

            }
        }
    }



    public String getMappedAttributeNames( String mainAttributeName, String callingClassName, String expectedAttributeNames) throws Exception{
        StringBuilder attributeText = new StringBuilder(1000);
        if(attributes == null) return "";
        if(expectedAttributeNames.contains("id")){
            attributeText.append("\t@NotNull\n   private Long " + mainAttributeName + "Id;\n");
        }
        for(AttributeTemplate template: attributes){
            if(expectedAttributeNames.contains(","+ template.attributeName + ",")) {
                attributeText.append(template.getJavaAttribute(null, false, this.className, mainAttributeName, false));
                attributeText.append("\n");
            }
        }
        return attributeText.toString();
    }

    public boolean generateFile(String fileName, String path,Settings settings, String templateText, Map<String, String> variables, boolean isEntity, boolean addVariables) throws Exception{
        if(addVariables) addClassTemplateVariables(settings, variables, isEntity);
        return TemplateUtils.getInstance().replaceVariablesAndWrite(templateText, variables, path, fileName, settings);
    }

    public void addClassTemplateVariables(Settings settings, Map<String,String> variables, boolean isEntity) throws Exception{
        MiscUtils.addClassStringAttributes(this, variables);
        variables.put("classAttributes", getAttributeNames(settings, isEntity));
        //  Add test data
        variables.put("beanID0", getBeanInstance(settings, "0L", true));
        variables.put("beanID01", getBeanInstance(settings, "0L", true));
        variables.put("beanID02", getBeanInstance(settings, "0L", true));
        variables.put("beanID03", getBeanInstance(settings, "0L", true));
        variables.put("beanID1", getBeanInstance(settings, "1L", true));
        variables.put("beanID2", getBeanInstance(settings, "2L", true));
        variables.put("beanID3", getBeanInstance(settings, "3L", true));
        variables.put("beanID4", getBeanInstance(settings, "4L", false));
        variables.put("beanID5", getBeanInstance(settings, "5L", true));
        variables.put("beanFailValidation", getBeanInstance(settings, "6L", false));

        //Add dependant class instance
        StringBuilder dependantBeans = new StringBuilder(2000);
        StringBuilder dependantBeanHeader = new StringBuilder(2000);
        dependantBeanHeader.append(" ");
        for(AttributeTemplate attributeTemplate: attributes){
            if(attributeTemplate.type == AttributeType.CLASS && (attributeTemplate.collectionType == null || attributeTemplate.collectionType == CollectionType.None)){
                if(isEntity) {
                    dependantBeanHeader.append("\t@Autowired\n\t");
                    dependantBeanHeader.append(attributeTemplate.classTypeName + "Repository " + StringUtils.uncapitalize(attributeTemplate.classTypeName) + "Repository; ");
                }
                addDependatBeanInstance(settings, dependantBeans, dependantBeanHeader, attributeTemplate, 0, true, isEntity);
                addDependatBeanInstance(settings, dependantBeans, dependantBeanHeader, attributeTemplate, 1, true, isEntity);
                addDependatBeanInstance(settings, dependantBeans, dependantBeanHeader, attributeTemplate, 2, false, isEntity);
            }
        }
        dependantBeans.append(" ");
        variables.put("dependantBeans", dependantBeans.toString());
        variables.put("dependantBeansHeader", dependantBeanHeader.toString());

        if(searchOptions == null){
            variables.put("controllerSearch", " ");
            variables.put("serviceSearch", " ");
            variables.put("serviceInterfaceSearch", " ");
            variables.put("repoSearch", " ");
            return;
        }

        StringBuilder controllerSearch = new StringBuilder(4000);
        StringBuilder serviceSearch = new StringBuilder(4000);
        StringBuilder repoSearch = new StringBuilder(4000);
        StringBuilder serviceInterfaceSearch = new StringBuilder(4000);
        controllerSearch.append(" ");
        serviceSearch.append(" ");
        repoSearch.append(" ");

        for(SearchOption searchOption: searchOptions){
            StringBuilder params = new StringBuilder(200);
            StringBuilder declare = new StringBuilder(200);
            StringBuilder validation = new StringBuilder(200);
            addSearchAttributeNames(settings, "," + searchOption.attributeNames + ",", declare, params, validation);
            Map<String, String> searchVariables = new HashMap<>();
            MiscUtils.addClassStringAttributes(searchOption, searchVariables);
            MiscUtils.addClassStringAttributes(this, searchVariables);
            searchVariables.put("paramComment", params.toString());
            searchVariables.put("args", declare.toString());
            searchVariables.put("argsValidate", validation.toString());
            searchVariables.put("attributeValues", searchOption.attributeNames.replaceAll(","," + \",\" + "));
            searchVariables.put("searchUrl",  searchOption.searchUrl + "/{" + searchOption.attributeNames.replaceAll(",","}/{") + "}");
            controllerSearch.append(TemplateUtils.getInstance().replaceVariables(SearchTemplates.controller, searchVariables));
            controllerSearch.append("\n");
            serviceSearch.append(TemplateUtils.getInstance().replaceVariables(SearchTemplates.service, searchVariables));
            serviceSearch.append("\n");
            serviceInterfaceSearch.append(TemplateUtils.getInstance().replaceVariables(SearchTemplates.serviceInterface, searchVariables));
            serviceInterfaceSearch.append("\n");
            repoSearch.append(TemplateUtils.getInstance().replaceVariables(SearchTemplates.repo, searchVariables));
            repoSearch.append("\n");

        }
        variables.put("controllerSearch", controllerSearch.toString());
        variables.put("serviceSearch", serviceSearch.toString());
        variables.put("serviceInterfaceSearch", serviceInterfaceSearch.toString());
        variables.put("repoSearch", repoSearch.toString());
    }

    private void addDependatBeanInstance(Settings settings, StringBuilder dependantBeans, StringBuilder dependantBeanHeader, AttributeTemplate attributeTemplate, int id, boolean valid, boolean isEntity) {
        dependantBeanHeader.append("\tprivate " + attributeTemplate.classTypeName + " " + attributeTemplate.attributeName + id + ";\n\t");
        dependantBeans.append( attributeTemplate.attributeName + id + " =  " );
        dependantBeans.append(settings.getBeanInstance(attributeTemplate.classTypeName, (isEntity && valid ? "0" : id ) + "L", valid));
        dependantBeans.append(";\n\t");
        if(valid && id > 0 && isEntity){
            dependantBeans.append(attributeTemplate.classTypeName + " saved" + attributeTemplate.attributeName + id + " = " +  StringUtils.uncapitalize(attributeTemplate.classTypeName) + "Repository.save(" +  attributeTemplate.attributeName + id + ");\n\t");
            dependantBeans.append(attributeTemplate.attributeName + id + " = saved" + attributeTemplate.attributeName + id  + ";\n");
        }
    }

    public String getClassVariableName(){
        return classVariableName;
    }

    public Map<String,String> getClassOnlyAttributes(){
        Map<String,String> map = new HashMap<>();
        MiscUtils.addClassStringAttributes(this, map);
        return map;
    }

    public String getPopulatesAngularAttributeNames(String partialTemplateText,  boolean showInGrid){
        return getPopulatesAngularAttributeNames(partialTemplateText, "", showInGrid);
    }

    public String getPopulatesAngularAttributeNames(String partialTemplateText, String enumSpecialTemplate, boolean showInGrid){
        StringBuilder sb = new StringBuilder(300);
        int count = 0;
        for(AttributeTemplate attribute: attributes){
            if(!attribute.includeInDTO) continue;
            if(showInGrid && (attribute.showOnGrid == null || !attribute.showOnGrid)) continue;

            if(count ++ > 0) sb.append("\n");
            if(attribute.type == AttributeType.ENUM && !StringUtils.isEmpty(enumSpecialTemplate)){
                sb.append(enumSpecialTemplate.replaceAll("\\$\\{attributeName\\}", attribute.attributeName)
                        .replaceAll("\\$\\{attributeError\\}", attribute.getAngularErrorMessage())
                        .replaceAll("\\$\\{attributeDisplayName\\}", attribute.getDisplayName())
                        .replaceAll("\\$\\{classVariableName\\}", classVariableName)
                );
            }else{
                sb.append(partialTemplateText.replaceAll("\\$\\{attributeName\\}", attribute.attributeName)
                        .replaceAll("\\$\\{attributeError\\}", attribute.getAngularErrorMessage())
                        .replaceAll("\\$\\{attributeDisplayName\\}", attribute.getDisplayName())
                        .replaceAll("\\$\\{classVariableName\\}", classVariableName)
                );
            }

        }
        return sb.toString();
    }

    public void addJavaAttributeResources(StringBuilder resources) throws Exception{
        for(AttributeTemplate attribute: attributes){
            attribute.addJavaAttributeResources(resources, className, classNameDisplay);
        }
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
            if(!attribute.includeInDTO) continue;
            if( count++ > 0) sb.append(", ");
            sb.append(attribute.getAngularAttribute());
        }
        return sb.toString();
    }

    public String getAngularAttributeNames(String separator, boolean skipLast){
        StringBuilder sb = new StringBuilder(300);
        int count = 0;
        for(AttributeTemplate attribute: attributes){
            if(!attribute.includeInDTO) continue;
            if(skipLast && count++ > 0) sb.append(separator);
            sb.append(attribute);
        }
        return sb.toString();
    }

    public String getToString(){
        StringBuilder sb = new StringBuilder(300);
        int count = 0;
        for(AttributeTemplate attribute: attributes){
            if(!attribute.includeInDTO) continue;
            if(count++ > 0) sb.append(" + ', ' + ");
            sb.append("'"+attribute.attributeName+":' + this."+attribute.attributeName+"" );
        }
        return sb.toString();
    }

    public String getEnumImports(){
        StringBuilder sb = new StringBuilder(300);
        sb.append("\n");
        for(AttributeTemplate attribute: attributes){
            if(!attribute.includeInDTO) continue;
            if(attribute.type == AttributeType.ENUM){
                sb.append("import {" + attribute.classTypeName +"} from '../enums/" + MiscUtils.splitCamelCase(attribute.classTypeName, "-").toLowerCase() +".enum';\n" );
            }
        }
        return sb.toString();
    }

    public String getEnumKeys(String templateText){
        StringBuilder sb = new StringBuilder(300);
        sb.append("\n");
        for(AttributeTemplate attribute: attributes){
            if(!attribute.includeInDTO) continue;
            if(attribute.type == AttributeType.ENUM){
                Map<String, String> tags = new HashMap<String, String>();
                tags.put("enumName", attribute.classTypeName);
                tags.put("enumVariableName", StringUtils.uncapitalize(attribute.classTypeName));
                sb.append(TemplateUtils.getInstance().replaceVariables(templateText, tags));
            }
        }
        return sb.toString();
    }

    /**
     * Get bean instance creation code with all arguments
     * with provided id
     * @param id Long
     * @return String
     */
    public String getBeanInstance(Settings settings, String id, boolean valid){
        StringBuilder sb = new StringBuilder(200);
        sb.append(" new "  + className + "(" + id  );
        for(AttributeTemplate attribute: attributes){
            attribute.addConstructorValues(settings, sb, id, valid);
        }
        sb.append(")");
        return sb.toString();
    }

}

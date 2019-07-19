package com.gmail.kasun.codegen.model;

import com.gmail.kasun.codegen.util.MiscUtils;
import com.gmail.kasun.codegen.util.TemplateUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

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
    public String inheritedClassName;
    public boolean hasChildren;
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

    public String getDependantClassNames(){
        StringBuilder sb = new StringBuilder(1000);
        sb.append(",");
        for(AttributeTemplate attributeTemplate: attributes){
            if(attributeTemplate.derivedAttributes != null && attributeTemplate.derivedAttributes.size() > 0){
                sb.append(attributeTemplate.classTypeName);
                sb.append(",");
            }
        }
        return sb.toString();
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

    //dtoMapping
    public void addJavaDependantMapping(StringBuilder attributeText, StringBuilder constructor, StringBuilder constructorAssign, StringBuilder variable){
        if(attributes == null) return ;
        for(AttributeTemplate template: attributes){
            if(StringUtils.isEmpty(template.relationShipDTOAttributes) || !template.includeInDTO) continue;
            attributeText.append("\t\tif(" + classVariableName + "DTO.get" + StringUtils.capitalize(template.attributeName) + "Id() != null && " + classVariableName + "DTO.get" + StringUtils.capitalize(template.attributeName) + "Id() > 0){\n" +
                    "            " + classVariableName + ".set" + StringUtils.capitalize(template.attributeName) + "( " + template.attributeName + "Service.getByID(" + classVariableName + "DTO.get" + StringUtils.capitalize(template.attributeName) + "Id()).get());\n" +
                    "        }");
            attributeText.append("\n");
            constructor.append(", " + template.classTypeName + "Service " +template.attributeName+ "Service");
            constructorAssign.append("\t\tthis." +template.attributeName+ "Service = " + template.attributeName+ "Service;\n");
            variable.append("\tprivate final "+ template.classTypeName +"Service " + template.attributeName + "Service;\n");
        }
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




    public String getMappedAttributeNames(Settings settings, String mainAttributeName, String callingClassName, String expectedAttributeNames) throws Exception{
        StringBuilder attributeText = new StringBuilder(1000);
        if(attributes == null) return "";
        if(StringUtils.isEmpty(inheritedClassName) && expectedAttributeNames.contains("id")){
            attributeText.append("\t@NotNull\n   private Long " + mainAttributeName + "Id;\n");
        }
        for(AttributeTemplate template: attributes){
            if(expectedAttributeNames.contains(","+ template.attributeName + ",")) {
                attributeText.append(template.getJavaAttribute(null, false, this.className, mainAttributeName, false));
                attributeText.append("\n");
            }
        }
        if(!StringUtils.isEmpty(inheritedClassName) && inheritedClassName.length() > 1){
            attributeText.append(settings.getMappedAttributeNames(inheritedClassName, callingClassName, mainAttributeName, expectedAttributeNames));
            attributeText.append("\n");
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
        variables.put("specialAnnotation", " ");
        variables.put("inheritedClassName", " ");
        variables.put("inheritedDTOName", " ");
        if(!StringUtils.isEmpty(inheritedClassName)){
            variables.put("inheritedClassName", " extends " + inheritedClassName);
            variables.put("inheritedDTOName", " extends " + inheritedClassName + "DTO ");
            variables.put("specialAnnotation", "@DiscriminatorValue(\""+ className +"\") ");
            variables.put("idDeclaration", " ");

        }else if(isEntity){
            variables.put("idDeclaration", "@GeneratedValue(strategy = GenerationType.IDENTITY)\n" +
                    "    @Id\n" +
                    "    private Long id; ");

        }else {
            variables.put("idDeclaration", "private Long id;");
        }
        if(hasChildren){
            variables.put("specialAnnotation", "@Inheritance(strategy = InheritanceType.SINGLE_TABLE)\n" +
                    "@DiscriminatorColumn(name = \""+ classVariableName +"\") ");
        }

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
        StringBuilder dtoMapping = new StringBuilder(1000);
        StringBuilder constructorMapping = new StringBuilder(1000);
        StringBuilder constructorAssign = new StringBuilder(1000);
        StringBuilder variableMapping = new StringBuilder(1000);
        addJavaDependantMapping(dtoMapping, constructorMapping, constructorAssign, variableMapping);
        variables.put("dtoMapping", dtoMapping.toString());
        variables.put("dependantConstructorService", constructorMapping.toString());
        variables.put("dependantVariableService", variableMapping.toString());
        variables.put("dependantConstructorAssign", constructorAssign.toString());

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
            variables.put("searchTestController", " ");
            variables.put("serviceSearch", " ");
            variables.put("searchTestService", " ");
            variables.put("serviceInterfaceSearch", " ");
            variables.put("repoSearch", " ");
            variables.put("searchTestRepo", " ");
            return;
        }

        StringBuilder controllerSearch = new StringBuilder(4000);
        StringBuilder controllerSearchTest = new StringBuilder(4000);
        StringBuilder serviceSearchTest = new StringBuilder(4000);
        StringBuilder repoSearchTest = new StringBuilder(4000);
        StringBuilder serviceSearch = new StringBuilder(4000);
        StringBuilder repoSearch = new StringBuilder(4000);
        StringBuilder serviceInterfaceSearch = new StringBuilder(4000);
        controllerSearch.append(" ");
        controllerSearchTest.append(" ");
        serviceSearch.append(" ");
        serviceSearchTest.append(" ");
        repoSearch.append(" ");
        repoSearchTest.append(" ");

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
            searchVariables.put("argsController", "@PathVariable " +  declare.toString().replaceAll(",", ", @PathVariable "));
            searchVariables.put("argsValidate", validation.toString());
            searchVariables.put("attributeValues", searchOption.attributeNames.replaceAll(","," + \",\" + "));
            searchVariables.put("searchUrl",  searchOption.searchUrl + "/{" + searchOption.attributeNames.replaceAll(",","}/{") + "}");
            searchVariables.put("testDataValues", getTestDataValues(searchOption.attributeNames));
            searchVariables.put("anyDataValues", getParamDataValues(searchOption.attributeNames, "any()"));
            searchVariables.put("nullDataValues", getParamDataValues(searchOption.attributeNames, "null"));
            controllerSearch.append(TemplateUtils.getInstance().replaceVariables(SearchTemplates.controller, searchVariables));
            controllerSearch.append("\n");
            controllerSearchTest.append(TemplateUtils.getInstance().replaceVariables(SearchTemplates.controllerTest, searchVariables));
            controllerSearchTest.append("\n");

            serviceSearch.append(TemplateUtils.getInstance().replaceVariables(SearchTemplates.service, searchVariables));
            serviceSearch.append("\n");
            serviceSearchTest.append(TemplateUtils.getInstance().replaceVariables(SearchTemplates.serviceSearchTest, searchVariables));
            serviceSearchTest.append("\n");


            serviceInterfaceSearch.append(TemplateUtils.getInstance().replaceVariables(SearchTemplates.serviceInterface, searchVariables));
            serviceInterfaceSearch.append("\n");
            repoSearch.append(TemplateUtils.getInstance().replaceVariables(SearchTemplates.repo, searchVariables));
            repoSearch.append("\n");


        }
        variables.put("controllerSearch", controllerSearch.toString());
        variables.put("searchTestController", controllerSearchTest.toString());
        variables.put("serviceSearch", serviceSearch.toString());
        variables.put("searchTestService", serviceSearchTest.toString());
        variables.put("serviceInterfaceSearch", serviceInterfaceSearch.toString());
        variables.put("repoSearch", repoSearch.toString());
        variables.put("searchTestRepo", " ");
    }

    private String getTestDataValues(String commaSeparatedAttributes){
        String [] tags = commaSeparatedAttributes.split(",");
        StringBuilder result = new StringBuilder(50);
        for(int i=0; i < tags.length; i++){
            if(i > 0) result.append(" ,");
            result.append("users.get(0).get" + StringUtils.capitalize(tags[i]) + "()");
        }
        return result.toString();
    }

    private String getParamDataValues(String commaSeparatedAttributes, String fixedValue){
        String [] tags = commaSeparatedAttributes.split(",");
        StringBuilder result = new StringBuilder(50);
        for(int i=0; i < tags.length; i++){
            if(i > 0) result.append(" ,");
            result.append(fixedValue);
        }
        return result.toString();
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
            if(StringUtils.isEmpty(attribute.relationShipDTOAttributes))  count = addToAngularAttribute(partialTemplateText, enumSpecialTemplate, showInGrid, sb, count, attribute);
            else {
                 for(AttributeTemplate derived: attribute.derivedAttributes){
                     count = addToAngularAttribute(partialTemplateText, enumSpecialTemplate, showInGrid, sb, count, derived);
                 }
            }
        }


        return sb.toString();
    }

    private int addToAngularAttribute(String partialTemplateText, String enumSpecialTemplate, boolean showInGrid, StringBuilder sb, int count, AttributeTemplate attribute) {
        if(!attribute.includeInDTO) return count;
        if(showInGrid && (attribute.showOnGrid == null || !attribute.showOnGrid)) return count;

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
        return count;
    }

    public void addJavaAttributeResources(StringBuilder resources) throws Exception{
        for(AttributeTemplate attribute: attributes){
            attribute.addJavaAttributeResources(resources, className, classNameDisplay);
        }
    }

    public String getAttributeFormElementHTML(Properties props){
        StringBuilder sb = new StringBuilder(3000);
        for(AttributeTemplate attribute: attributes){
            if(!attribute.includeInDTO) continue;
            if(attribute.derivedAttributes != null && attribute.derivedAttributes.size() > 0){
                for(AttributeTemplate derived: attribute.derivedAttributes){
                    if(derived.attributeName.endsWith("Id")) continue;
                    derived.addAttributeFormElementHTML(props, sb, attribute);
                }
            } else {
                attribute.addAttributeFormElementHTML(props, sb, null);
            }

        }
        return sb.toString();
    }

    public String getAngularFormValidators(){
        StringBuilder validators = new StringBuilder(2500);
        int count = attributes.size();
        for(AttributeTemplate attributeTemplate: attributes){
            if(!attributeTemplate.includeInDTO) continue;
            if(attributeTemplate.derivedAttributes != null && attributeTemplate.derivedAttributes.size() > 0){
                for(AttributeTemplate derived: attributeTemplate.derivedAttributes){
                    count += attributeTemplate.derivedAttributes.size();
                    derived.addToAngularFormBuilder(validators);
                    if (count-- > 1) validators.append(",");
                    validators.append("\n");
                }
            } else {
                attributeTemplate.addToAngularFormBuilder(validators);
                if (count-- > 1) validators.append(",");
                validators.append("\n");
            }
        }
        return validators.toString();
    }

    public String getAngularDependantObjectList(){
        StringBuilder inits = new StringBuilder(2500);
        int count = attributes.size();
        for(AttributeTemplate attributeTemplate: attributes){
            if(!attributeTemplate.includeInDTO) continue;
            if(attributeTemplate.derivedAttributes != null && attributeTemplate.derivedAttributes.size() > 0){
                inits.append("public " + StringUtils.uncapitalize(attributeTemplate.classTypeName) + "s: Array<" + attributeTemplate.classTypeName +">;\n" );
            }
        }
        return inits.toString();
    }

    public String getAngularDependantServices(){
        StringBuilder inits = new StringBuilder(2500);
        int count = attributes.size();
        for(AttributeTemplate attributeTemplate: attributes){
            if(!attributeTemplate.includeInDTO) continue;
            if(attributeTemplate.derivedAttributes != null && attributeTemplate.derivedAttributes.size() > 0){
                if(count++ > 0) inits.append(", ");
                inits.append("private " + StringUtils.uncapitalize(attributeTemplate.classTypeName) + "Service: " + attributeTemplate.classTypeName + "Service");
            }
        }
        return inits.toString();
    }

    public String getAngularDependantListLoads(){
        StringBuilder inits = new StringBuilder(2500);
        int count = attributes.size();
        for(AttributeTemplate attributeTemplate: attributes){
            if(!attributeTemplate.includeInDTO) continue;
            if(attributeTemplate.derivedAttributes != null && attributeTemplate.derivedAttributes.size() > 0){
                inits.append("this." + StringUtils.uncapitalize(attributeTemplate.classTypeName) + "Service.fetchAll" + attributeTemplate.classTypeName + "s().subscribe( data => this."+ StringUtils.uncapitalize(attributeTemplate.classTypeName) + "s = data, error1 => this.errorMessage = error1);\n");
            }
        }
        return inits.toString();
    }

    public String getAngularDependantValueSets(){
        StringBuilder inits = new StringBuilder(2500);

        int count = attributes.size();
        for(AttributeTemplate attributeTemplate: attributes){
            if(!attributeTemplate.includeInDTO) continue;
            if(attributeTemplate.derivedAttributes != null && attributeTemplate.derivedAttributes.size() > 0){
                inits.append("\nget all" + attributeTemplate.classTypeName + "s() {\n" +
                        "    return this."+StringUtils.uncapitalize(attributeTemplate.classTypeName)+"s;\n" +
                        "    }\n");
                inits.append(" set" + StringUtils.capitalize(attributeTemplate.attributeName) + "Id(id: number");
                for(AttributeTemplate derived: attributeTemplate.derivedAttributes){
                    if(derived.attributeName.toLowerCase().contains("id")) continue;
                    inits.append(", ");
                    inits.append(derived.getPlainAngularAttribute("","", false).replaceAll("public", ""));
                }
                inits.append(") {\n");
                 inits.append("    this."+ classVariableName +"."+ attributeTemplate.attributeName +"Id = id;\n");
                for(AttributeTemplate derived: attributeTemplate.derivedAttributes){

                    inits.append("\n\t");
                    inits.append("this." + derived.attributeName);
                    if(derived.attributeName.toLowerCase().contains("id")) inits.append(".setValue(id); ");
                    else inits.append(".setValue(" + derived.attributeName + "); ");
                }
                inits.append("\n}\n");
            }
        }
        return inits.toString();
    }




    public String getAngularConstructor(Settings settings){
        StringBuilder sb = new StringBuilder(300);
        int count = 0;
        for(AttributeTemplate attribute: attributes){
            if(!attribute.includeInDTO) continue;
            if( count++ > 0) sb.append(", ");

            sb.append(attribute.getAngularAttribute(settings, className, ""));
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

    public String getMappedAngularAttributes(Settings settings,   String mainAttributeName, String expectedAttributeNames) {
        if(attributes == null) return "";

        StringBuilder sb = new StringBuilder(300);
        if(expectedAttributeNames.contains(",id,") && !hasChildren){
            sb.append(" public " + mainAttributeName + "Id ?: number");
        }
        for(AttributeTemplate attribute: attributes){
            if(!attribute.includeInDTO) continue;
           if(expectedAttributeNames.contains("," + attribute.attributeName +  ",")){
               sb.append(", ");
               sb.append(attribute.getAngularAttribute(settings, this.className, mainAttributeName));
           }

        }

        return sb.toString();
    }

    public String getToString(){
        StringBuilder sb = new StringBuilder(300);

        int count = 0;
        for(AttributeTemplate attribute: attributes){
            if(!attribute.includeInDTO) continue;
            if(StringUtils.isEmpty(attribute.relationShipDTOAttributes)){
                if(count++ > 0) sb.append(" + ', ' + ");
                sb.append("'"+attribute.attributeName+":' + this."+attribute.attributeName+"" );
            }
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

    public void loadDerivedAttributes(Settings settings){
        for(AttributeTemplate attribute: attributes){
            if(!StringUtils.isEmpty(attribute.relationShipDTOAttributes)){
                attribute.derivedAttributes = settings.getDerivedAttributeNames(attribute.classTypeName, attribute.relationShipDTOAttributes, attribute.attributeName);
            }
        }
        if(!StringUtils.isEmpty(inheritedClassName) && inheritedClassName.length() > 1){
            attributes.addAll(settings.getClassAttributes(inheritedClassName));
        }
    }


    public List<AttributeTemplate> getDerivedAttributeNames(String expectedAttributeNames, String prefix) {
        List<AttributeTemplate> templateList = new ArrayList<>();
        String matchingAttributes = "," + expectedAttributeNames + ",";
        if(matchingAttributes.contains(",id,")){
            AttributeTemplate idTemplate = new AttributeTemplate(prefix + "Id",AttributeType.LONG);
            idTemplate.required = true;
            idTemplate.showOnGrid = false;
            templateList.add(idTemplate);
        }
        for(AttributeTemplate attribute: attributes){
            AttributeTemplate clone = new AttributeTemplate(attribute);
            clone.attributeName = prefix + StringUtils.capitalize(clone.attributeName);
           if(matchingAttributes.contains("," + attribute.attributeName + ",")) templateList.add(clone);
        }
        return templateList;
    }

}

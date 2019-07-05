package com.gmail.kasun.codegen.model;

import com.gmail.kasun.codegen.util.MiscUtils;
import com.gmail.kasun.codegen.util.TemplateUtils;
import com.gmail.kasun.codegen.util.TestDataHelper;
import org.apache.commons.lang3.StringUtils;

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
public class AttributeTemplate {
    public String attributeName;
    public String classTypeName;
    public AttributeType type = AttributeType.STRING;
    public CollectionType collectionType = CollectionType.None;
    public Integer min;
    public Integer max;
    public String keyType; //For maps
    public String validatorRegex;
    public Boolean required =  Boolean.FALSE;
    public Boolean showOnGrid = Boolean.TRUE;
    public Boolean includeInDTO  = Boolean.TRUE;
    public static String javaTemplateText;

    /** -------------- Constructors -------------- **/

    public AttributeTemplate(String attributeName) {
        this.attributeName = attributeName;
    }

    public AttributeTemplate(String attributeName, AttributeType type, String classTypeName) {
        this.attributeName = attributeName;
        this.type = type;
        this.classTypeName = classTypeName;
    }

    public AttributeTemplate(String attributeName, AttributeType type) {
        this.attributeName = attributeName;
        this.type = type;
    }

    public AttributeTemplate(String attributeName, AttributeType type, Integer min, Integer max) {
       this(attributeName, type, min, max, true);
    }

    public AttributeTemplate(String attributeName, AttributeType type, Integer min, Integer max, Boolean showOnGrid) {
        this.attributeName = attributeName;
        this.min = min;
        this.max = max;
        this.type = type;
        this.showOnGrid = showOnGrid;
    }

    public AttributeTemplate(String attributeName, AttributeType type, CollectionType collectionType) {
        this.attributeName = attributeName;
        this.type = type;
        this.collectionType = collectionType;
        this.showOnGrid = Boolean.FALSE;
    }

    public AttributeTemplate(String attributeName, AttributeType type, Integer min, Integer max, Boolean required, Boolean showOnUI, Boolean includeInDTO) {
        this.attributeName = attributeName;
        this.min = min;
        this.max = max;
        this.required = required;
        this.showOnGrid = showOnUI;
        this.includeInDTO = includeInDTO;
        this.type = type;
    }

    public String getDisplayName(){
        return StringUtils.capitalize(MiscUtils.splitCamelCase(attributeName, " "));
    }

    /** -------------- Java support methods -------------- **/

    public String getJavaAttribute(boolean isEntityClass, String className) throws Exception{
        if(StringUtils.isEmpty(javaTemplateText)) javaTemplateText = TemplateUtils.getInstance().readTemplateFile("javaAttribute", true);

        Map<String,String> attributeVariables = new HashMap<String,String>();
        attributeVariables.put("attributeName",attributeName);

        if(collectionType == null || collectionType == CollectionType.None){
            attributeVariables.put("type", getJavaName());
        }
        else {
            attributeVariables.put("type", collectionType.javaName.replaceAll("type", getJavaName()).replaceAll("key", keyType));
        }

        //Headers
        StringBuilder header = new StringBuilder(100);
        header.append(" ");
        if(isEntityClass && collectionType != null && collectionType != CollectionType.None && !type.isCustomClass()){
            header.append("\n\t@ElementCollection"); //Entity collection
        }
        addMinMaxJava(header, className);
        if(required){
            header.append("\n\t@NotNull(message = \"{" + className + "." + attributeName + ".required" +"}\")");
        }
        if(!StringUtils.isEmpty(validatorRegex)){
            header.append("\n\t@Pattern(regexp = \""+validatorRegex+"\", message = \"{"+ className + "." + attributeName + ".pattern}\")");
        }
        attributeVariables.put("attributeJavaHeader",header.toString());

        return TemplateUtils.getInstance().replaceVariables(javaTemplateText, attributeVariables);
    }


    public void addJavaAttributeResources(StringBuilder resources, String className, String humanClassName) throws Exception{
        addMinMaxResources(resources, className,humanClassName);
        if(required){
            resources.append("\n" + className + "." + attributeName + ".required=" + humanClassName + " " + attributeName + " is required");
        }
        if(!StringUtils.isEmpty(validatorRegex)){
            resources.append("\n"+ className + "." + attributeName + ".pattern=Expects " + humanClassName + " " + attributeName + " in valid format");
        }
    }

    private String getJavaName() {
        if(type == AttributeType.CLASS || type == AttributeType.ENUM) return classTypeName;
        else return type.javaName;
    }

    private void addMinMaxJava(StringBuilder header, String className) {
        boolean sizePresent = min != null || max != null;
        if(!sizePresent) return;
        if(type == AttributeType.STRING){
            header.append("\n\t@Size(");
            if(min != null){
                header.append("min = " + min);
                if(max != null) header.append(", max = " + max );
            } else if(max != null) header.append("max = " + max);
            header.append( ", message=\"{"+ className + "." + attributeName + ".length}\"" + ")");
        }else{
            if(min != null) header.append("\n\t@Min( value = " + min + ", message=\"{"+ className + "." + attributeName + ".min}\"" + ")");
            if(max != null) header.append("\n\t@Max( value = " + max + ", message=\"{"+ className + "." + attributeName + ".max}\"" + ")");
        }
    }

    private void addMinMaxResources(StringBuilder header, String className, String classHumanName) {
        boolean sizePresent = min != null || max != null;
        if(!sizePresent) return;


        if(type == AttributeType.STRING){
            header.append("\n" + className + "." + attributeName + ".length=");
            StringBuilder value = new StringBuilder(50);
            if(min != null){
                if(max != null) value.append(classHumanName + " length must be between " + min + " and " + max + " characters");
                else value.append(classHumanName + " must at least have " + min + " characters");
            } else if(max != null) value.append(classHumanName + " must not exceed " + min + " characters");
            header.append(value.toString());
        }else{
            if(min != null) header.append("\n"+ className + "." + attributeName + ".min=" +classHumanName + " " + attributeName +  " must not be smaller than " + min);
            if(max != null) header.append("\n"+ className + "." + attributeName + ".max=" +classHumanName + " " + attributeName +  " must not be larger than " + max);
        }
    }


    /** -------------- Angular support methods -------------- **/

    public String getAngularAttribute(){
        return "public " + attributeName +  "?: "
                + ((collectionType == null || collectionType == CollectionType.None) ?
                getAngularName() : collectionType.angularName.replaceAll("type", getAngularName()).replaceAll("key", keyType)
        );

    }

    private String getAngularName() {
        if(type == AttributeType.CLASS || type == AttributeType.ENUM ) return classTypeName;
        return type.angularName;
    }

    public String getAngularErrorMessage(){
        StringBuilder sb = new StringBuilder(200);
        sb.append(" ");
        if(required) sb.append(getDisplayName() + " is required.");
        if(min != null){
            if(type == AttributeType.STRING) sb.append(" Expects minimum of " + min + " characters " );
            else sb.append(" Expects minimum value of " + min );
        }
        if(max != null){
            if(type == AttributeType.STRING) sb.append(" Expects maximum of " + max + " characters " );
            else sb.append(" Expects maximum value of " + max );
        }
        return sb.toString();
    }

    /**
     * E.g name: ['', [Validators.required, Validators.minLength(4) ] ]
     */
    public void addToAngularFormBuilder(StringBuilder sb){
        int validatorCount = 0;
        sb.append(attributeName);
        sb.append(": [''" );

        //Add array of validator if any for attribute
        if(required) sb.append((validatorCount++ == 0 ? ", [": ", " ) + "Validators.required");

        if(min != null){
            sb.append((validatorCount++ == 0 ? ", [": ", " ));
            if(type == AttributeType.STRING) sb.append("Validators.minLength(" + min + ")");
            else  sb.append("Validators.min(" + min + ")");
        }

        if(max != null){
            sb.append((validatorCount++ == 0 ? ", [": ", " ));
            if(type == AttributeType.STRING) sb.append("Validators.maxLength(" + max + ")");
            else  sb.append("Validators.max(" + max + ")");
        }

        if(!StringUtils.isEmpty(validatorRegex)){
            sb.append((validatorCount++ == 0 ? ", [": ", " ) + "Validators.pattern('" + validatorRegex + "')");
        }
        if(validatorCount > 0) sb.append("]");//Closing validator array
        sb.append("]");//Close form group element
    }

    public void addAttributeFormElementHTML(Properties prop, StringBuilder sb){
        Map<String,String> attributeVariables = new HashMap<String,String>();
        attributeVariables.put("attributeName",attributeName);
        attributeVariables.put("attributeDisplayName",getDisplayName());
        attributeVariables.put("attributeError",getAngularErrorMessage());
        if(type == AttributeType.ENUM){
            attributeVariables.put("enumName", classTypeName);
            attributeVariables.put("enumVariableName", StringUtils.uncapitalize(classTypeName));
        }

        sb.append(TemplateUtils.getInstance().replaceVariables(prop.getProperty(getFormComponentName()), attributeVariables));
        sb.append("\n");
    }

    /**
     * Get relevant form html element as defined in properties based on attribute
     * @return
     */
    private String getFormComponentName(){
        if(collectionType != null && collectionType !=  CollectionType.None){
            return "attribute.form.input.select.multi";
        }
        if(type == AttributeType.ENUM) return "attribute.form.input.select.enum";
        if(type == AttributeType.INT || type == AttributeType.LONG || type == AttributeType.DOUBLE){
            return "attribute.form.input.number";
        }
        if(type == AttributeType.DATE || type == AttributeType.DATETIME){
            return "attribute.form.input.text";//@TODO correct input
        }
        if(max != null && max > 150) return "attribute.form.input.textarea";
        if(attributeName.toLowerCase().contains("password")) return "attribute.form.input.password";

        return "attribute.form.input.text";
    }

    public void addConstructorValues(Settings settings, StringBuilder sb, boolean valid){
        if(collectionType != null && collectionType != CollectionType.None) {
            sb.append(", null");
            return;
        }
      switch (type) {
          case INT:  sb.append(", ");sb.append(TestDataHelper.getInstance().getRandomIntegerValue(min, max)); break;
          case LONG: sb.append(", "); sb.append(TestDataHelper.getInstance().getRandomLongValue(min, max)); break;
          case DOUBLE: sb.append(", "); sb.append(TestDataHelper.getInstance().getRandomDoubleValue(min, max)); break;
          case STRING: sb.append(", "); sb.append("\""); sb.append(getStringAttributeValue(valid)); sb.append("\""); break;
          case DATE: sb.append(", ");  sb.append(TestDataHelper.getInstance().getRandomLocalDate()); break;
          case DATETIME: sb.append(", ");  sb.append(TestDataHelper.getInstance().getRandomLocalDateTime()); break;
          case TIME: sb.append(", ");  sb.append(TestDataHelper.getInstance().getRandomLocalTime()); break;
          case ENUM: sb.append(", ");  sb.append(settings.getRandomEnumValue(classTypeName)); break;
          case CLASS: sb.append(", ");  sb.append("null"); break;//@TODO implement in future as enhancement

      }
    }

    private String getStringAttributeValue(boolean valid){
        if(!valid){
            if(min != null && min > 1) return TestDataHelper.getInstance().getDescription(1);
            else if(max != null && max > 1) return TestDataHelper.getInstance().getDescription(max + 2);
            else if(required) return null;
            else if(!StringUtils.isEmpty(validatorRegex)) return "^2_sdf^%&)  (#!#%#@";
        }
        if(attributeName.toLowerCase().contains("name")) return TestDataHelper.getInstance().getRandomName(min, max);
        if(attributeName.toLowerCase().contains("item") || attributeName.toLowerCase().contains("product")) return TestDataHelper.getInstance().getRandomItemName(min, max);
        if(attributeName.toLowerCase().contains("number")) return TestDataHelper.getInstance().getRandomNumberStringWitLength(min, max);
        return TestDataHelper.getInstance().getRandomDescription(min, max);
    }


}

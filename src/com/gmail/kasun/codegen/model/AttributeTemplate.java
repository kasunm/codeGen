package com.gmail.kasun.codegen.model;

import com.gmail.kasun.codegen.util.MiscUtils;
import com.gmail.kasun.codegen.util.TemplateUtils;
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

    public String getJavaAttribute(boolean isEntityClass) throws Exception{
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
        addMinMaxJava(header);
        if(required) header.append("\n\t@NotNull");
        attributeVariables.put("attributeJavaHeader",header.toString());

        return TemplateUtils.getInstance().replaceVariables(javaTemplateText, attributeVariables);
    }

    private String getJavaName() {
        if(type == AttributeType.CLASS || type == AttributeType.ENUM) return classTypeName;
        else return type.javaName;
    }

    private void addMinMaxJava(StringBuilder header) {
        if(type == AttributeType.STRING){
            boolean sizePresent = min != null && max != null;
            if(sizePresent) header.append("\n\t@Size(");

            if(min != null){
                header.append("min = " + min);
                if(max != null) header.append(", max = " + max);
            } else if(max != null) header.append("max = " + max);
            if(sizePresent) header.append(")");
        }else{
            if(min != null) header.append("\n\t@Min( value = " + min + ")");
            if(max != null) header.append("\n\t@Max( value = " + max + ")");
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
        if(max != null && max > 150) return "attribute.form.input.textarea";
        if(attributeName.toLowerCase().contains("password")) return "attribute.form.input.password";

        return "attribute.form.input.text";
    }


}

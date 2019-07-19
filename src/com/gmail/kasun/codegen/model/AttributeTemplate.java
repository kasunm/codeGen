package com.gmail.kasun.codegen.model;

import com.gmail.kasun.codegen.util.MiscUtils;
import com.gmail.kasun.codegen.util.TemplateUtils;
import com.gmail.kasun.codegen.util.TestDataHelper;
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
    public RelationShip relationShip;
    public String relationShipDTOAttributes;

    protected List<AttributeTemplate> derivedAttributes;


    public static String javaTemplateText;

    /** -------------- Constructors -------------- **/

     public AttributeTemplate(AttributeTemplate another){
         this.attributeName = another.attributeName;
         this.classTypeName = another.classTypeName;
         this.type = another.type;
         this.collectionType = another.collectionType;
         this.min = another.min;
         this.max = another.max;
         this.keyType = another.keyType;
         this.validatorRegex = another.validatorRegex;
         this.required = another.required;
         this.showOnGrid = another.showOnGrid;
         this.includeInDTO = another.includeInDTO;
         this.relationShip = another.relationShip;
         this.relationShipDTOAttributes = another.relationShipDTOAttributes;
     }

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

    public String getJavaAttribute(Settings settings, boolean isEntityClass, String className, String parentAttributeName, boolean skipHeader) throws Exception{
        Map<String,String> attributeVariables = new HashMap<String,String>();
        return getJavaAttribute(settings, isEntityClass, className, parentAttributeName, skipHeader, attributeVariables);
    }

    public String getJavaAttribute(Settings settings, boolean isEntityClass, String className, String parentAttributeName, boolean skipHeader,  Map<String,String> attributeVariables) throws Exception{
        if(StringUtils.isEmpty(javaTemplateText)) javaTemplateText = TemplateUtils.getInstance().readTemplateFile("javaAttribute", true);
        if(skipHeader) javaTemplateText.replaceAll("private", "");


        if(!isEntityClass && relationShip != null && relationShip != RelationShip.NONE  &&  relationShip != RelationShip.OneToMany
                &&  relationShip != RelationShip.ManyToMany  && !StringUtils.isEmpty(relationShipDTOAttributes) && StringUtils.isEmpty(parentAttributeName)){
            //use mapped attributes if specified for DTO for ManyToOne, OneToOne,
            return settings.getMappedAttributeNames(this.classTypeName, className, attributeName,","+  relationShipDTOAttributes + ",");
        }

        if(!isEntityClass && !includeInDTO) return " ";

        if(StringUtils.isEmpty(parentAttributeName)) attributeVariables.put("attributeName",attributeName);
        else attributeVariables.put("attributeName",parentAttributeName + StringUtils.capitalize(attributeName));



        if(collectionType == null || collectionType == CollectionType.None){
            if(type == AttributeType.CLASS && !isEntityClass) attributeVariables.put("type", getJavaName() + "DTO");
            else attributeVariables.put("type", getJavaName());
        }
        else {
            if(type == AttributeType.CLASS && !isEntityClass) attributeVariables.put("type", collectionType.javaName.replaceAll("type", getJavaName()).replaceAll("key", keyType) + "DTO");
            attributeVariables.put("type", collectionType.javaName.replaceAll("type", getJavaName()).replaceAll("key", keyType));
        }


        //Headers
        StringBuilder header = new StringBuilder(100);
        header.append(" ");
        if(skipHeader) {
            attributeVariables.put("attributeJavaHeader",header.toString());
            return TemplateUtils.getInstance().replaceVariables(javaTemplateText, attributeVariables);
        }

        if(isEntityClass && collectionType != null && collectionType != CollectionType.None && !type.isCustomClass()){
            header.append("\n\t@ElementCollection"); //Entity collection
        }
        addMinMaxJava(header, className);
        if(required){
            header.append("\n\t@NotNull(message = \"{" + className + "." + attributeName  + ".required" +"}\")");
        }
        if(!StringUtils.isEmpty(validatorRegex)){
            header.append("\n\t@Pattern(regexp = \""+validatorRegex+"\", message = \"{"+ className + "." + attributeName + ".pattern}\")");
        }
        if(isEntityClass && relationShip != null) header.append("\n\t" + relationShip.name);

        if(isEntityClass && type == AttributeType.BLOB) header.append("\n\t@Lob");
        if(isEntityClass && type == AttributeType.CLOB) header.append("\n\t@Lob\n" +
                "    @Type(type = \"text\")");
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

    public String getAngularAttribute(Settings settings, String className,String parentAttributeName){
        if( relationShip != null && relationShip != RelationShip.NONE  &&  relationShip != RelationShip.OneToMany
                &&  relationShip != RelationShip.ManyToMany  && !StringUtils.isEmpty(relationShipDTOAttributes) && StringUtils.isEmpty(parentAttributeName)){
            //use mapped attributes if specified for DTO for ManyToOne, OneToOne,
            return settings.getMappedAngularAttributes(this.classTypeName, className, attributeName,","+  relationShipDTOAttributes + ",");
        }

        if(StringUtils.isEmpty(parentAttributeName)){
            return getPlainAngularAttribute("","", true);
        }else {
            return "public " + parentAttributeName + StringUtils.capitalize(attributeName) +  "?: "
                    + ((collectionType == null || collectionType == CollectionType.None) ?
                    getAngularName() : collectionType.angularName.replaceAll("type", getAngularName()).replaceAll("key", keyType)
            );

        }



    }

    public String getPlainAngularAttribute(String prefix, String postfix, boolean optional) {
        return "public " + prefix + attributeName + postfix + (optional ? "?: ": ": ")
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
        if(derivedAttributes != null && derivedAttributes.size() > 0){
            int count = derivedAttributes.size();
            for(AttributeTemplate derived: derivedAttributes){
                derived.addToAngularFormBuilder(sb);
                if(count-- > 1) sb.append(",");
                sb.append("\n");
            }
            return;
        }
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

    public void addAttributeFormElementHTML(Properties prop, StringBuilder sb, AttributeTemplate parent){
        Map<String,String> attributeVariables = new HashMap<String,String>();
        attributeVariables.put("attributeName",attributeName);
        attributeVariables.put("attributeDisplayName",getDisplayName());
        attributeVariables.put("attributeError",getAngularErrorMessage());
        if(type == AttributeType.ENUM){
            attributeVariables.put("enumName", classTypeName);
            attributeVariables.put("enumVariableName", StringUtils.uncapitalize(classTypeName));
        }

        if(parent != null){
            sb.append("<div class=\"form-group\">\n" +
                    "<label>" + MiscUtils.splitCamelCase(StringUtils.capitalize(attributeName), " ") + " &nbsp;</label>\n" +
                    "      <div class=\"btn-group\" dropdown>\n" +
                    "\n" +
                    "        <button dropdownToggle type=\"button\" class=\"btn btn-outline-secondary dropdown-toggle\"\n" +
                    "                aria-controls=\"dropdown-basic-"+ attributeName +"\" [class.is-invalid]=\"" + attributeName + ".invalid && " + attributeName + ".touched\"  type=\"text\"  name=\"" + attributeName + "\" id=\"" + attributeName + "\" placeholder=\"" + attributeName + "\">\n" +
                    "          {{" + attributeName + ".value || 'Select a " + attributeName + "'}} <span class=\"caret\"></span>\n" +
                    "        </button>\n" +
                    "        <ul id=\"dropdown-basic-"+ attributeName +"\" *dropdownMenu class=\"dropdown-menu\"\n" +
                    "            role=\"menu\" aria-labelledby=\"button-basic\"  >\n" +
                    "          <ng-container *ngFor=\"let " + StringUtils.uncapitalize(parent.classTypeName) + " of this.all" + parent.classTypeName + "s\">\n" +
                    "            <li role=\"menuitem\" class=\"dropdown-item\" (click)=\"set"+ StringUtils.capitalize(parent.attributeName)+"Id(" + StringUtils.uncapitalize(parent.classTypeName) + ".id, "
                    + StringUtils.uncapitalize(parent.classTypeName) + "." + extractOriginalAttributeName(parent) + ")\" value=\"{{"+StringUtils.uncapitalize(parent.classTypeName)+".id}}\">{{"+StringUtils.uncapitalize(parent.classTypeName)+"."+extractOriginalAttributeName(parent)+"}}</li>\n" +
                    "          </ng-container>\n" +
                    "        </ul>\n" +
                    "       <small class=\"text-danger\" *ngIf=\"" + attributeName + ".invalid  && " + attributeName + ".touched\">Error</small>\n" +
                    "      </div>\n" +
                    "</div>");
        }else {
            sb.append(TemplateUtils.getInstance().replaceVariables(prop.getProperty(getFormComponentName()), attributeVariables));
        }
        sb.append("\n");
    }

    private String extractOriginalAttributeName(AttributeTemplate parent) {
        return StringUtils.uncapitalize(attributeName.replaceAll(parent.attributeName, ""));
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
            if(attributeName.endsWith("Id")) return "attribute.form.input.id";
            return "attribute.form.input.number";
        }
        if(type == AttributeType.DATE){
            return "attribute.form.input.date";
        }
        if(type == AttributeType.TIME){
            return "attribute.form.input.time";
        }
        if(type == AttributeType.DATETIME){
            return "attribute.form.input.dateTime";//@TODO correct input
        }
        if(max != null && max > 150) return "attribute.form.input.textarea";
        if(attributeName.toLowerCase().contains("password")) return "attribute.form.input.password";

        return "attribute.form.input.text";
    }

    public void addConstructorValues(Settings settings, StringBuilder sb, String id, boolean valid){
        if(collectionType != null && collectionType != CollectionType.None) {
            if(type == AttributeType.CLASS || type == AttributeType.ENUM){
                sb.append(", new " + collectionType.defaultJavaInstance.replaceAll("type", classTypeName).replaceAll("key", keyType) + "()");
            }else{
                sb.append(", new " + collectionType.defaultJavaInstance.replaceAll("type", type.javaName).replaceAll("key", keyType) + "()");
            }
            return;
        }
        String dependantBeanName ;
        if(!valid){
            dependantBeanName = attributeName + "2";
        } else {
            dependantBeanName = attributeName + "1";
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
            case CLASS: sb.append(", ");  sb.append(dependantBeanName); break;
            case CLOB: sb.append(", "); sb.append("\""); sb.append(getStringAttributeValue(valid)); sb.append("\""); break;
            case BLOB: sb.append(", "); sb.append("\""); sb.append(getStringAttributeValue(valid)); sb.append("\""); break;

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

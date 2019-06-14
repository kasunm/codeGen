package com.gmail.kasun.codegen.model;

import org.apache.commons.lang3.StringUtils;

/**
 * <p>Title         : ${FILE_NAME}
 * <p>Project       : SpanCodeGenerator
 * <p>Description   :
 *
 * @author Kasun Madurasinghe
 * @version 1.0
 */
public enum AttributeType {
    STRING("String", "string"), INT("Integer", "number"), LONG("Long", "number"), DOUBLE("Double", "number"),  DATE("Date", "Date"), CLASS("Object", "any");

    public String javaName;
    public String angularName;
    private String typeClass;
    public boolean isEnum;

   AttributeType(String javaName, String angularName){
    this.javaName = javaName;
    this.angularName = angularName;
   }

   public void setTypeClass(String name, boolean isEnum){
       this.typeClass = name;
       this.javaName = name;
       this.angularName = name;
       this.isEnum = isEnum;
   }

   //@TODO in future 1..m m..1 change thins
   public boolean isCustomClass(){
       return  !StringUtils.isEmpty(typeClass) && !isEnum;
   }


}

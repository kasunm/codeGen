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
    STRING("String", "string"), INT("Integer", "number"), LONG("Long", "number"), DOUBLE("Double", "number"),  DATE("LocalDate", "Date"), DATETIME("LocalDateTime","Date"), TIME("LocalTime","string"), CLASS("Object", "any"), ENUM("Object", "any");

    public String javaName;
    public String angularName;


   AttributeType(String javaName, String angularName){
    this.javaName = javaName;
    this.angularName = angularName;
   }


   //@TODO in future 1..m m..1 change thins
   public boolean isCustomClass(){
       return  StringUtils.equals(angularName, "any");
   }


}

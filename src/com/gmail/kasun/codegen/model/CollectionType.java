package com.gmail.kasun.codegen.model;

/**
 * <p>Title         : ${FILE_NAME}
 * <p>Project       : SpanCodeGenerator
 * <p>Description   :
 *
 * @author Kasun Madurasinghe
 * @version 1.0
 */
public enum CollectionType {
    None("","", ""), List("List <type>", "Array <type>", "ArrayList<type>"), Set("Set <type>", "Array <type>", "HasSet <type>"), Map("Map <key, type>", "Map <key, type>", "HasMap <key, type>");

    public String javaName;
    public String angularName;
    public String defaultJavaInstance;

    CollectionType(String javaName,String angularName, String defaultJavaInstance){
        this.javaName = javaName;
        this.angularName = angularName;
        this.defaultJavaInstance = defaultJavaInstance;

    }
}

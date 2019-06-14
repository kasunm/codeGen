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
    None("",""), List("List <type>", "Array <type>"), Set("List <type>", "Array <type>"), Map("Map <key, type>", "Map <key, type>");

    public String javaName;
    public String angularName;

    CollectionType(String javaName,String angularName){
        this.javaName = javaName;
        this.angularName = angularName;
    }
}

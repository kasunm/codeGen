package com.gmail.kasun.codegen.model;

/**
 * <p>Title         : ${FILE_NAME}
 * <p>Project       : SpanCodeGenerator
 * <p>Description   :
 *
 * @author Kasun Madurasinghe
 * @version 1.0
 */
public class EnumTemplate {
    public String enumName;
    public String enumValues;


    public EnumTemplate(String enumName,String values ) {
        this.enumName = enumName;
        this.enumValues = values;
    }
}

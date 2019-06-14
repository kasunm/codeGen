package com.gmail.kasun.codegen.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    public String values;

    public EnumTemplate(String enumName,String values ) {
        this.enumName = enumName;
        this.values = values;
    }
}

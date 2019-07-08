package com.gmail.kasun.codegen.model;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * <p>Title         : ${FILE_NAME}
 * <p>Project       : SpanCodeGenerator
 * <p>Description   :
 *
 * @author Kasun Madurasinghe
 * @version 1.0
 */
public class SearchOption {
    public String methodName; //Expects repo findName
    public String attributeNames;
    public String searchUrl;
    public String searchQuery; //Optional query

    /**
     * Get URL for controller
     * @return String
     */
    public String getURL(){
        if(StringUtils.isEmpty(attributeNames)) return searchUrl;
        return searchUrl + "/" + attributeNames.replaceAll(",", "/");
    }

    public String getParameters(List<AttributeTemplate> attributeTemplateList){
        StringBuilder params = new StringBuilder(40);
        for(AttributeTemplate attributeTemplate: attributeTemplateList){
            //params.append(attributeTemplate.getJavaAttribute())

        }
        return params.toString();
    }

}

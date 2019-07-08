package com.gmail.kasun.codegen.model;

/**
 * <p>Title         : ${FILE_NAME}
 * <p>Project       : SpanCodeGenerator
 * <p>Description   :
 *
 * @author Kasun Madurasinghe
 * @version 1.0
 */
public class SearchTemplates {
    public static String controller = "\t/**\n" +
            "     * Find  ${classVariableName}s by ${attributeNames}\n" +
            "${paramComment}     \n" +
            "     * @return List<${className}>\n" +
            "     */\n" +
            "    @GetMapping(path = \"${searchUrl}\")\n" +
            "    public ResponseEntity<List<${className}>> ${methodName}(${args}){\n" +
            "        logger.debug(\"${methodName}(\" + ${attributeValues} + \")\");\n" +
            "${argsValidate}\n" +
            "        List<${className}> ${classVariableName}s =  ${classVariableName}Service.${methodName}(${attributeNames});\n" +
            "        if(${classVariableName}s == null || ${classVariableName}s.size() < 1) throw new ResourceNotFoundException(\"Unable to find any ${classVariableName}s matching criteria\");\n" +
            "        return new ResponseEntity(${classVariableName}s, HttpStatus.ACCEPTED);\n" +
            "    }";

    public static String service = "\t/**\n" +
            "     * Find  ${classVariableName}s by ${attributeNames}\n" +
            "{paramComment}     \n" +
            "     * @return List<${className}>\n" +
            "     */\n" +
            "    public List<${className}> ${methodName}(${args}){\n" +
            "${argsValidate}\n" +
            "        return ${classVariableName}Repository.${methodName}(${attributeNames});\n" +
            "    }";


    public static String serviceInterface = "\t/**\n" +
            "     * Find  ${classVariableName}s by ${attributeNames}\n" +
            "${paramComment}     \n" +
            "     * @return List<${className}>\n" +
            "     */\n" +
            "    public List<${className}> ${methodName}(${args});\n";

    public static String repo = "List<${className}> ${methodName}(${args});";
}

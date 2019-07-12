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
            "    public ResponseEntity<List<${className}>> ${methodName}(${argsController}){\n" +
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

    public static String controllerTest = "    @Test\n" +
            "    public void ${methodName}Test() throws Exception{\n" +
            "        ArrayList<User> matchedUsers = new ArrayList<>(1);\n" +
            "        matchedUsers.add(this.users.get(0));\n" +
            "        given(userService.${methodName}(${anyDataValues})).willReturn(matchedUsers);\n" +
            "        mockMvc.perform(MockMvcRequestBuilders.get(\"/${classURL}/${searchUrl}\" , ${testDataValues})\n" +
            "                    .accept(MediaType.APPLICATION_JSON)).andExpect(status().isAccepted())\n" +
            "                .andExpect(jsonPath(\"$\", hasSize(1)));\n" +
            "\n" +
            "\n" +
            "    }\n" +
            "\n" +
            "    @Test\n" +
            "    public void ${methodName}InvalidParamTest() throws Exception{\n" +
            "        ArrayList<User> matchedUsers = new ArrayList<>(1);\n" +
            "        matchedUsers.add(this.users.get(0));\n" +
            "        given(userService.${methodName}(${anyDataValues})).willReturn(matchedUsers);\n" +
            "        mockMvc.perform(MockMvcRequestBuilders.get(\"/${classURL}/${searchUrl}\", ${nullDataValues})\n" +
            "                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest()) ;\n" +
            "\n" +
            "    }\n" +
            "\n" +
            "    @Test\n" +
            "    public void ${methodName}NoMatchTest() throws Exception{\n" +
            "        given(userService.${methodName}(${anyDataValues})).willReturn(null);\n" +
            "        mockMvc.perform(MockMvcRequestBuilders.get(\"/${classURL}/${searchUrl}\"  , ${testDataValues})\n" +
            "                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound()) ;\n" +
            "\n" +
            "    }\n" +
            "\t";

    public static String serviceSearchTest = " \t\n" +
            "\t@Test\n" +
            "    public void ${methodName}Test() throws Exception{\n" +
            "        ArrayList<${className}> matched${className}s = new ArrayList<>(1);\n" +
            "        matched${className}s.add(this.${classVariableName}s.get(0));\n" +
            "        given(repository.${methodName}(${anyDataValues})).willReturn(matched${className}s);\n" +
            "        List <${className}> result${className}s = ${classVariableName}Service.${methodName}(${testDataValues});\n" +
            "        Assert.assertNotNull(result${className}s);\n" +
            "        Assert.assertTrue(result${className}s.size() > 0);\n" +
            "        Assert.assertTrue(result${className}s.get(0).getId() == this.${classVariableName}s.get(0).getId());\n" +
            "\n" +
            "    }\n" +
            "\n" +
            "    @Test\n" +
            "    public void ${methodName}InvalidParamTest() throws Exception{\n" +
            "        ArrayList<${className}> matched${className}s = new ArrayList<>(1);\n" +
            "        matched${className}s.add(this.${classVariableName}s.get(0));\n" +
            "        given(repository.${methodName}(${anyDataValues})).willReturn(matched${className}s);\n" +
            "        List <${className}> result${className}s = null;\n" +
            "        try {\n" +
            "            result${className}s = ${classVariableName}Service.${methodName}(${nullDataValues});\n" +
            "        } catch (IllegalArgumentException e) {\n" +
            "            Assert.assertTrue(\"Invalid parameters passed\", e != null && result${className}s == null);\n" +
            "        }\n" +
            "    }";

    public static String reposSearchTest = "\t\n" +
            "\t@Test\n" +
            "    public void findAllByNameAndAgeSuccess(){\n" +
            "        List<${className}> results = repository.${methodName}(${testDataValues});\n" +
            "        Assert.assertNotNull(${classVariableName}s);\n" +
            "        Assert.assertTrue(\"Expect 1 data element to be returned\", ${classVariableName}s.size() > 0);\n" +
            "    }\n" +
            "\n" +
            "    @Test\n" +
            "    public void ${methodName}NullFail(){\n" +
            "        try {\n" +
            "            repository.${methodName}(${nullDataValues});\n" +
            "        } catch (InvalidDataAccessApiUsageException e) {\n" +
            "            Assert.assertTrue(\"Expects InvalidDataAccessApiUsageException\", e != null);\n" +
            "        }\n" +
            "    }";
}

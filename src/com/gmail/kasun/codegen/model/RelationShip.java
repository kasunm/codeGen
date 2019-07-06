package com.gmail.kasun.codegen.model;

/**
 * <p>Title         : ${FILE_NAME}
 * <p>Project       : SpanCodeGenerator
 * <p>Description   :
 *
 * @author Kasun Madurasinghe
 * @version 1.0
 */
public enum RelationShip {
    NONE(" "), ManyToOne("@ManyToOne"), OneToMany("@OneToMany"), OneToOne("@OneToOne"), ManyToMany("@ManyToMany");
    String name;
    RelationShip(String name){
        this.name = name;
    }
}

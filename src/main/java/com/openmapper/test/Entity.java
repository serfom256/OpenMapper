package com.openmapper.test;

public class Entity {

    private Integer id;
    private String name;
    private String email;
    private boolean isEmployee;

    public Entity(Integer id, String name, String email, boolean isEmployee) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.isEmployee = isEmployee;
    }

    public Entity() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEmployee() {
        return isEmployee;
    }

    public void setEmployee(boolean employee) {
        isEmployee = employee;
    }
}

package com.devhunter.bounty.model.enums;

public enum UserRole {
    HUNTER("hunter"),
    MASTER("master"),
    ADMIN("admin");

    private String role;

    UserRole(String role){
        this.role = role;
    }
    public String getRole(){
        return this.role;
    }
}


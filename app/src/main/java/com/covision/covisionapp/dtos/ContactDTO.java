package com.covision.covisionapp.dtos;

public class ContactDTO {

    private String name, number;

    public ContactDTO(String name, String number) {
        this.setName(name);
        this.setNumber(number);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }
}

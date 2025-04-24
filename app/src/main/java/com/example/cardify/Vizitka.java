package com.example.cardify;

import java.io.Serializable;

public class Vizitka implements Serializable {
    public String TG;
    public String companyName;
    public String companySpec;
    public String description;
    public String email;
    public String phone;
    public String site;

    public Vizitka() {} // Пустой конструктор обязателен для Firebase

    public Vizitka(String TG, String companyName, String companySpec, String description,
                   String email, String phone, String site) {
        this.TG = TG;
        this.companyName = companyName;
        this.companySpec = companySpec;
        this.description = description;
        this.email = email;
        this.phone = phone;
        this.site = site;
    }
}


package com.example.cardify;

import java.io.Serializable;

public class Vizitka implements Serializable {
    public String id; // <-- добавлено поле ID

    public String TG;
    public String companyName;
    public String companySpec;
    public String description;
    public String email;
    public String phone;
    public String site;
    public String creatorId; // <-- поле для хранения ID создателя визитки

    public Vizitka() {} // Пустой конструктор обязателен для Firebase

    public Vizitka(String id, String TG, String companyName, String companySpec, String description,
                   String email, String phone, String site, String creatorId) {
        this.id = id;
        this.TG = TG;
        this.companyName = companyName;
        this.companySpec = companySpec;
        this.description = description;
        this.email = email;
        this.phone = phone;
        this.site = site;
        this.creatorId = creatorId;
    }

    public String getCompanyName() {
        return companyName != null ? companyName : "";
    }

    public String getSpecialization() {
        return companySpec != null ? companySpec : "";
    }
}


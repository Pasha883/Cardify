package com.example.cardify;

import java.io.Serializable;

public class VizitkaCreated implements Serializable {
    public String TG;
    public String companyName;
    public String companySpec;
    public String description;
    public String email;
    public String phone;
    public String site;

    public VizitkaCreated() {} // Пустой конструктор обязателен для Firebase

    public VizitkaCreated(String TG, String companyName, String companySpec, String description,
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


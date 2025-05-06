package com.example.cardify;

import java.io.Serializable;

public class VizitkaCreated implements Serializable {
    public String id;
    public String TG;
    public String companyName;
    public String companySpec;
    public String description;
    public String email;
    public String phone;
    public String site;
    public int users = 0; // <-- добавили поле users
    public String creatorId; // <-- поле для хранения ID создателя визитки

    public VizitkaCreated() {}

    public VizitkaCreated(String id, String TG, String companyName, String companySpec, String description,
                          String email, String phone, String site, int users, String creatorId) {
        this.id = id;
        this.TG = TG;
        this.companyName = companyName;
        this.companySpec = companySpec;
        this.description = description;
        this.email = email;
        this.phone = phone;
        this.site = site;
        this.users = users;
        this.creatorId = creatorId;
    }
}

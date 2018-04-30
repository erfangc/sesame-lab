package com.erfangc.sesamelab.corpus.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class EntityConfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String type;
    private String displayName;
    private String color;
    private String textColor;

    public Long getId() {
        return id;
    }

    public EntityConfiguration setId(Long id) {
        this.id = id;
        return this;
    }

    public String getType() {
        return type;
    }

    public EntityConfiguration setType(String type) {
        this.type = type;
        return this;
    }

    public String getDisplayName() {
        return displayName;
    }

    public EntityConfiguration setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public String getColor() {
        return color;
    }

    public EntityConfiguration setColor(String color) {
        this.color = color;
        return this;
    }

    public String getTextColor() {
        return textColor;
    }

    public EntityConfiguration setTextColor(String textColor) {
        this.textColor = textColor;
        return this;
    }

}

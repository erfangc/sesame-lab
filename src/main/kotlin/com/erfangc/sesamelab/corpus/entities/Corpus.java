package com.erfangc.sesamelab.corpus.entities;

import com.erfangc.sesamelab.model.entities.NERModel;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.List;

@Entity
public class Corpus {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String title;
    private String userID;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    private List<EntityConfiguration> entityConfigurations;
    @JsonIgnore
    @OneToMany(mappedBy = "corpus")
    private List<NERModel> nerModels;

    public Long getId() {
        return id;
    }

    public Corpus setId(Long id) {
        this.id = id;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Corpus setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getUserID() {
        return userID;
    }

    public Corpus setUserID(String userID) {
        this.userID = userID;
        return this;
    }

    public List<EntityConfiguration> getEntityConfigurations() {
        return entityConfigurations;
    }

    public Corpus setEntityConfigurations(List<EntityConfiguration> entityConfigurations) {
        this.entityConfigurations = entityConfigurations;
        return this;
    }

    public List<NERModel> getNerModels() {
        return nerModels;
    }

    public Corpus setNerModels(List<NERModel> nerModels) {
        this.nerModels = nerModels;
        return this;
    }
}

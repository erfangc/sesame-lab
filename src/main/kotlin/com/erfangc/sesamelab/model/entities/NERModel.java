package com.erfangc.sesamelab.model.entities;

import com.erfangc.sesamelab.corpus.entities.Corpus;

import javax.persistence.*;

@Entity
public class NERModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String userID;
    private String createdOn;
    private String fileLocation;
    @ManyToOne
    private Corpus corpus;

    public Long getId() {
        return id;
    }

    public NERModel setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public NERModel setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public NERModel setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getUserID() {
        return userID;
    }

    public NERModel setUserID(String userID) {
        this.userID = userID;
        return this;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public NERModel setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
        return this;
    }

    public String getFileLocation() {
        return fileLocation;
    }

    public NERModel setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
        return this;
    }

    public Corpus getCorpus() {
        return corpus;
    }

    public NERModel setCorpus(Corpus corpus) {
        this.corpus = corpus;
        return this;
    }
}

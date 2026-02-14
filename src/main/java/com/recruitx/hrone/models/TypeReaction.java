package com.recruitx.hrone.models;

public class TypeReaction {
    private String codeTypeReaction;
    private String descriptionReaction;

    // Constructeurs
    public TypeReaction() {}

    public TypeReaction(String codeTypeReaction, String descriptionReaction) {
        this.codeTypeReaction = codeTypeReaction;
        this.descriptionReaction = descriptionReaction;
    }

    // Getters et Setters
    public String getCodeTypeReaction() { return codeTypeReaction; }
    public void setCodeTypeReaction(String codeTypeReaction) { this.codeTypeReaction = codeTypeReaction; }

    public String getDescriptionReaction() { return descriptionReaction; }
    public void setDescriptionReaction(String descriptionReaction) { this.descriptionReaction = descriptionReaction; }

    @Override
    public String toString() {
        return codeTypeReaction + " - " + descriptionReaction;
    }
}
package com.example.appeng.Model;

import java.io.Serializable;

public class Vocab implements Serializable {
    private int id;
    private String word;
    private String meaning;
    private String phonetic;
    private String example;
    private String synonym;
    private String antonym;
    private String image;
    private String level;
    private int mistakeCount;
    private String lastMistakeTime;

    public Vocab() {}

    public Vocab(int id, String word, String meaning, String phonetic,
                 String example, String synonym, String antonym, String image) {

        this.id = id;
        this.word = word;
        this.meaning = meaning;
        this.phonetic = phonetic;
        this.example = example;
        this.synonym = synonym;
        this.antonym = antonym;
        this.image = image;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }

    public String getMeaning() { return meaning; }
    public void setMeaning(String meaning) { this.meaning = meaning; }

    public String getPhonetic() { return phonetic; }
    public void setPhonetic(String phonetic) { this.phonetic = phonetic; }

    public String getExample() { return example; }
    public void setExample(String example) { this.example = example; }

    public String getSynonym() { return synonym; }
    public void setSynonym(String synonym) { this.synonym = synonym; }

    public String getAntonym() { return antonym; }
    public void setAntonym(String antonym) { this.antonym = antonym; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public int getMistakeCount() { return mistakeCount; }
    public void setMistakeCount(int mistakeCount) { this.mistakeCount = mistakeCount; }

    public String getLastMistakeTime() { return lastMistakeTime; }
    public void setLastMistakeTime(String lastMistakeTime) { this.lastMistakeTime = lastMistakeTime; }
}

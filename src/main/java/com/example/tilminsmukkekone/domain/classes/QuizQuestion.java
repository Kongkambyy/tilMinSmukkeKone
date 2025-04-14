package com.example.tilminsmukkekone.domain.classes;

import com.example.tilminsmukkekone.domain.enums.DifficultyType;
import com.example.tilminsmukkekone.domain.enums.QuizCategory;

import java.util.List;

public class QuizQuestion {
    private Long id;
    private String questionText;
    private List<String> possibleAnswers;
    private int correctAnswerIndex;
    private String explanation;
    private DifficultyType difficulty;
    private QuizCategory category;

    // Getters
    public Long getId() {
        return id;
    }

    public String getQuestionText() {
        return questionText;
    }

    public List<String> getPossibleAnswers() {
        return possibleAnswers;
    }

    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }

    public String getExplanation() {
        return explanation;
    }

    public DifficultyType getDifficulty() {
        return difficulty;
    }

    public QuizCategory getCategory() {
        return category;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public void setPossibleAnswers(List<String> possibleAnswers) {
        this.possibleAnswers = possibleAnswers;
    }

    public void setCorrectAnswerIndex(int correctAnswerIndex) {
        this.correctAnswerIndex = correctAnswerIndex;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public void setDifficulty(DifficultyType difficulty) {
        this.difficulty = difficulty;
    }

    public void setCategory(QuizCategory category) {
        this.category = category;
    }
}
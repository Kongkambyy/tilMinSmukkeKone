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
}

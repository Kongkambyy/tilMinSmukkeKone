package com.example.tilminsmukkekone.util.Exceptions;

public class QuizException extends RuntimeException {
    public QuizException(String message) {
        super(message);
    }

    public static QuizException invalidAnswerFormatException() {
        return new QuizException("Answer format is invalid");
    }

    public static QuizException incompleteQuizException() {
        return new QuizException("Quiz is incomplete. All questions must be answered.");
    }
}

package com.example.tilminsmukkekone.infrastructure.repositories;

import com.example.tilminsmukkekone.domain.classes.QuizQuestion;
import com.example.tilminsmukkekone.domain.enums.DifficultyType;
import com.example.tilminsmukkekone.domain.enums.QuizCategory;
import com.example.tilminsmukkekone.infrastructure.util.DatabaseException;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class QuizQuestionDB {

    private final DatabaseOperations dbOps;

    public QuizQuestionDB(DatabaseOperations dbOps) {
        this.dbOps = dbOps;
    }

    private QuizQuestion mapResultSetToQuizQuestion(ResultSet rs) {
        try {
            QuizQuestion question = new QuizQuestion();

            question.setId(rs.getLong("id"));
            question.setQuestionText(rs.getString("question_text"));
            question.setExplanation(rs.getString("explanation"));
            question.setCorrectAnswerIndex(rs.getInt("correct_answer_index"));
            question.setDifficulty(DifficultyType.valueOf(rs.getString("difficulty")));
            question.setCategory(QuizCategory.valueOf(rs.getString("category")));

            return question;
        } catch (SQLException e) {
            throw new DatabaseException("Error mapping database row to QuizQuestion object",
                    "mapping", "quiz_question", e);
        }
    }

    private List<String> findPossibleAnswersByQuestionId(Long questionId) {
        try {
            String sql = "SELECT answer_text FROM quiz_answers WHERE question_id = ? ORDER BY answer_index";
            return dbOps.executeQuery(sql, new Object[]{questionId}, rs -> {
                try {
                    return rs.getString("answer_text");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (SQLException e) {
            throw DatabaseException.readError("quiz_answers", questionId, e);
        }
    }

    private void savePossibleAnswers(Long questionId, List<String> possibleAnswers) {
        try {
            String sql = "INSERT INTO quiz_answers (question_id, answer_text, answer_index) VALUES (?, ?, ?)";
            List<Object[]> batchParams = new ArrayList<>();

            for (int i = 0; i < possibleAnswers.size(); i++) {
                batchParams.add(new Object[]{questionId, possibleAnswers.get(i), i});
            }

            dbOps.executeBatch(sql, batchParams);
        } catch (SQLException e) {
            throw DatabaseException.writeError("quiz_answers", e);
        }
    }

    public Optional<QuizQuestion> findById(Long id) {
        try {
            String sql = "SELECT * FROM quiz_questions WHERE id = ?";
            QuizQuestion question = dbOps.executeQueryForSingleResult(sql, new Object[]{id}, this::mapResultSetToQuizQuestion);

            if (question != null) {
                question.setPossibleAnswers(findPossibleAnswersByQuestionId(id));
            }

            return Optional.ofNullable(question);
        } catch (SQLException e) {
            throw DatabaseException.readError("quiz_question", id, e);
        }
    }

    public List<QuizQuestion> findAll() {
        try {
            String sql = "SELECT * FROM quiz_questions";
            List<QuizQuestion> questions = dbOps.executeQuery(sql, null, this::mapResultSetToQuizQuestion);

            for (QuizQuestion question : questions) {
                question.setPossibleAnswers(findPossibleAnswersByQuestionId(question.getId()));
            }

            return questions;
        } catch (SQLException e) {
            throw DatabaseException.createError("read_all", "quiz_question", e);
        }
    }

    public List<QuizQuestion> findByCategory(QuizCategory category) {
        try {
            String sql = "SELECT * FROM quiz_questions WHERE category = ?";
            List<QuizQuestion> questions = dbOps.executeQuery(sql, new Object[]{category.name()}, this::mapResultSetToQuizQuestion);

            for (QuizQuestion question : questions) {
                question.setPossibleAnswers(findPossibleAnswersByQuestionId(question.getId()));
            }

            return questions;
        } catch (SQLException e) {
            throw DatabaseException.readError("quiz_questions_by_category", category.name(), e);
        }
    }

    public List<QuizQuestion> findByDifficulty(DifficultyType difficulty) {
        try {
            String sql = "SELECT * FROM quiz_questions WHERE difficulty = ?";
            List<QuizQuestion> questions = dbOps.executeQuery(sql, new Object[]{difficulty.name()}, this::mapResultSetToQuizQuestion);

            for (QuizQuestion question : questions) {
                question.setPossibleAnswers(findPossibleAnswersByQuestionId(question.getId()));
            }

            return questions;
        } catch (SQLException e) {
            throw DatabaseException.readError("quiz_questions_by_difficulty", difficulty.name(), e);
        }
    }

    public List<QuizQuestion> getRandomQuestions(int count) {
        try {
            String sql = "SELECT * FROM quiz_questions ORDER BY RAND() LIMIT ?";
            List<QuizQuestion> questions = dbOps.executeQuery(sql, new Object[]{count}, this::mapResultSetToQuizQuestion);

            for (QuizQuestion question : questions) {
                question.setPossibleAnswers(findPossibleAnswersByQuestionId(question.getId()));
            }

            return questions;
        } catch (SQLException e) {
            throw DatabaseException.readError("random_quiz_questions", count, e);
        }
    }

    public Long save(QuizQuestion question) {
        try {
            String sql = "INSERT INTO quiz_questions (question_text, explanation, correct_answer_index, " +
                    "difficulty, category) VALUES (?, ?, ?, ?, ?)";

            Object[] params = new Object[]{
                    question.getQuestionText(),
                    question.getExplanation(),
                    question.getCorrectAnswerIndex(),
                    question.getDifficulty().name(),
                    question.getCategory().name()
            };

            Long questionId = dbOps.executeInsertAndGetId(sql, params);

            if (question.getPossibleAnswers() != null && !question.getPossibleAnswers().isEmpty()) {
                savePossibleAnswers(questionId, question.getPossibleAnswers());
            }

            return questionId;
        } catch (SQLException e) {
            throw DatabaseException.writeError("quiz_question", e);
        }
    }

    public boolean update(QuizQuestion question) {
        try {
            String sql = "UPDATE quiz_questions SET question_text = ?, explanation = ?, " +
                    "correct_answer_index = ?, difficulty = ?, category = ? WHERE id = ?";

            Object[] params = new Object[]{
                    question.getQuestionText(),
                    question.getExplanation(),
                    question.getCorrectAnswerIndex(),
                    question.getDifficulty().name(),
                    question.getCategory().name(),
                    question.getId()
            };

            int rowsAffected = dbOps.executeUpdate(sql, params);

            if (question.getPossibleAnswers() != null) {
                String deleteAnswersSql = "DELETE FROM quiz_answers WHERE question_id = ?";
                dbOps.executeUpdate(deleteAnswersSql, new Object[]{question.getId()});

                savePossibleAnswers(question.getId(), question.getPossibleAnswers());
            }

            return rowsAffected > 0;
        } catch (SQLException e) {
            throw DatabaseException.updateError("quiz_question", question.getId(), e);
        }
    }

    public boolean deleteById(Long id) {
        try {
            String deleteAnswersSql = "DELETE FROM quiz_answers WHERE question_id = ?";
            dbOps.executeUpdate(deleteAnswersSql, new Object[]{id});

            String deleteUserAnswersSql = "DELETE FROM quiz_user_answers WHERE question_id = ?";
            dbOps.executeUpdate(deleteUserAnswersSql, new Object[]{id});

            String sql = "DELETE FROM quiz_questions WHERE id = ?";
            int rowsAffected = dbOps.executeUpdate(sql, new Object[]{id});

            return rowsAffected > 0;
        } catch (SQLException e) {
            throw DatabaseException.deleteError("quiz_question", id, e);
        }
    }

    public List<QuizQuestion> getQuestionsForQuiz(QuizCategory category, DifficultyType difficulty, int count) {
        try {
            StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM quiz_questions");
            List<Object> params = new ArrayList<>();

            if (category != null || difficulty != null) {
                sqlBuilder.append(" WHERE");

                if (category != null) {
                    sqlBuilder.append(" category = ?");
                    params.add(category.name());

                    if (difficulty != null) {
                        sqlBuilder.append(" AND");
                    }
                }

                if (difficulty != null) {
                    sqlBuilder.append(" difficulty = ?");
                    params.add(difficulty.name());
                }
            }

            sqlBuilder.append(" ORDER BY RAND() LIMIT ?");
            params.add(count);

            List<QuizQuestion> questions = dbOps.executeQuery(
                    sqlBuilder.toString(),
                    params.toArray(),
                    this::mapResultSetToQuizQuestion
            );

            for (QuizQuestion question : questions) {
                question.setPossibleAnswers(findPossibleAnswersByQuestionId(question.getId()));
            }

            return questions;
        } catch (SQLException e) {
            throw DatabaseException.readError("quiz_game_questions",
                    "category:" + (category != null ? category.name() : "any") +
                            ",difficulty:" + (difficulty != null ? difficulty.name() : "any") +
                            ",count:" + count, e);
        }
    }
}
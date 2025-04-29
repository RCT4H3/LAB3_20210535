package com.carlosdev.teletrivia;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TriviaQuestion {
    private String question;
    private String correct_answer;
    private List<String> incorrect_answers;

    public String getQuestion() { return question; }
    public String getCorrectAnswer() { return correct_answer; }
    public List<String> getAllAnswersShuffled() {
        List<String> allAnswers = new ArrayList<>(incorrect_answers);
        allAnswers.add(correct_answer);
        Collections.shuffle(allAnswers);
        return allAnswers;
    }
}

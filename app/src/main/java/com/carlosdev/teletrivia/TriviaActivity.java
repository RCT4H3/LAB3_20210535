package com.carlosdev.teletrivia;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.*;


import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class TriviaActivity extends AppCompatActivity {

    private TextView textViewQuestion, textViewTimer;
    private RadioGroup radioGroupOptions;
    private Button btnNext;

    private TextView numQuestion;


    private TextView categoryTextView;
    private List<TriviaQuestion> questions;
    private int currentQuestionIndex = 0;
    private int correctCount = 0;
    private int incorrectCount = 0;
    private int unansweredCount = 0;
    private int totalTimeInSeconds;
    private Thread timerThread;
    private boolean running = true;

    private Handler mainHandler = new Handler(Looper.getMainLooper());

    static class TriviaQuestion {
        String question;
        @SerializedName("correct_answer")
        String correctAnswer;
        @SerializedName("incorrect_answers")
        List<String> incorrectAnswers;

        List<String> getShuffledAnswers() {
            List<String> allAnswers = new ArrayList<>(incorrectAnswers);
            allAnswers.add(correctAnswer);
            Collections.shuffle(allAnswers);
            return allAnswers;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trivia);

        textViewQuestion = findViewById(R.id.textViewQuestion);
        textViewTimer = findViewById(R.id.textViewTimer);
        categoryTextView = findViewById(R.id.category);
        numQuestion = findViewById(R.id.numQuestion);
        radioGroupOptions = findViewById(R.id.radioGroupOptions);
        btnNext = findViewById(R.id.btnNext);



        int cantidad = getIntent().getIntExtra("cantidad", 5);
        String categoria = getIntent().getStringExtra("categoria");
        String dificultad = getIntent().getStringExtra("dificultad");
        categoryTextView.setText("Categoría: " + categoria);

        totalTimeInSeconds = cantidad * getTimePerQuestion(dificultad);
        startTimer();

        String dificultadApi = mapDificultad(dificultad);
        fetchQuestions(cantidad, categoria, dificultadApi);


        btnNext.setOnClickListener(v -> handleNext());
    }

    private void fetchQuestions(int amount, String category, String difficulty) {
        new Thread(() -> {
            try {
                int categoryId = mapCategoria(category);
                String urlStr = String.format(
                        "https://opentdb.com/api.php?amount=%d&category=%d&difficulty=%s&type=multiple",
                        amount, categoryId, difficulty
                );

                Log.d("TriviaAPI", "URL generada: " + urlStr);

                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder jsonResult = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    jsonResult.append(line);
                }
                in.close();

                Log.d("TriviaAPI", "JSON recibido: " + jsonResult);

                JSONObject response = new JSONObject(jsonResult.toString());
                JSONArray results = response.getJSONArray("results");

                Log.d("TriviaAPI", "Cantidad de preguntas: " + results.length());

                questions = new ArrayList<>();
                Gson gson = new Gson();
                for (int i = 0; i < results.length(); i++) {
                    TriviaQuestion q = gson.fromJson(results.getJSONObject(i).toString(), TriviaQuestion.class);
                    questions.add(q);
                }

                int responseCode = response.getInt("response_code");
                if (responseCode != 0) {
                    Log.e("TriviaAPI", "Código de respuesta: " + responseCode);
                    mainHandler.post(() -> Toast.makeText(this, "No se encontraron preguntas. Ajusta los filtros.", Toast.LENGTH_LONG).show());
                    return;
                }


                mainHandler.post(this::loadQuestion);

            } catch (Exception e) {
                Log.e("TriviaAPI", "Error al obtener preguntas", e);
            }
        }).start();
    }

    private void loadQuestion() {
        btnNext.setEnabled(false);
        radioGroupOptions.removeAllViews();

        if (currentQuestionIndex >= questions.size()) {
            endGame();
            return;
        }

        TriviaQuestion current = questions.get(currentQuestionIndex);
        numQuestion.setText("Pregunta " + (currentQuestionIndex + 1) + "/" + questions.size());

        textViewQuestion.setText(android.text.Html.fromHtml(current.question));

        for (String option : current.getShuffledAnswers()) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(android.text.Html.fromHtml(option));
            radioGroupOptions.addView(radioButton);
        }

        radioGroupOptions.setOnCheckedChangeListener((group, checkedId) -> {
            btnNext.setEnabled(true);
        });
    }

    private void handleNext() {
        RadioButton selectedButton = findViewById(radioGroupOptions.getCheckedRadioButtonId());
        if (selectedButton != null) {
            String selected = selectedButton.getText().toString();
            String correct = android.text.Html.fromHtml(questions.get(currentQuestionIndex).correctAnswer).toString();
            if (selected.equals(correct)) {
                correctCount++;
            } else {
                incorrectCount++;
            }
        } else {
            unansweredCount++;
        }

        currentQuestionIndex++;
        loadQuestion();
    }

    private void startTimer() {
        timerThread = new Thread(() -> {
            while (totalTimeInSeconds > 0 && running) {
                int displayTime = totalTimeInSeconds;
                mainHandler.post(() -> {
                    textViewTimer.setText("🕰️ 00:" + String.format("%02d", displayTime));
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    return;
                }
                totalTimeInSeconds--;
            }
            if (running) {
                mainHandler.post(this::endGame);
            }
        });
        timerThread.start();
    }

    private void endGame() {
        running = false;
        if (timerThread != null) {
            timerThread.interrupt();
        }
        unansweredCount += questions.size() - currentQuestionIndex;

        Intent intent = new Intent(this, StatsActivity.class);
        intent.putExtra("correct", correctCount);
        intent.putExtra("incorrect", incorrectCount);
        intent.putExtra("unanswered", unansweredCount);
        startActivity(intent);
        finish();
    }

    private int getTimePerQuestion(String dificultad) {
        switch (dificultad.toLowerCase()) {
            case "fácil":
                return 5;
            case "medio":
                return 7;
            case "difícil":
                return 10;
            default:
                return 5;
        }
    }

    private int mapCategoria(String categoria) {
        switch (categoria) {
            case "Cultura General":
                return 9;
            case "Libros":
                return 10;
            case "Películas":
                return 11;
            case "Música":
                return 12;
            case "Computación":
                return 18;
            case "Matemática":
                return 19;
            case "Deportes":
                return 21;
            case "Historia":
                return 23;
            default:
                return 9;
        }
    }

    @Override
    protected void onDestroy() {
        running = false;
        if (timerThread != null) {
            timerThread.interrupt();
        }
        super.onDestroy();
    }
    private String mapDificultad(String dificultad) {
        switch (dificultad.toLowerCase()) {
            case "fácil":
                return "easy";
            case "medio":
                return "medium";
            case "difícil":
                return "hard";
            default:
                return "easy";
        }
    }

}

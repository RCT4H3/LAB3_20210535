package com.carlosdev.teletrivia;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class StatsActivity extends AppCompatActivity {

    private TextView correctAnswer, incorrectAnswers, noAnswer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        correctAnswer = findViewById(R.id.correctAnswer);
        incorrectAnswers = findViewById(R.id.incorrectAnswers);
        noAnswer = findViewById(R.id.noAnswer);


        Intent intent = getIntent();
        int correct = intent.getIntExtra("correct", 0);
        int incorrect = intent.getIntExtra("incorrect", 0);
        int unanswered = intent.getIntExtra("unanswered", 0);


        correctAnswer.setText(String.valueOf(correct));
        incorrectAnswers.setText(String.valueOf(incorrect));
        noAnswer.setText(String.valueOf(unanswered));

        Button playAgain = findViewById(R.id.button);
        playAgain.setOnClickListener(v -> {
            finish();
        });
    }
}

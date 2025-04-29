package com.carlosdev.teletrivia;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private Spinner spinnerCategory, spinnerDifficulty;
    private EditText editTextQuantity;
    private Button btnCheckConnection, btnStart;

    private boolean internetAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerDifficulty = findViewById(R.id.spinnerDifficulty);
        editTextQuantity = findViewById(R.id.editTextQuantity);
        btnCheckConnection = findViewById(R.id.btnCheckConnection);
        btnStart = findViewById(R.id.btnStart);

        setupSpinners();

        btnCheckConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkInternetConnection()) {
                    Toast.makeText(MainActivity.this, "Conexión Exitosa", Toast.LENGTH_SHORT).show();
                    internetAvailable = true;
                    btnStart.setEnabled(true);
                } else {
                    Toast.makeText(MainActivity.this, "Error de Conexión", Toast.LENGTH_SHORT).show();
                    internetAvailable = false;
                    btnStart.setEnabled(false);
                }
            }
        });

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGameIfValid();
            }
        });
    }
    private void setupSpinners() {
        String[] categories = {"Cultura General", "Libros", "Películas", "Música", "Computación", "Matemática", "Deportes", "Historia"};
        ArrayAdapter<String> adapterCategory = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        spinnerCategory.setAdapter(adapterCategory);

        String[] difficulties = {"Fácil", "Medio", "Difícil"};
        ArrayAdapter<String> adapterDifficulty = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, difficulties);
        spinnerDifficulty.setAdapter(adapterDifficulty);
    }

    private boolean checkInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
        return false;
    }

    private void startGameIfValid() {
        String cantidadStr = editTextQuantity.getText().toString().trim();
        String categoria = spinnerCategory.getSelectedItem().toString();
        String dificultad = spinnerDifficulty.getSelectedItem().toString();

        if (cantidadStr.isEmpty() || categoria.isEmpty() || dificultad.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        int cantidad = Integer.parseInt(cantidadStr);

        if (cantidad <= 0) {
            Toast.makeText(this, "La cantidad debe ser un número positivo", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(MainActivity.this, TriviaActivity.class);
        intent.putExtra("cantidad", cantidad); // el int validado antes
        intent.putExtra("categoria", categoria);
        intent.putExtra("dificultad", dificultad);
        startActivity(intent);

    }
}
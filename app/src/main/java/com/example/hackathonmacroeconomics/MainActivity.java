package com.example.hackathonmacroeconomics;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    ImageView imgResearcher;
    ImageView imgGovernmentOfficial;
    Button btnMacroEconomy;
    Button btnAgriculture;
    Button btnDebit;
    Button btnOpenChatGPT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgResearcher = findViewById(R.id.imgResearcher);
        imgGovernmentOfficial = findViewById(R.id.imgGovernmentOfficial);
        btnMacroEconomy = findViewById(R.id.btnMacroEconomy);
        btnAgriculture = findViewById(R.id.btnAgriculture);
        btnDebit = findViewById(R.id.btnDebit);
        btnOpenChatGPT = findViewById(R.id.btnOpenChatGPT);

        // You can add your onClickListeners here for handling button clicks
        btnMacroEconomy.setOnClickListener(v -> {
            // Start MacroEconomyActivity
            startActivity(new Intent(this, MacroEconomy.class));
        });

        btnAgriculture.setOnClickListener(v -> {
            // Start AgricultureActivity
            startActivity(new Intent(this, Agriculture.class));
        });

        btnDebit.setOnClickListener(v -> {
            // Start DebitActivity
            startActivity(new Intent(this, Debit.class));
        });
        btnOpenChatGPT.setOnClickListener(v -> {
            // Start DebitActivity
            startActivity(new Intent(this, ChatGPTActivity.class));
        });
    }
}
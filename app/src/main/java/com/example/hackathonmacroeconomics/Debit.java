package com.example.hackathonmacroeconomics;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Debit extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private Spinner macroeconomicIndicatorSpinner;
    private Spinner yearStartSpinner;
    private Spinner yearEndSpinner;

    private GraphView graphView;
    private EditText annotationEditText;
    private CheckBox indiaCheckBox, chinaCheckBox, usaCheckBox;
    private DatabaseHelper dbHelper;

    String[] macroeconomics = new String[]{Constants.RESERVES, Constants.GNI, Constants.TOTAL_DEBT, Constants.GNI_CURRENT_US};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debit);
        dbHelper = new DatabaseHelper(this);

        // Initialize UI components
        macroeconomicIndicatorSpinner = findViewById(R.id.macroeconomic_indicator_spinner);
        yearStartSpinner = findViewById(R.id.start_year_spinner);
        yearEndSpinner = findViewById(R.id.end_year_spinner);

        graphView = findViewById(R.id.graph_view);
        annotationEditText = findViewById(R.id.annotation_edit_text);

        // Set up event listeners
        macroeconomicIndicatorSpinner.setOnItemSelectedListener(this);
        yearStartSpinner.setOnItemSelectedListener(this);
        yearEndSpinner.setOnItemSelectedListener(this);

        indiaCheckBox = findViewById(R.id.india_checkbox);
        chinaCheckBox = findViewById(R.id.china_checkbox);
        usaCheckBox = findViewById(R.id.usa_checkbox);
        Button addAnnotationButton = findViewById(R.id.add_annotation_button);
        SharedPreferences prefs = getSharedPreferences("AppAnnotations", MODE_PRIVATE);
        String savedAnnotation = prefs.getString("debitAnnotation", ""); // Default value is an empty string
        annotationEditText.setText(savedAnnotation);

        if (dbHelper.isDatabaseEmpty(Constants.RESERVES)) {
            try {
                InputStream inputStream = getResources().openRawResource(R.raw.gdp_growth);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                reader.readLine();
                while ((line = reader.readLine()) != null) {
                    try {
                        String[] row = line.split(",");
                        String year = row[0];
                        String india = row[1];
                        String china = row[2];
                        String usa = row[3];
                        String macroeconomics = Constants.RESERVES;
                        dbHelper.insertData(Integer.parseInt(year), Double.parseDouble(india), Double.parseDouble(china), Double.parseDouble(usa), macroeconomics);
                    } catch (Exception e) {
                        System.out.println("Vinit" + line);
                        e.printStackTrace();
                    }
                }
                inputStream = getResources().openRawResource(R.raw.gdp_usd);
                reader = new BufferedReader(new InputStreamReader(inputStream));
                reader.readLine();
                while ((line = reader.readLine()) != null) {
                    String[] row = line.split(",");
                    String year = row[0];
                    String india = row[1];
                    String china = row[2];
                    String usa = row[3];
                    String macroeconomics = Constants.GNI;
                    dbHelper.insertData(Integer.parseInt(year), Double.parseDouble(india), Double.parseDouble(china), Double.parseDouble(usa), macroeconomics);
                }
                inputStream = getResources().openRawResource(R.raw.fdi_inflows);
                reader = new BufferedReader(new InputStreamReader(inputStream));
                reader.readLine();
                while ((line = reader.readLine()) != null) {
                    String[] row = line.split(",");
                    String year = row[0];
                    String india = row[2];
                    String china = row[1];
                    String usa = row[3];
                    String macroeconomics = Constants.TOTAL_DEBT;
                    dbHelper.insertData(Integer.parseInt(year), Double.parseDouble(india), Double.parseDouble(china), Double.parseDouble(usa), macroeconomics);
                }
                inputStream = getResources().openRawResource(R.raw.fdi_outflows);
                reader = new BufferedReader(new InputStreamReader(inputStream));
                reader.readLine();
                while ((line = reader.readLine()) != null) {
                    String[] row = line.split(",");
                    String year = row[0];
                    String india = row[2];
                    String china = row[1];
                    String usa = row[3];
                    String macroeconomics = Constants.GNI_CURRENT_US;
                    dbHelper.insertData(Integer.parseInt(year), Double.parseDouble(india), Double.parseDouble(china), Double.parseDouble(usa), macroeconomics);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Populate country and time series spinners
        ArrayAdapter macroeconomicAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, macroeconomics);
        macroeconomicAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        macroeconomicIndicatorSpinner.setAdapter(macroeconomicAdapter);

        // Populate year spinners
        List<String> years = new ArrayList<>();
        for (int i = 1960; i <= 2023; i++) {
            years.add(String.valueOf(i));
        }
        ArrayAdapter yearAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearStartSpinner.setAdapter(yearAdapter);
        yearEndSpinner.setAdapter(yearAdapter);

        // Set default spinner selections
        yearStartSpinner.setSelection(years.indexOf("1960"));
        yearEndSpinner.setSelection(years.indexOf("2023"));
        // Display initial graph
        updateGraph();
        AdapterView.OnItemSelectedListener checkboxListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateGraph(); // Update graph when any checkbox state changes
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Not used here
            }
        };
        indiaCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> updateGraph());
        chinaCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> updateGraph());
        usaCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> updateGraph());
        addAnnotationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String annotation = annotationEditText.getText().toString();
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("debitAnnotation", annotation);
                editor.apply(); // Save the annotation

                // Optionally, show a confirmation message
                Toast.makeText(Debit.this, "Annotation saved", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // Update graph when spinner selections change
        updateGraph();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothing
    }

    public void onAddAnnotationButtonClick(View view) {
        // Handle annotation button click
    }

    private void updateGraph() {
        graphView.removeAllSeries();

        // Get selected country
        String macroEconomyName = (String) macroeconomicIndicatorSpinner.getSelectedItem();
        int startYear = Integer.parseInt((String) yearStartSpinner.getSelectedItem());
        int endYear = Integer.parseInt((String) yearEndSpinner.getSelectedItem());
        // selected countries
        List<String> selectedCountries = new ArrayList<>();
        if (indiaCheckBox.isChecked()) {
            selectedCountries.add(Constants.INDIA);
        }
        if (chinaCheckBox.isChecked()) {
            selectedCountries.add(Constants.CHINA);
        }
        if (usaCheckBox.isChecked()) {
            selectedCountries.add(Constants.USA);
        }
        double minValue = Double.MAX_VALUE;
        double maxValue = Double.MIN_VALUE;
        int indiaColor = Color.RED;
        int chinaColor = Color.GREEN;
        int usaColor = Color.BLUE;
        for (String country : selectedCountries) {
            // Create graph series
            LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
            Cursor cursor = dbHelper.getCountryData(country, macroEconomyName);
            int maxDataPoints = 0;
            while (cursor.moveToNext()) {
                // print the complete record
                int year = cursor.getInt(cursor.getColumnIndexOrThrow("year"));
                if (year < startYear || year > endYear) {
                    continue;
                }
                maxDataPoints++;
            }
            cursor.moveToFirst();
            while (cursor.moveToNext()) {
                // print the complete record
                int year = cursor.getInt(cursor.getColumnIndexOrThrow("year"));
                int data = cursor.getInt(cursor.getColumnIndexOrThrow(country));
                if (year < startYear || year > endYear) {
                    continue;
                }
                if (data < minValue) {
                    minValue = data;
                }
                if (data > maxValue) {
                    maxValue = data;
                }
                series.appendData(new DataPoint(year, data), true, maxDataPoints);
            }
            if (country.equals(Constants.INDIA)) {
                series.setColor(indiaColor);
            } else if (country.equals(Constants.CHINA)) {
                series.setColor(chinaColor);
            } else if (country.equals(Constants.USA)) {
                series.setColor(usaColor);
            }

            series.setTitle(country);
            graphView.addSeries(series);
        }

        graphView.getViewport().setScalable(true);
        graphView.getViewport().setScalableY(true);
        graphView.getViewport().setScrollable(true);
        graphView.getViewport().setScrollableY(true);
        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMinX(startYear - 3);
        graphView.getViewport().setMaxX(endYear + 3);
        graphView.getViewport().setYAxisBoundsManual(true);
        graphView.getViewport().setMinY(minValue);
        graphView.getViewport().setMaxY(maxValue);
        graphView.getLegendRenderer().setVisible(true);
        graphView.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
    }
}

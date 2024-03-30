package com.example.hackathonmacroeconomics;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class ChatGPTActivity extends AppCompatActivity {

    private EditText userInput;
    private Button sendButton;
    private TextView chatGPTResponse;

    private static final String token="Secret";

    private static final String instruction="You are a specialist information retrieval bot designed to work with attached PDF documents. To the questions I ask you, get the answers only from the files I provided and also provide information on from which PDF filename the information is extracted. The filename from which the information is extracted is very important. The response should Have information regarding the query and at the end have a Citation section and give the filename in that place.Please, always strive to learn from the feedback given, adapting your approach to ensure that each summary better meets the expectations outlined. Your goal is not only to provide information but to evolve in your capacity to discern and communicate the essence of the PDF documents in the most effective manner possible. Make sure you take less than 9 seconds to get a response so prioritze the time limit too";

    private static final String assistant="Assistant";

    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<Message> messageList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatgpt);

        userInput = findViewById(R.id.userInput);
        sendButton = findViewById(R.id.sendButton);
        chatRecyclerView = findViewById(R.id.chatRecyclerView);

        chatAdapter = new ChatAdapter(messageList);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        createThread();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messageContent = userInput.getText().toString();
                if (!messageContent.isEmpty()) {
                    Log.d("ChatGPTActivity", messageContent);
                    sendMessageToThread(messageContent);
                    userInput.setText(""); // Clear input after sending
                }
            }
        });
    }

    private void sendMessageToThread(String messageContent) {

        SharedPreferences sharedPref = getSharedPreferences("AppData", MODE_PRIVATE);
        String threadId = sharedPref.getString("ThreadID", null);

        OkHttpClient client = new OkHttpClient();
        String json = "{\"role\": \"user\", \"content\": \"" + messageContent + "\"}";
        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        Log.d("ChatGPTActivity","In Messages:"+json+"|"+threadId);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/threads/" + threadId + "/messages")
                .post(body)
                .addHeader("OpenAI-Beta", "assistants=v1")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.isSuccessful()) {
                    Log.d("ChatGPTActivity",""+response.body().string());
                    triggerRunOnThread();
                }
            }
        });
    }

    private void triggerRunOnThread() {
        // Use the same threadId and token logic as above
        SharedPreferences sharedPref = getSharedPreferences("AppData", MODE_PRIVATE);
        String threadId = sharedPref.getString("ThreadID", null);

        OkHttpClient client = new OkHttpClient();
        String json = "{\"assistant_id\": \"" + assistant + "\", \"instructions\": \""+instruction+"\"}";
        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        Log.d("ChatGPTActivity","In Messages:"+json+"|"+threadId);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/threads/" + threadId + "/runs")
                .post(body)
                .addHeader("OpenAI-Beta", "assistants=v1")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("ChatGPTActivity",""+response.body().string());
                if (response.isSuccessful()) {

                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            fetchThreadMessages(threadId, token);

                        }
                    }, 10000);

                }
            }
        });
    }

    private void fetchThreadMessages(String threadId, String token) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/threads/" + threadId + "/messages")
                .get()
                .addHeader("OpenAI-Beta", "assistants=v1")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Process the messages
                    String responseData = response.body().string();

                    Log.d("ChatGPTActivity",""+responseData);
                    extractTextValues(responseData);
                    //Log.d("ChatGPTActivity",""+textValues.toString());
                    // Update your UI or process data as needed
                }
            }
        });
    }

    private void createThread() {
        OkHttpClient client = new OkHttpClient();

        // Assuming you have no specific body data to send, use an empty JSON object or adjust as needed
        RequestBody body = RequestBody.create("{}", MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/threads")
                .post(body)
                .addHeader("OpenAI-Beta", "assistants=v1")
                .addHeader("Authorization", "Bearer "+token) // Replace with your actual API key
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Handle failure
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    // Handle the error
                    throw new IOException("Unexpected code " + response);
                } else {
                    // Extract the response data
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        String threadId = jsonObject.getString("id"); // Extract the thread ID

                        // Save the thread ID in shared preferences
                        SharedPreferences sharedPref = getSharedPreferences("AppData", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("ThreadID", threadId);
                        editor.apply();
                        Log.d("ChatGPTActivity",threadId);
                        // Optionally, do something with the thread ID or response data
                    } catch (JSONException e) {
                        e.printStackTrace();
                        // Handle JSON parsing error
                    }
                }
            }
        });
    }
    private void extractTextValues(String jsonResponse) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                List<String> textValues = new ArrayList<>();
                try {
                    JSONObject jsonObject = new JSONObject(jsonResponse);
                    JSONArray dataArray = jsonObject.getJSONArray("data");

                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject dataObject = dataArray.getJSONObject(i);
                        JSONArray contentArray = dataObject.getJSONArray("content");

                        for (int j = 0; j < contentArray.length(); j++) {
                            JSONObject contentObject = contentArray.getJSONObject(j);
                            JSONObject textObject = contentObject.getJSONObject("text");
                            String value = textObject.getString("value");
                            textValues.add(value);
                        }
                    }
                    processMessages(textValues);
                    Log.d("ChatGPTActivity",""+textValues);
                    chatAdapter.notifyDataSetChanged(); // Refresh RecyclerView
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }
    private void processMessages(List<String> messages) {
        List<Message> newMessageList = new ArrayList<>();
        for (int i = messages.size()-1; i > 0; i -= 2) {
            // User input
            if (i > 0) {
                newMessageList.add(new Message(messages.get(i), true)); // true indicates user message
            }
            // ChatGPT response
            if (i - 1 >= 0) {
                newMessageList.add(new Message(messages.get(i - 1), false)); // false indicates ChatGPT message
            }
        }

        Log.d("ChatGPTActivity",""+newMessageList);
        chatAdapter.setMessages(newMessageList);
    }
}
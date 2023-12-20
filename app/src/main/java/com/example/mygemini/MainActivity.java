package com.example.mygemini;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;

import com.example.mygemini.databinding.ActivityMainBinding;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding activityMainBinding;
    ProgressDialog progressDialog;
    private static final int PICK_IMAGE_REQUEST = 1;
    Bitmap image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = activityMainBinding.getRoot();
        setContentView(view);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Connecting to Gemini...");

        activityMainBinding.buttonImage.setOnClickListener(view12 -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });


        activityMainBinding.buttonGenerate.setOnClickListener(view1 -> {
            hideKeyboard();
            progressDialog.show();
            activityMainBinding.editText.clearFocus();
            GenerativeModel gm = new GenerativeModel("gemini-pro-vision", "your-api-key-here");
            GenerativeModelFutures model = GenerativeModelFutures.from(gm);

            String text = activityMainBinding.editText.getText().toString();
            Content userMessage = new Content.Builder()
                    .addText(text)
                    .addImage(image)
                    .build();

                    Executor executor = MoreExecutors.directExecutor();
                    ListenableFuture<GenerateContentResponse> response = model.generateContent(userMessage);

            Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    progressDialog.hide();
                    String resultText = result.getText();
                    activityMainBinding.textViewResult.setText(resultText);
                    Animation fadeIn = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_in);
                    activityMainBinding.textViewResult.startAnimation(fadeIn);
                    activityMainBinding.editText.setText("");
                }

                @Override
                public void onFailure(Throwable t) {
                    t.printStackTrace();
                }

            }, executor);

        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            // Get the selected image URI
            Uri selectedImageUri = data.getData();

            try {
                image = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}

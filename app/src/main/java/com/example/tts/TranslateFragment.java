package com.example.tts;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions;
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions;
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions;
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TranslateFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TranslateFragment extends Fragment {


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TextRecognizer textRecognizerLatin;
    private TextRecognizer textRecognizerChinese;
    private TextRecognizer textRecognizerDevanagari;
    private TextRecognizer textRecognizerJapanese;
    private TextRecognizer textRecognizerKorean;

    private String recognizedText;

    private Button openGalleryBtn;
    private Button getTextBtn;

    private Translator englishSpanishTranslator;

    private Button translateBtn;
    private ImageView showImageTranslateBtn;
    private FloatingActionButton howToUseBtn;


    private Uri imageUri = null;


    public TranslateFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TranslateFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TranslateFragment newInstance(String param1, String param2) {

        TranslateFragment fragment = new TranslateFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_translate, container, false);

        openGalleryBtn = v.findViewById(R.id.openGallery);
        getTextBtn = v.findViewById(R.id.getText);
        showImageTranslateBtn = v.findViewById(R.id.showImageTranslate);
        howToUseBtn = v.findViewById(R.id.help);
        translateBtn = v.findViewById(R.id.translate);


        openGalleryBtn.setOnClickListener(view -> {
            pickImage();
        });

        getTextBtn.setOnClickListener(view -> {
            recognizedTextFromImage();
        });

        howToUseBtn.setOnClickListener(view -> {
            onFloatingActionButton();
            Toast.makeText(getContext(), recognizedText, Toast.LENGTH_SHORT).show();
        });

        translateBtn.setOnClickListener(view -> {
            translateText();
        });

        textRecognizerLatin = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        textRecognizerChinese = TextRecognition.getClient(new ChineseTextRecognizerOptions.Builder().build());
        textRecognizerDevanagari = TextRecognition.getClient(new DevanagariTextRecognizerOptions.Builder().build());
        textRecognizerJapanese = TextRecognition.getClient(new JapaneseTextRecognizerOptions.Builder().build());
        textRecognizerKorean = TextRecognition.getClient(new KoreanTextRecognizerOptions.Builder().build());




        return v;

    }

    private void onFloatingActionButton() {
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Can't find your image?")
                .setMessage("Click on the open gallery button. If you have taken an image before, it will be in your 'Photos' folder, if you took your image on the previous step, it will be in the 'pictures' folder")
                .setPositiveButton("OK", null)
                .show();
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }


    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        imageUri = data.getData();
                        Log.d(TAG, "onActivityResult: imageUri " + imageUri);
                        showImageTranslateBtn.setImageURI(imageUri);
                    } else {
                        Toast.makeText(getActivity(), "Cancelled....", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );


    // Text Recognition
    private void recognizedTextFromImage() {

        try {
            InputImage inputImageLatin = InputImage.fromFilePath(getContext(), imageUri);

            // Try to Recognize Latin Recognizer
            textRecognizerLatin.process(inputImageLatin)
                    .addOnSuccessListener(text -> {
                        recognizedText = text.getText();

                        if (recognizedText.length() < 1) {
                            // Go to Chinese Recognizer
                            try {
                                InputImage inputImageChinese = InputImage.fromFilePath(getContext(), imageUri);

                                textRecognizerChinese.process(inputImageChinese)
                                        .addOnSuccessListener(textChinese -> {
                                            recognizedText = textChinese.getText();

                                            if (recognizedText.length() < 1) {
                                                // Go to JAPANESE Recognizer
                                                try {
                                                    InputImage inputImageJapanese = InputImage.fromFilePath(getContext(), imageUri);

                                                    textRecognizerJapanese.process(inputImageJapanese)
                                                            .addOnSuccessListener(textJapanese -> {
                                                                recognizedText = textJapanese.getText();

                                                                if (recognizedText.length() < 1) {

                                                                    // Go to KOREAN Recognizer
                                                                    try {
                                                                        InputImage inputImageKorean = InputImage.fromFilePath(getContext(), imageUri);

                                                                        textRecognizerKorean.process(inputImageKorean)
                                                                                .addOnSuccessListener(textKorean -> {
                                                                                    recognizedText = textKorean.getText();

                                                                                    if (recognizedText.length() < 1) {
                                                                                        try {
                                                                                            InputImage inputImageDevanagari = InputImage.fromFilePath(getContext(), imageUri);

                                                                                            textRecognizerDevanagari.process(inputImageDevanagari)
                                                                                                    .addOnSuccessListener(textDevanagari -> {
                                                                                                        String recognizedTextDevanagari = textDevanagari.getText();

                                                                                                        // COULD NOT RECOGNIZE ANY TEXTS
                                                                                                        if (recognizedTextDevanagari.length() < 1) {
                                                                                                            Toast.makeText(getContext(), "Could not recognize text in image", Toast.LENGTH_SHORT).show();
                                                                                                        }

                                                                                                        // RECOGNIZED DEVANAGARI TEXT
                                                                                                        else {
                                                                                                            Toast.makeText(getContext(), recognizedTextDevanagari, Toast.LENGTH_SHORT).show();
                                                                                                        }
                                                                                                    });

                                                                                        // EXCEPTION DEVANAGARI (ALL)
                                                                                        } catch (Exception exceptionDevanagari) {
                                                                                            Toast.makeText(getContext(), "Could not get text from image", Toast.LENGTH_SHORT).show();
                                                                                        }
                                                                                    }

                                                                                    // Korean RECOGNIZER WORKS
                                                                                    else {
                                                                                        Toast.makeText(getContext(), recognizedText, Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                });
                                                                    }

                                                                    //EXCEPTION KOREAN
                                                                    catch (Exception exceptionKorean) {
                                                                        Toast.makeText(getContext(), "Could not get text from image", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }

                                                                // Japanese Recognizer WORKS
                                                                else {
                                                                    Toast.makeText(getContext(), recognizedText, Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                // EXCEPTION JAPANESE
                                                } catch (Exception exceptionJapanese) {
                                                    Toast.makeText(getContext(), "Could not get text from image", Toast.LENGTH_SHORT).show();
                                                }

                                            }

                                            // Chinese Recognizer WORKS
                                            else {
                                                Toast.makeText(getContext(), recognizedText, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            // EXCEPTION CHINESE
                            } catch (Exception exceptionChinese) {
                                Toast.makeText(getContext(), "Could not get text from image", Toast.LENGTH_SHORT).show();
                            }

                        // LATIN WORKS
                        } else {
                            Toast.makeText(getContext(), recognizedText, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        // Exception ALL
        catch (Exception e) {
            Toast.makeText(getContext(), "Could not get text from image", Toast.LENGTH_SHORT).show();
        }

    }

    public void translateText() {
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setTargetLanguage("es")
                .setSourceLanguage("en")
                .build();
        Translator translator = Translation.getClient(options);
        String sourceText = recognizedText;
        Task<String> result
    }

}

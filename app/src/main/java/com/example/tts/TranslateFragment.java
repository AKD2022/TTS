package com.example.tts;

import static android.content.ContentValues.TAG;


import static androidx.core.content.ContextCompat.checkSelfPermission;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Camera;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentifier;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TranslateFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TranslateFragment extends Fragment  {


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
    private String languageCode;
    private String translateTo;

    public File folder;

    private TextToSpeech speech;
    private String translatedText;

    private Button openGalleryBtn;
    private Button getTextBtn;


    private Button generateAudioBtn;

    private static final int REQUEST_INSTALL_PACKAGES = 100;

    private String languageTranslateTo;
    private String languageTranslateFrom;

    private Locale selectedLocale;
    private String mAudioFilename = "";
    private String fileNameForSaving = "";
    private final String mUtteranceID = "TextToSpeechAudio";

    private Button translateBtn;
    private ImageView showImageTranslateBtn;
    private FloatingActionButton howToUseBtn;

    private static final int WRITE_EXTERNAL_STORAGE_CODE = 100;

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
        generateAudioBtn = v.findViewById(R.id.generateAudio);



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
            showTranslateDialog();
        });


        generateAudioBtn.setOnClickListener(view -> {
            initializeTextToSpeech(selectedLocale);
            createFile();
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
        if (recognizedText == null || recognizedText.isEmpty() || languageCode == null || languageCode.isEmpty()) {
            Toast.makeText(getContext(), "Please recognize text and identify language first", Toast.LENGTH_SHORT).show();
            return;
        }

        TranslatorOptions options = new TranslatorOptions.Builder()
                .setTargetLanguage(translateTo)
                .setSourceLanguage(languageCode)
                .build();
        Translator translator = Translation.getClient(options);
        String sourceText = recognizedText;
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Downloading Translation Model....");
        progressDialog.setCancelable(false);
        progressDialog.show();

        translator.downloadModelIfNeeded()
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    translateWithModelAvailable(translator, sourceText);
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    new MaterialAlertDialogBuilder(getContext())
                            .setMessage("Translation Failed. May be a problem with installing the translation model")
                            .setPositiveButton("OK", null)
                            .show();
                    Log.e(TAG, "Translation model download failed: " + e.getMessage());
                });
    }

    private void translateWithModelAvailable(Translator translator, String sourceText) {
        Task<String> result = translator.translate(sourceText)
                .addOnSuccessListener(s -> {
                    new MaterialAlertDialogBuilder(getContext())
                            .setMessage(s)
                            .setPositiveButton("Ok", null)
                            .show();
                    translatedText = s;
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "If the model is already installed, text cannot be translated", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Translation failed: " + e.getMessage());
                });
    }


    public void identifyLanguage() {
        LanguageIdentifier languageIdentifier = LanguageIdentification.getClient();
        languageIdentifier.identifyLanguage(recognizedText)
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String language) {
                        if (language.equals("und")) {
                            Toast.makeText(getContext(), "Can't Identify Language", Toast.LENGTH_SHORT).show();
                        } else {
                            languageCode = language;
                            translateText();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Exception" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showTranslateDialog() {
        Context wrapper = new ContextThemeWrapper(getContext(), R.style.popupStyle);
        PopupMenu popupMenu = new PopupMenu(wrapper, translateBtn);

        popupMenu.getMenu().add(Menu.NONE, 2, 2, "Arabic");
        popupMenu.getMenu().add(Menu.NONE, 4, 4, "Bulgarian");
        popupMenu.getMenu().add(Menu.NONE, 5, 5, "Bengali");
        popupMenu.getMenu().add(Menu.NONE, 6, 6, "Catalan");
        popupMenu.getMenu().add(Menu.NONE, 7, 7, "Czech");
        popupMenu.getMenu().add(Menu.NONE, 8, 8, "Welsh");
        popupMenu.getMenu().add(Menu.NONE, 9, 9, "Danish");
        popupMenu.getMenu().add(Menu.NONE, 10, 10, "German");
        popupMenu.getMenu().add(Menu.NONE, 11, 11, "Greek");
        popupMenu.getMenu().add(Menu.NONE, 12, 12, "English");
        popupMenu.getMenu().add(Menu.NONE, 14, 14, "Spanish");
        popupMenu.getMenu().add(Menu.NONE, 15, 15, "Estonian");
        popupMenu.getMenu().add(Menu.NONE, 17, 17, "Finnish");
        popupMenu.getMenu().add(Menu.NONE, 18, 18, "French");
        popupMenu.getMenu().add(Menu.NONE, 21, 21, "Gujarati");
        popupMenu.getMenu().add(Menu.NONE, 22, 22, "Hebrew");
        popupMenu.getMenu().add(Menu.NONE, 23, 23, "Hindi");
        popupMenu.getMenu().add(Menu.NONE, 24, 24, "Croatian");
        popupMenu.getMenu().add(Menu.NONE, 26, 26, "Hungarian");
        popupMenu.getMenu().add(Menu.NONE, 27, 27, "Indonesian");
        popupMenu.getMenu().add(Menu.NONE, 28, 28, "Icelandic");
        popupMenu.getMenu().add(Menu.NONE, 29, 29, "Italian");
        popupMenu.getMenu().add(Menu.NONE, 30, 30, "Japanese");
        popupMenu.getMenu().add(Menu.NONE, 32, 32, "Kannada");
        popupMenu.getMenu().add(Menu.NONE, 33, 33, "Korean");
        popupMenu.getMenu().add(Menu.NONE, 34, 34, "Lithuanian");
        popupMenu.getMenu().add(Menu.NONE, 35, 35, "Latvian");
        popupMenu.getMenu().add(Menu.NONE, 37, 37, "Marathi");
        popupMenu.getMenu().add(Menu.NONE, 38, 38, "Malay");
        popupMenu.getMenu().add(Menu.NONE, 40, 40, "Dutch");
        popupMenu.getMenu().add(Menu.NONE, 41, 41, "Norwegian");
        popupMenu.getMenu().add(Menu.NONE, 42, 42, "Polish");
        popupMenu.getMenu().add(Menu.NONE, 43, 43, "Portuguese");
        popupMenu.getMenu().add(Menu.NONE, 44, 44, "Romanian");
        popupMenu.getMenu().add(Menu.NONE, 45, 45, "Russian");
        popupMenu.getMenu().add(Menu.NONE, 46, 46, "Slovak");
        popupMenu.getMenu().add(Menu.NONE, 48, 48, "Albanian");
        popupMenu.getMenu().add(Menu.NONE, 49, 49, "Swedish");
        popupMenu.getMenu().add(Menu.NONE, 50, 50, "Swahili");
        popupMenu.getMenu().add(Menu.NONE, 51, 51, "Tamil");
        popupMenu.getMenu().add(Menu.NONE, 52, 52, "Telugu");
        popupMenu.getMenu().add(Menu.NONE, 53, 53, "Thai");
        popupMenu.getMenu().add(Menu.NONE, 54, 54, "Tagalog");
        popupMenu.getMenu().add(Menu.NONE, 55, 55, "Turkish");
        popupMenu.getMenu().add(Menu.NONE, 56, 56, "Ukrainian");
        popupMenu.getMenu().add(Menu.NONE, 57, 57, "Urdu");
        popupMenu.getMenu().add(Menu.NONE, 58, 58, "Vietnamese");
        popupMenu.getMenu().add(Menu.NONE, 59, 59, "Chinese");


        popupMenu.show();


        popupMenu.setOnMenuItemClickListener(menuItem -> {
            int id = menuItem.getItemId();

            switch (id) {
                case 2: translateTo = "ar"; selectedLocale = new Locale("ar"); identifyLanguage(); break; // Arabic
                case 4: translateTo = "bg"; selectedLocale = new Locale("bg"); identifyLanguage(); break; // Bulgarian
                case 5: translateTo = "bn"; selectedLocale = new Locale("bn"); identifyLanguage(); break; // Bengali
                case 6: translateTo = "ca"; selectedLocale = new Locale("ca"); identifyLanguage(); break; // Catalan
                case 7: translateTo = "cs"; selectedLocale = new Locale("cs"); identifyLanguage(); break; // Czech
                case 8: translateTo = "cy"; selectedLocale = new Locale("cy"); identifyLanguage(); break; // Welsh
                case 9: translateTo = "da"; selectedLocale = new Locale("da"); identifyLanguage(); break; // Danish
                case 10: translateTo = "de"; selectedLocale = new Locale("de"); identifyLanguage(); break; // German
                case 11: translateTo = "el"; selectedLocale = new Locale("el"); identifyLanguage(); break; // Greek
                case 12: translateTo = "en"; selectedLocale = new Locale("en"); identifyLanguage(); break; // English
                case 14: translateTo = "es"; selectedLocale = new Locale("es"); identifyLanguage(); break; // Spanish
                case 15: translateTo = "et"; selectedLocale = new Locale("et"); identifyLanguage(); break; // Estonian
                case 17: translateTo = "fi"; selectedLocale = new Locale("fi"); identifyLanguage(); break; // Finnish
                case 18: translateTo = "fr"; selectedLocale = new Locale("fr"); identifyLanguage(); break; // French
                case 21: translateTo = "gu"; selectedLocale = new Locale("gu"); identifyLanguage(); break; // Gujarati
                case 22: translateTo = "he"; selectedLocale = new Locale("he"); identifyLanguage(); break; // Hebrew
                case 23: translateTo = "hi"; selectedLocale = new Locale("hi"); identifyLanguage(); break; // Hindi
                case 24: translateTo = "hr"; selectedLocale = new Locale("hr"); identifyLanguage(); break; // Croatian
                case 26: translateTo = "hu"; selectedLocale = new Locale("hu"); identifyLanguage(); break; // Hungarian
                case 27: translateTo = "id"; selectedLocale = new Locale("id"); identifyLanguage(); break; // Indonesian
                case 28: translateTo = "is"; selectedLocale = new Locale("is"); identifyLanguage(); break; // Icelandic
                case 29: translateTo = "it"; selectedLocale = new Locale("it"); identifyLanguage(); break; // Italian
                case 30: translateTo = "ja"; selectedLocale = new Locale("ja"); identifyLanguage(); break; // Japanese
                case 32: translateTo = "kn"; selectedLocale = new Locale("kn"); identifyLanguage(); break; // Kannada
                case 33: translateTo = "ko"; selectedLocale = new Locale("ko"); identifyLanguage(); break; // Korean
                case 34: translateTo = "lt"; selectedLocale = new Locale("lt"); identifyLanguage(); break; // Lithuanian
                case 35: translateTo = "lv"; selectedLocale = new Locale("lv"); identifyLanguage(); break; // Latvian
                case 37: translateTo = "mr"; selectedLocale = new Locale("mr"); identifyLanguage(); break; // Marathi
                case 38: translateTo = "ms"; selectedLocale = new Locale("ms"); identifyLanguage(); break; // Malay
                case 40: translateTo = "nl"; selectedLocale = new Locale("nl"); identifyLanguage(); break; // Dutch
                case 41: translateTo = "no"; selectedLocale = new Locale("no"); identifyLanguage(); break; // Norwegian
                case 42: translateTo = "pl"; selectedLocale = new Locale("pl"); identifyLanguage(); break; // Polish
                case 43: translateTo = "pt"; selectedLocale = new Locale("pt"); identifyLanguage(); break; // Portuguese
                case 44: translateTo = "ro"; selectedLocale = new Locale("ro"); identifyLanguage(); break; // Romanian
                case 45: translateTo = "ru"; selectedLocale = new Locale("ru"); identifyLanguage(); break; // Russian
                case 46: translateTo = "sk"; selectedLocale = new Locale("sk"); identifyLanguage(); break; // Slovakian
                case 48: translateTo = "sq"; selectedLocale = new Locale("sq"); identifyLanguage(); break; // Albanian
                case 49: translateTo = "sv"; selectedLocale = new Locale("sv"); identifyLanguage(); break; // Swedish
                case 50: translateTo = "sw"; selectedLocale = new Locale("sw"); identifyLanguage(); break; // Swahili
                case 51: translateTo = "ta"; selectedLocale = new Locale("ta"); identifyLanguage(); break; // Tamil
                case 52: translateTo = "te"; selectedLocale = new Locale("te"); identifyLanguage(); break; // Telugu
                case 53: translateTo = "th"; selectedLocale = new Locale("th"); identifyLanguage(); break; // Thai
                case 54: translateTo = "tl"; selectedLocale = new Locale("tl"); identifyLanguage(); break; // Tagalog
                case 55: translateTo = "tr"; selectedLocale = new Locale("tr"); identifyLanguage(); break; // Turkish
                case 56: translateTo = "uk"; selectedLocale = new Locale("uk"); identifyLanguage(); break; // Ukrainian
                case 57: translateTo = "ur"; selectedLocale = new Locale("ur"); identifyLanguage(); break; // Urdu
                case 58: translateTo = "vi"; selectedLocale = new Locale("vi"); identifyLanguage(); break; // Vietnamese
                case 59: translateTo = "zh"; selectedLocale = new Locale("zh"); identifyLanguage(); break; // Chinese
            }

            Configuration config = getResources().getConfiguration();
            config.setLocale(selectedLocale);
            getResources().updateConfiguration(config, getResources().getDisplayMetrics());

            return true;

        });
    }


    private void initializeTextToSpeech(Locale selectedLocale) {
        speech = new TextToSpeech(getContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = speech.setLanguage(selectedLocale);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Language not supported");
                    new MaterialAlertDialogBuilder(getContext())
                            .setMessage("Language is not supported, language cannot be detected in the image. ")
                            .setPositiveButton("OK", null)
                            .show();
                } else {
                    Log.i(TAG, "Text-to-Speech initialized");
                    String textToSpeak = translatedText;
                    saveAudioToFile(textToSpeak);
                }
            } else {
                Log.e(TAG, "Text-to-Speech initialization failed");
                new MaterialAlertDialogBuilder(getContext())
                        .setMessage("Cannot translate text-to-speech. Please try taking a clearer image or try again.")
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }

    public void languageName() {
        switch(languageCode) {
            case "ar": languageTranslateFrom = "Arabic"; break;
            case "bg": languageTranslateFrom = "Bulgarian"; break;
            case "bn": languageTranslateFrom = "Bengali"; break;
            case "ca": languageTranslateFrom = "Catalan"; break;
            case "cs": languageTranslateFrom = "Czech"; break;
            case "cy": languageTranslateFrom = "Welsh"; break;
            case "da": languageTranslateFrom = "Danish"; break;
            case "de": languageTranslateFrom = "German"; break;
            case "el": languageTranslateFrom = "Greek"; break;
            case "en": languageTranslateFrom = "English"; break;
            case "es": languageTranslateFrom = "Spanish"; break;
            case "et": languageTranslateFrom = "Estonian"; break;
            case "fi": languageTranslateFrom = "Finnish"; break;
            case "fr": languageTranslateFrom = "French"; break;
            case "gu": languageTranslateFrom = "Gujarati"; break;
            case "he": languageTranslateFrom = "Hebrew"; break;
            case "hi": languageTranslateFrom = "Hindi"; break;
            case "hr": languageTranslateFrom = "Croatian"; break;
            case "hu": languageTranslateFrom = "Hungarian"; break;
            case "id": languageTranslateFrom = "Indonesian"; break;
            case "is": languageTranslateFrom = "Icelandic"; break;
            case "it": languageTranslateFrom = "Italian"; break;
            case "ja": languageTranslateFrom = "Japanese"; break;
            case "kn": languageTranslateFrom = "Kannada"; break;
            case "ko": languageTranslateFrom = "Korean"; break;
            case "lt": languageTranslateFrom = "Lithuanian"; break;
            case "mr": languageTranslateFrom = "Latvian"; break;
            case "ms": languageTranslateFrom = "Marathi"; break;
            case "nl": languageTranslateFrom = "Malay"; break;
            case "no": languageTranslateFrom = "Dutch"; break;
            case "pl": languageTranslateFrom = "Norwegian"; break;
            case "pt": languageTranslateFrom = "Polish"; break;
            case "ro": languageTranslateFrom = "Portuguese"; break;
            case "ru": languageTranslateFrom = "Romanian"; break;
            case "sk": languageTranslateFrom = "Russian"; break;
            case "sq": languageTranslateFrom = "Slovakian"; break;
            case "sv": languageTranslateFrom = "Albanian"; break;
            case "sw": languageTranslateFrom = "Swedish"; break;
            case "ta": languageTranslateFrom = "Swahili"; break;
            case "te": languageTranslateFrom = "Tamil"; break;
            case "th": languageTranslateFrom = "Telugu"; break;
            case "tl": languageTranslateFrom = "Thai"; break;
            case "tr": languageTranslateFrom = "Tagalog"; break;
            case "uk": languageTranslateFrom = "Turkish"; break;
            case "ur": languageTranslateFrom = "Ukrainian"; break;
            case "vi": languageTranslateFrom = "Urdu"; break;
            case "zh": languageTranslateFrom = "Vietnamese"; break;
        }

        switch(translateTo) {
            case "ar": languageTranslateTo = "Arabic"; break;
            case "bg": languageTranslateTo = "Bulgarian"; break;
            case "bn": languageTranslateTo = "Bengali"; break;
            case "ca": languageTranslateTo = "Catalan"; break;
            case "cs": languageTranslateTo = "Czech"; break;
            case "cy": languageTranslateTo = "Welsh"; break;
            case "da": languageTranslateTo = "Danish"; break;
            case "de": languageTranslateTo = "German"; break;
            case "el": languageTranslateTo = "Greek"; break;
            case "en": languageTranslateTo = "English"; break;
            case "es": languageTranslateTo = "Spanish"; break;
            case "et": languageTranslateTo = "Estonian"; break;
            case "fi": languageTranslateTo = "Finnish"; break;
            case "fr": languageTranslateTo = "French"; break;
            case "gu": languageTranslateTo = "Gujarati"; break;
            case "he": languageTranslateTo = "Hebrew"; break;
            case "hi": languageTranslateTo = "Hindi"; break;
            case "hr": languageTranslateTo = "Croatian"; break;
            case "hu": languageTranslateTo = "Hungarian"; break;
            case "id": languageTranslateTo = "Indonesian"; break;
            case "is": languageTranslateTo = "Icelandic"; break;
            case "it": languageTranslateTo = "Italian"; break;
            case "ja": languageTranslateTo = "Japanese"; break;
            case "kn": languageTranslateTo = "Kannada"; break;
            case "ko": languageTranslateTo = "Korean"; break;
            case "lt": languageTranslateTo = "Lithuanian"; break;
            case "mr": languageTranslateTo = "Latvian"; break;
            case "ms": languageTranslateTo = "Marathi"; break;
            case "nl": languageTranslateTo = "Malay"; break;
            case "no": languageTranslateTo = "Dutch"; break;
            case "pl": languageTranslateTo = "Norwegian"; break;
            case "pt": languageTranslateTo = "Polish"; break;
            case "ro": languageTranslateTo = "Portuguese"; break;
            case "ru": languageTranslateTo = "Romanian"; break;
            case "sk": languageTranslateTo = "Russian"; break;
            case "sq": languageTranslateTo = "Slovakian"; break;
            case "sv": languageTranslateTo = "Albanian"; break;
            case "sw": languageTranslateTo = "Swedish"; break;
            case "ta": languageTranslateTo = "Swahili"; break;
            case "te": languageTranslateTo = "Tamil"; break;
            case "th": languageTranslateTo = "Telugu"; break;
            case "tl": languageTranslateTo = "Thai"; break;
            case "tr": languageTranslateTo = "Tagalog"; break;
            case "uk": languageTranslateTo = "Turkish"; break;
            case "ur": languageTranslateTo = "Ukrainian"; break;
            case "vi": languageTranslateTo = "Urdu"; break;
            case "zh": languageTranslateTo = "Vietnamese"; break;

        }
    }

    private void createFile() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_CODE);
            }
        }

        folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Text To Speech Audio");
        if (!folder.exists()) {
            boolean isDirectoryCreated = folder.mkdirs();
            if (!isDirectoryCreated)
                Toast.makeText(getContext(), "Can't create directory to save the Audio", Toast.LENGTH_SHORT).show();
        }
        folder.mkdirs();
        languageName();

        mAudioFilename = folder.getAbsolutePath() + "/" + mUtteranceID + System.currentTimeMillis() + ".wav";
        fileNameForSaving = "Internal Storage/Download/Text To Speech Audio as:  \n" + languageCode + "-" + translateTo + ".wav";
    }

    private void saveAudioToFile(String textToSpeak) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            speech.synthesizeToFile(textToSpeak, null, new File(mAudioFilename), mUtteranceID);
            new MaterialAlertDialogBuilder(getContext())
                    .setMessage("Saved to: " + fileNameForSaving)
                    .setPositiveButton("OK", null)
                    .show();
        } else {
            HashMap<String, String> hm = new HashMap();
            hm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, mUtteranceID);
            speech.synthesizeToFile(textToSpeak, hm, mAudioFilename);
            new MaterialAlertDialogBuilder(getContext())
                    .setMessage("Saved to: " + mAudioFilename)
                    .setPositiveButton("OK", null)
                    .show();
        }
    }




}
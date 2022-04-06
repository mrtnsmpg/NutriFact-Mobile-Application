package com.example.nutrifact;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.TensorOperator;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp;
import org.tensorflow.lite.support.label.TensorLabel;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Classifier extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    ImageView imageView;
    TextView textFruitName, textFruitQuality, headCal, headCarbs, headFat, headProtein;
    TextView textCarbs, textDFiber, textSugar, textFat, textSaturated, textPUnsaturated, textMUnsaturated, textTrans, textProtein, textSodium, textPotassium, textCholesterol, textVitaminA, textVitaminC, textCalcium, textIron;
    Spinner fruitSizes;
    Bitmap imageBitmap;


    protected Interpreter tflite;
    private MappedByteBuffer tfliteModel;
    private TensorImage inputImageBuffer;
    private  int imageSizeX;
    private  int imageSizeY;
    private TensorBuffer outputProbabilityBuffer;
    private TensorProcessor probabilityProcessor;
    private static final float IMAGE_MEAN = 0.0f;
    private static final float IMAGE_STD = 1.0f;
    private static final float PROBABILITY_MEAN = 0.0f;
    private static final float PROBABILITY_STD = 255.0f;
    private List<String> labels;


    String fruitKey;
    List<String> spinnerItems = new ArrayList<>();
    List<Map<String,String>> selectedValue = new ArrayList<>();
    List<List<String>> nutritionalValues = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classifier);


        imageView = findViewById(R.id.showImage);
        textFruitName = findViewById(R.id.fruitName);
        textFruitQuality = findViewById(R.id.fruitQuality);
        headCal = findViewById(R.id.headCal);
        headCarbs = findViewById(R.id.headCarbs);
        headFat = findViewById(R.id.headFat);
        headProtein = findViewById(R.id.headProtein);

        textCarbs = findViewById(R.id.textCarbs);
        textDFiber = findViewById(R.id.textDFiber);
        textSugar = findViewById(R.id.textSugar);
        textFat = findViewById(R.id.textFat);
        textSaturated = findViewById(R.id.textSaturated);
        textPUnsaturated = findViewById(R.id.textPUnsaturated);
        textMUnsaturated = findViewById(R.id.textMUnsaturated);
        textTrans = findViewById(R.id.textTrans);
        textProtein = findViewById(R.id.textProtein);
        textSodium = findViewById(R.id.textSodium);
        textPotassium = findViewById(R.id.textPotassium);
        textCholesterol = findViewById(R.id.textCholesterol);
        textVitaminA = findViewById(R.id.textVitaminA);
        textVitaminC = findViewById(R.id.textVitaminC);
        textCalcium = findViewById(R.id.textCalcium);
        textIron = findViewById(R.id.textIron);

        fruitSizes = findViewById(R.id.servingSize);



        fruitSizes.setOnItemSelectedListener(this);


        Bundle bundleExtras = getIntent().getExtras();

        if(bundleExtras != null){
            Uri savedBitmapUri = bundleExtras.getParcelable("FILE_URI");
            imageView.setImageURI(savedBitmapUri);
            imageBitmap = ((BitmapDrawable)(imageView).getDrawable()).getBitmap();
        }

        try{
            tflite=new Interpreter(loadmodelfile(this));
        }catch (IOException e){
            e.printStackTrace();
        }

        predictImage(imageBitmap);


    }

    private void predictImage(Bitmap bitmap){

        int imageTensorIndex = 0;
        int[] imageShape = tflite.getInputTensor(imageTensorIndex).shape(); // {1, height, width, 3}
        imageSizeY = imageShape[1];
        imageSizeX = imageShape[2];
        DataType imageDataType = tflite.getInputTensor(imageTensorIndex).dataType();

        int probabilityTensorIndex = 0;
        int[] probabilityShape = tflite.getOutputTensor(probabilityTensorIndex).shape(); // {1, NUM_CLASSES}
        DataType probabilityDataType = tflite.getOutputTensor(probabilityTensorIndex).dataType();

        inputImageBuffer = new TensorImage(imageDataType);
        outputProbabilityBuffer = TensorBuffer.createFixedSize(probabilityShape, probabilityDataType);
        probabilityProcessor = new TensorProcessor.Builder().add(getPostprocessNormalizeOp()).build();

        inputImageBuffer = loadImage(bitmap);

        tflite.run(inputImageBuffer.getBuffer(),outputProbabilityBuffer.getBuffer().rewind());
        showresult();
    }


    private TensorImage loadImage(final Bitmap bitmap) {
        // Loads bitmap into a TensorImage.
        inputImageBuffer.load(bitmap);

        // Creates processor for the TensorImage.
        int cropSize = Math.min(bitmap.getWidth(), bitmap.getHeight());
        // TODO(b/143564309): Fuse ops inside ImageProcessor.
        ImageProcessor imageProcessor =
                new ImageProcessor.Builder()
                        .add(new ResizeWithCropOrPadOp(cropSize, cropSize))
                        .add(new ResizeOp(imageSizeX, imageSizeY, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
                        .add(getPreprocessNormalizeOp())
                        .build();
        return imageProcessor.process(inputImageBuffer);
    }

    private MappedByteBuffer loadmodelfile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor=activity.getAssets().openFd("thesis3.tflite");
        FileInputStream inputStream=new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel=inputStream.getChannel();
        long startoffset = fileDescriptor.getStartOffset();
        long declaredLength=fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startoffset,declaredLength);
    }

    private TensorOperator getPreprocessNormalizeOp() {
        return new NormalizeOp(IMAGE_MEAN, IMAGE_STD);
    }
    private TensorOperator getPostprocessNormalizeOp(){
        return new NormalizeOp(PROBABILITY_MEAN, PROBABILITY_STD);
    }

    private void showresult(){

        try{
            labels = FileUtil.loadLabels(this,"label.txt");
        }catch (Exception e){
            e.printStackTrace();
        }
        Map<String, Float> labeledProbability =
                new TensorLabel(labels, probabilityProcessor.process(outputProbabilityBuffer))
                        .getMapWithFloatValue();
        float maxValueInMap =(Collections.max(labeledProbability.values()));

        for (Map.Entry<String, Float> entry : labeledProbability.entrySet()) {
            if (entry.getValue()==maxValueInMap) {
                String fruitName = "";
                switch (entry.getKey()){
                    case "Apple Washington":
                        fruitName = "apple washington";
                        textFruitName.setText("Washington Apple");
                        textFruitQuality.setText("Fresh");
                        break;
                    case "Apple Fuji":
                        fruitName = "apple fuji";
                        textFruitName.setText("Fuji Apple");
                        textFruitQuality.setText("Fresh");
                        break;
                    case "Banana Lakatan":
                        fruitName = "banana lakatan";
                        textFruitName.setText("Lakatan");
                        textFruitQuality.setText("Fresh");
                        break;
                    case "Banana Latundan":
                        fruitName = "banana latundan";
                        textFruitName.setText("Latundan");
                        textFruitQuality.setText("Fresh");
                        break;
                    case "Banana Saba":
                        fruitName = "banana saba";
                        textFruitName.setText("Saba");
                        textFruitQuality.setText("Fresh");
                        break;
                    case "Mango Carabao":
                        fruitName = "mango carabao";
                        textFruitName.setText("Carabao Mango");
                        textFruitQuality.setText("Fresh");
                        break;
                    case "Mango Indian":
                        fruitName = "mango indian";
                        textFruitName.setText("Indian Mango");
                        textFruitQuality.setText("Fresh");
                        break;
                    case "Orange":
                        fruitName = "orange";
                        textFruitName.setText("Orange");
                        textFruitQuality.setText("Fresh");
                        break;
                    case "Pineapple":
                        fruitName = "pineapple";
                        textFruitName.setText("Pineapple");
                        textFruitQuality.setText("Fresh");
                        break;
                    case "Watermelon":
                        fruitName = "watermelon";
                        textFruitName.setText("Watermelon");
                        textFruitQuality.setText("Fresh");
                        break;
                }
                nutritionalValues = getNutritionalValue(fruitName);

                if(nutritionalValues.size() != 0){


                    for(int i = 0; i < nutritionalValues.size(); i++){
                        spinnerItems.add(nutritionalValues.get(i).get(0));
                    }

                    ArrayAdapter aa = new ArrayAdapter(this, android.R.layout.simple_list_item_1, spinnerItems);
                    aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    fruitSizes.setAdapter(aa);
                }
            }
        }
    }

    public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
       // String[] nutriNames = {"Carbs", "DietaryFiber", "Sugar","Fat", "Saturated", "Polyunsaturated", "Monounsaturated", "Trans", "Protein", "Sodium", "Potassium", "Cholesterol", "VitaminA", "VitaminC", "Calcium", "Iron", "Calories"};
        String selectedSize = fruitSizes.getItemAtPosition(position).toString();

        for(int i = 0; i < nutritionalValues.size(); i++){

            if(nutritionalValues.get(i).get(0) == selectedSize){

                textCarbs.setText(nutritionalValues.get(i).get(1) + " g");
                textDFiber.setText(nutritionalValues.get(i).get(2) + " g");
                textSugar.setText(nutritionalValues.get(i).get(3) + " g");
                textFat.setText(nutritionalValues.get(i).get(4) + " g");
                textSaturated.setText(nutritionalValues.get(i).get(5) + " g");
                textPUnsaturated.setText(nutritionalValues.get(i).get(6) + " g");
                textMUnsaturated.setText(nutritionalValues.get(i).get(7) + " g");
                textTrans.setText(nutritionalValues.get(i).get(8) + " g");
                textProtein.setText(nutritionalValues.get(i).get(9) + " g");
                textSodium.setText(nutritionalValues.get(i).get(10) + " mg");
                textPotassium.setText(nutritionalValues.get(i).get(11) + " mg");
                textCholesterol.setText(nutritionalValues.get(i).get(12) + " mg");
                textVitaminA.setText(nutritionalValues.get(i).get(13) + " %");
                textVitaminC.setText(nutritionalValues.get(i).get(14) + " %");
                textCalcium.setText(nutritionalValues.get(i).get(15) + " %");
                textIron.setText(nutritionalValues.get(i).get(16) + " %");
                headCal.setText(nutritionalValues.get(i).get(17));
                headProtein.setText(nutritionalValues.get(i).get(9) + "g");
                headFat.setText(nutritionalValues.get(i).get(4) + "g");
                headCarbs.setText(nutritionalValues.get(i).get(1) + "g");
            }
        }

    }
    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

    private List<List<String>> getNutritionalValue(String fruitName){
        List<List<String>> tempNutriValues = new ArrayList<>();
        String[] nutriNames = {"Size", "Carbs", "DietaryFiber", "Sugar","Fat","Saturated","Polyunsaturated","Monounsaturated","Trans", "Protein", "Sodium", "Potassium", "Cholesterol", "VitaminA", "VitaminC", "Calcium", "Iron", "Calories"};

        if(fruitName != ""){
            try {

                JSONObject obj = new JSONObject(loadJSONFromAsset());

                JSONArray fruitArray = obj.getJSONArray(fruitName);

                for (int i = 0; i < fruitArray.length(); i++) {

                    List<String> temp = new ArrayList<>();
                    JSONObject fruitDetail = fruitArray.getJSONObject(i);
                    for(int y =0; y < nutriNames.length; y++){
                        temp.add(fruitDetail.getString(nutriNames[y]));
                    }

                    tempNutriValues.add(temp);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            List<String> temp = new ArrayList<>();
            for(int y =0; y < nutriNames.length; y++){

                temp.add("--");
            }

            tempNutriValues.add(temp);
        }

        return tempNutriValues;

    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getAssets().open("nutrition.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

}
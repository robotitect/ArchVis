package com.hackwestern.archvis;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.ibm.watson.developer_cloud.visual_recognition.v3.*;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.*;

public class MainActivity extends AppCompatActivity
{
    ImageView image; //image which will be taken
    Button butt; //capture butt

    Classifier styles;
    CreateClassifierOptions createClassifierOptions;
    VisualRecognition service;
    ClassifiedImages result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //1 - train classifier
//        service = new VisualRecognition(
//                VisualRecognition.VERSION_DATE_2016_05_20
//        );
//        service.setApiKey("8d7aced8efa9ce11cca985d203dce5989cc20148");
//        try
//        {
//            createClassifierOptions = new
//            CreateClassifierOptions.Builder()
//                    .name("styles")
//                    .addClass("baroque", new File("baroque.zip"))
//                    .addClass("brutalism", new File("brutalism.zip"))
//                    .addClass("edwardian", new File("edwardian.zip"))
//                    .addClass("georgian", new File("georgian.zip"))
//                    .addClass("gothic revival", new File("gothicrevival.zip"))
//                    .addClass("modern", new File("modern.zip"))
//                    .addClass("neoclassical", new File("neoclassical.zip"))
//                    .addClass("postmodern", new File("postmodern.zip"))
//                    .addClass("victorian", new File("victorian.zip"))
//                    .negativeExamples(new File("negative.zip"))
//                    .build();
//            styles = service.createClassifier(createClassifierOptions).execute();
//        }
//        catch(FileNotFoundException f)
//        {
//            f.printStackTrace();
//        }

        image = (ImageView)findViewById(R.id.imageView);
      //  butt = (Button)findViewById(R.id.button);

        //instantiate camera application
        Intent camInt = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult((Intent)camInt,0);
    }


    //press "Control & O" for shortcut
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        // turn bitmap into a png, so that it can actually be put into the algorithm
        Bitmap bitmap = (Bitmap)data.getExtras().get("data"); //data is Intent-var "data" (captured picture)
//        image.setImageBitmap(bitmap); //what is "setImagBitmap"?
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        String filePath = this.getFilesDir().getPath().toString() + "/picture.png";
        File testImages = new File(filePath);
        try{
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(testImages));
//        byte[] byteArray = os.toByteArray();
//        FileInputStream fileInputStream=null;
//
//        File file = new File("yourfile");
//
//        byteArray = new byte[(int) file.length()];
//
//        try {
//            //convert file into array of bytes
//            fileInputStream = new FileInputStream(file);
//            fileInputStream.read(byteArray);
//            fileInputStream.close();
//
//            //convert array of bytes into file
//            FileOutputStream fileOutputStream =
//                    new FileOutputStream("picture.jpg");
//            fileOutputStream.write(byteArray);
//            fileOutputStream.close();
//
//            System.out.println("Done");
        }catch(Exception e){
            e.printStackTrace();
        }

               //2 a) - input "image" into ObjectRecognition algorithm
        try {
           // String id = styles.getClassifierId();
            final ClassifyOptions classifyOptions = new ClassifyOptions.Builder()
                    .imagesFile(testImages)
//                    .imagesFilename("picture.jpg")
                   // .parameters("{\"classifier_ids\": [\"/*"+ id + "*/styles_1512772808\"],"
                    .parameters("{\"classifier_ids\": [\"styles_952611272\"],"
                            + "\"owners\": [\"me\"]}")
                    .build();
            service = new VisualRecognition(VisualRecognition.VERSION_DATE_2016_05_20);
            service.setApiKey("8d7aced8efa9ce11cca985d203dce5989cc20148");
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    //code to do the HTTP request
                    result = service.classify(classifyOptions).execute();
                }
            });
            thread.start();
            thread.sleep(4000);

            // 2. b) - find the top scoring result
            String currStyle = "";
            float max = 0f;
            for(ClassifiedImage check : result.getImages())
            {
                max = 0f;
                for(ClassifierResult check2 : check.getClassifiers())
                {
                    for(ClassResult check3: check2.getClasses())
                    {
                        if(check3.getScore() > max)
                        {
                            max = check3.getScore();
                            currStyle = check3.getClassName();
                        }
                    }
                }
            }

            //3 - output result in popup
            String message;
            if(max >= 0.8f)
                message = "This building is in " + currStyle + " style.";
            else
                message = "This is an invalid input.";

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

            // set dialog message
            alertDialogBuilder.setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            })
            .setMessage(message);

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();
            // show it
            alertDialog.show();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}

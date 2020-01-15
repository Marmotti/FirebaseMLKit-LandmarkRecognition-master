package com.ayushivadwala.landmarkrecognition;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ayushivadwala.landmarkrecognition.singleton.Stopwatch;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmark;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmarkDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.util.List;

public class MainActivity extends BaseActivity  {

    private ImageView myImageView;
    private TextView tv_description;
    private TextView tv_elapsedTime;
    private Bitmap myBitmap;
    private Button tryAnother;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        listeners();
    }

    public void init() {
        tv_description = findViewById(R.id.tv_description);
        tv_elapsedTime = findViewById(R.id.tv_timeElapsed);
        myImageView = findViewById(R.id.imageView);
        tryAnother = (Button) findViewById(R.id.btn);

    }

    public void listeners() {
        tryAnother.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(MainActivity.this, MainActivity.class);
                startActivity(intent);

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Stopwatch stopwatchSingleton = Stopwatch.getInstance();
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case WRITE_STORAGE:
                    checkPermission(requestCode);
                case CAMERA:
                    checkPermission(requestCode);
                    break;
                case SELECT_PHOTO:
                    stopwatchSingleton.start();
                    Uri dataUri = data.getData();
                    String path = MyHelper.getPath(this, dataUri);
                    if (path == null) {
                        myBitmap = MyHelper.resizePhoto(photoFile, this, dataUri, myImageView);
                    } else {
                        myBitmap = MyHelper.resizePhoto(photoFile, path, myImageView);
                    }
                    if (myBitmap != null) {
                        tv_description.setText(null);
                        myImageView.setImageBitmap(myBitmap);
                        runLandmarkDetector(myBitmap);
                    }
                    break;
                case TAKE_PHOTO:
                    myBitmap = MyHelper.resizePhoto(photoFile, photoFile.getPath(), myImageView);
                    if (myBitmap != null) {
                        tv_description.setText(null);
                        myImageView.setImageBitmap(myBitmap);
                        runLandmarkDetector(myBitmap);
                    }
                    break;
            }
        }
    }

    private void runLandmarkDetector(Bitmap bitmap) {
        /*FirebaseVisionFaceDetectorOptions options = new FirebaseVisionFaceDetectorOptions.Builder()
                .setModeType(FirebaseVisionFaceDetectorOptions.FAST_MODE)
                .setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .setLandmarkType(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                .setMinFaceSize(0.1f)
                .setTrackingEnabled(false)
                .build();

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(myBitmap);
        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance().getVisionFaceDetector(options);
        detector.detectInImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
            @Override
            public void onSuccess(List<FirebaseVisionFace> faces) {
                tv_description.setText(runFaceRecog(faces));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure
                    (@NonNull Exception exception) {
                Toast.makeText(MainActivity.this,
                        "Exception", Toast.LENGTH_LONG).show();
            }
        });*/

        if (bitmap == null) {
            Toast.makeText(this, "There was some error", Toast.LENGTH_SHORT).show();
            return;
        }


        final FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionCloudDetectorOptions options =
                new FirebaseVisionCloudDetectorOptions.Builder()
                        .setModelType(FirebaseVisionCloudDetectorOptions.LATEST_MODEL)
                        .setMaxResults(15)
                        .build();
        FirebaseVisionCloudLandmarkDetector detector = FirebaseVision.getInstance()
                .getVisionCloudLandmarkDetector();

        Task<List<FirebaseVisionCloudLandmark>> result = detector.detectInImage(FirebaseVisionImage.fromBitmap(bitmap))
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionCloudLandmark>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionCloudLandmark> firebaseVisionCloudLandmarks) {
                        // Task completed successfully
                        // ...
                        Stopwatch stopwatchSingleton = Stopwatch.getInstance();
                        for (FirebaseVisionCloudLandmark landmark: firebaseVisionCloudLandmarks) {

                            Rect bounds = landmark.getBoundingBox();
                            String landmarkName = landmark.getLandmark();
                            String entityId = landmark.getEntityId();
                            float confidence = landmark.getConfidence();
                            stopwatchSingleton.stop();
                            String elapsedTime = Long.toString(stopwatchSingleton.getRunningTime());
                            if(confidence<0.3){
                                tv_description.setText(" not sure what place it is!! ");
                                tv_elapsedTime.setText("time passed: " + elapsedTime);

                            }
                            else{
                                tv_description.setText(landmarkName + "\nProbability:: "+confidence);
                                tv_elapsedTime.setText("time passed: " + elapsedTime);
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this,"Exception", Toast.LENGTH_LONG).show();
                    }
                });
    }


}



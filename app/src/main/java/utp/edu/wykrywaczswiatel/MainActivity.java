package utp.edu.wykrywaczswiatel;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.custom.FirebaseCustomRemoteModel;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.vision.detector.Detection;
import org.tensorflow.lite.task.vision.detector.ObjectDetector;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private static final String TAG = "";
    private ObjectDetector detector;
    private Bitmap image;
    private Bitmap out;
    private ImageView photo;
    //private TextView light;
    public String result;
    public static TensorImage tmp;
    public Mat bmp;
    private Canvas canvas;

    private RoomConnect roomConnect;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        roomConnect= new RoomConnect(this);
        photo = (ImageView) findViewById(R.id.photo);
        //light = (TextView) findViewById(R.id.light);
        InitModel();
    }

   private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("OpenCV", "OpenCV loaded successfully");
                    bmp = new Mat();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int MY_STORGE_PERMISSION_CODE = 101;

    private void dispatchTakePictureIntent() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePicture, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            //Error
            Toast.makeText(this, "Brak aparatu w telefonie!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            image = (Bitmap) extras.get("data");
            Detect();
        }
    }

    private LightResults.Light GetLight(Mat bmp1)
    {
        Mat img = new Mat();
        Size desired_dim = new Size(30, 60);
        Imgproc.resize(bmp1, img, desired_dim);

        Imgproc.blur(img, img, new Size(3, 3));

        Mat img_hsv = new Mat();
        Imgproc.cvtColor(img, img_hsv, Imgproc.COLOR_RGB2HSV_FULL);

        float rate;

        Scalar lower_green;
        Scalar upper_green;
        lower_green = new Scalar(38, 10, 20);
        upper_green = new Scalar(85, 255, 255);
        Mat mask2 = new Mat();
        Core.inRange(img_hsv, lower_green, upper_green, mask2);

        Mat circle = new Mat();
        Imgproc.HoughCircles(mask2, circle, Imgproc.HOUGH_GRADIENT, 1, mask2.rows()/10, 200, 10, 0,0);
        if (!circle.empty())
        {
            return LightResults.Light.LIGHT_GREEN;
        }

        Scalar lower_yellow;
        Scalar upper_yellow;
        lower_yellow = new Scalar(13, 10, 20);
        upper_yellow = new Scalar(38, 255, 255);
        Mat mask1 = new Mat();
        Core.inRange(img_hsv, lower_yellow, upper_yellow, mask1);

        circle = new Mat();
        Imgproc.HoughCircles(mask1, circle, Imgproc.HOUGH_GRADIENT, 1, mask1.rows()/10, 200, 10, 0,0);
        if (!circle.empty())
        {
            return LightResults.Light.LIGHT_YELLOW;
        }

        Scalar lower_red;
        Scalar upper_red;
        lower_red = new Scalar(0, 10, 20);
        upper_red = new Scalar(13, 255, 255);
        Mat mask0 = new Mat();
        Core.inRange(img_hsv, lower_red, upper_red, mask0);
        lower_red = new Scalar(165, 10, 20);
        upper_red = new Scalar(180, 225, 255);
        Mat mask0a = new Mat();
        Core.inRange(img_hsv, lower_red, upper_red, mask0a);
        Mat mask = new Mat();
        Core.subtract(mask0, mask0a, mask);

        circle = new Mat();
        Imgproc.HoughCircles(mask, circle, Imgproc.HOUGH_GRADIENT, 1, mask.rows()/10, 200, 10, 0,0);
        if (!circle.empty())
        {
            return LightResults.Light.LIGHT_RED;
        }
        return LightResults.Light.LIGHT_NULL;
    }

    private void InitModel() {
        FirebaseCustomRemoteModel rm = new FirebaseCustomRemoteModel.Builder("Traffic-Detector").build();
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                .requireWifi()
                .build();
        FirebaseModelManager.getInstance().download(rm, conditions)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        FirebaseModelManager.getInstance().getLatestModelFile(rm)
                                .addOnCompleteListener(new OnCompleteListener<File>() {
                                    @Override
                                    public void onComplete(@NonNull Task<File> task) {
                                        File modelFile = task.getResult();
                                        if (modelFile != null) {
                                            ObjectDetector.ObjectDetectorOptions options =
                                                    ObjectDetector.ObjectDetectorOptions.builder()
                                                            .setScoreThreshold(0.5f)
                                                            .setMaxResults(5)
                                                            .build();
                                            try {
                                                ///data/data/utp.edu.wykrywaczswiatel/no_backup/com.google.firebase.ml.custom.models/W0RFRkFVTFRd+MTo3NjYzNzQ0Njc1MjphbmRyb2lkOmQxYTgwNjIzZTJiMGEyNTcyNzE5MDU/Traffic-Detector/0
                                                File model = new File("model.tflite");
                                                modelFile.renameTo(model);
                                                Log.d("Path", model.getPath());
                                                detector = ObjectDetector.createFromFileAndOptions(
                                                        getApplicationContext(), model.getPath(), options);

                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                });
                    }
                });
    }

    private void SaveOdp(Bitmap bitmap, RectF rect, LightResults.Light light)
    {
        Bitmap bmp = Bitmap.createBitmap(bitmap, Math.round(rect.left), Math.round(rect.top),
                Math.round(rect.right - rect.left), Math.round(rect.bottom - rect.top));
        roomConnect.SaveResult(bmp, light);
    }

    private void Detect() {
        out = image.copy(Bitmap.Config.ARGB_8888, true);
        canvas= new Canvas(out);
        canvas.drawBitmap(image, image.getWidth(), image.getHeight(), null);
        Paint p = new Paint();

        result = "";
        tmp = TensorImage.fromBitmap(image);

        List<Detection> results = detector.detect(tmp);
            for (Detection detectedObject : results) {
                RectF boundingBox = detectedObject.getBoundingBox();
                result = result + boundingBox.toString() + "\n";
                for (Category label : detectedObject.getCategories()) {
                    if (label.getIndex() == 9) {
                        Utils.bitmapToMat(image.copy(Bitmap.Config.ARGB_8888, true), bmp);
                        Mat out = new Mat(bmp, new Rect(Math.round(boundingBox.left), Math.round(boundingBox.top),
                                Math.round(boundingBox.right - boundingBox.left), Math.round(boundingBox.bottom - boundingBox.top)));
                        String info = "";
                        LightResults.Light l = GetLight(out);
                        if (l == LightResults.Light.LIGHT_GREEN) {
                            p.setStyle(Paint.Style.STROKE);
                            p.setColor(Color.GREEN);
                            p.setFilterBitmap(true);
                            canvas.drawRect(boundingBox, p);
                            info = "Light green!";
                        } else if (l == LightResults.Light.LIGHT_RED) {
                            p.setStyle(Paint.Style.STROKE);
                            p.setColor(Color.RED);
                            p.setFilterBitmap(true);
                            canvas.drawRect(boundingBox, p);
                            info = "Light red!";
                        } else if (l == LightResults.Light.LIGHT_YELLOW) {
                            p.setStyle(Paint.Style.STROKE);
                            p.setColor(Color.YELLOW);
                            p.setFilterBitmap(true);
                            canvas.drawRect(boundingBox, p);
                            info = "Light yellow!";
                        } else if (l == LightResults.Light.LIGHT_NULL) {
                            p.setStyle(Paint.Style.STROKE);
                            p.setColor(Color.DKGRAY);
                            p.setFilterBitmap(true);
                            canvas.drawRect(boundingBox, p);
                            info = "Light not detected!";
                        }
                        result = result + info + "\n";
                        SaveOdp(image, boundingBox, l);
                    }
                    String text = label.getLabel();
                    int index = label.getIndex();
                    float confidence = label.getScore();
                    result = result + text + " " + String.valueOf(index) + " " + String.valueOf(confidence);
                }
                result = result + "\n\n";
            }
            photo.setImageBitmap(out);
            photo.setScaleType(ImageView.ScaleType.FIT_CENTER);
            //light.setText(result);
    }

    public void NewImage(View view) {
        if (detector == null)
        {
            return;
        }
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
        }
        else
        {
            dispatchTakePictureIntent();
        }
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, MY_STORGE_PERMISSION_CODE);
        }
    }

    public void NextView(View view) {
        Intent intent = new Intent(this, BazaWykrywaczSwiatel.class);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "Uprawnienia przyznane dla aparatu.", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
            }
            else
            {
                Toast.makeText(this, "Brak uprawnień dla aparatu!", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == MY_STORGE_PERMISSION_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "Uprawnienia przyznane dla katalogu.", Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(this, "Brak uprawnień dla katalogu!", Toast.LENGTH_LONG).show();
            }
        }
    }
}
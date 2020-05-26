package com.example.imagelabeler;

//Imports

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.icu.text.SimpleDateFormat;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.label.FirebaseVisionCloudImageLabelerOptions;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.objects.FirebaseVisionObject;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetector;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    Map<Integer, String> categoryMap = new HashMap<>();
    static final int REQUEST_IMAGE_CAPTURE = 1;
    ImageView imageView;
    static final int REQUEST_TAKE_PHOTO = 1;
    String currentPhotoPath;
    Uri uri;

    private Canvas canvas;
    Bitmap bitmap;
    LinkedList<Rect> rectanglesList;
    LinkedList<Integer> categoriesList;
    LinkedList<String> labelsList;
    Map<String, List> processorMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        processorMap = new HashMap<>();
        rectanglesList = new LinkedList<>();
        categoriesList = new LinkedList<>();
        labelsList = new LinkedList<>();
    }

    // -------------------------------------------- //
    //   AndroidStudio - take photos Documentation
    // -------------------------------------------- //

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName, ".jpg", storageDir
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void onTakePhoto(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imageView = findViewById(R.id.image);
        populateCategories();
        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e("Error:", ex.toString());
            }
            if (photoFile != null) {
                uri = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(intent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode,data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            bitmap = BitmapFactory.decodeFile(currentPhotoPath);
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            bitmap = rotateBitmap(bitmap);
            processImage();
            imageView.setImageBitmap(bitmap);
        }
    }

    // -------------------------------------------- //
    //   AndroidGraphics - PAINT and StackOverflow
    // -------------------------------------------- //

    private void draw(Rect rect, int classificationCategory, String label) {
        Paint strokePaint = new Paint();
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setColor(Color.RED);
        strokePaint.setStrokeWidth(10);
        canvas.drawRect(rect, strokePaint);
        Paint text = new Paint();
        text.setStyle(Paint.Style.FILL);
        text.setColor(Color.RED);
        text.setTextSize(100);
        if ((rect.top - 100) <= 0) {
            canvas.drawText(label, rect.left, rect.bottom + 100, text);
        } else {
            canvas.drawText(label, rect.left, rect.top - 50, text);
        }
        imageView.postInvalidate();
    }
    public void labelBuffer(String label) {
        this.labelsList.add(label);
        startDraw();
    }
    public void rectanglesBuffer(Rect rect) {
        this.rectanglesList.add(rect);
    }
    public void categoriesBuffer(int category) {
        this.categoriesList.add(category);
    }
    public void startDraw() {
        draw(rectanglesList.remove(), categoriesList.remove(), labelsList.remove());
    }
    
    public void faceBuffer(Rect face) {
        this.facesList.add(face);
        drawFaces(face);
    }
  
    
    private void drawFaces(Rect rect) {
        Paint strokePaint = new Paint();
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setColor(Color.YELLOW);
        strokePaint.setStrokeWidth(10);
        canvas.drawRect(rect, strokePaint);
        Paint text = new Paint();
        text.setStyle(Paint.Style.FILL);
        text.setColor(Color.RED);
        text.setTextSize(100);
        if ((rect.top - 100) <= 0) {
            canvas.drawText("FACE", rect.left, rect.bottom + 100, text);
        } else {
            canvas.drawText("FACE", rect.left, rect.top - 50, text);
        }
        imageView.postInvalidate();
    }
  
    // -------------------------------------------- //
    ///        FireBase Documentation
    // -------------------------------------------- //

    private void processImage() {
        FirebaseVisionObjectDetectorOptions options = new FirebaseVisionObjectDetectorOptions.Builder().setDetectorMode(FirebaseVisionObjectDetectorOptions.SINGLE_IMAGE_MODE).enableMultipleObjects().enableClassification().build();
        final FirebaseVisionObjectDetector objectDetector = FirebaseVision.getInstance().getOnDeviceObjectDetector(options);
        FirebaseVisionCloudImageLabelerOptions labelerOptions = new FirebaseVisionCloudImageLabelerOptions.Builder().build();
        final FirebaseVisionImageLabeler imageLabeler = FirebaseVision.getInstance().getCloudImageLabeler(labelerOptions);
        final FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionFaceDetectorOptions realTimeOpts = new FirebaseVisionFaceDetectorOptions.Builder().setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS).build();
        final FirebaseVisionFaceDetector detector = FirebaseVision.getInstance().getVisionFaceDetector(realTimeOpts);
        canvas = new Canvas(bitmap);
        objectDetector.processImage(image)
                .addOnSuccessListener(
                        new OnSuccessListener<List<FirebaseVisionObject>>() {
                            @Override
                            public void onSuccess(List<FirebaseVisionObject> detectedObjects) {
                                processorMap.put("imageObjectData", detectedObjects);
                                for (int i = 0; i < detectedObjects.size(); i++) {
                                    FirebaseVisionObject obj = detectedObjects.get(i);
                                    Rect rect = obj.getBoundingBox();
                                    int classificationCategory = obj.getClassificationCategory();
                                    rectanglesBuffer(rect);
                                    categoriesBuffer(classificationCategory);
                                    assert(rect.left < rect.right && rect.top < rect.bottom);
                                    Bitmap resultBitmap = Bitmap.createBitmap(rect.right-rect.left + 100, rect.bottom-rect.top + 100, Bitmap.Config.ARGB_8888);
                                    new Canvas(resultBitmap).drawBitmap(bitmap, -rect.left - 50, -rect.top - 50, null);
                                    FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(resultBitmap);

                                    //First we detect an object and then we add the label for the object
                                    imageLabeler.processImage(image)
                                            .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
                                                @Override
                                                public void onSuccess(List<FirebaseVisionImageLabel> labels) {
                                                    for(FirebaseVisionImageLabel label : labels) {
                                                        String itemLabels = label.getText();
                                                        labelBuffer(itemLabels);
                                                        break;
                                                    }
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.e("Error", e.toString());
                                                }
                                            });
                                }
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("Error", e.toString());
                            }
                        });
        //First we detect an object and now we are detecting faces and drawing it on the canvas
        //Removed it because it made the image labelling really slow.
        //detector.detectInImage(image)
                //.addOnSuccessListener(
                        //new OnSuccessListener<List<FirebaseVisionFace>>() {
                            @Override
                            //public void onSuccess(List<FirebaseVisionFace> faces) {
                                //processorMap.put("imageObjectData",faces);
                                //for (int i = 0; i < faces.size(); i++) {
                                    //FirebaseVisionFace obj = faces.get(i);
                                    //List<FirebaseVisionPoint> leftEyeContour =
                                            //obj.getContour(FirebaseVisionFaceContour.LEFT_EYE).getPoints();
                                    //List<FirebaseVisionPoint> upperLipBottomContour =
                                            //obj.getContour(FirebaseVisionFaceContour.UPPER_LIP_BOTTOM).getPoints();
                                    //Rect rect = obj.getBoundingBox();
                                    //rectanglesBuffer(rect);
//                                    float x = translateX(obj.getBoundingBox().centerX());
//                                    float y = translateY(obj.getBoundingBox().centerY());
//                                    float xOffset = scaleX(obj.getBoundingBox().width() / 2.0f);
//                                    float yOffset = scaleY(obj.getBoundingBox().height() / 2.0f);
//                                    float left = x - xOffset;
//                                    float top = y - yOffset;
//                                    float right = x + xOffset;
//                                    float bottom = y + yOffset;
//                                    canvas.drawRect(left, top, right, bottom, boxPaint);
//
//                                    FirebaseVisionFaceContour contour = obj.getContour(FirebaseVisionFaceContour.ALL_POINTS);
//                                    for (com.google.firebase.ml.vision.common.FirebaseVisionPoint point : contour.getPoints()) {
//                                        float px = translateX(point.getX());
//                                        float py = translateY(point.getY());
//                                        canvas.drawCircle(px, py, FACE_POSITION_RADIUS, facePositionPaint);
//                                    }
                                    //assert(rect.left < rect.right && rect.top < rect.bottom);
                                    //Bitmap resultBitmap = Bitmap.createBitmap(rect.right-rect.left + 100, rect.bottom-rect.top + 100, Bitmap.Config.ARGB_8888);
                                    //new Canvas(resultBitmap).drawBitmap(bitmap, -rect.left - 50, -rect.top - 50, null);
                                }
                            }
                        })
                //.addOnFailureListener(
                        //new OnFailureListener() {
                           // @Override
                           // public void onFailure(@NonNull Exception e) {
                                //Log.e("Error", e.toString());
                            //}
                        //});

    }

    //Couldn't figure out the firebase documentation to rotate images so used stackoverflow instead.
//    private class YourAnalyzer implements ImageAnalysis.Analyzer {
//
//        private int degreesToFirebaseRotation(int degrees) {
//            switch (degrees) {
//                case 0:
//                    return FirebaseVisionImageMetadata.ROTATION_0;
//                case 90:
//                    return FirebrectanglesList = new LinkedList<>();
//        categoriesList = new LinkedList<>();
//        labelsList = new LinkedList<>();eVisionImageMetadata.ROTATION_90;
//                case 180:
//                    return FirebaseVisionImageMetadata.ROTATION_180;
//                case 270:
//                    return FirebaseVisionImageMetadata.ROTATION_270;
//                default:
//                    throw new IllegalArgumentException(
//                            "Rotation must be 0, 90, 180, or 270.");
//            }
//        }
//    }


    // -------------------------------------------- //
    //          Stack Overflow Materials
    // -------------------------------------------- //

    //Got these categories through Stack overflow after searching multiple threads.
    private void populateCategories() {
        this.categoryMap.put(0, "Category Unknown");
        this.categoryMap.put(1, "Home Good");
        this.categoryMap.put(2, "Fashion Good");
        this.categoryMap.put(3, "Food");
        this.categoryMap.put(4, "Place");
        this.categoryMap.put(5, "Plant");
    }

    //I used stack overflow to find how to rotate a bitmap dependent on the device as devices rotate the picture with different angles
    //LINK: https://stackoverflow.com/questions/8981845/android-rotate-image-in-imageview-by-an-angle
    protected Bitmap rotateBitmap(Bitmap bitmap){
        Matrix matrix = new Matrix();
        matrix.postRotate(getExifOrientation(currentPhotoPath));
        return Bitmap.createBitmap(bitmap, 0, 0,bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    //Used stack overflow to get how to rotate the image based on the degree the device rotated it to.
    //LINK: https://stackoverflow.com/questions/20478765/how-to-get-the-correct-orientation-of-the-image-selected-from-the-default-image
    public static int getExifOrientation(String filepath) {
        int degree = 0;
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(filepath);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (exif != null) {
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            if (orientation != -1) {
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                }
            }
        }
        return degree;
    }

}

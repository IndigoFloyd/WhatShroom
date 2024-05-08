package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import androidx.core.content.FileProvider;
import android.graphics.drawable.BitmapDrawable;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;
import android.os.Environment;
import android.graphics.BitmapFactory;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

public class FirstActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final int REQUEST_IMAGE_SELECT = 3;
    private static final int REQUEST_INTERNET_PERMISSION = 4;
    private static final MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpeg");
    private static final String SERVER_URL = "http://192.168.31.44:5000/upload";
    private boolean cameraPermissionGranted = false;
    private boolean readStoragePermissionGranted = false;
    private ImageView imageview;
    private Button button1;
    private Button button2;
    private Bitmap imageBitmap;
    private File photoFile;
    private EditText textbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.firstlayout);
        imageview = findViewById(R.id.imageview);
        imageview.setScaleType(ImageView.ScaleType.FIT_CENTER);
        button1 = findViewById(R.id.Button1);
        button2 = findViewById(R.id.Button2);
        textbox = findViewById(R.id.textbox);
        textbox.setFocusable(false);
        textbox.setClickable(false);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageview.setImageResource(R.drawable.logo1);
                // 检查相机权限
                if (ContextCompat.checkSelfPermission(FirstActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    // 请求相机权限
                    ActivityCompat.requestPermissions(FirstActivity.this,
                            new String[]{Manifest.permission.CAMERA}, REQUEST_IMAGE_CAPTURE);
                } else {
                    // 启动相机拍摄照片
                    dispatchTakePictureIntent();
                }
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 检查网络权限
                if (ContextCompat.checkSelfPermission(FirstActivity.this, Manifest.permission.INTERNET)
                        != PackageManager.PERMISSION_GRANTED) {
                    // 请求网络权限
                    ActivityCompat.requestPermissions(FirstActivity.this,
                            new String[]{Manifest.permission.INTERNET}, REQUEST_INTERNET_PERMISSION);
                } else {
                    // 网络权限已授予,可以上传图片
                    uploadImageToServer();
                }
            }
        });

        imageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageview.setImageResource(R.drawable.logo1);
                // 检查读取外部存储权限
                if (ContextCompat.checkSelfPermission(FirstActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    // 请求读取外部存储权限
                    ActivityCompat.requestPermissions(FirstActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_IMAGE_SELECT);
                } else {
                    // 从图库选择照片
                    dispatchSelectPictureIntent();
                }
            }
        });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // 处理文件创建错误
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.myapplication.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // 创建一个以时间戳命名的图像文件
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        return image;
    }




    private void dispatchSelectPictureIntent() {
        Intent selectPictureIntent = new Intent(Intent.ACTION_GET_CONTENT);
        selectPictureIntent.setType("image/*");
        startActivityForResult(Intent.createChooser(selectPictureIntent, "选择照片"), REQUEST_IMAGE_SELECT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                if (photoFile != null) {
                    // 加载并显示原始图像
                    imageBitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                    imageview.setImageBitmap(imageBitmap);
                }
            } else if (requestCode == REQUEST_IMAGE_SELECT) {
                Uri imageUri = data.getData();
                try {
                    imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    imageview.setImageBitmap(imageBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 相机权限已授予
                cameraPermissionGranted = true;
                dispatchTakePictureIntent();
            } else {
                // 相机权限被拒绝
                Toast.makeText(this, "相机权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_IMAGE_SELECT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 读取外部存储权限已授予
                readStoragePermissionGranted = true;
                dispatchSelectPictureIntent();
            } else {
                // 读取外部存储权限被拒绝
                Toast.makeText(this, "外部存储权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_INTERNET_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 网络权限已授予
                cameraPermissionGranted = true;
                uploadImageToServer();
            } else {
                // 网络权限被拒绝
                Toast.makeText(this, "网络权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadImageToServer() {
        // 获取 ImageView 中的图片 Bitmap
        Bitmap imageBitmap = ((BitmapDrawable) imageview.getDrawable()).getBitmap();

        // 将 Bitmap 转换为字节数组
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();

        // 异步任务发送 POST 请求
        new UploadImageTask().execute(imageBytes);
        Toast.makeText(this, "成功转为字节数组并发起POST请求", Toast.LENGTH_SHORT).show();
    }

    private class UploadImageTask extends AsyncTask<byte[], Void, Boolean> {

        private Bitmap resultBitmap;
        private void drawRect(Bitmap bitmap, int x1, int y1, int x2, int y2) {
            if (bitmap != null) {
//                imageView.buildDrawingCache();
//                Bitmap bitmap = imageView.getDrawingCache();
                Canvas canvas = new Canvas(bitmap);
                Paint paint = new Paint();
                paint.setColor(Color.RED);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth((int) (bitmap.getHeight() / 300));
                Rect rect = new Rect(x1, y1, x2, y2);
                canvas.drawRect(rect, paint);
            }
        }

        private void drawText(Bitmap bitmap, String text, int x, int y) {
            if (bitmap != null) {
//                imageView.buildDrawingCache();
//                Bitmap bitmap = imageView.getDrawingCache();
                Canvas canvas = new Canvas(bitmap);
                Paint paint = new Paint();
                paint.setColor(Color.WHITE);
                paint.setTextSize((int) (bitmap.getHeight() / 15));
                canvas.drawText(text, x, y, paint);
            }
        }

        @Override
        protected Boolean doInBackground(byte[]... params) {
            byte[] imageBytes = params[0];
            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = RequestBody.create(MEDIA_TYPE_JPEG, imageBytes);
            Request request = new Request.Builder()
                    .url(SERVER_URL)
                    .post(requestBody)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    JSONObject jsonObject; // 将jsonObject声明在try块外部
                    try {
                        jsonObject = new JSONObject(responseData);
                        JSONArray results = jsonObject.getJSONArray("objects");
                        resultBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);
                        String resultString = "";
                        if (results.length() > 0){
                            for (int i = 0; i < results.length(); i++) {
                                // 获取单个结果
                                JSONObject result = results.getJSONObject(i);
                                String class_name = result.getString("class");
                                String confidence = result.getString("confidence");
                                JSONArray box = result.getJSONArray("box");
                                int x1 = box.getInt(0);
                                int y1 = box.getInt(1);
                                int x2 = box.getInt(2);
                                int y2 = box.getInt(3);
                                drawRect(resultBitmap, x1, y1, x2, y2);
                                drawText(resultBitmap, ""+i, x1+5, y1+5);
                                resultString += ("Number: " + ""+i + "\n" + "Class name: " + class_name + "\n" + "Confidence: " + confidence + '\n');
                            }
                            textbox.setText(resultString + "检测到疑似毒菇，不建议食用");}
                        else{
                            textbox.setText("未检测到疑似毒菇，建议重新拍摄或谨慎食用");
                        }
                        return true;
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return false; // 或者其他适当的处理方式，例如返回一个错误状态
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(FirstActivity.this, "图片上传成功", Toast.LENGTH_SHORT).show();
//                imageview.invalidate();
                imageview.setImageBitmap(resultBitmap);
            } else {
                Toast.makeText(FirstActivity.this, "图片上传失败", Toast.LENGTH_SHORT).show();
            }
        }
    }


}


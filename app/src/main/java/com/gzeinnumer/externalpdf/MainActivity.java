package com.gzeinnumer.externalpdf;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gzeinnumer.externalpdf.helper.FunctionGlobalDir;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity_";

    String[] permissions = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    Button btnCreate;
    EditText editText;

    TextView tv;
    String msg = "externalpdf\n";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(TAG);

        tv = findViewById(R.id.tv);

        btnCreate = findViewById(R.id.create);
        editText = findViewById(R.id.edittext);

        if (checkPermissions()) {
            msg += "Izin diberikan\n";
            tv.setText(msg);
            onSuccessCheckPermitions();
        } else {
            msg += "Beri izin dulu\n";
            tv.setText(msg);
        }

    }

    private void createPdf(String sometext) {
        // create a new document
        PdfDocument document = new PdfDocument();
        int pageWidth = 600;
        int pageHeight = 900;
        int marginLeft = 50;
        int firstTop = 50;

        // crate a page description
        PdfDocument.PageInfo pageInfo;
        for (int i=1; i<=2; i++){
            pageInfo= new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, i).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setTextSize(23);
            canvas.drawText(sometext, firstTop, marginLeft, paint);
            Bitmap bm = resizeImageForImageView(pageWidth,getBitmap(FunctionGlobalDir.getStorageCard+FunctionGlobalDir.appFolder+"/test.jpg"));
            canvas.drawBitmap(bm,marginLeft,firstTop+30, null);
            document.finishPage(page);
        }

        String directory_path = FunctionGlobalDir.getStorageCard + FunctionGlobalDir.appFolder;
        File file = new File(directory_path);
        if (!file.exists()) {
            file.mkdirs();
        }
        String targetPdf = directory_path + "/test-2.pdf";
        File filePath = new File(targetPdf);
        try {
            document.writeTo(new FileOutputStream(filePath));
            Toast.makeText(this, "Done", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e("main", "error " + e.toString());
            Toast.makeText(this, "Something wrong: " + e.toString(), Toast.LENGTH_LONG).show();
        }

        document.close();
    }

    private void onSuccessCheckPermitions() {
        if (FunctionGlobalDir.initFolder()) {
            if (FunctionGlobalDir.isFileExists(FunctionGlobalDir.appFolder)) {
                msg += "Sudah bisa lanjut\n";
                tv.setText(msg);

                btnCreate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        createPdf(editText.getText().toString());
                    }
                });

            } else {
                msg += "Direktory tidak ditemukan\n";
                tv.setText(msg);
            }
        } else {
            msg += "Gagal membuat folder\n";
            tv.setText(msg);
        }
    }

    int MULTIPLE_PERMISSIONS = 1;

    private boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();

        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(getApplicationContext(), p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[0]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MULTIPLE_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onSuccessCheckPermitions();
            } else {
                StringBuilder perStr = new StringBuilder();
                for (String per : permissions) {
                    perStr.append("\n").append(per);
                }
            }
        }
    }

    public Bitmap getBitmap(String path) {
        Bitmap bitmap=null;
        try {
            File f= new File(path);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return bitmap ;
    }

    public Bitmap resizeImageForImageView(int scaleSize,Bitmap bitmap) {
        scaleSize = scaleSize/2;
        Bitmap resizedBitmap = null;
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int newWidth = -1;
        int newHeight = -1;
        float multFactor = -1.0F;
        if(originalHeight > originalWidth) {
            newHeight = scaleSize ;
            multFactor = (float) originalWidth/(float) originalHeight;
            newWidth = (int) (newHeight*multFactor);
        } else if(originalWidth > originalHeight) {
            newWidth = scaleSize ;
            multFactor = (float) originalHeight/ (float)originalWidth;
            newHeight = (int) (newWidth*multFactor);
        } else if(originalHeight == originalWidth) {
            newHeight = scaleSize ;
            newWidth = scaleSize ;
        }
        resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
        return resizedBitmap;
    }
}
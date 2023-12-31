package com.laatech.imagetotext;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.imagetotextocrscanner.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Locale;
import static com.laatech.imagetotext.MainActivity.EXTRA_TEXT;

public class detail extends AppCompatActivity {
    private EditText text1;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        text1 = findViewById(R.id.text_display);
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        InterstitialAd.load(this,getString(R.string.inter), adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                    }
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        mInterstitialAd = null;
                    }
                });
        TextView tooltext = findViewById(R.id.toolbartitle);
        tooltext.setText("Scan Result");
        findViewById(R.id.favourite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id="+getPackageName())));
            }
        });
        Intent intent = getIntent();
        String text = intent.getStringExtra(EXTRA_TEXT);
        @SuppressLint("CutPasteId") EditText textView1 = findViewById(R.id.text_display);
        textView1.append(text);
    }

    public void export (View view) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                detail.this, R.style.BottomSheetDialogTheme);
        View bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(
                R.layout.layout_bottom_sheet,(LinearLayout)findViewById(R.id.bottomSheetContainer)
        );
        bottomSheetView.findViewById(R.id.exportpdf).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT> Build.VERSION_CODES.M)
                {
                    if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED)
                    {
                        String[] parmission={Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions(parmission,1000);
                    }
                    else savepdf();
                }
                else savepdf();
                if (mInterstitialAd != null) {
                    mInterstitialAd.show(detail.this);
                }
            }
        });
        bottomSheetView.findViewById(R.id.exporttxt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String mfile=new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis());
                    FileOutputStream fileout=openFileOutput(mfile+".txt", MODE_PRIVATE);
                    OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
                    outputWriter.write(text1.getText().toString());
                    outputWriter.close();
                    Toast.makeText(getBaseContext(), "File saved successfully!",
                            Toast.LENGTH_SHORT).show();
                    if (mInterstitialAd != null) {
                        mInterstitialAd.show(detail.this);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    public void copy (View v) {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("EditText", text1.getText().toString());
        clipboardManager.setPrimaryClip(clipData);
        Toast.makeText(detail.this, "Copied", Toast.LENGTH_SHORT).show();
        if (mInterstitialAd != null) {
            mInterstitialAd.show(detail.this);
        }
    }

    public void share (View view){
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String text = text1.getText().toString();
        sharingIntent.putExtra(Intent.EXTRA_TEXT,text);
        startActivity(Intent.createChooser(sharingIntent, "Share Using"));
    }

    public void ocr (View v) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private  void savepdf()
    {
        Document doc=new Document();
        String mfile=new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis());
        String mfilepath= Environment.getExternalStorageDirectory()+"/"+mfile+".pdf";
        Font smallBold=new Font(Font.FontFamily.TIMES_ROMAN,12,Font.BOLD);
        try{
            PdfWriter.getInstance(doc,new FileOutputStream(mfilepath));
            doc.open();
            String mtext=text1.getText().toString();
            doc.addAuthor("laatech");
            doc.add(new Paragraph(mtext,smallBold));
            doc.close();
            Toast.makeText(this, ""+mfile+".pdf"+" is saved to "+mfilepath, Toast.LENGTH_SHORT).show();
        }
        catch (Exception e)
        {
            Toast.makeText(this,"This is Error msg : " +e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case  1000:
                if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED)
                {
                    savepdf();
                }
                else Toast.makeText(this, "parmission denied..", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}

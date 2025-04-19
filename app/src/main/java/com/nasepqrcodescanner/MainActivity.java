package com.nasepqrcodescanner;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import org.json.JSONException;
import org.json.JSONObject;
import android.Manifest;

import java.util.regex.Pattern;

//implementasi dari onclicklistener
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //View Objects
    private Button buttonScan;
    private TextView textViewNama, textViewKelas, textViewNim;
    //qr code scanner object
    private IntentIntegrator qrScan;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //View objects
        buttonScan = (Button) findViewById(R.id.buttonScan);
        textViewNama = (TextView) findViewById(R.id.textViewNama);
        textViewKelas = (TextView) findViewById(R.id.textViewKelas);
        textViewNim = (TextView) findViewById(R.id.textViewNim);
        //intialisasi scan object
        qrScan = new IntentIntegrator(this);
        //implementasi onclick listener
        buttonScan.setOnClickListener(this);
    }
    //untuk mendapatkan hasil scanning
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode,
                resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Hasil SCAN tidak ada", Toast.LENGTH_LONG).show();
            } else {
                String contents = result.getContents();

                // Jika isi QR adalah URL
                if (Patterns.WEB_URL.matcher(contents).matches()) {
                    Intent visitUrl = new Intent(Intent.ACTION_VIEW, Uri.parse(contents));
                    startActivity(visitUrl);
                }
                // Jika isi QR adalah nomor telepon
                else if (contents.startsWith("tel:")) {
                    String PhoneNum = contents.replace("tel:", "");
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:"+Uri.encode(PhoneNum.trim())));
                    callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(callIntent);
                }
                // Jika isi QR adalah nomor WA, contoh format: wa:628123456789
                else if (contents.startsWith("wa:")) {
                    String phoneNumber = contents.replace("wa:", "");
                    String url = "https://wa.me/" + phoneNumber;
                    Intent whatsappIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(whatsappIntent);
                }
                // Jika isi QR adalah email
                else if (contents.startsWith("MATMSG:")) {
                    // Hapus prefix "MATMSG:" dan trailing ;;
                    String matmsg = contents.substring(7).replaceAll(";;$", "");

                    String to = "", subject = "", body = "";

                    // Split berdasarkan ";"
                    String[] parts = matmsg.split(";");
                    for (String part : parts) {
                        if (part.startsWith("TO:")) {
                            to = part.substring(3).trim();
                        } else if (part.startsWith("SUB:")) {
                            subject = part.substring(4).trim();
                        } else if (part.startsWith("BODY:")) {
                            body = part.substring(5).trim();
                        }
                    }

                    // Debug print untuk lihat hasil parsing (opsional)
                    Log.d("QR_EMAIL", "To: " + to);
                    Log.d("QR_EMAIL", "Subject: " + subject);
                    Log.d("QR_EMAIL", "Body: " + body);

                    // Buat intent email
                    Intent i = new Intent(Intent.ACTION_SENDTO);
                    i.setData(Uri.parse("mailto:" + to));

                    if (!subject.isEmpty()) {
                        i.putExtra(Intent.EXTRA_SUBJECT, subject);
                    } else {
                        i.putExtra(Intent.EXTRA_SUBJECT, "tugas uas");
                    }

                    if (!body.isEmpty()) {
                        i.putExtra(Intent.EXTRA_TEXT, body);
                    } else {
                        i.putExtra(Intent.EXTRA_TEXT, "telah berhasil");
                    }

                    try {
                        startActivity(Intent.createChooser(i, "Send mail..."));
                        finish();
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(MainActivity.this, "Tidak ada aplikasi email terpasang.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
                else if (contents.contains("geo:")) {
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(contents));
                    mapIntent.setPackage("com.go ogle.android.apps.maps"); // Paksa buka dengan Google Maps (jika ada)

                    try {
                        startActivity(mapIntent);
                    } catch (ActivityNotFoundException ex) {
                        // Kalau Google Maps nggak ada, fallback ke browser
                        Intent fallbackIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(contents));
                        startActivity(fallbackIntent);
                    }
                }
                // Jika isi QR adalah JSON
                else {
                    try {
                        JSONObject obj = new JSONObject(contents);
                        textViewNama.setText(obj.getString("nama"));
                        textViewKelas.setText(obj.getString("kelas"));
                        textViewNim.setText(obj.getString("nim"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, contents, Toast.LENGTH_LONG).show();
                    }
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }
    @Override
    public void onClick(View view) {
        //inisialisasi scanning qr code
        qrScan.initiateScan();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Izin diberikan, bisa ulangi pemanggilan intent jika perlu
            } else {
                Toast.makeText(this, "Izin panggilan ditolak", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
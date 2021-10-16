package com.rakin.rajcpsccovid.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.rakin.rajcpsccovid.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

public class InfoActivity extends AppCompatActivity {
    public String CurrentVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        Objects.requireNonNull(getSupportActionBar()).setTitle("About");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        PromptforUpdate();

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    public void opengithubwise(View view) {
        String url = "https://github.com/rakinthegreat/";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    public void opentgwise(View view) {
        String url = "https://t.me/rakinthegreat/";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }
    public String checkupdateVersionID(){
        try {

            URL url = new URL("https://raw.githubusercontent.com/rakinthegreat/BanCOVID-Source/master/lastversion");

            // read text returned by server
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

            String line;
            while ((line = in.readLine()) != null) {
                // Toast.makeText(this, "Online" + line, Toast.LENGTH_SHORT).show();
                return(line);
            }

        }
        catch (MalformedURLException e) {
            System.out.println("Malformed URL: " + e.getMessage());
        }
        catch (IOException e) {
            System.out.println("I/O Error: " + e.getMessage());
        }
        return "";
    }
    public void PromptforUpdate() {
        StrictMode.ThreadPolicy  policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        PackageInfo str = null;
        Context context = this;
        try {
            str = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            final String versionName = String.valueOf(str.versionCode);
            CurrentVersion = versionName;
            // Toast.makeText(context, versionName, Toast.LENGTH_SHORT).show();
            if (checkupdateVersionID().equals("") || checkupdateVersionID() == null){
                Toast.makeText(this, "Could not check for updates!", Toast.LENGTH_SHORT).show();
            }
             else if (!versionName.equals(checkupdateVersionID())){
                UpdateDialog();
                // Do What to do
            }
            else {
                Toast.makeText(this, "This App is Up to Date!", Toast.LENGTH_SHORT).show();
                // Do what to do
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void UpdateDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, R.style.AppTheme3);
        alertDialogBuilder.setTitle("Update Available!!!");
        alertDialogBuilder.setMessage("Your Current Version is " + CurrentVersion + " but the latest version is " + checkupdateVersionID() +". Do you want to download?");
        alertDialogBuilder.setCancelable(true);
        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                String url = updateVersionLink();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });
        alertDialogBuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    public String updateVersionLink(){
        try {

            URL url = new URL("https://raw.githubusercontent.com/rakinthegreat/Updates/main/BanCovidNewLink.txt");

            // read text returned by server
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

            String line;
            while ((line = in.readLine()) != null) {
                // Toast.makeText(this, "Online" + line, Toast.LENGTH_SHORT).show();
                return(line);
            }

        }
        catch (MalformedURLException e) {
            System.out.println("Malformed URL: " + e.getMessage());
        }
        catch (IOException e) {
            System.out.println("I/O Error: " + e.getMessage());
        }
        return "";
    }
}

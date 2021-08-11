package com.rakin.rajcpsccovid;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.rakin.rajcpsccovid.activities.InfoActivity;
import com.rakin.rajcpsccovid.activities.WorldDataActivity;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {
    public static int confirmation = 0;
    public static boolean isRefreshed;
    String confirmed;
    String active;
    String date;
    String recovered;
    String deaths;
    String newConfirmed;
    String newDeaths;
    String newRecovered;
    String totalTests;
    String oldTests;
    BigInteger testsInt;
    String totalTestsCopy;
    String version;
    String updateVersion;
    String updateUrl;
    String updateChanges;
    TextView textView_confirmed, textView_confirmed_new, textView_active, textView_active_new, textView_recovered, textView_recovered_new, textView_death, textView_death_new, textView_tests, textView_date, textView_tests_new, textview_time;
    ProgressDialog progressDialog;
    SwipeRefreshLayout swipeRefreshLayout;
    private long backPressTime;
    private Toast backToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        version = String.valueOf(R.string.version);
        SharedPreferences sharedPreferences = getSharedPreferences("prefs", MODE_PRIVATE);

        textView_confirmed = findViewById(R.id.confirmed_textView);
        textView_confirmed_new = findViewById(R.id.confirmed_new_textView);
        textView_active = findViewById(R.id.active_textView);
        textView_active_new = findViewById(R.id.active_new_textView);
        textView_recovered = findViewById(R.id.recovered_textView);
        textView_recovered_new = findViewById(R.id.recovered_new_textView);
        textView_death = findViewById(R.id.death_textView);
        textView_death_new = findViewById(R.id.death_new_textView);
        textView_tests = findViewById(R.id.tests_textView);
        textView_tests_new = findViewById(R.id.tests_new_textView);
        swipeRefreshLayout = findViewById(R.id.main_refreshLayout);

        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        Objects.requireNonNull(getSupportActionBar()).setTitle("COVID-19 Stats (Bangladesh)");

        fetchUpdate();
        showProgressDialog();
        fetchData();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchData();
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(MainActivity.this, "Data refreshed!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void showChanges() {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.info) {
            Intent intent = new Intent(this, InfoActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        if (backPressTime + 800 > System.currentTimeMillis()) {
            backToast.cancel();
            super.onBackPressed();
            return;
        } else {
            backToast = Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT);
            backToast.show();
        }
        backPressTime = System.currentTimeMillis();
    }

    private void putData() {
        try {
            BigInteger confirmedInt = new BigInteger(confirmed);
            confirmed = NumberFormat.getInstance().format(confirmedInt);
            textView_confirmed.setText(confirmed);

            int newConfirmedInt = Integer.parseInt(newConfirmed);
            newConfirmed = NumberFormat.getInstance().format(newConfirmedInt);
            textView_confirmed_new.setText("+" + newConfirmed);

            BigInteger activeInt = new BigInteger(active);
            active = NumberFormat.getInstance().format(activeInt);
            textView_active.setText(active);

            BigInteger recoveredInt = new BigInteger(recovered);
            recovered = NumberFormat.getInstance().format(recoveredInt);
            textView_recovered.setText(recovered);

            int recoveredNewInt = Integer.parseInt(newRecovered);
            newRecovered = NumberFormat.getInstance().format(recoveredNewInt);
            textView_recovered_new.setText("+" + newRecovered);

            BigInteger deathsInt = new BigInteger(deaths);
            deaths = NumberFormat.getInstance().format(deathsInt);
            textView_death.setText(deaths);

            int deathsNewInt = Integer.parseInt(newDeaths);
            newDeaths = NumberFormat.getInstance().format(deathsNewInt);
            textView_death_new.setText("+" + newDeaths);

            String dateFormat = formatDate(date, 1);
            textView_date.setText(dateFormat);

            String timeFormat = formatDate(date, 2);
            textview_time.setText(timeFormat);
        } catch (NullPointerException e) {
        }
    }

    public void fetchData() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String apiUrl = "https://disease.sh/v3/covid-19/countries/Bangladesh";
        final PieChart mPieChart = findViewById(R.id.piechart);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, apiUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (isRefreshed) {
                        confirmed = response.getString("cases");
                        active = response.getString("active");
                        recovered = response.getString("recovered");
                        newRecovered = response.getString("todayRecovered");
                        deaths = response.getString("deaths");
                        newDeaths = response.getString("todayDeaths");
                        textView_confirmed.setText(confirmed);
                        totalTests = response.getString("tests");
                        textView_tests.setText(totalTests);
                        newConfirmed = response.getString("todayCases");

                        Runnable progressRunnable = new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.cancel();
                                String totalActiveCopy = active;
                                String totalDeceasedCopy = deaths;
                                String totalRecoveredCopy = recovered;

                                putData();

                                mPieChart.addPieSlice(new PieModel("Active", Integer.parseInt(totalActiveCopy), Color.parseColor("#007afe")));
                                mPieChart.addPieSlice(new PieModel("Recovered", Integer.parseInt(totalRecoveredCopy), Color.parseColor("#08a045")));
                                mPieChart.addPieSlice(new PieModel("Deceased", Integer.parseInt(totalDeceasedCopy), Color.parseColor("#F6404F")));

                                mPieChart.startAnimation();
                            }
                        };
                        Handler pdCanceller = new Handler();
                        pdCanceller.postDelayed(progressRunnable, 0);
                    } else {
                        confirmed = response.getString("cases");
                        active = response.getString("active");
                        recovered = response.getString("recovered");
                        newRecovered = response.getString("todayRecovered");
                        deaths = response.getString("deaths");
                        newDeaths = response.getString("todayDeaths");
                        totalTests = response.getString("tests");
                        textView_confirmed.setText(confirmed);
                        textView_tests.setText(totalTests);
                        newConfirmed = response.getString("todayCases");

                        if (totalTests != "") {
                            Runnable progressRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.cancel();
                                    String totalActiveCopy = active;
                                    String totalDeceasedCopy = deaths;
                                    String totalRecoveredCopy = recovered;

                                    putData();
                                    try {
                                        mPieChart.addPieSlice(new PieModel("Active", Integer.parseInt(totalActiveCopy), Color.parseColor("#007afe")));
                                        mPieChart.addPieSlice(new PieModel("Recovered", Integer.parseInt(totalRecoveredCopy), Color.parseColor("#08a045")));
                                        mPieChart.addPieSlice(new PieModel("Deceased", Integer.parseInt(totalDeceasedCopy), Color.parseColor("#F6404F")));

                                        mPieChart.startAnimation();
                                    } catch (NullPointerException e) {
                                    }
                                }
                            };
                            Handler pdCanceller = new Handler();
                            pdCanceller.postDelayed(progressRunnable, 0);
                            confirmation = 1;
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    public String formatDate(String date, int testCase) {
        Date mDate = null;
        String dateFormat;
        try {
            mDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).parse(date);
            if (testCase == 0) {
                dateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(mDate);
                return dateFormat;
            } else if (testCase == 1) {
                dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.US).format(mDate);
                return dateFormat;
            } else if (testCase == 2) {
                dateFormat = new SimpleDateFormat("hh:mm a", Locale.US).format(mDate);
                return dateFormat;
            } else {
                Log.d("error", "Wrong input! Choose from 0 to 2");
                return "Error";
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return date;
        }
    }

    public void fetchTests() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String apiUrl = "https://disease.sh/v3/covid-19/countries/Bangladesh";
        JsonObjectRequest jsonObjectRequestTests = new JsonObjectRequest(Request.Method.GET, apiUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("tests");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject statewise = jsonArray.getJSONObject(i);
                        totalTests = statewise.getString("tests");
                    }

                    for (int i = 0; i < jsonArray.length() - 1; i++) {
                        JSONObject statewise = jsonArray.getJSONObject(i);
                        oldTests = statewise.getString("tests");
                    }
                    if (totalTests.isEmpty()) {
                        for (int i = 0; i < jsonArray.length() - 1; i++) {
                            JSONObject statewise = jsonArray.getJSONObject(i);
                            totalTests = statewise.getString("tests");
                        }
                        totalTestsCopy = totalTests;
                        testsInt = new BigInteger(totalTests);
                        totalTests = NumberFormat.getInstance().format(testsInt);
                        textView_tests.setText(totalTests);


                        for (int i = 0; i < jsonArray.length() - 2; i++) {
                            JSONObject statewise = jsonArray.getJSONObject(i);
                            oldTests = statewise.getString("tests");
                        }
                        int testsNew = (Integer.parseInt(totalTestsCopy)) - (Integer.parseInt(oldTests));
                        textView_tests_new.setText("[+" + NumberFormat.getInstance().format(testsNew) + "]");

                    } else {
                        totalTestsCopy = totalTests;
                        testsInt = new BigInteger(totalTests);
                        totalTests = NumberFormat.getInstance().format(testsInt);
                        textView_tests.setText(totalTests);

                        if (oldTests.isEmpty()) {
                            for (int i = 0; i < jsonArray.length() - 2; i++) {
                                JSONObject statewise = jsonArray.getJSONObject(i);
                                oldTests = statewise.getString("tests");
                            }
                        }
                        long testsNew = (Integer.parseInt(totalTestsCopy)) - (Integer.parseInt(oldTests));
                        textView_tests_new.setText("+" + NumberFormat.getInstance().format(testsNew));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        requestQueue.add(jsonObjectRequestTests);
    }

    public void showProgressDialog() {
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.setCanceledOnTouchOutside(false);
        Objects.requireNonNull(progressDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        Runnable progressRunnable = new Runnable() {

            @Override
            public void run() {
                if (confirmation != 1) {
                    progressDialog.cancel();
                    Toast.makeText(MainActivity.this, "Your Internet Connection is slow/not available", Toast.LENGTH_SHORT).show();
                }
            }
        };
        Handler pdCanceller = new Handler();
        pdCanceller.postDelayed(progressRunnable, 8000);
    }

    public void openStatewise(View view) {
        String url = "https://rajcpsc.edu.bd";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    public void openCovidwise(View view) {
        String url = "https://corona.gov.bd";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    public void openMoreInfo(View view) {
        Intent intent = new Intent(this, WorldDataActivity.class);
        startActivity(intent);
    }

    public void fetchUpdate() {
    }

}

package com.example.uap_pam;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = "DetailActivity";
    private static final String BASE_URL = "https://uappam.kuncipintu.my.id";
    private static final int REQUEST_UPDATE_PLANT = 2;

    private ImageView ivPlantImage;
    private TextView tvPlantName;
    private TextView tvPlantPrice;
    private TextView tvPlantDescription;
    private Button btnUpdate;

    private RequestQueue requestQueue;

    // Data dari intent
    private String plantName;
    private String plantPrice;
    private String plantDescription;
    private int plantImage;
    private int plantPosition;
    private int plantId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        initViews();
        getIntentData();
        setupClickListeners();

        // Initialize Volley request queue
        requestQueue = Volley.newRequestQueue(this);

        // Load data from API if plant has ID, otherwise use intent data
        if (plantName != null && !plantName.isEmpty()) {
            loadPlantDetailFromAPI(plantName);
        } else {
            displayPlantData();
        }
    }

    private void initViews() {
        ivPlantImage = findViewById(R.id.ivPlantImage);
        tvPlantName = findViewById(R.id.tvPlantName);
        tvPlantPrice = findViewById(R.id.tvPlantPrice);
        tvPlantDescription = findViewById(R.id.tvPlantDescription);
        btnUpdate = findViewById(R.id.btnUpdate);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        plantName = intent.getStringExtra("plant_name");
        plantPrice = intent.getStringExtra("plant_price");
        plantDescription = intent.getStringExtra("plant_description");
        plantImage = intent.getIntExtra("plant_image", R.drawable.daun_hijau);
        plantPosition = intent.getIntExtra("plant_position", -1);
        plantId = intent.getIntExtra("plant_id", -1);

        // Set default description if empty
        if (plantDescription == null || plantDescription.isEmpty()) {
            plantDescription = "Deskripsi tanaman tidak tersedia";
        }
    }

    private void setupClickListeners() {
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUpdatePage();
            }
        });
    }

    private void openUpdatePage() {
        Intent intent = new Intent(DetailActivity.this, AddItemActivity.class);

        // Kirim data untuk update mode
        intent.putExtra("mode", "update");
        intent.putExtra("plant_id", plantId);
        intent.putExtra("plant_name", plantName);
        intent.putExtra("plant_price", plantPrice);
        intent.putExtra("plant_description", plantDescription);
        intent.putExtra("plant_position", plantPosition);

        startActivityForResult(intent, REQUEST_UPDATE_PLANT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_UPDATE_PLANT && resultCode == RESULT_OK && data != null) {
            String updatedName = data.getStringExtra("plant_name");
            String updatedPrice = data.getStringExtra("plant_price");
            String updatedDescription = data.getStringExtra("plant_description");
            int position = data.getIntExtra("plant_position", -1);

            // Update variabel lokal
            plantName = updatedName;
            plantPrice = updatedPrice;
            plantDescription = updatedDescription;
            plantPosition = position;

            // Update tampilan
            displayPlantData();

            Toast.makeText(this, "Data tanaman berhasil diperbarui!", Toast.LENGTH_SHORT).show();

            // Kirim data ke MainActivity supaya list bisa diupdate
            Intent resultIntent = new Intent();
            resultIntent.putExtra("plant_name", updatedName);
            resultIntent.putExtra("plant_price", updatedPrice);
            resultIntent.putExtra("plant_description", updatedDescription);
            resultIntent.putExtra("plant_position", position);
            setResult(RESULT_OK, resultIntent);
        }
    }


    private void loadPlantDetailFromAPI(String plantNameParam) {
        try {
            String encodedName = java.net.URLEncoder.encode(plantNameParam, "UTF-8");
            String url = BASE_URL + "/plant/" + encodedName;

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                // parsing response JSON
                                JSONObject data = response.getJSONObject("data");
                                String apiName = data.getString("plant_name");
                                String apiPrice = data.getString("price");
                                String apiDescription = data.optString("description", "Deskripsi tidak tersedia");

                                // update variabel kelas, tidak deklarasi ulang
                                DetailActivity.this.plantName = apiName;
                                DetailActivity.this.plantPrice = apiPrice;
                                DetailActivity.this.plantDescription = apiDescription;

                                displayPlantData();

                            } catch (JSONException e) {
                                Log.e(TAG, "JSON parsing error: " + e.getMessage());
                                Toast.makeText(DetailActivity.this,
                                        "Error parsing plant detail data",
                                        Toast.LENGTH_SHORT).show();
                                displayPlantData();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "Plant detail API Error: " + error.getMessage());
                            Toast.makeText(DetailActivity.this,
                                    "Gagal memuat detail dari server. Menampilkan data lokal.",
                                    Toast.LENGTH_SHORT).show();
                            displayPlantData();
                        }
                    }
            );

            requestQueue.add(jsonObjectRequest);

        } catch (Exception e) {
            Log.e(TAG, "URL Encoding error: " + e.getMessage());
            Toast.makeText(this, "Terjadi kesalahan saat memuat data", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayPlantData() {
        // Set plant image
        ivPlantImage.setImageResource(plantImage);

        // Set plant name
        tvPlantName.setText(plantName != null ? plantName : "Nama Tanaman");

        // Set plant price
        tvPlantPrice.setText(plantPrice != null ? plantPrice : "Harga tidak tersedia");

        // Set plant description
        tvPlantDescription.setText(plantDescription != null && !plantDescription.isEmpty()
                ? plantDescription : "Deskripsi tanaman tidak tersedia");

        Log.d(TAG, "Displaying plant data: " + plantName + ", " + plantPrice + ", " + plantDescription);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel any pending requests
        if (requestQueue != null) {
            requestQueue.cancelAll(TAG);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
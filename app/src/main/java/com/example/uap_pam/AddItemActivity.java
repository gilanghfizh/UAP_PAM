package com.example.uap_pam;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class AddItemActivity extends AppCompatActivity {

    private static final String TAG = "AddItemActivity";
    private static final String BASE_URL = "https://uappam.kuncipintu.my.id";

    private EditText etNamaTanaman;
    private EditText etHarga;
    private EditText etDeskripsi;
    private Button btnTambah;
    private TextView tvTitle;
    private RequestQueue requestQueue;

    // Update mode variables
    private boolean isUpdateMode = false;
    private int plantId = -1;
    private int plantPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        initViews();
        checkMode();
        setupClickListeners();

        // Initialize Volley request queue
        requestQueue = Volley.newRequestQueue(this);
    }

    private void initViews() {
        etNamaTanaman = findViewById(R.id.etNamaTanaman);
        etHarga = findViewById(R.id.etHarga);
        etDeskripsi = findViewById(R.id.etDeskripsi);
        btnTambah = findViewById(R.id.btnTambah);

        // Tambahkan TextView untuk title jika belum ada di layout
        // tvTitle = findViewById(R.id.tvTitle); // Uncomment jika ada di layout
    }

    private void checkMode() {
        Intent intent = getIntent();
        String mode = intent.getStringExtra("mode");

        if ("update".equals(mode)) {
            isUpdateMode = true;
            plantId = intent.getIntExtra("plant_id", -1);
            plantPosition = intent.getIntExtra("plant_position", -1);

            // Set title dan button text untuk update mode
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Update Tanaman");
            }
            btnTambah.setText("Simpan");

            // Populate fields with existing data
            String existingName = intent.getStringExtra("plant_name");
            String existingPrice = intent.getStringExtra("plant_price");
            String existingDescription = intent.getStringExtra("plant_description");

            if (existingName != null) etNamaTanaman.setText(existingName);
            if (existingPrice != null) {
                // Remove "Rp " prefix for editing
                String priceWithoutRp = existingPrice.replace("Rp ", "").trim();
                etHarga.setText(priceWithoutRp);
            }
            if (existingDescription != null) etDeskripsi.setText(existingDescription);

        } else {
            isUpdateMode = false;
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Tambah Tanaman");
            }
            btnTambah.setText("Tambah");
        }
    }

    private void setupClickListeners() {
        btnTambah.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUpdateMode) {
                    updatePlantToAPI();
                } else {
                    addPlantToAPI();
                }
            }
        });
    }

    private void addPlantToAPI() {
        String namaTanaman = etNamaTanaman.getText().toString().trim();
        String harga = etHarga.getText().toString().trim();
        String deskripsi = etDeskripsi.getText().toString().trim();

        // Validasi input
        if (!validateInput(namaTanaman, harga, deskripsi)) {
            return;
        }

        // Format harga jika tidak dimulai dengan "Rp"
        if (!harga.startsWith("Rp")) {
            harga = "Rp " + harga;
        }

        // Disable button to prevent multiple submissions
        btnTambah.setEnabled(false);
        btnTambah.setText("Menambahkan...");

        // Send data to API
        sendPlantDataToAPI(namaTanaman, harga, deskripsi, Request.Method.POST, BASE_URL + "/plant/new");
    }

    private void updatePlantToAPI() {
        String namaTanaman = etNamaTanaman.getText().toString().trim();
        String harga = etHarga.getText().toString().trim();
        String deskripsi = etDeskripsi.getText().toString().trim();

        if (!validateInput(namaTanaman, harga, deskripsi)) {
            return;
        }

        if (!harga.startsWith("Rp")) {
            harga = "Rp " + harga;
        }

        btnTambah.setEnabled(false);
        btnTambah.setText("Menyimpan...");

        // Gunakan nama tanaman lama dari intent
        String oldName = getIntent().getStringExtra("plant_name");
        if (oldName == null || oldName.isEmpty()) {
            Toast.makeText(this, "Nama tanaman lama tidak ditemukan.", Toast.LENGTH_SHORT).show();
            resetButton();
            return;
        }

        String updateUrl = BASE_URL + "/plant/" + Uri.encode(oldName);
        sendPlantDataToAPI(namaTanaman, harga, deskripsi, Request.Method.PUT, updateUrl);
    }


    private boolean validateInput(String nama, String harga, String deskripsi) {
        if (nama.isEmpty()) {
            etNamaTanaman.setError("Nama tanaman tidak boleh kosong");
            etNamaTanaman.requestFocus();
            return false;
        }

        if (harga.isEmpty()) {
            etHarga.setError("Harga tidak boleh kosong");
            etHarga.requestFocus();
            return false;
        }

        if (deskripsi.isEmpty()) {
            etDeskripsi.setError("Deskripsi tidak boleh kosong");
            etDeskripsi.requestFocus();
            return false;
        }

        return true;
    }

    private void sendPlantDataToAPI(String name, String price, String description, int method, String url) {
        JSONObject plantData = new JSONObject();
        try {
            // Ganti key JSON sesuai API
            plantData.put("plant_name", name);
            plantData.put("price", price);
            plantData.put("description", description);

        } catch (JSONException e) {
            Log.e(TAG, "JSON creation error: " + e.getMessage());
            resetButton();
            Toast.makeText(this, "Error creating data", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                method, url, plantData,
                response -> {
                    Log.d(TAG, "API Response: " + response.toString());
                    try {
                        JSONObject data = response.getJSONObject("data");

                        String responseName = data.getString("plant_name");
                        String responsePrice = data.getString("price");
                        String responseDescription = data.optString("description", "");
                        int id = data.optInt("id", plantId);

                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("plant_name", responseName);
                        resultIntent.putExtra("plant_price", responsePrice);
                        resultIntent.putExtra("plant_description", responseDescription);
                        resultIntent.putExtra("plant_id", id);
                        resultIntent.putExtra("plant_image", R.drawable.daun_hijau);

                        if (isUpdateMode) {
                            resultIntent.putExtra("plant_position", plantPosition);
                        }

                        setResult(RESULT_OK, resultIntent);
                        Toast.makeText(AddItemActivity.this, "Tanaman berhasil diperbarui!", Toast.LENGTH_SHORT).show();
                        finish();

                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error: " + e.getMessage());
                        sendFallbackResult(name, price, description);
                    }
                },
                error -> {
                    Log.e(TAG, "API Error: " + error.getMessage());
                    String errorMessage = isUpdateMode ? "Gagal memperbarui data di server" : "Gagal menambahkan ke server";
                    if (error.networkResponse != null) {
                        errorMessage += " (Kode: " + error.networkResponse.statusCode + ")";
                    }
                    Toast.makeText(AddItemActivity.this, errorMessage + ". Data akan disimpan secara lokal.", Toast.LENGTH_LONG).show();
                    sendFallbackResult(name, price, description);
                }
        );

        requestQueue.add(request);
    }

    private void sendFallbackResult(String name, String price, String description) {
        // Send data back to previous activity for local storage
        Intent resultIntent = new Intent();
        resultIntent.putExtra("plant_name", name);
        resultIntent.putExtra("plant_price", price);
        resultIntent.putExtra("plant_description", description);
        resultIntent.putExtra("plant_id", isUpdateMode ? plantId : -1);
        resultIntent.putExtra("plant_image", R.drawable.daun_hijau);

        if (isUpdateMode) {
            resultIntent.putExtra("plant_position", plantPosition);
        }

        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void resetButton() {
        btnTambah.setEnabled(true);
        btnTambah.setText(isUpdateMode ? "Simpan" : "Tambah");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel any pending requests
        if (requestQueue != null) {
            requestQueue.cancelAll(TAG);
        }
    }
}
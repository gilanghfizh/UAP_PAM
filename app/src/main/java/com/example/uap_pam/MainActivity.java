package com.example.uap_pam;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import com.example.uap_pam.adapter.PlantAdapter;
import com.example.uap_pam.model.Plant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ADD_PLANT = 1;
    private static final int REQUEST_DETAIL_PLANT = 2;
    private static final String TAG = "MainActivity";
    private static final String BASE_URL = "https://uappam.kuncipintu.my.id";

    private RecyclerView recyclerViewPlants;
    private PlantAdapter plantAdapter;
    private List<Plant> plantList;
    private Button btnTambahList;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupRecyclerView();
        setupClickListeners();

        // Initialize Volley request queue
        requestQueue = Volley.newRequestQueue(this);

        // Load data from API instead of dummy data
        loadPlantsFromAPI();
    }

    private void initViews() {
        recyclerViewPlants = findViewById(R.id.recyclerViewPlants);
        btnTambahList = findViewById(R.id.btnTambahList);
    }

    private void setupRecyclerView() {
        plantList = new ArrayList<>();
        plantAdapter = new PlantAdapter(plantList, new PlantAdapter.OnPlantClickListener() {
            @Override
            public void onDeleteClick(int position) {
                deletePlantFromAPI(position);
            }

            @Override
            public void onDetailClick(int position) {
                openDetailPage(position);
            }
        });

        recyclerViewPlants.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPlants.setAdapter(plantAdapter);
    }

    private void setupClickListeners() {
        btnTambahList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddItemActivity.class);
                startActivityForResult(intent, REQUEST_ADD_PLANT);
            }
        });
    }

    private void loadPlantsFromAPI() {
        String url = BASE_URL + "/plant/all";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray dataArray = response.getJSONArray("data");

                            plantList.clear();

                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject plantObject = dataArray.getJSONObject(i);

                                String name = plantObject.getString("plant_name");
                                String price = plantObject.getString("price");
                                String description = plantObject.getString("description");
                                int id = plantObject.getInt("id");

                                Plant plant = new Plant(name, price, R.drawable.daun_hijau);
                                plant.setId(id);
                                plant.setDescription(description);

                                plantList.add(plant);
                            }

                            plantAdapter.notifyDataSetChanged();

                        } catch (JSONException e) {
                            Log.e(TAG, "JSON Parsing Error: " + e.getMessage());
                            Toast.makeText(MainActivity.this,
                                    "Data dari server tidak bisa diproses", Toast.LENGTH_SHORT).show();
                            loadDummyData();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "API Error: " + error.toString());
                        Toast.makeText(MainActivity.this,
                                "Gagal mengambil data dari server", Toast.LENGTH_SHORT).show();
                        loadDummyData();
                    }
                }
        );
        requestQueue.add(jsonObjectRequest);
    }

    private void deletePlantFromAPI(int position) {
        if (position >= 0 && position < plantList.size()) {
            Plant plant = plantList.get(position);
            String plantName = plant.getName();

            if (plantName != null && !plantName.isEmpty()) {
                try {
                    String encodedName = URLEncoder.encode(plantName, "UTF-8");
                    String url = BASE_URL + "/plant/" + encodedName;

                    JsonObjectRequest deleteRequest = new JsonObjectRequest(
                            Request.Method.DELETE, url, null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    plantList.remove(position);
                                    plantAdapter.notifyItemRemoved(position);
                                    plantAdapter.notifyItemRangeChanged(position, plantList.size());

                                    Toast.makeText(MainActivity.this,
                                            "Tanaman \"" + plantName + "\" berhasil dihapus!",
                                            Toast.LENGTH_SHORT).show();
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.e(TAG, "Delete API Error: " + error.getMessage());
                                    Toast.makeText(MainActivity.this,
                                            "Gagal menghapus tanaman dari server",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                    );

                    requestQueue.add(deleteRequest);

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this,
                            "Error encoding nama tanaman",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ADD_PLANT && resultCode == RESULT_OK && data != null) {
            String name = data.getStringExtra("plant_name");
            String price = data.getStringExtra("plant_price");
            String description = data.getStringExtra("plant_description");
            int id = data.getIntExtra("plant_id", -1);
            int position = data.getIntExtra("plant_position", -1);  // -1 jika tambah baru
            int imageRes = data.getIntExtra("plant_image", R.drawable.daun_hijau);

            Plant updatedPlant = new Plant(id, name, price, description, imageRes);

            if (position >= 0) {
                // Update data tanaman di posisi tertentu
                plantAdapter.updatePlant(position, updatedPlant);
            } else {
                // Tambah tanaman baru
                plantAdapter.addPlant(updatedPlant);
            }
        }
    }



    private void handleUpdateResult(Intent data) {
        // Check if this is an update operation
        boolean isUpdated = data.getBooleanExtra("plant_updated", false);

        if (isUpdated) {
            int plantId = data.getIntExtra("plant_id", -1);
            String updatedName = data.getStringExtra("plant_name");
            String updatedPrice = data.getStringExtra("plant_price");
            String updatedDescription = data.getStringExtra("plant_description");
            int updatedImageResource = data.getIntExtra("plant_image", R.drawable.daun_hijau);

            // Find and update the plant in the list
            updatePlantInList(plantId, updatedName, updatedPrice, updatedDescription, updatedImageResource);

            // Alternatively, just refresh the entire list from API
            // loadPlantsFromAPI();
        }
    }

    private void updatePlantInList(int plantId, String name, String price, String description, int imageResource) {
        for (int i = 0; i < plantList.size(); i++) {
            Plant plant = plantList.get(i);

            // Find plant by ID (for API plants) or by name (for local plants)
            if ((plantId > 0 && plant.getId() == plantId) ||
                    (plantId <= 0 && plant.getName().equals(name))) {

                // Update plant data
                plant.setName(name);
                plant.setPrice(price);
                plant.setDescription(description);
                plant.setImageResource(imageResource);

                // Notify adapter about the change
                plantAdapter.notifyItemChanged(i);

                Log.d(TAG, "Updated plant at position " + i + ": " + name);
                break;
            }
        }
    }

    private void addPlantToAPI(String name, String price, String description, int imageResource) {
        String url = BASE_URL + "/plant/new";

        JSONObject plantData = new JSONObject();
        try {
            plantData.put("name", name);
            plantData.put("price", price);
            plantData.put("description", description);

        } catch (JSONException e) {
            Log.e(TAG, "JSON creation error: " + e.getMessage());
            return;
        }

        JsonObjectRequest postRequest = new JsonObjectRequest(
                Request.Method.POST, url, plantData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Get the created plant data from response
                            int id = response.getInt("id");
                            String responseName = response.getString("name");
                            String responsePrice = response.getString("price");
                            String responseDescription = response.optString("description", "");

                            // Create Plant object and add to list
                            Plant newPlant = new Plant(responseName, responsePrice, imageResource);
                            newPlant.setId(id);
                            newPlant.setDescription(responseDescription);

                            plantList.add(newPlant);
                            plantAdapter.notifyItemInserted(plantList.size() - 1);

                            Toast.makeText(MainActivity.this,
                                    "Tanaman \"" + responseName + "\" berhasil ditambahkan!",
                                    Toast.LENGTH_SHORT).show();

                        } catch (JSONException e) {
                            Log.e(TAG, "JSON parsing error in add response: " + e.getMessage());
                            // Add locally as fallback
                            addPlantLocally(name, price, description, imageResource);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Add API Error: " + error.getMessage());
                        Toast.makeText(MainActivity.this,
                                "Gagal menambahkan ke server. Menambahkan secara lokal.",
                                Toast.LENGTH_SHORT).show();

                        // Add locally as fallback
                        addPlantLocally(name, price, description, imageResource);
                    }
                }
        );

        requestQueue.add(postRequest);
    }

    private void addPlantLocally(String name, String price, String description, int imageResource) {
        Plant newPlant = new Plant(name, price, imageResource);
        newPlant.setDescription(description);

        plantList.add(newPlant);
        plantAdapter.notifyItemInserted(plantList.size() - 1);

        Toast.makeText(this,
                "Tanaman \"" + name + "\" berhasil ditambahkan secara lokal!",
                Toast.LENGTH_SHORT).show();
    }

    private void loadDummyData() {
        plantList.clear();
        plantList.add(new Plant("Monstera Deliciosa", "Rp 250.000", R.drawable.daun_hijau));
        plantList.add(new Plant("Sansevieria", "Rp 150.000", R.drawable.daun_hijau));
        plantList.add(new Plant("Pothos Golden", "Rp 75.000", R.drawable.daun_hijau));
        plantList.add(new Plant("Fiddle Leaf Fig", "Rp 1.200.000", R.drawable.daun_hijau));

        plantAdapter.notifyDataSetChanged();
    }

    private void openDetailPage(int position) {
        if (position >= 0 && position < plantList.size()) {
            Plant selectedPlant = plantList.get(position);

            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
            intent.putExtra("plant_name", selectedPlant.getName());
            intent.putExtra("plant_price", selectedPlant.getPrice());
            intent.putExtra("plant_image", selectedPlant.getImageResource());
            intent.putExtra("plant_position", position);
            intent.putExtra("plant_description", selectedPlant.getDescription());
            intent.putExtra("plant_id", selectedPlant.getId());

            startActivityForResult(intent, REQUEST_DETAIL_PLANT);

            Toast.makeText(this,
                    "Detail untuk: " + selectedPlant.getName(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        loadPlantsFromAPI();
    }
}
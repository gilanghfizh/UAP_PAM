package com.example.uap_pam.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uap_pam.R;
import com.example.uap_pam.model.Plant;

import java.util.List;

public class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.PlantViewHolder> {

    private List<Plant> plantList;
    private OnPlantClickListener listener;

    public interface OnPlantClickListener {
        void onDeleteClick(int position);
        void onDetailClick(int position);
    }

    public PlantAdapter(List<Plant> plantList, OnPlantClickListener listener) {
        this.plantList = plantList;
        this.listener = listener;
    }
    public void updatePlant(int position, Plant updatedPlant) {
        plantList.set(position, updatedPlant);
        notifyItemChanged(position);
    }

    public void addPlant(Plant newPlant) {
        plantList.add(newPlant);
        notifyItemInserted(plantList.size() - 1);
    }


    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plant, parent, false);
        return new PlantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        Plant plant = plantList.get(position);

        holder.tvPlantName.setText(plant.getName());
        holder.tvPlantPrice.setText(plant.getPrice());
        holder.ivPlantImage.setImageResource(plant.getImageResource());

        // Set click listeners
        holder.btnHapus.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(holder.getAdapterPosition());
            }
        });

        holder.btnDetail.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDetailClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return plantList.size();
    }

    public static class PlantViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPlantImage;
        TextView tvPlantName;
        TextView tvPlantPrice;
        Button btnHapus;
        Button btnDetail;

        public PlantViewHolder(@NonNull View itemView) {
            super(itemView);

            ivPlantImage = itemView.findViewById(R.id.ivPlantImage);
            tvPlantName = itemView.findViewById(R.id.tvPlantName);
            tvPlantPrice = itemView.findViewById(R.id.tvPlantPrice);
            btnHapus = itemView.findViewById(R.id.btnHapus);
            btnDetail = itemView.findViewById(R.id.btnDetail);
        }
    }
}
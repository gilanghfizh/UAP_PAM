package com.example.uap_pam.model;

public class Plant {
    private int id;
    private String name;
    private String price;
    private String description;
    private int imageResource;

    // Constructor untuk data lokal (tanpa ID dari API)
    public Plant(String name, String price, int imageResource) {
        this.id = -1; // -1 menandakan data lokal
        this.name = name;
        this.price = price;
        this.imageResource = imageResource;
        this.description = "";
    }

    // Constructor lengkap untuk data dari API
    public Plant(int id, String name, String price, String description, int imageResource) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageResource = imageResource;
    }

    // Constructor dengan deskripsi
    public Plant(String name, String price, String description, int imageResource) {
        this.id = -1;
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageResource = imageResource;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public int getImageResource() {
        return imageResource;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }

    // Helper method untuk mengecek apakah data dari API atau lokal
    public boolean isFromAPI() {
        return id > 0;
    }

    @Override
    public String toString() {
        return "Plant{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price='" + price + '\'' +
                ", description='" + description + '\'' +
                ", imageResource=" + imageResource +
                '}';
    }
}
package com.example.bytes.model;

import androidx.annotation.NonNull;

public class Contact {
    private String name;
    private String number;

    public enum Variable{
        NAME("name"),
        NUMBER("number");

        private String value;
        Variable(String value){
            this.value = value;
        }

        @NonNull
        @Override
        public String toString() {
            return this.value;
        }
    }

    public Contact(@NonNull String name, @NonNull String number){
        this.name = name;
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }
}

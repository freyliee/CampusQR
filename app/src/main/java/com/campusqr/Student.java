package com.campusqr;

public class Student {
    public String id;
    public String name;
    public String group;
    public String status;

    public Student() {} // нужен для Firestore

    public Student(String id, String name, String group, String status) {
        this.id = id;
        this.name = name;
        this.group = group;
        this.status = status;
    }
}

package com.campusqr;

import java.util.HashMap;
import java.util.Map;

public class StudentRepository {
    public static class Student {
        public final String id;
        public final String name;
        public final String group;
        public final String status;

        public Student(String id, String name, String group, String status) {
            this.id = id;
            this.name = name;
            this.group = group;
            this.status = status;
        }
    }

    private static final Map<String, Student> DB = new HashMap<>();

    static {
        // Mock data for demo
        DB.put("1001", new Student("1001", "Иван Иванов", "ИКБО-01", "Студент"));
        DB.put("1002", new Student("1002", "Мария Петрова", "ИКБО-02", "Студент"));
        DB.put("2001", new Student("2001", "Охрана Кампуса", "—", "Сотрудник"));
    }

    public static Student findById(String id) {
        return DB.get(id);
    }
}
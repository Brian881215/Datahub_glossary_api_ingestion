package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.Data;
import com.example.demo.service.DataService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/data")
@Tag(name= "My own example testing",description = "測試CRUD功能用")
public class DataController {
    @Autowired
    private DataService service;

    // CRUD API endpoints
   

        // Create
        @PostMapping("/")
        public Data createData(@RequestBody Data data) {
            return service.createData(data);
        }

        // Read
        @GetMapping("/{id}")
        public Data getDataById(@PathVariable Long id) {
            return service.getDataById(id);
        }

        // Update
        @PutMapping("/{id}")
        //改的那一筆會受到pathvariable的id影響，如果requestBody裡面有設id為新的話不會受到影響
        public Data updateData(@PathVariable Long id, @RequestBody Data data) {
            return service.updateData(id, data);
        }

        // Delete
        @DeleteMapping("/{id}")
        public void deleteData(@PathVariable Long id) {
            service.deleteData(id);
        }

        // List all data
        @GetMapping("/")
        public List<Data> getAllData() {
            return service.getAllData();
        }
}



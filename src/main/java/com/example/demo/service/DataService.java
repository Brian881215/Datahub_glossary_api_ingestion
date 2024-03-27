package com.example.demo.service;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Data;
import com.example.demo.repository.DataRepository;

@Service
public class DataService {

    @Autowired
    private DataRepository repository;

    // Create
    public Data createData(Data data) {
        return repository.save(data);
    }

    // Read by ID
    public Data getDataById(Long id) {
        return repository.findById(id).orElse(null);
    }

    // Update
    public Data updateData(Long id, Data newData) {
        return repository.findById(id)
                .map(data -> {
                    data.setData(newData.getData());
                    // set other fields...
                    return repository.save(data);
                })
                .orElseGet(() -> {
                    newData.setId(id);
                    return repository.save(newData);
                });
    }

    // Delete
    public void deleteData(Long id) {
        repository.deleteById(id);
    }

    // List all data
    public List<Data> getAllData() {
        return repository.findAll();
    }
}

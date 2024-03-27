package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.entity.Data;

public interface DataRepository extends JpaRepository<Data, Long> {
}


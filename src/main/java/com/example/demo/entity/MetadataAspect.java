package com.example.demo.entity;

import java.time.LocalDateTime;

import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "metadata_aspect_v2")
@IdClass(MetadataAspectId.class) // 用於複合主鍵
@Getter
@Setter
public class MetadataAspect {

    @Id
    @Column(name = "urn", nullable = false, length = 500)
    private String urn;

    @Id
    @Column(name = "aspect", nullable = false, length = 200)
    private String aspect;

    @Id
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "metadata", nullable = false, columnDefinition = "LONGTEXT")
    private String metadata;

    @Column(name = "systemmetadata", columnDefinition = "LONGTEXT")
    private String systemMetadata;

    @Column(name = "createdon", nullable = false)
    private LocalDateTime createdOn;

    @Column(name = "createdby", nullable = false, length = 256)
    private String createdBy;

    @Column(name = "createdfor", length = 256)
    private String createdFor;

    // Getters and setters
}


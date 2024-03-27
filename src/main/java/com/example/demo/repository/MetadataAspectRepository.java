package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.MetadataAspect;

public interface MetadataAspectRepository extends JpaRepository<MetadataAspect, Long> {
	
	//這裡各欄位內的值大小寫真的要跟database的一樣不然會找不到回傳null(e.g. glossaryTermInfo 不可以傳入GlossaryTermInfo)
    @Query("SELECT m FROM MetadataAspect m WHERE m.metadata LIKE %:keyword% AND m.aspect = :aspect AND m.version = :version")
    Optional<MetadataAspect> findByMetadataContainingAndAspectAndVersion(@Param("keyword") String keyword, @Param("aspect") String aspect, @Param("version") Long version); 
    
    @Query("SELECT m.metadata FROM MetadataAspect m WHERE m.urn = :urn AND m.aspect = :aspect AND m.version = :version")
    String findMetadataByUrnAndAspectAndVersion(@Param("urn") String urn, @Param("aspect") String aspect, @Param("version") Long version);

    @Query("SELECT MAX(m.version) FROM MetadataAspect m WHERE m.urn = :urn AND m.aspect = :aspect")
    Long findMaxVersionByUrnAndAspect(@Param("urn") String urn, @Param("aspect") String aspect);
    
    @Query("SELECT m FROM MetadataAspect m WHERE m.urn = :urn AND m.aspect = :aspect AND m.version = :version")
    MetadataAspect findByUrnAndAspectAndVersion(@Param("urn") String urn, @Param("aspect") String aspect, @Param("version") Long version);

}

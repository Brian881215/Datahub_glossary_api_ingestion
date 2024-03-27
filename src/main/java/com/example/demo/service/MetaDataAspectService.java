package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.MetadataAspect;
import com.example.demo.repository.MetadataAspectRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.Setter;

@Service
@Transactional
public class MetaDataAspectService {
	@Autowired MetadataAspectRepository metadataAspectRepository;
	
	@PersistenceContext
	private EntityManager entityManager;
	
	public String getTermUrn(String termName, String aspect,Long version) {
		MetadataAspect results = metadataAspectRepository.findByMetadataContainingAndAspectAndVersion(termName, aspect, version).orElse(null);
        String urnResult = results.getUrn();
        return urnResult;
	}
	public String getTermLinkMetadata(String urn, String aspect, Long version) {
		String results = metadataAspectRepository.findMetadataByUrnAndAspectAndVersion(urn, aspect, version);
		System.out.println("linkMetadata org result:"+results);
		return results;
	}
	@Transactional
	public void updateTermVersionToMax(String urn, String aspect) {
	    Long maxVersion = metadataAspectRepository.findMaxVersionByUrnAndAspect(urn, aspect);
	    String sql = "UPDATE metadata_aspect_v2 m SET version = :newVersion WHERE m.urn = :urn AND m.aspect = :aspect AND m.version = 0";
	    Query query = (Query) entityManager.createNativeQuery(sql);
	    query.setParameter("urn", urn);
	    query.setParameter("aspect", aspect);
	    query.setParameter("newVersion", maxVersion + 1);
	    query.executeUpdate();
//不可以用這個方法，因為你這樣會改到複合主鍵的其中一個key值，對於spring boot hibernate是不行的，所以我們只能採用上面raw sql的方式去做，但要注意寫進去修改後就算後面有錯誤也是會改掉，所以記得到mysql裡面看你操作的那筆資料變動為何！
//	    if (maxVersion != null) {
//	        MetadataAspect aspectToUpdate = metadataAspectRepository.findByUrnAndAspectAndVersion(urn, aspect, 0L);
//	        if (aspectToUpdate != null) {
//	            aspectToUpdate.setVersion(maxVersion + 1);
//	            metadataAspectRepository.save(aspectToUpdate);
//	        }
//	    }
	}
	
	public String processLinkJson(String jsonStr, String description, String url) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Container container = mapper.readValue(jsonStr, Container.class);

        // Access elements
        List<Element> elements = container.getElements();
//        for (Element element : elements) {
//            System.out.println("Description: " + element.getDescription());
//            System.out.println("URL: " + element.getUrl());
//            // You can access other properties similarly
//        }
        Element element = new Element();
        long currentTimeMillis = System.currentTimeMillis();
        element.getCreateStamp().setTime(currentTimeMillis);
        element.setDescription(description);  
        element.setUrl(url);
        
        elements.add(element);
        
        String updatedJsonStr = mapper.writeValueAsString(container);
        System.out.println("updatedJsonStr:"+updatedJsonStr);
        return updatedJsonStr;
    }
	
	public void insertMetadataAspect(String urn,String metadata) {
	    MetadataAspect aspect = new MetadataAspect();
	    aspect.setUrn(urn);
	    aspect.setAspect("institutionalMemory");
	    aspect.setVersion(0L);
	    aspect.setMetadata(metadata);  // JSON 字串
	    aspect.setSystemMetadata("{\"properties\":{\"appSource\":\"ui\"}}");
	    aspect.setCreatedOn(LocalDateTime.now());
	    aspect.setCreatedBy("urn:li:corpuser:datahub");
	    aspect.setCreatedFor(null);
	    metadataAspectRepository.save(aspect);
	}
		
	@Getter
	@Setter
    public static class Container {
        private List<Element> elements;
        // getters and setters
    }

	@Getter
	@Setter
    public static class Element {
        private CreateStamp createStamp = new CreateStamp();
        private String description;
        private String url;
        // getters and setters
    }

	@Getter
	@Setter
    public static class CreateStamp {
        private String actor="urn:li:corpuser:datahub";
        private Long time;
        // getters and setters
    }
}

package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.example.demo.service.SwaggerAPIService;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;

@SpringBootApplication
@OpenAPIDefinition(
		info = @Info(
				title="Datahub swagger API connection",
				version="1.0.0",
				description = "This is for our Data Goverence Team to customize the term properties by using datahub api without adding it mannually.",
				termsOfService = "runcodenow",
				contact = @Contact(
						name="I-BRIAN.CN.HUANG",
						email="brianhuang881215@gmail.com"
						),
				license = @License(
						name = "license",
						url = "runcodenow"
						)
				)
		
		)
public class DatahubApplication {
	
	public static void main(String[] args) {
		
		ConfigurableApplicationContext context = SpringApplication.run(DatahubApplication.class, args);
	    SwaggerAPIService service = context.getBean(SwaggerAPIService.class);
        System.out.println("hello my deer");
        //如果用block的話他就會當成一種阻塞式的操作，並且可以返回Mono中的數據
        String result1Get = service.getGlossaryTermbyURN("urn:li:glossaryTerm:c17d3d53-db91-434b-8d63-52060e42beff").block();
        System.out.println("My get glossary term: "+result1Get);
//        String resultPostOwner = service.postGlossaryTermOwner("urn:li:glossaryTerm:c17d3d53-db91-434b-8d63-52060e42beff","technical_OWNER").block();
//        System.out.println("My get glossary term of link: "+resultPostOwner);
//        
//        String[] testContain = {"urn:li:glossaryTerm:c17d3d53-db91-434b-8d63-52060e42beff","urn:li:glossaryTerm:f5dc732b-b24b-4d09-9e1e-5d134d438a63"};
//        String[] testInherit = {"urn:li:glossaryTerm:be3ca612-bd25-47f6-bd6e-7c3f70a004ec"};
//        String resultPostRelated = service.postGlossaryTermRelated("urn:li:glossaryTerm:38bca1e4-8f22-4847-8023-b45e349593ba",testContain,testInherit).block();
//        System.out.println("My get glossary term of related: "+resultPostRelated);
//        String result2Get = service.getGlossaryNodeOfTerm("urn:li:glossaryTerm:38bca1e4-8f22-4847-8023-b45e349593ba").block();
//        System.out.println("My get glossary term: "+result2Get);
//        String resultPost =  service.postGlossaryTermLink("urn:li:glossaryTerm:be3ca612-bd25-47f6-bd6e-7c3f70a004ec","https://datahubproject.io/docs/generated/ingestion/sources/oracle/","my first PostAPI").block();
//        System.out.println("My post glossary term:"+resultPost);
	}
}

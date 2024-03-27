package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.MetaDataAspectService;
import com.example.demo.service.SwaggerAPIService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/metadata-aspects")
@Tag(name= "Datahub Term Update Api",description = "使用datahub的api將我們認為需要批次自動化的功能實現")
public class MetadataAspectController {

    @Autowired
    private MetaDataAspectService metaDataAspectService;
    
    @Autowired
    private SwaggerAPIService swaggerAPIService;
//這隻會有error，當你的glossaryTermName有類似的，造成他回傳不只一筆時，就會有internal server error: e.g. TestingForNoLink , TestingForNoLink2
    @GetMapping("/TermNameUrnByDB")
    @Operation(summary = "取得特定term的URN代碼，這會有db connection", description= "根據datahub複合主鍵(termName,aspect,version)的輸入透過修改datahub mysql資料來取的用戶唯一的URN")
    public ResponseEntity<String> getTermNameUrnByDB(@Parameter(description = "根據你輸入的termName來找到對應term的urn") @RequestParam String termName, @RequestParam String aspect, @RequestParam Long version) {
        return ResponseEntity.ok(metaDataAspectService.getTermUrn(termName,aspect,version));
    }
    
    @PostMapping("/GlossaryLinkByDB")
    @Operation(summary = "加入一個新的glossaryLink在特定的term中，這會有db connection", description= "根據你輸入的termName他就可以對應到你要的那個urn並且輸入你要加入link的標籤與連結，他就會自動幫你加入一筆了")
    public ResponseEntity<String> addNewGlossaryLinkByDB(@RequestParam String termName, @RequestParam String url, @RequestParam String description) throws JsonProcessingException{
    	String myUrn = metaDataAspectService.getTermUrn(termName,"glossaryTermInfo", 0L);
    	System.out.println("myUrn:"+myUrn);
    	String myMetadataOrg = metaDataAspectService.getTermLinkMetadata(myUrn, "institutionalMemory", 0L);
    	metaDataAspectService.updateTermVersionToMax(myUrn, "institutionalMemory");
    	String myMetadataFinal = metaDataAspectService.processLinkJson(myMetadataOrg, description, url);
    	metaDataAspectService.insertMetadataAspect(myUrn, myMetadataFinal);
    	return ResponseEntity.ok("Success");
    }
    
    @GetMapping("/GlossaryTerm/{urn}")
    @Operation(summary = "取得特定glossaryUrn的所有資訊", description= "取得特定urn term的所有相關資訊")
    @ApiResponse(responseCode = "200", description = "Successful retrieval of glossary Term",
    			 content = { @Content(mediaType = "application/json", 
                 examples = @ExampleObject(value = "{\"key\": \"value\", \"key2\": \"value2\"}"),
                 schema = @Schema(implementation = String.class)) })
    @ApiResponse(responseCode = "404", description = "Glossary Term not found")
    public ResponseEntity<String> getGlossaryTermbyURN(@Parameter(description = "特定term的urn") @PathVariable String urn) {
    	String response = swaggerAPIService.getGlossaryTermbyURN(urn).block();
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/GlossaryTermByQueryName")
    @Operation(summary = "取得所有有包含該termName字詞term的所有資訊", description= "取得一個或多個terms的所有相關資訊")
    @ApiResponse(responseCode = "200", description = "Successful retrieval of glossary Term",
    			 content = { @Content(mediaType = "application/json", 
                 examples = @ExampleObject(value = "{\"key\": \"value\", \"key2\": \"value2\"}"),
                 schema = @Schema(implementation = String.class)) })
    @ApiResponse(responseCode = "404", description = "Glossary Term not found")
    public ResponseEntity<String> getGlossaryTermbyQueryName(@Parameter(description = "你想要模糊查詢查的termName") @RequestParam String queryName) {
    	String response = swaggerAPIService.getGlossaryTermByQueryName(queryName).block();
        return ResponseEntity.ok(response);
    }
    //這個會根據你的特定名字回傳urn
    @GetMapping("/GlossaryTermUrnByTermName")
    @Operation(summary = "取得該確切termName對應的urn", description= "回傳該urn value")
    @ApiResponse(responseCode = "200", description = "Successful retrieval of glossary Term",
    			 content = { @Content(mediaType = "application/json", 
                 examples = @ExampleObject(value = "{\"key\": \"value\", \"key2\": \"value2\"}"),
                 schema = @Schema(implementation = String.class)) })
    @ApiResponse(responseCode = "404", description = "Glossary Term not found")
    public ResponseEntity<String> getGlossaryTermUrnbyTermName(@Parameter(description = "你想要詢查的確切termName為何") @RequestParam String termName) throws JsonMappingException, JsonProcessingException {
    	String response = swaggerAPIService.getGlossaryTermUrnbyTermName(termName);
        return ResponseEntity.ok(response);
    }
    
    
    
    @PostMapping("/GlossaryTermLink")
    @Operation(summary = "加入一個新的glossaryLink在特定的term中", description= "根據你輸入唯一的URN並且輸入你要加入link的標籤與連結，它就會自動幫你加入一筆了")
    @ApiResponse(responseCode = "200", description = "Successfully add glossary Term Link",
	      		 content = { @Content(mediaType = "application/json", 
	      		 examples = @ExampleObject(value = "[ {\"key\": \"value\", \"key2\": \"value2\"} ]"),
	      		 schema = @Schema(implementation = String.class)) })
    @ApiResponse(responseCode = "404", description = "Cannot Add glossary Term")
    public ResponseEntity<String> postGlossaryTermLink(@RequestParam String urn, @RequestParam String url, @RequestParam String description) throws JsonProcessingException{
    	String response = swaggerAPIService.postGlossaryTermLink(urn,url,description).block();
    	return ResponseEntity.ok(response);
    }
    
    @PostMapping("/GlossaryTermOwner")
    @Operation(summary = "加入一個新的glossaryOwner在特定的term中", description= "根據你輸入的owner type，它就會自動對應幫你加入一筆了，他可以重複加入一樣的ownership不會有error，api傳入參數有technical_owner等大小寫不一樣沒關係，我程式判斷不看大小寫e.g.:\"typeUrn\":\"urn:li:ownershipType:__system__technical_OWNER\",")
    @ApiResponse(responseCode = "200", description = "Successfully add glossary Term Owner",
	      		 content = { @Content(mediaType = "application/json", 
	      		 examples = @ExampleObject(value = "[ {\"key\": \"value\", \"key2\": \"value2\"} ]"),
	      		 schema = @Schema(implementation = String.class)) })
    @ApiResponse(responseCode = "404", description = "Cannot Add glossary term Owner")
    public ResponseEntity<String> postGlossaryTermOwner(@RequestParam String urn, @RequestParam String type) throws JsonProcessingException{
    	String response = swaggerAPIService.postGlossaryTermOwner(urn,type).block();
    	return ResponseEntity.ok(response);
    }
    
    @PostMapping("/GlossaryTermRelated")
    @Operation(summary = "加入多個的glossaryRelated在特定的term中", description= "根據你輸入的,urn,contains,inherits的陣列，它就就會將這個term related相關的term做連結，我們可以針對同一個重複加入，雖然撈出來得json資料中related如果有相同重複的多筆，在UI上面只會顯示出ㄧ筆而已喔！")
    @ApiResponse(responseCode = "200", description = "Successfully add glossary Term Related",
	      		 content = { @Content(mediaType = "application/json", 
	      		 examples = @ExampleObject(value = "[ {\"key\": \"value\", \"key2\": \"value2\"} ]"),
	      		 schema = @Schema(implementation = String.class)) })
    @ApiResponse(responseCode = "404", description = "Cannot Add glossary term Related")
    public ResponseEntity<String> postGlossaryTermRelated(@RequestParam String urn, @RequestParam String[] contains, @RequestParam String[] inherits) throws JsonProcessingException{
    	String response = swaggerAPIService.postGlossaryTermRelated(urn,contains,inherits).block();
    	return ResponseEntity.ok(response);
    }
    
}


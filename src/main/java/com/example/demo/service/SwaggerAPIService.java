package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import reactor.core.publisher.Mono;

@Service
public class SwaggerAPIService {
	private final WebClient webClient;
	private final String baseUrl;
    public SwaggerAPIService(@Value("${swagger.api.base-url}") String baseUrl,@Value("${datahub.api.token}") String token) {
        this.baseUrl = baseUrl;
    	this.webClient = WebClient.builder()
                                  .baseUrl(baseUrl)
                                  .defaultHeaders(Headers -> Headers.setBearerAuth(token))
                                  .build();
    }

    public Mono<String> getGlossaryTermbyURN(String urn) {
        String url = UriComponentsBuilder.fromUriString(baseUrl)
                                         .path("/v2/entity/glossaryterm/{urn}")
                                         .buildAndExpand(urn)
                                         .toUriString();
        return webClient.get()
                        .uri(url)
                        .header("Accept", "application/json")
                        .retrieve() 
                        .bodyToMono(String.class);
    }
    
    public String getGlossaryTermUrnbyTermName(String termName) throws JsonMappingException, JsonProcessingException {
    	
    	String jsonPayload = getGlossaryTermByQueryName(termName).block();
//    	System.out.println("print jsonPayload"+jsonPayload);
	    ObjectMapper mapper = new ObjectMapper();
	    JsonNode rootNode = mapper.readTree(jsonPayload);
		String returnUrn="";
	    JsonNode entitiesNode = rootNode.path("entities");
    	for(JsonNode entityNodes: entitiesNode) {
    		JsonNode glossaryTermInfoNode = entityNodes.path("glossaryTermInfo").path("value").path("name");
    		System.out.println("glossaryTermName:"+glossaryTermInfoNode.asText());
    		if(glossaryTermInfoNode.asText().equals(termName)) {
    			returnUrn=entityNodes.path("urn").asText();
//    			System.out.println("my return urn:"+returnUrn);
    		}
    	}
    
	    return returnUrn;
    }
    
    public Mono<String> getGlossaryTermByQueryName(String queryTermName) {
        String url = UriComponentsBuilder.fromUriString(baseUrl)
                                         .path("/v2/entity/glossaryterm")
                                         .queryParam("query", queryTermName) // 使用 queryParam 添加查詢參數
                                         .build()
                                         .toUriString();
        return webClient.get()
                        .uri(url)
                        .header("Accept", "application/json")
                        .retrieve() 
                        .bodyToMono(String.class);
    }
    
    public Mono<String> getGlossaryNodeOfTerm(String urn) {
        String url = UriComponentsBuilder.fromUriString(baseUrl)
                                         .path("/v2/entity/glossarynode/{urn}")
                                         .buildAndExpand(urn)
                                         .toUriString();
        return webClient.get()
                        .uri(url)
                        .header("Accept", "application/json")
                        .retrieve() 
                        .bodyToMono(String.class);
    }
    
    public Mono<String> postGlossaryTermLink(String urn, String url,String description){
             
    	//下面可以進行你對這次post要update什麼樣的資料
    	String postUrl = "/v2/entity/glossaryterm";
    	String jsonPayload = getGlossaryTermbyURN(urn).block();
	    ObjectMapper mapper = new ObjectMapper();
	    String updatedJson="";
		try {
		//用這個方法會需要自己建立Json物件們的class結構，好處是容易理解結構，壞處是需要建立一個個class
//			Container container = mapper.readValue(jsonPayload, Container.class);
//			List<Element> elements = container.getElements();
//			String element0 =  elements.get(0).toString();
//			System.out.println(element0);
			
//1.JsonNode,2.Map<String,Object> 在這兩種方法中，您都不需要為 JSON 數據創建一個嚴格定義的 Java 類。這可以在處理動態結構或未知結構的 JSON 數據時非常有用。	
	    
	        JsonNode rootNode = mapper.readTree(jsonPayload);
//	        System.out.println(rootNode);
	        
	        ArrayNode elementsArray;
	        
	        // 创建一个可变的ObjectNode并复制rootNode的内容，這樣才可以將rootNode上面的內容加入新東西
	        ObjectNode mutableRootNode = mapper.createObjectNode();
	        mutableRootNode.setAll((ObjectNode) rootNode);

	        JsonNode institutionalMemoryNode = mutableRootNode.path("institutionalMemory");

	        // 检查institutionalMemory节点是否存在
	        if (institutionalMemoryNode.isMissingNode()) {
	            // 创建institutionalMemory及其子结构
	            ObjectNode institutionalMemory = mapper.createObjectNode();
	            ObjectNode valueNode = institutionalMemory.putObject("value");
	            valueNode.put("__type", "InstitutionalMemory");
	            //對原始的JsonNode直接引用，所以當我的ArrayNode添加新的元素時，會反映在原始的rootNode上面
	            elementsArray = valueNode.putArray("elements");
	            mutableRootNode.set("institutionalMemory", institutionalMemory);
	        }else {
	        	 // 从更新的institutionalMemory节点中获取elements数组
	        	elementsArray = (ArrayNode)mutableRootNode.path("institutionalMemory").path("value").path("elements");
	        }
	        	
	        	//創建一個新的json物件
	            ObjectNode newElement = mapper.createObjectNode();
	            newElement.put("url",url);
	            newElement.put("description",description);
	            ObjectNode createStamp = newElement.putObject("createStamp");
	            createStamp.put("time", System.currentTimeMillis());  // 示例时间戳
	            createStamp.put("actor", "urn:li:corpuser:newuser");

	            // 将新元素添加到数组中，新加入的element會反映在原始jsonNode上面
	            elementsArray.add(newElement);

	            // 打印更新后的 JSON 字符串
	            updatedJson = mutableRootNode.toString();
	            System.out.println("Post link update:"+updatedJson);
	            
  /*定義type種類：對照getGlossaryTerm(urn)的結構，這個當作後續其他隻post api設計要用的：
*	type有六種：
*   "glossaryTermKey" 看我們需不需要把termKey與name做相同的
* 	"glossaryTermInfo" 可以改name，加入definition，或是加入customProperties：{"additionalProp1": "additionalProp1"...}
*   "glossaryRelatedTerms" 裡面的"hasRelatedTerms"下可以加入多個urn來連接多個term
*   "institutionalMemory" 底下的value物件下可以加入elements物件，也就是加入新的link
*   "domains" value底下的"domains"可以加入新的一到多個domain's urns
*   "ownership" 底下value的owners物件下可以加入新的物件去加入新的owner，可能是technical,none之類的
*/          
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String returnPayload =  "[" + updatedJson + "]"; 
        return webClient.post()
                        .uri(postUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(returnPayload)
                        .retrieve()
                        .bodyToMono(String.class);
    }
    
    public Mono<String> postGlossaryTermOwner(String urn,String type){
        
    	//下面可以進行你對這次post要update什麼樣的資料
    	String postUrl = "/v2/entity/glossaryterm";
    	String jsonPayload = getGlossaryTermbyURN(urn).block();
	    ObjectMapper mapper = new ObjectMapper();
	    String updatedJson="";
		try {    
	        JsonNode rootNode = mapper.readTree(jsonPayload);
	        System.out.println(rootNode);
	        
	        ArrayNode elementsArray;
	        
	        // 创建一个可变的ObjectNode并复制rootNode的内容，這樣才可以將rootNode上面的內容加入新東西
	        ObjectNode mutableRootNode = mapper.createObjectNode();
	        mutableRootNode.setAll((ObjectNode) rootNode);

	        JsonNode ownershipNode = mutableRootNode.path("ownership");

	        // 检查institutionalMemory节点是否存在
	        if (ownershipNode.isMissingNode()) {
	            // 创建institutionalMemory及其子结构
	            ObjectNode ownership = mapper.createObjectNode();
	            ObjectNode valueNode = ownership.putObject("value");
	            valueNode.put("__type", "Ownership");
	            //對原始的JsonNode直接引用，所以當我的ArrayNode添加新的元素時，會反映在原始的rootNode上面
	            elementsArray = valueNode.putArray("owners");
	            mutableRootNode.set("ownership", ownership);
	        }else {
	        	 // 从更新的institutionalMemory节点中获取elements数组
	        	elementsArray = (ArrayNode)mutableRootNode.path("ownership").path("value").path("owners");
	        }
	        //如果打post程式碼出現500 error代表說你的payload結構或內容格式有錯誤所導致，這要一個個慢慢對應
	        	//創建一個新的json物件
	            ObjectNode newElement = mapper.createObjectNode();
	            newElement.put("owner","urn:li:corpuser:datahub");
	            if(type.equalsIgnoreCase("technical_owner")) {
	            	newElement.put("type","TECHNICAL_OWNER");
	            }else {
	            	newElement.put("type","CUSTOM");
	            }
	            String typeUrn = "urn:li:ownershipType:__system__" ;
	            typeUrn = typeUrn.concat(type);
	            System.out.println(typeUrn);
	            newElement.put("typeUrn",typeUrn);
	            ObjectNode source = newElement.putObject("source");
	            //MANUAL前面多加一個空格導致有錯誤
	            source.put("type","MANUAL");

	            // 将新元素添加到数组中，新加入的element會反映在原始jsonNode上面
	            elementsArray.add(newElement);

	            // 打印更新后的 JSON 字符串
	            updatedJson = mutableRootNode.toString();
	            System.out.println("更改後的json: "+updatedJson);
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String returnPayload =  "[" + updatedJson + "]"; 
        return webClient.post()
                        .uri(postUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(returnPayload)
                        .retrieve()
                        .bodyToMono(String.class);
    }
    
    
	 public Mono<String> postGlossaryTermRelated(String urn,String[] contains, String[] inherits){
	        
	    	//下面可以進行你對這次post要update什麼樣的資料
	    	String postUrl = "/v2/entity/glossaryterm";
	    	String jsonPayload = getGlossaryTermbyURN(urn).block();
		    ObjectMapper mapper = new ObjectMapper();
		    String updatedJson="";
			try {    
		        JsonNode rootNode = mapper.readTree(jsonPayload);
		        System.out.println(rootNode);
		        
		        ArrayNode elementsArrayInherit;
		        ArrayNode elementsArrayContain;
		        // 创建一个可变的ObjectNode并复制rootNode的内容，這樣才可以將rootNode上面的內容加入新東西
		        ObjectNode mutableRootNode = mapper.createObjectNode();
		        mutableRootNode.setAll((ObjectNode) rootNode);
	
		        JsonNode glossaryRelatedTermsNode = mutableRootNode.path("glossaryRelatedTerms");
	
		        // 检查institutionalMemory节点是否存在
		        if (glossaryRelatedTermsNode.isMissingNode()) {
		            // 创建institutionalMemory及其子结构
		            ObjectNode glossaryRelatedTerms = mapper.createObjectNode();
		            ObjectNode valueNode = glossaryRelatedTerms.putObject("value");
		            valueNode.put("__type", "GlossaryRelatedTerms");
		            elementsArrayInherit = valueNode.putArray("isRelatedTerms");
		            elementsArrayContain = valueNode.putArray("hasRelatedTerms");
		            //對原始的JsonNode直接引用，所以當我的ArrayNode添加新的元素時，會反映在原始的rootNode上面
		            mutableRootNode.set("glossaryRelatedTerms", glossaryRelatedTerms);
		        }else {
		        	 // 从更新的institutionalMemory节点中获取elements数组
		        	elementsArrayInherit = (ArrayNode)mutableRootNode.path("glossaryRelatedTerms").path("value").path("isRelatedTerms");
		            elementsArrayContain = (ArrayNode)mutableRootNode.path("glossaryRelatedTerms").path("value").path("hasRelatedTerms");
		        }
		        if(inherits !=null) {
		            for(int i=0;i<inherits.length;i++) {
		            	elementsArrayInherit.add(inherits[i]);
		            }
		        }
		        if(contains !=null) {
		            for(int i=0;i<contains.length;i++) {
		            	elementsArrayContain.add(contains[i]);
		            }
		        }
	            // 打印更新后的 JSON 字符串
	            updatedJson = mutableRootNode.toString();
	            System.out.println("更改後的json: "+updatedJson);
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			String returnPayload =  "[" + updatedJson + "]"; 
	        return webClient.post()
	                        .uri(postUrl)
	                        .contentType(MediaType.APPLICATION_JSON)
	                        .bodyValue(returnPayload)
	                        .retrieve()
	                        .bodyToMono(String.class);
	    }
}

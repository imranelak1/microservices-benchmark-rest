package ma.imrane.benchmark;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import java.util.*;
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,properties={"spring.datasource.url=jdbc:h2:mem:catalog;DB_CLOSE_DELAY=-1","spring.datasource.username=sa","spring.datasource.password=","spring.jpa.hibernate.ddl-auto=create-drop","management.server.port=0","logging.level.root=WARN","logging.level.org.springframework=WARN"})
class CatalogHttpTest {
    @Autowired TestRestTemplate http;
    @Test void categoryAndItemCrudIncludingFilterAndRelation() {
        var category=http.postForEntity("/categories",Map.of("code","TEST-"+UUID.randomUUID().toString().substring(0,8),"name","Informatique"),Map.class);
        assertEquals(201,category.getStatusCode().value());Long cid=((Number)category.getBody().get("id")).longValue();
        var input=new HashMap<String,Object>();input.put("sku","SKU-"+UUID.randomUUID());input.put("name","Clavier");input.put("price",25);input.put("stock",10);input.put("description","test");input.put("categoryId",cid);input.put("category",http.getRootUri()+"/categories/"+cid);
        var item=http.postForEntity("/items",input,Map.class);assertEquals(201,item.getStatusCode().value());Long id=((Number)item.getBody().get("id")).longValue();
        assertTrue(http.getForObject("/items?categoryId="+cid+"&size=50",String.class).contains("Clavier"));
        assertTrue(http.getForObject("/categories/"+cid+"/items?size=50",String.class).contains("Clavier"));
        input.put("price",30);http.put("/items/"+id,input);assertTrue(http.getForObject("/items/"+id,String.class).contains("30"));
        http.delete("/items/"+id);assertEquals(404,http.getForEntity("/items/"+id,String.class).getStatusCode().value());
        http.delete("/categories/"+cid);
    }
    @Test void paginationIsBounded() {assertEquals(200,http.getForEntity("/items?page=0&size=2",String.class).getStatusCode().value());}
}

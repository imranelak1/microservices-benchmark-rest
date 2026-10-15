package ma.imrane.benchmark;

import com.fasterxml.jackson.databind.JsonNode;
import ma.imrane.benchmark.model.*;
import ma.imrane.benchmark.repositories.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,properties={"spring.datasource.url=jdbc:h2:mem:pagination;DB_CLOSE_DELAY=-1","spring.datasource.username=sa","spring.datasource.password=","spring.jpa.hibernate.ddl-auto=create-drop","management.server.port=0","logging.level.root=WARN","logging.level.org.springframework=WARN","spring.jpa.properties.hibernate.session_factory.statement_inspector=ma.imrane.benchmark.PaginationContractTest$SqlCapture"})
public class PaginationContractTest {
    public static class SqlCapture implements org.hibernate.resource.jdbc.spi.StatementInspector {
        static final java.util.Queue<String> SQL=new java.util.concurrent.ConcurrentLinkedQueue<>();
        public String inspect(String sql){SQL.add(sql);return sql;}
    }
    @Autowired TestRestTemplate http;
    @Autowired ItemRepository items;
    @Autowired CategoryRepository categories;

    @Test void stablePagesForCollectionFilterAndRelationBeyondFiftyItems(){
        var category=new Category();category.setCode("PAGING");category.setName("Pagination");category=categories.saveAndFlush(category);
        var fixtures=new ArrayList<Item>();
        for(int n=0;n<65;n++){
            var item=new Item();item.setSku("PAGE-"+n);item.setName("Nom "+(65-n));item.setPrice(BigDecimal.TEN);item.setStock(1);item.setDescription("fixture");item.setCategory(category);fixtures.add(item);
        }
        fixtures=new ArrayList<>(items.saveAllAndFlush(fixtures));
        var ids=fixtures.stream().map(Item::getId).sorted().toList();
        long cid=category.getId();
        try{
            for(String prefix:List.of("/items?", "/items?categoryId="+cid+"&", "/categories/"+cid+"/items?")){
                SqlCapture.SQL.clear();
                var page=http.getForObject(prefix+"page=2&size=7",JsonNode.class);
                assertTrue(SqlCapture.SQL.stream().anyMatch(sql->sql.toLowerCase().contains("order by") && sql.toLowerCase().contains(".id")),"Actual SQL must order by id: "+prefix);
                assertEquals(ids.subList(14,21),ids(page),prefix);
                assertEquals(65,total(page),prefix);
                assertEquals(ids.subList(63,65),ids(http.getForObject(prefix+"page=9&size=7",JsonNode.class)),prefix);
            }
            assertEquals(404,http.getForEntity("/categories/999999/items?page=1&size=1",String.class).getStatusCode().value());
            var extra=new ArrayList<Category>();
            for(String code:List.of("Z-LAST","A-FIRST")){var value=new Category();value.setCode(code);value.setName(code);extra.add(categories.saveAndFlush(value));}
            try{
                SqlCapture.SQL.clear();
                var body=http.getForObject("/categories?page=0&size=3",JsonNode.class);
                assertTrue(SqlCapture.SQL.stream().anyMatch(sql->sql.toLowerCase().contains("order by") && sql.toLowerCase().contains(".id")));
                var actual=ids(body);var sorted=new ArrayList<>(actual);Collections.sort(sorted);assertEquals(sorted,actual);
            }finally{categories.deleteAll(extra);}
        }finally{items.deleteAll(fixtures);categories.deleteById(cid);}
    }
    private List<Long> ids(JsonNode page){
        JsonNode content=page.has("content")?page.get("content"):page.path("_embedded").elements().next();
        var ids=new ArrayList<Long>();content.forEach(value->ids.add(value.path("id").asLong()));return ids;
    }
    private long total(JsonNode page){return page.has("totalElements")?page.path("totalElements").asLong():page.path("page").path("totalElements").asLong();}
}

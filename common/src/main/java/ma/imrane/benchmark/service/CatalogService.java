package ma.imrane.benchmark.service;
import ma.imrane.benchmark.model.*;
import ma.imrane.benchmark.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
/** Même mapping et mêmes requêtes dans les deux variantes à contrôleurs manuels. */
@Service @Transactional
public class CatalogService {
    public record CategoryInput(String code,String name) { }
    public record CategoryView(Long id,String code,String name,LocalDateTime updatedAt) { }
    public record ItemInput(String sku,String name,BigDecimal price,int stock,Long categoryId,String description) { }
    public record ItemView(Long id,String sku,String name,BigDecimal price,int stock,Long categoryId,String description,LocalDateTime updatedAt) { }
    private final CategoryRepository categories;
    private final ItemRepository items;
    public CatalogService(CategoryRepository categories,ItemRepository items) { this.categories=categories;this.items=items; }
    public Page<CategoryView> categories(int page,int size) {return categories.findAll(paging(page,size)).map(this::view);}
    public CategoryView category(Long id) {return view(entityCategory(id));}
    public CategoryView saveCategory(Long id,CategoryInput input) {
        if(input.code()==null || input.name()==null || input.code().length()>32 || input.name().length()>128) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Catégorie invalide");
        var category=id==null?new Category():entityCategory(id);category.setCode(input.code());category.setName(input.name());return view(categories.saveAndFlush(category));
    }
    public void deleteCategory(Long id) { categories.delete(entityCategory(id));categories.flush(); }
    public Page<ItemView> items(Long categoryId,int page,int size) {return (categoryId==null?items.findAll(paging(page,size)):items.findByCategoryId(categoryId,paging(page,size))).map(this::view);}
    public ItemView item(Long id) {return view(entityItem(id));}
    public ItemView saveItem(Long id,ItemInput input) {
        if(input.sku()==null || input.name()==null || input.price()==null || input.price().signum()<0 || input.stock()<0 || input.categoryId()==null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Produit invalide");
        var item=id==null?new Item():entityItem(id);item.setSku(input.sku());item.setName(input.name());item.setPrice(input.price());item.setStock(input.stock());item.setDescription(input.description());item.setCategory(entityCategory(input.categoryId()));return view(items.saveAndFlush(item));
    }
    public void deleteItem(Long id) { items.delete(entityItem(id)); }
    public Category entityCategory(Long id) {return categories.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Catégorie introuvable"));}
    private Item entityItem(Long id) {return items.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Produit introuvable"));}
    private CategoryView view(Category value) {return new CategoryView(value.getId(),value.getCode(),value.getName(),value.getUpdatedAt());}
    private ItemView view(Item value) {return new ItemView(value.getId(),value.getSku(),value.getName(),value.getPrice(),value.getStock(),value.getCategory().getId(),value.getDescription(),value.getUpdatedAt());}
    private Pageable paging(int page,int size) {if(page<0 || size<1 || size>200) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Pagination invalide");return PageRequest.of(page,size,Sort.by("id"));}
}

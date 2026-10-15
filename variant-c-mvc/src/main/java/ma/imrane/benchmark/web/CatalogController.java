package ma.imrane.benchmark.web;
import ma.imrane.benchmark.service.CatalogService;
import ma.imrane.benchmark.service.CatalogService.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
@RestController
public class CatalogController {
    private final CatalogService service;
    public CatalogController(CatalogService service) {this.service=service;}
    @GetMapping("/categories") public Page<CategoryView> categories(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="50") int size) {return service.categories(page,size);}
    @GetMapping("/categories/{id}") public CategoryView category(@PathVariable Long id) {return service.category(id);}
    @PostMapping("/categories") @ResponseStatus(HttpStatus.CREATED) public CategoryView addCategory(@RequestBody CategoryInput input) {return service.saveCategory(null,input);}
    @PutMapping("/categories/{id}") public CategoryView updateCategory(@PathVariable Long id,@RequestBody CategoryInput input) {return service.saveCategory(id,input);}
    @DeleteMapping("/categories/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteCategory(@PathVariable Long id) {service.deleteCategory(id);}
    @GetMapping("/items") public Page<ItemView> items(@RequestParam(required=false) Long categoryId,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="50") int size) {return service.items(categoryId,page,size);}
    @GetMapping("/categories/{id}/items") public Page<ItemView> related(@PathVariable Long id,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="50") int size) {service.category(id);return service.items(id,page,size);}
    @GetMapping("/items/{id}") public ItemView item(@PathVariable Long id) {return service.item(id);}
    @PostMapping("/items") @ResponseStatus(HttpStatus.CREATED) public ItemView addItem(@RequestBody ItemInput input) {return service.saveItem(null,input);}
    @PutMapping("/items/{id}") public ItemView updateItem(@PathVariable Long id,@RequestBody ItemInput input) {return service.saveItem(id,input);}
    @DeleteMapping("/items/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteItem(@PathVariable Long id) {service.deleteItem(id);}
}

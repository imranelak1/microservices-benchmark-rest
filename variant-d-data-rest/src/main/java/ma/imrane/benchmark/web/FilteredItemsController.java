package ma.imrane.benchmark.web;
import ma.imrane.benchmark.model.Item;
import ma.imrane.benchmark.repositories.ItemRepository;
import ma.imrane.benchmark.repositories.CategoryRepository;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
/** HAL et CRUD restent Spring Data REST ; le filtre et la relation utilisent une vraie page SQL. */
@RepositoryRestController
public class FilteredItemsController {
    private final ItemRepository items;
    private final CategoryRepository categories;
    public FilteredItemsController(ItemRepository items,CategoryRepository categories) {this.items=items;this.categories=categories;}
    @GetMapping(value="/items",params="categoryId") @ResponseBody
    public ResponseEntity<?> byCategory(@RequestParam Long categoryId,@PageableDefault(size=50,sort="id") Pageable pageable,PagedResourcesAssembler<Item> pages,PersistentEntityResourceAssembler assembler) {
        return ResponseEntity.ok(pages.toModel(items.findByCategoryId(categoryId,pageable),item -> assembler.toModel(item)));
    }
    @GetMapping("/categories/{id}/items") @ResponseBody
    public ResponseEntity<?> related(@PathVariable Long id,@PageableDefault(size=50,sort="id") Pageable pageable,PagedResourcesAssembler<Item> pages,PersistentEntityResourceAssembler assembler){
        categories.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Catégorie introuvable"));
        return ResponseEntity.ok(pages.toModel(items.findByCategoryId(id,pageable),item -> assembler.toModel(item)));
    }
}

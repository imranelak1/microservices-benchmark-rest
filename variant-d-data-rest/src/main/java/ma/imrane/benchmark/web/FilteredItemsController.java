package ma.imrane.benchmark.web;
import ma.imrane.benchmark.model.Item;
import ma.imrane.benchmark.repositories.ItemRepository;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
/** Seul le filtre demandé ajoute un adaptateur ; le CRUD et les relations restent automatiques. */
@RepositoryRestController
public class FilteredItemsController {
    private final ItemRepository items;
    public FilteredItemsController(ItemRepository items) {this.items=items;}
    @GetMapping(value="/items",params="categoryId") @ResponseBody
    public ResponseEntity<?> byCategory(@RequestParam Long categoryId,Pageable pageable,PagedResourcesAssembler<Item> pages,PersistentEntityResourceAssembler assembler) {
                    return ResponseEntity.ok(pages.toModel(items.findByCategoryId(categoryId,pageable),item -> assembler.toModel(item)));
    }
}

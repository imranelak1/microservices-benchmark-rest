package ma.imrane.benchmark.repositories;
import ma.imrane.benchmark.model.Item;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.*;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
@RepositoryRestResource(path="items",collectionResourceRel="items")
public interface ItemRepository extends JpaRepository<Item,Long> {
    @EntityGraph(attributePaths="category") Page<Item> findByCategoryId(Long categoryId,Pageable pageable);
    @Override @EntityGraph(attributePaths="category") Page<Item> findAll(Pageable pageable);
}

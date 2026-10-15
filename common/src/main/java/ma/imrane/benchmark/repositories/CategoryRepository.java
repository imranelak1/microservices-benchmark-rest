package ma.imrane.benchmark.repositories;
import ma.imrane.benchmark.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
@RepositoryRestResource(path="categories",collectionResourceRel="categories")
public interface CategoryRepository extends JpaRepository<Category,Long> { }

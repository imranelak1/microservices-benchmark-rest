package ma.imrane.benchmark.model;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@Entity @Table(name="item",indexes={@Index(name="idx_item_category",columnList="category_id"),@Index(name="idx_item_updated_at",columnList="updatedAt")})
@Getter @Setter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown=true)
public class Item {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false,unique=true,length=64) private String sku;
    @Column(nullable=false,length=128) private String name;
    @Column(nullable=false,precision=10,scale=2) private BigDecimal price;
    @Column(nullable=false) private int stock;
    @Column(length=6000) private String description;
    private LocalDateTime updatedAt;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="category_id") private Category category;
    @PrePersist @PreUpdate void stamp() { updatedAt=LocalDateTime.now(); }
}

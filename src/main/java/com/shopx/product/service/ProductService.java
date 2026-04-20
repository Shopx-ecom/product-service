
package com.shopx.product.service;

import com.shopx.product.core.DefaultFilter;
import com.shopx.product.core.FindResourceOption;
import com.shopx.product.core.PageResponse;
import com.shopx.product.entity.Product;
import com.shopx.product.filter.ProductFilter;
import com.shopx.product.repository.ProductRepository;
import com.shopx.product.core.ResourceService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService extends ResourceService<Product> {

    private final ProductRepository repository;

    protected Class<Product> getEntityType() { return Product.class; }
    protected JpaRepository<Product, Long> getRepository() { return repository; }
    protected JpaSpecificationExecutor<Product> getSpecificationExecutorRepository() { return repository; }
    protected String getResourceName() { return "products"; }

    @Override
    protected Specification<Product> getPassedFilters(Object filters, DefaultFilter defaultFilter) {

        Specification<Product> parentSpec = super.getPassedFilters(filters, defaultFilter);
        ProductFilter filter = (ProductFilter) filters;

        Specification<Product> childSpec = (root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            if (filter.getId() != null)
                predicate = cb.and(predicate, cb.equal(root.get("id"), filter.getId()));

            if (filter.getIds() != null && !filter.getIds().isEmpty())
                predicate = cb.and(predicate, root.get("id").in(filter.getIds()));

            if (filter.getName() != null)
                predicate = cb.and(predicate,
                        cb.like(cb.lower(root.get("name")),
                                "%" + filter.getName().toLowerCase() + "%"));

            if (filter.getCategory() != null)
                predicate = cb.and(predicate,
                        cb.equal(cb.lower(root.get("category")),
                                filter.getCategory().toLowerCase()));

            if (filter.getActive() != null)
                predicate = cb.and(predicate,
                        cb.equal(root.get("active"), filter.getActive()));

            // search across fields
            if (filter.getSearch() != null) {
                String pattern = "%" + filter.getSearch().toLowerCase() + "%";

                Predicate nameLike = cb.like(cb.lower(root.get("name")), pattern);
                Predicate categoryLike = cb.like(cb.lower(root.get("category")), pattern);

                predicate = cb.and(predicate, cb.or(nameLike, categoryLike));
            }

            return predicate;
        };

        return Specification.where(parentSpec).and(childSpec);
    }

    // create
    public Product createProduct(Product product) {
        return create(product, Map.of());
    }

    // get by id
    public Product getProductById(Long id) {
        return findResource(id);
    }

    // get all with filter
    public PageResponse<Product> getAllProducts(
            ProductFilter filter,
            FindResourceOption option,
            DefaultFilter defaultFilter
    ) {
        return findResources(filter, option, defaultFilter);
    }

    // update
    public Product updateProduct(Long id, Map<String, Object> updates) {
        return update(id, updates, Optional.empty());
    }

    // delete (soft delete)
    public void deleteProduct(Long id) {
        Product product = findResource(id);
        product.setDeleted(true);
        product.setLastUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
        getRepository().save(product);
    }
}

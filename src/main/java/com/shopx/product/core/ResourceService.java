package com.shopx.product.core;

import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
public abstract class ResourceService<T extends BaseEntity> {
    @PersistenceContext
    private EntityManager entityManager;

    protected abstract Class<T> getEntityType();

    protected abstract JpaRepository<T, Long> getRepository();
    protected abstract JpaSpecificationExecutor<T> getSpecificationExecutorRepository();
    protected abstract String getResourceName();

    protected Specification<T> getPassedFilters(Object filters, DefaultFilter defaultFilter) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            Predicate predicate = cb.conjunction();
            predicate = cb.and(predicate, cb.equal(root.get("deleted"), false));
            if (defaultFilter.getCreatedBy() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("createdBy"), defaultFilter.getCreatedBy()));
            }
            if (defaultFilter.getUpdatedBy() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("updatedBy"), defaultFilter.getUpdatedBy()));
            }
            if (defaultFilter.getDeletedBy() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("deletedBy"), defaultFilter.getDeletedBy()));
            }
            return predicate;
        };
    }
    protected void beforeCreate(T resource, Map<String, Object> extras) throws RuntimeException {
        resource.setDeleted(false);
    }
    protected void afterCreate(T resource, Map<String, Object> extras) {}

    public T create(T resource, Map<String, Object> extras) throws RuntimeException {
        log.info("Creating {} with data: {}", this.getResourceName(), resource);
        this.beforeCreate(resource, extras);
        T createdResource = this.getRepository().save(resource);
        this.afterCreate(createdResource, extras);
        log.info("{} created, and createdData = {}", this.getResourceName(), createdResource);
        return createdResource;
    }

    public Long countResources(Object filters, DefaultFilter defaultFilter) {
        Specification<T> spec = getPassedFilters(filters, defaultFilter);
        return this.getSpecificationExecutorRepository().count(spec);
    }

    public void beforeUpdate(Long resourceId, Map<String, Object> _input, T resource) throws RuntimeException {
        resource.setLastUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
    }

    public void afterUpdate(Long resourceId, Map<String, Object> _input, T oldResource, T updatedResource) {}

    public T findResource(Long resourceId) {
        log.info("Finding {} with resourceId: {}", this.getResourceName(), resourceId);
        Optional<T> fetchedResource = this.getRepository().findById(resourceId);
        if (fetchedResource.isEmpty()) {
            throw new EntityNotFoundException(String.format("%s not found for resourceId: %s", this.getResourceName(), resourceId));
        }
        if (fetchedResource.get().getDeleted()) {
            throw new EntityNotFoundException(String.format("%s not found for resourceId: %s", this.getResourceName(), resourceId));
        }
        return fetchedResource.get();
    }

    @SuppressWarnings("unchecked")
    public T findResource(Long resourceId, Long userId) {
        log.info("Finding {} with resourceId: {} for user: {}", this.getResourceName(), resourceId, userId);
        String tableName = this.getEntityType().getAnnotation(Table.class).name();
        String sql = """
            SELECT * FROM %s\s
            WHERE id = :resourceId\s
              AND deleted = false
       \s""".formatted(tableName);
        Query query = entityManager.createNativeQuery(sql, this.getEntityType());
        query.setParameter("resourceId", resourceId);
        try {
            T rs = (T) query.getSingleResult();
            if (!Objects.equals(rs.getCreatedBy(), userId)) {
                throw new EntityNotFoundException(String.format("%s not found for resourceId: %s", this.getEntityType().getSimpleName(), resourceId));
            }
            return rs;
        } catch (Exception e) {
            throw new EntityNotFoundException(String.format("%s not found for resourceId: %s", this.getEntityType().getSimpleName(), resourceId));
        }
    }

    public T update(Long resourceId, Map<String, Object> _input, Optional<T> _resource) throws RuntimeException {
        log.info("Updating {} for resourceId: {} with data: {}", this.getResourceName(), resourceId, _input);
        T resource = _resource.orElseGet(() -> this.findResource(resourceId));
        this.beforeUpdate(resourceId, _input, resource);
        ObjectMerger.mergeObjects(resource, _input);
        T updatedResource = this.getRepository().save(resource);
        this.afterUpdate(resourceId, _input, resource, updatedResource);
        log.info("{} updated with final data: {}", this.getResourceName(), updatedResource);
        return updatedResource;
    }

    public PageResponse<T> findResources(
            Object filters,
            FindResourceOption option,
            DefaultFilter defaultFilter
    ) {

        log.info("Finding {} with filter: {}", this.getResourceName(), filters);

        Specification<T> spec = this.getPassedFilters(filters, defaultFilter);

        String sortBy = option.getSortBy() != null ? option.getSortBy() : "id";
        String sortOrder = option.getSortOrder() != null ? option.getSortOrder() : "desc";

        Sort sort = "desc".equalsIgnoreCase(sortOrder)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        if (option.getOffset() != null && option.getLimit() != null) {

            int offset = option.getOffset();
            int limit = option.getLimit();
            int page = offset / limit;

            Pageable pageable = PageRequest.of(page, limit, sort);

            Page<T> pagedData = this.getSpecificationExecutorRepository()
                    .findAll(spec, pageable);

            return new PageResponse<>(
                    pagedData.getContent(),
                    pagedData.getNumber(),
                    pagedData.getSize(),
                    pagedData.getTotalElements(),
                    pagedData.getTotalPages(),
                    pagedData.hasNext(),
                    pagedData.hasPrevious()
            );
        }

        List<T> data = this.getSpecificationExecutorRepository().findAll(spec, sort);

        return new PageResponse<>(
                data,
                0,
                data.size(),
                data.size(),
                1,
                false,
                false
        );
    }

    public List<T> bulkInsertOrUpdate(List<T> resources) {
        for (T resource : resources) {
            if (resource.getDeleted() == null || !resource.getDeleted()) {
                resource.setDeleted(false);
            }
        }
        return this.getRepository().saveAll(resources);
    }
}

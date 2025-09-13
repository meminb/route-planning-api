package com.turkishairlines.routeplanning.repository;

import com.turkishairlines.routeplanning.model.entity.BaseEntity;
import com.turkishairlines.routeplanning.model.entity.Location;
import com.turkishairlines.routeplanning.model.entity.Transportation;
import com.turkishairlines.routeplanning.model.enumaration.TransportationType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class TransportationCriteriaRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Page<Transportation> findAllWithFilters(Pageable pageable, Long originLocationId,
            Long destinationLocationId, TransportationType transportationType) {

        log.debug("Building criteria query with filters - origin: {}, destination: {}, type: {}",
                originLocationId, destinationLocationId, transportationType);

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Transportation> criteriaQuery = criteriaBuilder.createQuery(Transportation.class);
        Root<Transportation> root = criteriaQuery.from(Transportation.class);

        Join<Object, Object> originLocationJoin = root.join(Transportation.Fields.originLocation, JoinType.INNER);
        Join<Object, Object> destinationLocationJoin = root.join(Transportation.Fields.destinationLocation,
                JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();

        if (originLocationId != null) {
            predicates.add(criteriaBuilder.equal(originLocationJoin.get(BaseEntity.Fields.id), originLocationId));
        }

        if (destinationLocationId != null) {
            predicates
                    .add(criteriaBuilder.equal(destinationLocationJoin.get(BaseEntity.Fields.id), destinationLocationId));
        }

        if (transportationType != null) {
            predicates
                    .add(criteriaBuilder.equal(root.get(Transportation.Fields.transportationType), transportationType));
        }

        predicates.add(criteriaBuilder.isNull(root.get(BaseEntity.Fields.deletedAt)));

        criteriaQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));

        criteriaQuery.orderBy(criteriaBuilder.desc(root.get(BaseEntity.Fields.createdAt)));

        TypedQuery<Transportation> query = entityManager.createQuery(criteriaQuery);

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<Transportation> results = query.getResultList();

        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<Transportation> countRoot = countQuery.from(Transportation.class);

        Join<Object, Object> countOriginJoin = countRoot.join(Transportation.Fields.originLocation, JoinType.INNER);
        Join<Object, Object> countDestinationJoin = countRoot.join(Transportation.Fields.destinationLocation,
                JoinType.INNER);

        List<Predicate> countPredicates = new ArrayList<>();

        if (originLocationId != null) {
            countPredicates.add(criteriaBuilder.equal(countOriginJoin.get(BaseEntity.Fields.id), originLocationId));
        }

        if (destinationLocationId != null) {
            countPredicates
                    .add(criteriaBuilder.equal(countDestinationJoin.get(BaseEntity.Fields.id), destinationLocationId));
        }

        if (transportationType != null) {
            countPredicates.add(
                    criteriaBuilder.equal(countRoot.get(Transportation.Fields.transportationType), transportationType));
        }

        countPredicates.add(criteriaBuilder.isNull(countRoot.get(BaseEntity.Fields.deletedAt)));

        countQuery.select(criteriaBuilder.count(countRoot));
        countQuery.where(criteriaBuilder.and(countPredicates.toArray(new Predicate[0])));

        Long totalElements = entityManager.createQuery(countQuery).getSingleResult();

        log.debug("Found {} total elements with applied filters", totalElements);

        return new PageImpl<>(results, pageable, totalElements);
    }
}

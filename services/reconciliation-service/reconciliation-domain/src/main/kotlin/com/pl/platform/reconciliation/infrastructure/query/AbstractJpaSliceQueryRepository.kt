package com.pl.platform.reconciliation.infrastructure.query

import com.pl.platform.common.rest.SliceResponse
import jakarta.persistence.EntityManager
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Root
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification

abstract class AbstractJpaSliceQueryRepository<E : Any, Q, I>(
    protected val entityManager: EntityManager,
) {
    protected fun searchSlice(
        query: Q,
        pageable: Pageable,
    ): SliceResponse<I> {

        val specification =
            specification(
                query = query,
            )

        val cb: CriteriaBuilder =
            entityManager.criteriaBuilder

        val cq: CriteriaQuery<E> =
            cb.createQuery(entityType())

        val root: Root<E> =
            cq.from(entityType())

        cq.select(root)

        val predicate =
            specification.toPredicate(
                root,
                cq,
                cb,
            )

        if (predicate != null) {
            cq.where(predicate)
        }

        if (pageable.sort.isSorted) {
            val orders = pageable.sort
                .toList()
                .map { order ->
                    if (order.isAscending) {
                        cb.asc(root.get<Any>(order.property))
                    } else {
                        cb.desc(root.get<Any>(order.property))
                    }
                }

            cq.orderBy(orders)
        }

        var result =
            entityManager
                .createQuery(cq)
                .setFirstResult(pageable.offset.toInt())
                .setMaxResults(
                    minOf(
                        pageable.pageSize + 1,
                        501,
                    )
                )
                .resultList

        val hasNext =
            result.size > pageable.pageSize

        if (hasNext) {
            result = result.subList(
                0,
                pageable.pageSize,
            )
        }

        return SliceResponse(
            content = mapAll(result),
            hasNext = hasNext,
        )
    }

    /** Maps a page at once; override to load related rows in one query instead of one per entity. */
    protected open fun mapAll(entities: List<E>): List<I> = entities.map(::from)

    protected abstract fun entityType(): Class<E>

    protected abstract fun specification(
        query: Q,
    ): Specification<E>

    protected abstract fun from(
        entity: E,
    ): I
}
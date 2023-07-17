package com.portfolio.models.repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.io.Serializable;

public class CommonRepositoryImpl<T, ID extends Serializable> implements CommonRepository<T, ID> {
    @PersistenceContext
    protected EntityManager entityManager;

    @Override
    public Query createQuery(String jpql) {
        return entityManager.createQuery(jpql);
    }

    @Override
    public TypedQuery<T> createTypedQuery(String jpql, Class<T> clazz) {
        return entityManager.createQuery(jpql, clazz);
    }

    @Override
    public TypedQuery<T> createTypedQuery(StringBuilder jpql, Class<T> clazz) {
        return entityManager.createQuery(jpql.toString(), clazz);
    }

    @Override
    public boolean contains(T entity) {
        return entityManager.contains(entity);
    }
}

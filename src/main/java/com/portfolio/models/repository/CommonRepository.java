package com.portfolio.models.repository;

import org.springframework.stereotype.Repository;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.io.Serializable;

@Repository
public interface CommonRepository<T, ID extends Serializable> {
    public Query createQuery(String jpql);
    public TypedQuery<T> createTypedQuery(String jpql, Class<T> clazz);
    public TypedQuery<T> createTypedQuery(StringBuilder jpql, Class<T> clazz);
    public boolean contains(T entity);
}

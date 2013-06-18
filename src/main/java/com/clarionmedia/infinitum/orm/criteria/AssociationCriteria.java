package com.clarionmedia.infinitum.orm.criteria;

import com.clarionmedia.infinitum.orm.criteria.criterion.Criterion;
import com.clarionmedia.infinitum.orm.relationship.ModelRelationship;

import java.lang.reflect.Field;
import java.util.List;

public interface AssociationCriteria<T> extends Criteria<T> {

    ModelRelationship getRelationship();

    Field getRelationshipField();

    <E> List<E> list(Class<E> type);

    AssociationCriteria<T> add(Criterion criterion);

    AssociationCriteria<T> limit(int limit);

    AssociationCriteria<T> offset(int offset);

}

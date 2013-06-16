package com.clarionmedia.infinitum.orm.criteria;

import com.clarionmedia.infinitum.orm.relationship.ModelRelationship;

import java.lang.reflect.Field;

public interface AssociationCriteria<T> extends Criteria<T> {

    ModelRelationship getRelationship();

    Field getRelationshipField();

}

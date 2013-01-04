/*
 * Copyright (c) 2012 Tyler Treat
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.clarionmedia.infinitum.orm;

import java.util.ArrayList;
import java.util.List;

import com.clarionmedia.infinitum.internal.Pair;
import com.clarionmedia.infinitum.orm.relationship.ManyToManyRelationship;
import com.clarionmedia.infinitum.orm.relationship.ManyToOneRelationship;
import com.clarionmedia.infinitum.orm.relationship.OneToManyRelationship;
import com.clarionmedia.infinitum.orm.relationship.OneToOneRelationship;

/**
 * <p>
 * Represents a domain model instance mapped to a database table.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/23/12
 */
public abstract class ModelMap {

	protected Object mModel;
	protected List<Pair<ManyToManyRelationship, Iterable<Object>>> mManyToManyRelationships;
	protected List<Pair<ManyToOneRelationship, Object>> mManyToOneRelationships;
	protected List<Pair<OneToManyRelationship, Iterable<Object>>> mOneToManyRelationships;
	protected List<Pair<OneToOneRelationship, Object>> mOneToOneRelationships;

	/**
	 * Constructs a new {@code ModelMap} for the given {@link Object} model.
	 * 
	 * @param model
	 *            the mapped {@code Object}
	 */
	public ModelMap(Object model) {
		mModel = model;
		mManyToManyRelationships = new ArrayList<Pair<ManyToManyRelationship, Iterable<Object>>>();
		mOneToManyRelationships = new ArrayList<Pair<OneToManyRelationship, Iterable<Object>>>();
		mManyToOneRelationships = new ArrayList<Pair<ManyToOneRelationship, Object>>();
		mOneToOneRelationships = new ArrayList<Pair<OneToOneRelationship, Object>>();
	}

	/**
	 * Returns the {@link Object} model being mapped.
	 * 
	 * @return model
	 */
	public Object getModel() {
		return mModel;
	}

	/**
	 * Sets the {@link Object} model to map.
	 * 
	 * @param model
	 *            {@code Object} being mapped
	 */
	public void setModel(Object model) {
		mModel = model;
	}

	/**
	 * Returns the many-to-many relationships mapped to this model as a
	 * {@code List} of {@code Pairs}. Each {@code Pair} contains a
	 * {@link ManyToManyRelationship} and an {@code Iterable<Object>} containing
	 * the associated entities.
	 * 
	 * @return {@code List<Pair<ManyToManyRelationship, Iterable<Object>>>
	 */
	public List<Pair<ManyToManyRelationship, Iterable<Object>>> getManyToManyRelationships() {
		return mManyToManyRelationships;
	}

	/**
	 * Adds a many-to-many relationship map to this model.
	 * 
	 * @param relationship
	 *            the {@code Pair<ModelRelationship, Iterable<Object>>} to add
	 *            to this map
	 */
	public void addManyToManyRelationship(
			Pair<ManyToManyRelationship, Iterable<Object>> relationship) {
		mManyToManyRelationships.add(relationship);
	}

	/**
	 * Returns the many-to-one relationships mapped to this model as a
	 * {@code List} of {@code Pairs}. Each {@code Pair} contains a
	 * {@link ManyToOneRelationship} and an {@code Iterable<Object>} containing
	 * the associated entities.
	 * 
	 * @return {@code List<Pair<ManyToOneRelationship, Iterable<Object>>>
	 */
	public List<Pair<ManyToOneRelationship, Object>> getManyToOneRelationships() {
		return mManyToOneRelationships;
	}

	/**
	 * Adds a many-to-one relationship map to this model.
	 * 
	 * @param relationship
	 *            the {@code Pair<ManyToOneRelationship, Iterable<Object>>} to
	 *            add to this map
	 */
	public void addManyToOneRelationship(
			Pair<ManyToOneRelationship, Object> relationship) {
		mManyToOneRelationships.add(relationship);
	}

	/**
	 * Returns the one-to-one relationships mapped to this model as a
	 * {@code List} of {@code Pairs}. Each {@code Pair} contains a
	 * {@link OneToOneRelationship} and an {@code Iterable<Object>} containing
	 * the associated entities.
	 * 
	 * @return {@code List<Pair<OneToOneRelationship, Iterable<Object>>>
	 */
	public List<Pair<OneToOneRelationship, Object>> getOneToOneRelationships() {
		return mOneToOneRelationships;
	}

	/**
	 * Adds a one-to-one relationship map to this model.
	 * 
	 * @param relationship
	 *            the {@code Pair<OneToOneRelationship, Iterable<Object>>} to
	 *            add to this map
	 */
	public void addOneToOneRelationship(
			Pair<OneToOneRelationship, Object> relationship) {
		mOneToOneRelationships.add(relationship);
	}

	/**
	 * Returns the one-to-many relationships mapped to this model as a
	 * {@code List} of {@code Pairs}. Each {@code Pair} contains a
	 * {@link OneToManyRelationship} and an {@code Iterable<Object>} containing
	 * the associated entities.
	 * 
	 * @return {@code List<Pair<OneToOneRelationship, Iterable<Object>>>
	 */
	public List<Pair<OneToManyRelationship, Iterable<Object>>> getOneToManyRelationships() {
		return mOneToManyRelationships;
	}

	/**
	 * Adds a one-to-many relationship map to this model.
	 * 
	 * @param relationship
	 *            the {@code Pair<OneToManyRelationship, Iterable<Object>>} to
	 *            add to this map
	 */
	public void addOneToManyRelationship(
			Pair<OneToManyRelationship, Iterable<Object>> relationship) {
		mOneToManyRelationships.add(relationship);
	}

}

/*
 * Copyright (C) 2013 Clarion Media, LLC
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

package com.clarionmedia.infinitum.orm.criteria;

/**
 * <p> Represents a query ordering, including the property to order by, ascending or descending, and if case is ignored
 * or not. </p>
 *
 * @author Tyler Treat
 * @version 1.1.0 06/13/13
 * @since 1.1.0
 */
public class Order {

    public static enum Ordering {ASC, DESC}

    private String mProperty;
    private Ordering mOrdering;
    private boolean mIgnoreCase;

    /**
     * Returns a new {@code Order} that is ascending on the given property.
     *
     * @param property the property to order by
     * @return {@code Order}
     */
    public static Order asc(String property) {
        return new Order(property, Ordering.ASC);
    }

    /**
     * Returns a new {@code Order} that is descending on the given property.
     *
     * @param property the property to order by
     * @return {@code Order}
     */
    public static Order desc(String property) {
        return new Order(property, Ordering.DESC);
    }

    private Order(String property, Ordering ordering) {
        mProperty = property;
        mOrdering = ordering;
    }

    /**
     * Sets the {@code Order} to ignore case.
     *
     * @return the {@code Order} with case ignored
     */
    public Order ignoreCase() {
        mIgnoreCase = true;
        return this;
    }

    public String getProperty() {
        return mProperty;
    }

    public Ordering getOrdering() {
        return mOrdering;
    }

    public boolean isIgnoreCase() {
        return mIgnoreCase;
    }

}

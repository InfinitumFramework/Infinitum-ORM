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

package com.clarionmedia.infinitum.orm.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p> This annotation indicates that the annotated {@link java.lang.reflect.Field} represents a one-to-many
 * relationship with another persistent class. </p>
 *
 * @author Tyler Treat
 * @version 1.1.0 06/15/13
 * @since 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface OneToMany {

    /**
     * Returns the name of the column representing the foreign key in the other entity's table linking back to this
     * {@code
     * Class}
     *
     * @return name of foreign key column
     */
    String column();

    /**
     * Returns the name of this relationship.
     *
     * @return name of one-to-many relationship.
     */
    String name();

}

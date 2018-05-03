/*
 * Copyright 2018 the original author or authors.
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

package io.appulse.encon.databind;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import io.appulse.encon.databind.annotation.IgnoreField;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
public final class PojoParser {

  private static final Map<Class<?>, List<FieldDescriptor>> CACHE;

  static {
    CACHE = new ConcurrentHashMap<>(5);
  }

  public static List<FieldDescriptor> parse (Class<?> type) {
    return CACHE.computeIfAbsent(type, PojoParser::createDescriptors);
  }

  private static List<FieldDescriptor> createDescriptors (Class<?> type) {
    return Stream.of(type.getDeclaredFields())
        .filter(it -> !it.isAnnotationPresent(IgnoreField.class))
        .map(FieldDescriptor::new)
        .sorted(comparing(FieldDescriptor::getOrder))
        .collect(toList());
  }

  private PojoParser () {
  }
}

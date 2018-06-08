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

package io.appulse.encon.databind.deserializer;

import static io.appulse.encon.databind.deserializer.Deserializer.findInPredefined;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static lombok.AccessLevel.PRIVATE;

import io.appulse.encon.terms.ErlangTerm;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.val;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ListAndTupleDeserializer implements Deserializer<Object> {

  Deserializer<?> elementDeserializer;

  Class<?> elementClass;

  Class<?> resultType;

  public ListAndTupleDeserializer (@NonNull Class<?> elementClass, @NonNull Class<?> resultType) {
    elementDeserializer = findInPredefined(elementClass);
    this.elementClass = elementClass;
    this.resultType = resultType;
  }

  @Override
  public Object deserialize (@NonNull ErlangTerm list) {
    val array = toArray(list);
    if (resultType.isArray()) {
      return array;
    } else if (String.class.isAssignableFrom(resultType)) {
      val codePoints = Stream.of(array)
          .mapToInt(it -> (Integer) it)
          .toArray();
      return new String(codePoints, 0, codePoints.length);
    } else if (List.class.isAssignableFrom(resultType)) {
      return Stream.of(array)
          .collect(toList());
    } else if (Set.class.isAssignableFrom(resultType)) {
      return Stream.of(array)
          .collect(toSet());
    }
    throw new UnsupportedOperationException("Unsupported result type " + resultType);
  }

  private Object[] toArray (ErlangTerm list) {
    val array = (Object[]) Array.newInstance(elementClass, list.size());
    for (int i = 0; i < list.size(); i++) {
      val term = list.getUnsafe(i);
      val element = elementDeserializer.deserialize(term);
      array[i] = element;
    }
    return array;
  }
}
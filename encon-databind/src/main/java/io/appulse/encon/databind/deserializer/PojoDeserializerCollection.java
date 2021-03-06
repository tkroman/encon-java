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

import static lombok.AccessLevel.PRIVATE;

import java.util.List;

import io.appulse.encon.databind.parser.FieldDescriptor;
import io.appulse.encon.terms.ErlangTerm;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * Common collection POJO's deserializer from tuple or list.
 *
 * @param <T> the type of deserialization result
 *
 * @since 1.1.0
 * @author Artem Labazin
 */
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class PojoDeserializerCollection<T> implements Deserializer<T> {

  Class<T> type;

  List<FieldDescriptor> fields;

  @Override
  @SneakyThrows
  public T deserialize (@NonNull ErlangTerm tuple) {
    T result = type.newInstance();
    for (int i = 0; i < fields.size(); i++) {
      FieldDescriptor descriptor = fields.get(i);
      ErlangTerm term = tuple.getUnsafe(i);
      try {
        Object value = descriptor.getDeserializer().deserialize(term);
        descriptor.getField().set(result, value);
      } catch (Exception ex) {
        log.error("\n  error with descriptor '{}'\n  and term: {}\n  at index: {}\n  in tuple: {}",
                  descriptor, term, i, tuple, ex);
        throw ex;
      }
    }
    return result;
  }
}

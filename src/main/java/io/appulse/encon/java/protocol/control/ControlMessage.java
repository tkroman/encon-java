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

package io.appulse.encon.java.protocol.control;

import java.util.stream.Stream;

import io.appulse.encon.java.protocol.term.ErlangTerm;
import io.appulse.encon.java.protocol.type.IntegralNumber;
import io.appulse.encon.java.protocol.type.Nil;
import io.appulse.encon.java.protocol.type.Tuple;
import io.appulse.encon.java.protocol.type.Tuple.TupleBuilder;

/**
 *
 * @author Artem Labazin
 * @since 0.0.1
 */
public abstract class ControlMessage {

  protected static final ErlangTerm UNUSED = new Nil();

  public Tuple toTuple () {
    TupleBuilder builder = Tuple.builder();
    builder.add(IntegralNumber.from(getTag().getCode()));
    Stream.of(elements()).forEach(builder::add);
    return builder.build();
  }

  public byte[] toBytes () {
    return toTuple().toBytes();
  }

  protected abstract ControlMessageTag getTag ();

  protected abstract ErlangTerm[] elements ();
}
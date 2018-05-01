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

package io.appulse.encon.java.terms.type;

import static io.appulse.encon.java.terms.TermType.PORT;
import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PRIVATE;

import io.appulse.encon.java.common.NodeDescriptor;
import io.appulse.encon.java.terms.ErlangTerm;
import io.appulse.encon.java.terms.TermType;
import io.appulse.encon.java.terms.exception.IllegalErlangTermTypeException;

import io.netty.buffer.ByteBuf;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@Getter
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ErlangPort extends ErlangTerm {

  private static final long serialVersionUID = 6837541013041204637L;

  @NonFinal
  NodeDescriptor descriptor;

  ErlangAtom node;

  int id;

  int creation;

  public ErlangPort (TermType type, @NonNull ByteBuf buffer) {
    super(type);

    node = ErlangTerm.newInstance(buffer);

    switch (getType()) {
    case PORT:
      id = buffer.readInt() & 0xFFFFFFF; // 28 bits
      creation = buffer.readUnsignedByte() & 0x3; // 2 bits
      break;
    case NEW_PORT:
      id = buffer.readInt();
      creation = buffer.readInt();
      break;
    default:
      throw new IllegalErlangTermTypeException(getClass(), getType());
    }
  }

  @Builder
  private ErlangPort (TermType type, @NonNull String node, int id, int creation) {
    super(ofNullable(type).orElse(PORT));
    this.node = new ErlangAtom(node);

    if (getType() == PORT) {
      this.id = id & 0xFFFFFFF; // 28 bits
      this.creation = creation & 0x3; // 2 bits
    } else {
      this.id = id;
      this.creation = creation;
    }
  }

  @Override
  public String asText (String defaultValue) {
    return toString();
  }

  @Override
  public ErlangPort asPort () {
    return this;
  }

  @Override
  public String toString () {
    return new StringBuilder()
        .append("#Port<")
        .append(creation).append('.')
        .append(id)
        .append('>')
        .toString();
  }

  public final NodeDescriptor getDescriptor () {
    if (descriptor == null) {
      descriptor = NodeDescriptor.from(node.asText());
    }
    return descriptor;
  }

  @Override
  protected void serialize (ByteBuf buffer) {
    node.writeTo(buffer);
    buffer.writeInt(id);

    switch (getType()) {
    case PORT:
      buffer.writeByte(creation);
      break;
    case NEW_PORT:
      buffer.writeInt(creation);
      break;
    default:
      throw new IllegalErlangTermTypeException(getClass(), getType());
    }
  }
}
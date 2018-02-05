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

package io.appulse.encon.java.protocol.type;

import static io.appulse.encon.java.protocol.TermType.INTEGER;
import static io.appulse.encon.java.protocol.TermType.LARGE_BIG;
import static io.appulse.encon.java.protocol.TermType.SMALL_BIG;
import static io.appulse.encon.java.protocol.TermType.SMALL_INTEGER;
import static java.math.BigInteger.ZERO;
import static lombok.AccessLevel.PRIVATE;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.stream.IntStream;

import io.appulse.encon.java.protocol.TermType;
import io.appulse.encon.java.protocol.term.ErlangTerm;
import io.appulse.utils.Bytes;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.val;

/**
 *
 * @author Artem Labazin
 * @since 0.0.1
 */
@ToString
@FieldDefaults(level = PRIVATE)
@EqualsAndHashCode(callSuper = true)
public class IntegralNumber extends ErlangTerm {

  private static final int MAX_INTEGER;

  private static final int MIN_INTEGER;

  private static final int MIN_CACHE;

  private static final int MAX_CACHE;

  private static final IntegralNumber[] CACHE;

  static {
    MAX_INTEGER = (1 << 27) - 1;
    MIN_INTEGER = -(1 << 27) - 1;

    MIN_CACHE = -1;
    MAX_CACHE = 256;

    CACHE = IntStream.range(MIN_CACHE, MAX_CACHE)
        .boxed()
        .map(IntegralNumber::new)
        .toArray(IntegralNumber[]::new);
  }

  public static IntegralNumber from (int number) {
    return number > MAX_CACHE || number < MIN_CACHE
           ? new IntegralNumber(number)
           : CACHE[number - MIN_CACHE];
  }

  BigInteger value;

  public IntegralNumber (TermType type) {
    super(type);
  }

  public IntegralNumber (char value) {
    this((long) value);
  }

  public IntegralNumber (byte value) {
    this((long) value);
  }

  public IntegralNumber (short value) {
    this((long) value);
  }

  public IntegralNumber (int value) {
    this((long) value);
  }

  public IntegralNumber (long value) {
    this(
        (value & 0xFFL) == value
        ? SMALL_INTEGER
        : value < MIN_INTEGER || value > MAX_INTEGER
          ? SMALL_BIG
          : INTEGER
    );
    this.value = BigInteger.valueOf(value);
  }

  public IntegralNumber (BigInteger value) {
    this(
        value.abs().toByteArray().length < 256
        ? SMALL_BIG
        : LARGE_BIG
    );
    this.value = value;
  }

  @Override
  public boolean isShort () {
    return (value.bitLength() + 1) <= Short.SIZE;
  }

  @Override
  public boolean isInt () {
    return (value.bitLength() + 1) <= Integer.SIZE;
  }

  @Override
  public boolean isLong () {
    return (value.bitLength() + 1) <= Long.SIZE;
  }

  @Override
  public boolean isBigInteger () {
    return true;
  }

  @Override
  public Number asNumber () {
    return value;
  }

  @Override
  public boolean asBoolean (boolean defaultValue) {
    return value.equals(ZERO);
  }

  @Override
  public String asText (String defaultValue) {
    return value.toString();
  }

  @Override
  public short asShort (short defaultValue) {
    return value.shortValue();
  }

  @Override
  public int asInt (int defaultValue) {
    return value.intValue();
  }

  @Override
  public long asLong (long defaultValue) {
    return value.longValue();
  }

  @Override
  public BigInteger asBigInteger (BigInteger defaultValue) {
    return value;
  }

  @Override
  public float asFloat (float defaultValue) {
    return value.floatValue();
  }

  @Override
  public double asDouble (double defaultValue) {
    return value.doubleValue();
  }

  @Override
  public BigDecimal asDecimal (BigDecimal defaultValue) {
    return new BigDecimal(value);
  }

  @Override
  protected void read (@NonNull Bytes buffer) {
    int arity = -1;
    int sign = -1;
    switch (getType()) {
    case SMALL_INTEGER:
      value = BigInteger.valueOf(buffer.getByte());
      break;
    case INTEGER:
      value = BigInteger.valueOf(buffer.getInt());
      break;
    case SMALL_BIG:
      arity = buffer.getByte();
    case LARGE_BIG:
      if (arity == -1) {
        arity = buffer.getInt();
      }
      sign = buffer.getByte();
      byte[] bytes = buffer.getBytes(arity);
      // Reverse the array to make it big endian.
      for (int i = 0, j = bytes.length - 1; i < j; i++, j--) {
        // Swap [i] with [j]
        byte tmp = bytes[i];
        bytes[i] = bytes[j];
        bytes[j] = tmp;
      }
      value = new BigInteger(bytes);
      if (sign != 0) {
        value = value.negate();
      }
      break;
    default:
      throw new IllegalArgumentException("");
    }
  }

  @Override
  protected void write (@NonNull Bytes buffer) {
    switch (getType()) {
    case SMALL_INTEGER:
      buffer.put2B(value.shortValue());
      break;
    case INTEGER:
      buffer.put4B(value.intValue());
      break;
    case SMALL_BIG:
    case LARGE_BIG:
      byte[] magnitude = value.abs().toByteArray();
      int length = magnitude.length;
      // Reverse the array to make it little endian.
      for (int i = 0, j = length; i < j--; i++) {
        // Swap [i] with [j]
        byte temp = magnitude[i];
        magnitude[i] = magnitude[j];
        magnitude[j] = temp;
      }

      if ((length & 0xFF) == length) {
        buffer.put1B(length); // length
      } else {
        buffer.put4B(length); // length
      }
      val sign = value.signum() < 0
                 ? 1
                 : 0;
      buffer.put1B(sign);
      buffer.put(magnitude);
      break;
    default:
      throw new IllegalArgumentException("");
    }
  }
}
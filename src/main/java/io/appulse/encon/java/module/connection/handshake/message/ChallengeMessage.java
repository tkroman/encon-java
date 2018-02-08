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

package io.appulse.encon.java.module.connection.handshake.message;

import static io.appulse.encon.java.module.connection.handshake.message.MessageType.CHALLENGE;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static lombok.AccessLevel.PRIVATE;

import java.nio.ByteBuffer;
import java.util.Set;

import io.appulse.encon.java.DistributionFlag;
import io.appulse.epmd.java.core.model.Version;
import io.appulse.utils.Bytes;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 *
 * @author Artem Labazin
 * @since 0.0.1
 */
@Getter
@ToString
@FieldDefaults(level = PRIVATE)
@EqualsAndHashCode(callSuper = true)
public class ChallengeMessage extends Message {

  Version distribution;

  Set<DistributionFlag> flags;

  int challenge;

  String fullName;

  public ChallengeMessage () {
    super(CHALLENGE);
  }

  @Builder
  private ChallengeMessage (Version distribution, @Singular Set<DistributionFlag> flags,
                            int challenge, String fullName, ByteBuffer byteBuffer
  ) {
    this();
    this.distribution = distribution;
    this.flags = flags;
    this.challenge = challenge;
    this.fullName = fullName;
  }

  @Override
  void write (@NonNull Bytes buffer) {
    buffer.put2B(distribution.getCode());
    buffer.put4B(DistributionFlag.bitwiseOr(flags));
    buffer.put4B(challenge);
    buffer.put(fullName.getBytes(ISO_8859_1));
  }

  @Override
  void read (@NonNull Bytes buffer) {
    distribution = Version.of(buffer.getShort());
    flags = DistributionFlag.parse(buffer.getInt());
    challenge = buffer.getInt();
    fullName = buffer.getString(ISO_8859_1);
  }
}

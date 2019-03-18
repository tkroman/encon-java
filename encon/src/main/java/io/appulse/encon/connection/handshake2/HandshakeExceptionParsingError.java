/*
 * Copyright 2019 the original author or authors.
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

package io.appulse.encon.connection.handshake2;

import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 *
 * @since 2.0.0
 * @author Artem Labazin
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class HandshakeExceptionParsingError extends HandshakeException {

  private static final long serialVersionUID = 1806592799632947035L;

  byte code;

  public HandshakeExceptionParsingError (byte code) {
    super("Unknown handshake message with tag '" + code + "'");
    this.code = code;
  }
}
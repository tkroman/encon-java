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

package io.appulse.encon.module.connection.handshake.exception;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
public class HandshakeException extends RuntimeException {

  private static final long serialVersionUID = 1170636244372819840L;

  public HandshakeException () {
    super();
  }

  public HandshakeException (String message) {
    super(message);
  }

  public HandshakeException (String message, Throwable cause) {
    super(message, cause);
  }

  public HandshakeException (Throwable cause) {
    super(cause);
  }
}
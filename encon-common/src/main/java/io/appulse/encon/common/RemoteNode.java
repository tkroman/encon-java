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

package io.appulse.encon.common;

import java.io.IOException;
import java.net.Socket;
import java.util.Optional;

import io.appulse.epmd.java.core.model.NodeType;
import io.appulse.epmd.java.core.model.Protocol;
import io.appulse.epmd.java.core.model.Version;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.Value;
import lombok.val;

/**
 * Representation of remote Erlang node.
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@Value
@Builder
@ToString(of = {
    "descriptor",
    "port"
})
@EqualsAndHashCode(of = {
    "descriptor",
    "protocol",
    "port"
})
public class RemoteNode {

  @NonNull
  NodeDescriptor descriptor;

  @NonNull
  Protocol protocol;

  @NonNull
  NodeType type;

  @NonNull
  Version high;

  @NonNull
  Version low;

  int port;

  @NonNull
  Optional<byte[]> extra;

  /**
   * Tells if the remote node is alive or not.
   *
   * @return {@code true} if the node is alive, {@code false} otherwise
   */
  public boolean isAlive () {
    try (val socket = new Socket(descriptor.getAddress(), port)) {
      return !socket.isClosed();
    } catch (IOException ex) {
      return false;
    }
  }

  /**
   * Tells if the remote node is not alive or not.
   *
   * @return {@code true} if the node is NOT alive, {@code false} otherwise
   */
  public boolean isNotAlive () {
    return !isAlive();
  }
}

/*
 * Copyright 2018 Appulse.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.appulse.encon;

import static io.netty.channel.ChannelOption.ALLOCATOR;
import static io.netty.channel.ChannelOption.SO_BACKLOG;
import static io.netty.channel.ChannelOption.SO_KEEPALIVE;
import static io.netty.channel.ChannelOption.SO_REUSEADDR;
import static io.netty.channel.ChannelOption.TCP_NODELAY;
import static io.netty.channel.ChannelOption.WRITE_BUFFER_WATER_MARK;
import static io.netty.handler.logging.LogLevel.DEBUG;
import static lombok.AccessLevel.PRIVATE;

import io.appulse.encon.common.RemoteNode;
import io.appulse.encon.connection.handshake.HandshakeServerInitializer;
import io.appulse.encon.connection.regular.ConnectionHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.handler.logging.LoggingHandler;
import java.io.Closeable;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Artem Labazin <xxlabaza@gmail.com>
 * @since 23.06.2018
 */
@Slf4j
@FieldDefaults(level = PRIVATE, makeFinal = true)
class ModuleServer implements Closeable {

  int port;

  Node node;

  ModuleConnection moduleConnection;

  ModuleServer (@NonNull Node node, @NonNull ModuleConnection moduleConnection, int port) {
    this.node = node;
    this.moduleConnection = moduleConnection;
    this.port = port;
    start();
  }

  @Override
  public void close () {
    log.debug("Closing sever module");
    moduleConnection.close();
    log.debug("Server module closed");
  }

  @SneakyThrows
  private void start () {
    log.debug("Starting server on port {}", port);

    new ServerBootstrap()
        .group(moduleConnection.getBossGroup(),
               moduleConnection.getWorkerGroup())
        .channel(moduleConnection.getServerChannelClass())
        .handler(new LoggingHandler(DEBUG))
        .childHandler(HandshakeServerInitializer.builder()
            .node(node)
            .consumer(moduleConnection::add)
            .channelCloseListener(future -> {
              ConnectionHandler connectionHandler = future.channel()
                  .pipeline()
                  .get(ConnectionHandler.class);

              if (connectionHandler == null) {
                return;
              }

              RemoteNode remote = connectionHandler.getRemote();
              node.moduleLookup.remove(remote);
              node.moduleConnection.remove(remote);
            })
            .build())
        .option(SO_BACKLOG, 1024)
        .option(SO_REUSEADDR, true)
        .childOption(SO_REUSEADDR, true)
        .childOption(SO_KEEPALIVE, true)
        .childOption(TCP_NODELAY, true)
        .childOption(WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(64 * 1024, 128 * 1024))
        .childOption(ALLOCATOR, new PooledByteBufAllocator(true))
        .bind(port);
  }
}

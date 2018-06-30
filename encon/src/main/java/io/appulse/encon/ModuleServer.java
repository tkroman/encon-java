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

package io.appulse.encon;

import static io.netty.channel.ChannelOption.ALLOCATOR;
import static io.netty.channel.ChannelOption.SO_BACKLOG;
import static io.netty.channel.ChannelOption.SO_KEEPALIVE;
import static io.netty.channel.ChannelOption.SO_REUSEADDR;
import static io.netty.channel.ChannelOption.TCP_NODELAY;
import static io.netty.channel.ChannelOption.WRITE_BUFFER_WATER_MARK;
import static lombok.AccessLevel.PRIVATE;

import java.io.Closeable;

import io.appulse.encon.connection.handshake.HandshakeServerInitializer;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.sctp.nio.NioSctpServerChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @since 1.2.0
 * @author Artem Labazin
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

    log.debug("Starting server on port {}", port);
    switch (moduleConnection.getProtocol()) {
    case TCP:
      tcp();
      break;
    case UDP:
      udp();
      break;
    case SCTP:
      sctp();
      break;
    default:
      throw new UnsupportedOperationException("Unsupported protocol: " + moduleConnection.getProtocol());
    }
  }

  @Override
  public void close () {
    log.debug("Closing sever module");
    moduleConnection.close();
    log.debug("Server module closed");
  }

  @SneakyThrows
  private void tcp () {
    new ServerBootstrap()
        .group(moduleConnection.getBossGroup(),
               moduleConnection.getWorkerGroup()
        )
        .channel(Epoll.isAvailable()
                 ? EpollServerSocketChannel.class
                 : NioServerSocketChannel.class
        )
        .childHandler(createMainChannelHandler())
        .option(SO_BACKLOG, 1024)
        .option(SO_REUSEADDR, true)
        .childOption(SO_REUSEADDR, true)
        .childOption(SO_KEEPALIVE, true)
        .childOption(TCP_NODELAY, true)
        .childOption(WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(64 * 1024, 128 * 1024))
        .childOption(ALLOCATOR, new PooledByteBufAllocator(true))
        .bind(port);
  }

  private void udp () {
    new Bootstrap()
        .group(moduleConnection.getWorkerGroup())
        .channel(Epoll.isAvailable()
                 ? EpollDatagramChannel.class
                 : NioDatagramChannel.class
        )
        .handler(createMainChannelHandler())
        .option(SO_BACKLOG, 1024)
        .option(SO_REUSEADDR, true)
        .bind(port);
  }

  private void sctp () {
    new ServerBootstrap()
        .group(moduleConnection.getBossGroup(),
               moduleConnection.getWorkerGroup()
        )
        .channel(NioSctpServerChannel.class)
        .childHandler(createMainChannelHandler())
        .option(SO_BACKLOG, 1024)
        .option(SO_REUSEADDR, true)
        .childOption(SO_REUSEADDR, true)
        .childOption(SO_KEEPALIVE, true)
        .childOption(TCP_NODELAY, true)
        .childOption(WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(64 * 1024, 128 * 1024))
        .childOption(ALLOCATOR, new PooledByteBufAllocator(true))
        .bind(port);
  }

  private ChannelHandler createMainChannelHandler () {
    return HandshakeServerInitializer.builder()
        .node(node)
        .consumer(moduleConnection::add)
        .channelCloseAction(remote -> {
          log.debug("Closing connection to {}", remote);
          node.moduleLookup.remove(remote);
          node.moduleConnection.remove(remote);
        })
        .build();
  }
}

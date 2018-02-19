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

package io.appulse.encon.java;

import static java.util.Locale.ENGLISH;
import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PRIVATE;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import io.appulse.encon.java.exception.NodeAlreadyRegisteredException;
import io.appulse.encon.java.module.NodeInternalApi;
import io.appulse.encon.java.module.connection.ConnectionModule;
import io.appulse.encon.java.module.connection.ConnectionModuleApi;
import io.appulse.encon.java.module.generator.pid.PidGeneratorModule;
import io.appulse.encon.java.module.generator.pid.PidGeneratorModuleApi;
import io.appulse.encon.java.module.generator.port.PortGeneratorModule;
import io.appulse.encon.java.module.generator.port.PortGeneratorModuleApi;
import io.appulse.encon.java.module.generator.reference.ReferenceGeneratorModule;
import io.appulse.encon.java.module.generator.reference.ReferenceGeneratorModuleApi;
import io.appulse.encon.java.module.lookup.LookupModule;
import io.appulse.encon.java.module.lookup.LookupModuleApi;
import io.appulse.encon.java.module.mailbox.MailboxModule;
import io.appulse.encon.java.module.ping.PingModule;
import io.appulse.encon.java.module.ping.PingModuleApi;
import io.appulse.epmd.java.client.EpmdClient;
import io.appulse.epmd.java.core.model.request.Registration;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Delegate;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import io.appulse.encon.java.module.server.ServerModule;
import io.appulse.encon.java.module.server.ServerModuleApi;
import io.appulse.encon.java.protocol.term.ErlangTerm;
import io.appulse.encon.java.module.mailbox.MailboxModuleApi;

/**
 *
 * @author Artem Labazin
 * @since 0.0.1
 */
@Slf4j
@ToString(of = {
  "descriptor",
  "cookie",
  "port",
  "meta"
})
@FieldDefaults(level = PRIVATE)
public final class Node implements PingModuleApi, Closeable {

  @Getter
  final NodeDescriptor descriptor;

  @Getter
  final String cookie;

  @Getter
  final int port;

  @Getter
  final Meta meta;

  @Getter
  EpmdClient epmd;

  @Delegate(types = PingModuleApi.class)
  PingModule pingModule;

  @Delegate(types = LookupModuleApi.class)
  LookupModule lookupModule;

  @Delegate(types = PidGeneratorModuleApi.class)
  PidGeneratorModule pidGeneratorModule;

  @Delegate(types = PortGeneratorModuleApi.class)
  PortGeneratorModule portGeneratorModule;

  @Delegate(types = ReferenceGeneratorModuleApi.class)
  ReferenceGeneratorModule referenceGeneratorModule;

  @Delegate(types = MailboxModuleApi.class)
  MailboxModule processModule;

  @Delegate(types = ConnectionModuleApi.class)
  ConnectionModule connectionModule;

 @Delegate(types = ServerModuleApi.class)
 ServerModule serverModule;
  @Builder
  private Node (@NonNull String name, String cookie, int port, Meta meta) {
    descriptor = NodeDescriptor.from(name);

    this.cookie = ofNullable(cookie).orElse(Default.COOKIE);
    this.port = port;
    this.meta = ofNullable(meta).orElse(Meta.DEFAULT);
  }

  public boolean isRegistered () {
    return epmd != null;
  }

  public Node register () {
    if (isRegistered()) {
      throw new NodeAlreadyRegisteredException();
    }
    epmd = new EpmdClient();
    return selfRegistration();
  }

  public Node register (int epmdPort) {
    if (isRegistered()) {
      throw new NodeAlreadyRegisteredException();
    }
    epmd = new EpmdClient(epmdPort);
    return selfRegistration();
  }

  @Override
  public void close () {
    if (processModule != null) {
      processModule.close();
      processModule = null;
    }
    if (connectionModule != null) {
      connectionModule.close();
      connectionModule = null;
    }
    if (serverModule != null) {
      serverModule.close();
      serverModule = null;
    }
    if (epmd != null) {
      epmd.stop(descriptor.getShortName());
      epmd.close();
      epmd = null;
    }

    pidGeneratorModule = null;
    portGeneratorModule = null;
    referenceGeneratorModule = null;

    log.debug("Node '{}' was closed", descriptor.getFullName());
  }

  private Node selfRegistration () {
    val creation = epmd.register(Registration.builder()
        .name(descriptor.getShortName())
        .port(port)
        .type(meta.getType())
        .protocol(meta.getProtocol())
        .high(meta.getHigh())
        .low(meta.getLow())
        .build()
    );

    NodeInternalApi internal = new NodeInternalApi() {

      @Override
      public Node node () {
        return Node.this;
      }

      @Override
      public MailboxModule processes () {
        return processModule;
      }

      @Override
      public EpmdClient epmd () {
        return epmd;
      }

      @Override
      public int creation () {
        return creation;
      }

      @Override
      public ConnectionModule connections () {
        return connectionModule;
      }
    };

    pidGeneratorModule = new PidGeneratorModule(internal);
    portGeneratorModule = new PortGeneratorModule(internal);
    referenceGeneratorModule = new ReferenceGeneratorModule(internal);

    pingModule = new PingModule(internal);
    lookupModule = new LookupModule(internal);

    processModule = new MailboxModule(internal);
    connectionModule = new ConnectionModule(internal);
    serverModule = new ServerModule(internal);
    serverModule.start(port);

    createMailbox("net_kernel", (self, message) -> {
      log.debug("Handler working");
      if (!message.isTuple()) {
        log.debug("Not a tuple");
        return;
      }
      if (!message.get(0).map(ErlangTerm::asText).orElse("").equals("$gen_call")) {
        log.debug("Uh?");
        return;
      }

      ErlangTerm tuple = message.get(1).get();
      self.request().makeTuple()
          .add(tuple.get(1).get().asReference())
          .addAtom("yes")
          .send(tuple.get(0).get().asPid());

      log.debug("Ping response was sent to {}", tuple);
    });

    return this;
  }

  private static class Default {

    private static final String COOKIE = getDefaultCookie();

    private static String getDefaultCookie () {
      val cookieFile = Paths.get(getHomeDir(), ".erlang.cookie");
      if (!Files.exists(cookieFile)) {
        return "";
      }

      try {
        return Files.lines(cookieFile)
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(it -> !it.isEmpty())
            .findFirst()
            .orElse("");
      } catch (IOException ex) {
        return "";
      }
    }

    private static String getHomeDir () {
      val home = System.getProperty("user.home");
      if (!System.getProperty("os.name").toLowerCase(ENGLISH).contains("windows")) {
        return home;
      }

      val drive = System.getenv("HOMEDRIVE");
      val path = System.getenv("HOMEPATH");
      return drive != null && path != null
             ? drive + path
             : home;
    }
  }
}

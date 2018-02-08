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

package io.appulse.encon.java.module.mailbox;

import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PRIVATE;

import java.io.Closeable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.appulse.encon.java.Node;
import io.appulse.encon.java.NodeDescriptor;
import io.appulse.encon.java.RemoteNode;
import io.appulse.encon.java.module.NodeInternalApi;
import io.appulse.encon.java.protocol.request.RequestBuilder;
import io.appulse.encon.java.protocol.term.ErlangTerm;
import io.appulse.encon.java.protocol.type.Pid;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

/**
 *
 * @author Artem Labazin
 * @since 0.0.1
 */
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class Mailbox implements Closeable {

  @Getter
  @NonFinal
  @Setter(PACKAGE)
  String name;

  @NonNull
  NodeInternalApi internal;

  @NonNull
  InboxHandler inboxHandler;

  @Getter
  @NonNull
  Pid pid;

  ExecutorService executor = Executors.newSingleThreadExecutor();

  public Node getNode () {
    return internal.node();
  }

  public RequestBuilder request () {
    return new RequestBuilder(this);
  }

  public void request (Object obj) {
  }

  public void send (@NonNull Pid pid, @NonNull ErlangTerm message) {
    val remoteAddress = pid.getDescriptor().getAddress();
    val localAddress = internal.node().getDescriptor().getAddress();
    if (remoteAddress.equals(localAddress)) {
      internal.mailboxes()
          .getMailbox(pid)
          .orElseThrow(RuntimeException::new)
          .inbox(message);;
    }
  }

  public void send (@NonNull String mailbox, @NonNull ErlangTerm message) {
    internal.mailboxes()
        .getMailbox(mailbox)
        .orElseThrow(RuntimeException::new)
        .inbox(message);;
  }

  public void send (@NonNull String node, @NonNull String mailbox, @NonNull ErlangTerm message) {
    val descriptor = NodeDescriptor.from(node);
    send(descriptor, mailbox, message);
  }

  public void send (@NonNull NodeDescriptor descriptor, @NonNull String mailbox, @NonNull ErlangTerm message) {
    RemoteNode remote = internal.node()
        .lookup(descriptor)
        .orElseThrow(RuntimeException::new);

    send(remote, mailbox, message);
  }

  public void send (@NonNull RemoteNode remote, @NonNull String mailbox, @NonNull ErlangTerm message) {
    if (internal.node().getDescriptor().equals(remote.getDescriptor())) {
      send(mailbox, message);
    } else {
      internal.connections()
          .connect(remote)
          .send(mailbox, message);
    }
  }

  public void inbox (ErlangTerm message) {
    executor.execute(() -> inboxHandler.receive(this, message));
  }

  @Override
  public void close () {
    executor.shutdown();
    name = null;
  }

  public interface InboxHandler {

    void receive (Mailbox self, ErlangTerm message);
  }
}

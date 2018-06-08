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

package io.appulse.encon.module.mailbox.handler;

import java.util.Optional;

import io.appulse.encon.module.connection.control.ControlMessage;
import io.appulse.encon.module.connection.control.Exit;
import io.appulse.encon.module.connection.control.ExitTraceToken;
import io.appulse.encon.module.connection.control.Link;
import io.appulse.encon.module.connection.control.Unlink;
import io.appulse.encon.module.connection.regular.Message;
import io.appulse.encon.module.mailbox.Mailbox;
import io.appulse.encon.terms.ErlangTerm;
import io.appulse.encon.terms.type.ErlangPid;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@Slf4j
public abstract class AbstractMailboxHandler implements MailboxHandler {

  @Override
  public void handle (@NonNull Mailbox self, @NonNull Message message) {
    log.debug("Mailbox {} received:\n  {}",
              self, message);

    val header = message.getHeader();
    try {
      switch (header.getTag()) {
      case LINK:
        handle(self, (Link) header);
        break;
      case UNLINK:
        handle(self, (Unlink) header);
        break;
      case EXIT:
      case EXIT2:
        handle(self, (Exit) header);
        break;
      case EXIT_TT:
      case EXIT2_TT:
        handle(self, (ExitTraceToken) header);
        break;
      default:
        handle(self, header, message.getBody());
      }
    } catch (Exception ex) {
      log.error("Exception during processing received message", ex);
      self.exit(ex.getMessage());
    }
  }

  protected abstract void handle (Mailbox self, ControlMessage header, Optional<ErlangTerm> body);

  /**
   * Handles link requests.
   *
   * @param self   reference to this mailbox
   *
   * @param header received Link control message
   */
  protected void handle (Mailbox self, Link header) {
    val pidFrom = header.getFrom();
    self.getLinks().add(pidFrom);
  }

  /**
   * Handles unlink requests.
   *
   * @param self   reference to this mailbox
   *
   * @param header received Unlink control message
   */
  protected void handle (Mailbox self, Unlink header) {
    val pidFrom = header.getFrom();
    self.getLinks().remove(pidFrom);
  }

  /**
   * Handles exit requests.
   *
   * @param self   reference to this mailbox
   *
   * @param header received Exit control message
   */
  protected void handle (Mailbox self, Exit header) {
    exit(self, header.getFrom(), header.getReason());
  }

  /**
   * Handles exit trace token requests.
   *
   * @param self   reference to this mailbox
   *
   * @param header received ExitTraceToken control message
   */
  protected void handle (Mailbox self, ExitTraceToken header) {
    exit(self, header.getFrom(), header.getReason());
  }

  private void exit (Mailbox self, ErlangPid from, ErlangTerm reason) {
    self.getLinks().remove(from);

    if (reason.isAtom()) {
      val test = reason.asText();
      if ("normal".equals(test)) {
        // ignore 'normal' exit
        return;
      } else if ("kill".equals(test)) {
        self.close();
        return;
      }
    }

    self.exit(reason);
  }
}
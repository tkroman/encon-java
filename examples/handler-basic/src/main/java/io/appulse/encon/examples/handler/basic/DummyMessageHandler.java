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

package io.appulse.encon.examples.handler.basic;

import io.appulse.encon.connection.control.ControlMessage;
import io.appulse.encon.handler.message.MessageHandler;
import io.appulse.encon.mailbox.Mailbox;
import io.appulse.encon.terms.ErlangTerm;

/**
 *
 * @since 1.6.2
 * @author Artem Labazin
 */
public class DummyMessageHandler implements MessageHandler {

  @Override
  public void handle (Mailbox self, ControlMessage header, ErlangTerm body) {
    self.send(body.getUnsafe(0).asPid(), body.getUnsafe(1));
  }
}

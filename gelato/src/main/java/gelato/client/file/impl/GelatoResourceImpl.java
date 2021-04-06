/*
 * Copyright (c) 2021.  Enzo Reyes Licensed under the Apache License, Version 2.0 (the "License");   you may
 * not use this file except in compliance with the License.   You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and limitations under the License.
 *
 */

package gelato.client.file.impl;

import gelato.GelatoFileDescriptor;
import gelato.client.GelatoMessage;
import gelato.client.GelatoMessaging;
import gelato.client.file.GelatoResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.StatStruct;
import protocol.messages.request.StatRequest;
import protocol.messages.response.StatResponse;

public abstract class GelatoResourceImpl implements GelatoResource {

  private final Logger logger = LoggerFactory.getLogger(GelatoResourceImpl.class);
  private StatStruct statStruct;
  private GelatoFileDescriptor descriptor;
  private GelatoMessaging messaging;
  private long cacheLoaded = 0;
  private long cacheExpiry = 4;
  private boolean resourceValid = true;

  public GelatoResourceImpl(GelatoMessaging messaging, GelatoFileDescriptor descriptor) {
    this.descriptor = descriptor;
    this.messaging = messaging;
  }

  @Override
  public synchronized void cacheValidate() {
    logger.trace("Validating Cache entries");
    long lifeSpan = (System.currentTimeMillis() / 1000) - cacheLoaded;
    if (lifeSpan > cacheExpiry) {
      refreshSelf();
      cacheLoaded = (System.currentTimeMillis() / 1000);
    }
  }

  public void setDescriptor(GelatoFileDescriptor newDescriptor) {
    descriptor = newDescriptor;
  }

  @Override
  public void refreshSelf() {
    GelatoMessage<StatRequest, StatResponse> statRequest = messaging.createStatTransaction();
    statRequest.getMessage().setFileDescriptor(descriptor.getRawFileDescriptor());
    messaging.submitMessage(statRequest);
    if (statRequest.getResponse() == null) {
      resourceValid = false;
      logger.error(statRequest.getErrorMessage());
    } else {
      setStatStruct(statRequest.getResponse().getStatStruct());
    }
    messaging.close(statRequest);
  }

  public boolean isResourceValid() {
    return resourceValid;
  }

  public void setResourceValid(boolean resourceValid) {
    this.resourceValid = resourceValid;
  }

  public long getCacheExpiry() {
    return cacheExpiry;
  }

  public void setCacheExpiry(long cacheExpiry) {
    this.cacheExpiry = cacheExpiry;
  }

  public StatStruct getStatStruct() {
    return statStruct;
  }

  public void setStatStruct(StatStruct newStatStruct) {
    statStruct = newStatStruct;
    cacheLoaded = (System.currentTimeMillis() / 1000);
  }

  public GelatoMessaging getMessaging() {
    return messaging;
  }

  @Override
  public String getName() {
    return statStruct.getName();
  }

  @Override
  public long getSize() {
    return statStruct.getLength();
  }

  @Override
  public GelatoFileDescriptor getFileDescriptor() {
    return descriptor;
  }

  @Override
  public boolean valid() {
    return resourceValid;
  }

  @Override
  public long getCacheLoaded() {
    return cacheLoaded;
  }

  @Override
  public void setCacheLoaded(long cacheLoaded) {
    this.cacheLoaded = cacheLoaded;
  }
}

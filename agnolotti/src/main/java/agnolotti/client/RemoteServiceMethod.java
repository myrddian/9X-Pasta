/*
 *   Copyright (c) 2020. Enzo Reyes
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package agnolotti.client;

import agnolotti.Agnolotti;
import agnolotti.schema.AgnelottiSchema;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gelato.client.file.GelatoFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.StatStruct;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RemoteServiceMethod {

  private final GelatoFile remoteMethodFile;
  private final JsonObject idl;
  private final Logger logger = LoggerFactory.getLogger(RemoteServiceMethod.class);
  private boolean noReturn = false;
  private boolean noParameters = false;

  public RemoteServiceMethod(GelatoFile file, JsonObject methodIdl) {
    remoteMethodFile = file;
    idl = methodIdl;
    if (idl.getAsJsonPrimitive(Agnolotti.RETURN_FIELD)
        .getAsString()
        .equals(Agnolotti.PARAMETER_VOID)) {
      noReturn = true;
    }
    JsonArray methodParameters = idl.getAsJsonArray(Agnolotti.PARAMETERS);
    if (methodParameters.size() == 0) {
      noParameters = true;
    }
  }

  public String getMethodName() {
    return remoteMethodFile.getName();
  }

  private Object invokeNoRetNoParam() {

    String jsonString = AgnelottiSchema.getInstance().generateInvocationJson(getMethodName());
    try {
      byte[] bytesToSend = jsonString.getBytes();
      OutputStream invokeStream = remoteMethodFile.getFileOutputStream();
      invokeStream.write(bytesToSend);
      invokeStream.close();
    } catch (IOException e) {
      logger.error("Error Invoking handler exception:", e);
    }
    return null;
  }

  private Object invokeParamNoReturn(Object[] parameters) {
    String jsonString =
        AgnelottiSchema.getInstance().generateInvocationJson(getMethodName(), idl, parameters);
    try {
      OutputStream invokeStream = remoteMethodFile.getFileOutputStream();
      invokeStream.write(jsonString.getBytes());
      invokeStream.close();
    } catch (IOException e) {
      logger.error("Error invoking parameters", e);
    }

    return null;
  }

  private Object getResults() {
    // Request stat update
    remoteMethodFile.refreshSelf();
    StatStruct statStruct = remoteMethodFile.getStatStruct();
    if (statStruct.getLength() == 0) {
      logger.error("CLIENT RPC - Failed - Expected return got NULL");
      throw new RuntimeException("RPC-Error");
    }
    try {
      InputStream fileStream = remoteMethodFile.getFileInputStream();
      return AgnelottiSchema.getInstance().parseReturnJson(fileStream, idl);
    } catch (Exception ex) {
      logger.error("Unable to parse/process return data");
      throw new RuntimeException("Parse Exception");
    }
  }

  private Object invokeReturnNoParam() {
    invokeNoRetNoParam();
    return getResults();
  }

  private Object invokeRetParam(Object[] parameters) {
    invokeParamNoReturn(parameters);
    return getResults();
  }

  public Object invoke(Object[] parameters) {
    if (noReturn) {
      if (noParameters) {
        return invokeNoRetNoParam();
      } else {
        return invokeParamNoReturn(parameters);
      }
    } else {
      if (noParameters) {
        return invokeReturnNoParam();
      } else {
        return invokeRetParam(parameters);
      }
    }
  }
}

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

package agnolotti.schema;

import agnolotti.Agnolotti;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.ByteEncoder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AgnelottiSchema {

  private static AgnelottiSchema SINGLE_INSTANCE = null;
  private final Logger logger = LoggerFactory.getLogger(AgnelottiSchema.class);
  private Gson gson = new Gson();
  private SystemMapper mapper = new SystemMapper();

  private AgnelottiSchema() {}

  public static AgnelottiSchema getInstance() {
    if (SINGLE_INSTANCE == null) {
      synchronized (AgnelottiSchema.class) {
        if (SINGLE_INSTANCE == null) {
          SINGLE_INSTANCE = new AgnelottiSchema();
        }
      }
    }
    return SINGLE_INSTANCE;
  }

  public String generateInvocationJson(String methodName) {
    return gson.toJson(generateInvocationHeader(methodName));
  }

  public Map<String, Object> generateInvocationHeader(String methodName) {
    Map<String, Object> topLevel = new ConcurrentHashMap<>();
    topLevel.put(Agnolotti.PARAMETER_NAME, methodName);
    topLevel.put(Agnolotti.PARAMETERS, new ArrayList<>());
    return topLevel;
  }

  public String generateInvocationJson(String methodName, JsonObject idl, Object[] parameters) {
    Map<String, Object> header = generateInvocationHeader(methodName);
    List<Object> jsonParameters = (List<Object>) header.get(Agnolotti.PARAMETERS);
    JsonArray parameterArray = idl.getAsJsonArray(Agnolotti.PARAMETERS);
    int number = parameterArray.size();
    if (number != parameters.length) {
      throw new RuntimeException("Invalid Parameter Count");
    }

    for (int i = 0; i < number; ++i) {
      Map<String, Object> param = new HashMap<>();
      param.put(Agnolotti.PARAMETER_POS, i);
      param.put(Agnolotti.PARAMETER_VALUE, parameters[i]);
      jsonParameters.add(param);
    }
    return gson.toJson(header);
  }

  public Object parseReturnJson(InputStream inputStream, JsonObject idl) {
    try {
      Reader reader = new InputStreamReader(inputStream);
      JsonReader jsonReader = new JsonReader(reader);
      JsonObject jsonObject = JsonParser.parseReader(jsonReader).getAsJsonObject();
      String returnType = idl.get(Agnolotti.RETURN_FIELD).getAsString();
      inputStream.close();
      return mapper.getValue(jsonObject.get(Agnolotti.RETURN_FIELD), returnType);
    } catch (ClassNotFoundException e) {
      logger.error("Converting to Class failed", e);
      throw new RuntimeException("Error Converting return");
    } catch (IOException e) {
      logger.error("Error reading stream ", e);
      throw new RuntimeException("Error Converting return");
    }
  }

  public String generateReturnJson(Object returnValue) {
    Map<String, Object> retMap = new HashMap<>();
    retMap.put(Agnolotti.RETURN_FIELD, returnValue);
    return gson.toJson(retMap);
  }

  public Object[] getParametersFromJson(JsonObject marshalledObject, JsonContract contract) {
    JsonArray parameters = marshalledObject.getAsJsonArray(Agnolotti.PARAMETERS);
    Object[] invokeParameters = contract.getInvocationTarget().getParameters();
    Object[] methodParameter = new Object[invokeParameters.length];
    List arrayList = contract.getArrayList();
    try {
      for (int i = 0; i < invokeParameters.length; ++i) {
        JsonElement parameter = parameters.get(i);
        JsonObject objectParamerter = parameter.getAsJsonObject();
        int location = objectParamerter.get(Agnolotti.PARAMETER_POS).getAsInt();
        Map<String, Object> methodsMap = (Map) arrayList.get(location);
        JsonElement fieldValue = objectParamerter.get(Agnolotti.PARAMETER_VALUE);
        String jsonClassType = (String) methodsMap.get(Agnolotti.PARAMETER_TYPE);
        Object objectValue = mapper.getValue(fieldValue, jsonClassType);
        methodParameter[location] = objectValue;
      }
      return methodParameter;
    } catch (ClassNotFoundException e) {
      logger.error("Cannot Find Class Initialiser", e);
      throw new RuntimeException("");
    }
  }

  public String decodeJson(List<byte[]> byteArray) {
    int byteArraySize = 0;
    for (byte[] item : byteArray) {
      byteArraySize += item.length;
    }
    byte[] buffer = new byte[byteArraySize];
    int ptr = 0;
    for (byte[] section : byteArray) {
      ByteEncoder.copyBytesTo(section, buffer, ptr, section.length);
      ptr += section.length;
    }

    return new String(buffer);
  }

  public JsonContract getJsonIdlForMethod(Method invocationTarget) {
    Map<String, Object> jsonFieldMap = new ConcurrentHashMap<>();
    List arrayList = new ArrayList();
    jsonFieldMap.put(Agnolotti.PARAMETERS, arrayList);
    jsonFieldMap.put(
        Agnolotti.RETURN_FIELD, mapper.mapGenericName(invocationTarget.getReturnType().getName()));
    int fieldPosition = 0;
    for (Parameter param : invocationTarget.getParameters()) {
      Map<String, Object> methodsMap = new HashMap<>();
      methodsMap.put(Agnolotti.PARAMETER_NAME, param.getName());
      String typeMapper = mapper.mapGenericName(param.getType().getName());
      methodsMap.put(Agnolotti.PARAMETER_TYPE, typeMapper);
      methodsMap.put(Agnolotti.PARAMETER_POS, fieldPosition);
      arrayList.add(methodsMap);
      ++fieldPosition;
    }
    JsonContract contract = new JsonContract();
    contract.setInvocationTarget(invocationTarget);
    contract.setJsonMapping(jsonFieldMap);
    if (invocationTarget.getReturnType().getName().equals(Agnolotti.PARAMETER_VOID)) {
      contract.setReturnMethod(false);
    }

    if (arrayList.isEmpty()) {
      contract.setParameterMethod(false);
    }

    return contract;
  }
}

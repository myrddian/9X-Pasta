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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SystemMapper {

    public static String INTEGER_SYSTEM_TYPE = "int";
    public static String GENERIC_INTEGER_TYPE = "integer";
    public static String SYSTEM_STRING_TYPE = "java.lang.String";
    public static String GENERIC_STRING_TYPE = "string";
    public static String LONG_SYSTEM_TYPE = "long";
    public static String GENERIC_LONG_TYPE = "long";
    public static String BOOLEAN_SYSTEM_TYPE = "boolean";
    public static String GENERIC_BOOLEAN_TYPE = "boolean";
    public static String DOUBLE_SYSTEM_TYPE = "double";
    public static String GENERIC_DOUBLE_TYPE = "double";
    public static String FLOAT_SYSTEM_TYPE = "float";
    public static String GENRIC_FLOAT_TYPE  = "float";
    public static String SHORT_GENERIC_TYPE = "short";
    public static String CHARACTER_GENERIC_TYPE = "char";
    public static String NO_RETURN = "void";
    public static String LIST_SYSTEM_TYPE= "java.util.List";
    public static String GENERIC_LIST_TYPE = "List";
    public static String MAP_SYSTEM_TYPE = "java.util.Map";
    public static String GENERIC_MAP_TYPE = "Map";

    private Map<String, String> serialiseOutMap = new ConcurrentHashMap<>();
    private Map<String, String> deserialiseInMap = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(SystemMapper.class);
    private Gson gson = new Gson();

    public SystemMapper() {

        serialiseOutMap.put(INTEGER_SYSTEM_TYPE,GENERIC_INTEGER_TYPE);
        serialiseOutMap.put(SYSTEM_STRING_TYPE, GENERIC_STRING_TYPE);
        serialiseOutMap.put(LONG_SYSTEM_TYPE, GENERIC_LONG_TYPE);
        serialiseOutMap.put(BOOLEAN_SYSTEM_TYPE, GENERIC_BOOLEAN_TYPE);
        serialiseOutMap.put(DOUBLE_SYSTEM_TYPE, GENERIC_DOUBLE_TYPE);
        serialiseOutMap.put(FLOAT_SYSTEM_TYPE, GENRIC_FLOAT_TYPE);
        serialiseOutMap.put(SHORT_GENERIC_TYPE, SHORT_GENERIC_TYPE);
        serialiseOutMap.put(CHARACTER_GENERIC_TYPE, CHARACTER_GENERIC_TYPE);
        serialiseOutMap.put(LIST_SYSTEM_TYPE, GENERIC_LIST_TYPE);
        serialiseOutMap.put(MAP_SYSTEM_TYPE, GENERIC_MAP_TYPE);
        serialiseOutMap.put(NO_RETURN, NO_RETURN);

        //Reverse the map
        for(String generic: serialiseOutMap.keySet()) {
            deserialiseInMap.put(serialiseOutMap.get(generic), generic);
        }

    }

    public Object getValue(JsonElement object, String fieldType) throws ClassNotFoundException {
        try {
            Class type = getClass(fieldType);
            return gson.fromJson(object, type);
        } catch (ClassNotFoundException e) {
            logger.error("Unable to find TYPE: " + fieldType);
            throw e;
        }
    }

    public String mapGenericName(String typeName) {
        if(serialiseOutMap.containsKey(typeName)) {
            return serialiseOutMap.get(typeName);
        }
        logger.warn("Type has no generic mapping returnign system type "+ typeName);
        return typeName;
    }

    public String getSystemType(String genericName) {
        if(deserialiseInMap.containsKey(genericName)) {
            return deserialiseInMap.get(genericName);
        }
        logger.warn("Type has no System mapping type "+ genericName);
        return genericName;
    }

    public Class getClass(String typeName) throws ClassNotFoundException {
        String systemType = getSystemType(typeName);
        if(ForName.isPrimitive(systemType)) {
            return ForName.forName(systemType);
        }
        return Class.forName(systemType);
    }


}

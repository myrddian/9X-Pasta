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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class JsonContract {

    private Map<String,Object> jsonMapping;
    private boolean isReturnMethod = true;
    private boolean isParameterMethod = true;
    private Method invocationTarget;


    public Map<String, Object> getJsonMapping() {
        return jsonMapping;
    }

    public void setJsonMapping(Map<String, Object> jsonMapping) {
        this.jsonMapping = jsonMapping;
    }

    public boolean isReturnMethod() {
        return isReturnMethod;
    }

    public void setReturnMethod(boolean returnMethod) {
        isReturnMethod = returnMethod;
    }

    public boolean isParameterMethod() {
        return isParameterMethod;
    }

    public void setParameterMethod(boolean parameterMethod) {
        isParameterMethod = parameterMethod;
    }

    public boolean isNoParameters() {
        return !isParameterMethod;
    }

    public boolean isVoidMethod() {
        return !isReturnMethod;
    }

    public Method getInvocationTarget() {
        return invocationTarget;
    }

    public void setInvocationTarget(Method invocationTarget) {
        this.invocationTarget = invocationTarget;
    }

    public List<Object> getArrayList() {
        return (List) jsonMapping.get(Agnolotti.PARAMETERS);
    }


}

package ciotola.actor;

import ciotola.annotations.CiotolaScriptMethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ActorRunner implements CiotolaScript {

    private boolean hasParams = false;
    private boolean hasRet = false;
    private Method target;
    private Object host;

    public ActorRunner(Object runner) {

        boolean foundMethod = false;
        Method targetMethod;
        host = runner;
        for (Method method : runner.getClass().getMethods()) {
            CiotolaScriptMethod startAnnotation = method.getAnnotation(CiotolaScriptMethod.class);
            if (startAnnotation != null) {
                targetMethod = method;
                foundMethod = true;
                processMethod(targetMethod);
            }
        }

        if(!foundMethod) {
            throw new RuntimeException("INVALID - NEEDS ANNOTATION");
        }
    }


    private void processMethod(Method targetMethod) {

        if(targetMethod.getParameterCount()!=0) {
            hasParams = true;
        }

        if(!targetMethod.getReturnType().getName().equals("void")) {
            hasRet = true;
        }

        target = targetMethod;
    }


    @Override
    public Object process(Object message)  {
        try {
            if(hasParams && hasRet) {
                return target.invoke(host, message);
            }
            else if(hasParams) {
                target.invoke(host,message);
            } else if(hasRet){
                return target.invoke(host);
            } else {
                target.invoke(host);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean hasReturn() {
        return hasRet;
    }

    @Override
    public boolean hasValues() {
        return hasParams;
    }
}

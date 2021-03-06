package org.whitesource.agent.dependency.resolver.go;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whitesource.fs.configuration.ResolverConfiguration;

public enum GoDependencyManager {
    DEP("dep"),
    GO_DEP("godep"),
    VNDR("vndr");


    private final String type;
    private final static Logger logger = LoggerFactory.getLogger(GoDependencyManager.class);

    GoDependencyManager(String type){
        this.type = type;
    }

    public String getType(){
        return this.type;
    }

    public static GoDependencyManager getFromType(String type){
        for (GoDependencyManager goDependencyManager : GoDependencyManager.values()){
            if (goDependencyManager.getType().equals(type.trim()))
                return goDependencyManager;
        }
        return null;
    }
}

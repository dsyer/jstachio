package io.jstach.spi;

import java.io.IOException;

import org.eclipse.jdt.annotation.Nullable;

import io.jstach.RenderFunction;

public interface RenderService {
    
    default RenderFunction renderer(String template, Object context, RenderFunction previous) throws IOException {
        return previous;
    }
    
    default Formatter formatter(String path, @Nullable Object context, Formatter previous) throws IOException {
    	return previous;
    }
    
    default Formatter formatter(String path, @Nullable Object context) throws IOException {
        return formatter(path, context, Formatter.DefaultFormatter.INSTANCE);
    }
    
    public static RenderService findService() {
        return RenderServiceResolver.INSTANCE;
    }

}

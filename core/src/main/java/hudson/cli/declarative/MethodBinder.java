/*
 * The MIT License
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.cli.declarative;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.Setter;
import org.kohsuke.args4j.spi.OptionHandler;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Binds method parameters to CLI arguments and parameters via args4j.
 * Once the parser fills in the instance state, {@link #call(Object)}
 * can be used to invoke a method.
 *
 * @author Kohsuke Kawaguchi
 */
class MethodBinder {

    private final Method method;
    private final Object[] arguments;

    /**
     * @param method
     */
    public MethodBinder(Method method, CmdLineParser parser) {
        this.method = method;

        Type[] params = method.getGenericParameterTypes();
        final Class[] paramTypes = method.getParameterTypes();
        arguments = new Object[params.length];

        // to work in cooperation with earlier arguments, add bias to all the ones that this one defines.
        final int bias = parser.getArguments().size();

        Annotation[][] pa = method.getParameterAnnotations();
        for (int i=0; i<params.length; i++) {
            final int index = i;
            for (Annotation a : pa[i]) {
                // TODO: collection and map support
                Setter setter = new Setter() {
                    public void addValue(Object value) throws CmdLineException {
                        arguments[index] = value;
                    }

                    public Class getType() {
                        return paramTypes[index];
                    }

                    public boolean isMultiValued() {
                        return false;
                    }
                };
                if (a instanceof Option) {
                    parser.addOption(setter,(Option)a);
                }
                if (a instanceof Argument) {
                    if (bias>0) a = new ArgumentImpl((Argument)a,bias);
                    parser.addArgument(setter,(Argument)a);
                }
            }
        }
    }

    public Object call(Object instance) throws Exception {
        try {
            return method.invoke(instance,arguments);
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if (t instanceof Exception)
                throw (Exception) t;
            throw e;
        }
    }

    /**
     * {@link Argument} implementation that adds a bias to {@link #index()}.
     */
    @SuppressWarnings({"ClassExplicitlyAnnotation"})
    private static final class ArgumentImpl implements Argument {
        private final Argument base;
        private final int bias;

        private ArgumentImpl(Argument base, int bias) {
            this.base = base;
            this.bias = bias;
        }

        public String usage() {
            return base.usage();
        }

        public String metaVar() {
            return base.metaVar();
        }

        public boolean required() {
            return base.required();
        }

        public Class<? extends OptionHandler> handler() {
            return base.handler();
        }

        public int index() {
            return base.index()+bias;
        }

        public boolean multiValued() {
            return base.multiValued();
        }

        public Class<? extends Annotation> annotationType() {
            return base.annotationType();
        }
    }
}

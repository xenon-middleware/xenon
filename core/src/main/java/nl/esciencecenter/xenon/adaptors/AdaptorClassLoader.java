/*
 * Copyright 2013 Netherlands eScience Center
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.esciencecenter.xenon.adaptors;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

public class AdaptorClassLoader extends ClassLoader {

    private ChildClassLoader childClassLoader;

    public AdaptorClassLoader(List<URL> classpath) {
        super(Thread.currentThread().getContextClassLoader());
        URL[] urls = classpath.toArray(new URL[classpath.size()]);
        childClassLoader = new ChildClassLoader(urls, new DetectClass(this.getParent()));
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        try {
            // System.out.println("CustomClassLoader.loadClass: " + name + " " + resolve);
            return childClassLoader.findClass(name);
        } catch (ClassNotFoundException e) {
            // System.out.println("CustomClassLoader.super.loadClass: " + name + " " + resolve);
            return super.loadClass(name, resolve);
        }
    }

    private static class ChildClassLoader extends URLClassLoader {

        private DetectClass realParent;

        public ChildClassLoader(URL[] urls, DetectClass realParent) {
            super(urls, null);
            this.realParent = realParent;
        }

        @Override
        public Class<?> findClass(String name) throws ClassNotFoundException {

            // System.out.println("ChildClassLoader.findClass: " + name);

            try {
                Class<?> loaded = super.findLoadedClass(name);

                if (loaded != null)
                    return loaded;

                // System.out.println("ChildClassLoader.super.findClass: " + name);
                return super.findClass(name);
            } catch (ClassNotFoundException e) {
                // System.out.println("ChildClassLoader.realParent.loadClass: " + name);
                return realParent.loadClass(name);
            }
        }
    }

    private static class DetectClass extends ClassLoader {

        public DetectClass(ClassLoader parent) {
            super(parent);
        }

        @Override
        public Class<?> findClass(String name) throws ClassNotFoundException {
            // System.out.println("DetectClass.findClass: " + name);
            return super.findClass(name);
        }
    }
}

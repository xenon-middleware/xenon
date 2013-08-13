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

package nl.esciencecenter.octopus.engine.util;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 *
 */
public class ImmutableArrayTest {

    @Test
    public void constructor_noParameters_works() throws Exception {
        ImmutableArray<String> tmp = new ImmutableArray<>();
        assertTrue(tmp.length() == 0);
    }
    
    @Test
    public void constructor_nullStringParameter_works() throws Exception {
        ImmutableArray<String> tmp = new ImmutableArray<>((String)null);
        assertTrue(tmp.length() == 1);
    }
    
    @Test
    public void constructor_nullStringArrayParameter_works() throws Exception {
        ImmutableArray<String> tmp = new ImmutableArray<>((String [])null);
        assertTrue(tmp.length() == 0);
    }

    @Test
    public void constructor_oneParameter_works() throws Exception {
        ImmutableArray<String> tmp = new ImmutableArray<>("aap");
        assertTrue(tmp.length() == 1);
        assertTrue(Arrays.equals(new String [] { "aap" }, tmp.asArray()));
    }
    
    @Test
    public void constructor_twoParameters_works() throws Exception {
        ImmutableArray<String> tmp = new ImmutableArray<>("aap", "noot");
        assertTrue(tmp.length() == 2);
        assertTrue(Arrays.equals(new String [] { "aap", "noot" }, tmp.asArray()));
    }
}

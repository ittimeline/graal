/*
 * Copyright (c) 2017, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.truffle.tck.tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import org.graalvm.polyglot.Value;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.tck.Snippet;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

@RunWith(Parameterized.class)
public class ScriptTest {
    private static final TestUtil.CollectingMatcher<TestRun> TEST_RESULT_MATCHER = TestUtil.createTooManyFailuresMatcher();
    private static TestContext context;
    private final TestRun testRun;

    @Parameterized.Parameters(name = "{0}")
    public static Collection<TestRun> createScriptTests() {
        context = new TestContext();
        final Collection<TestRun> res = new ArrayList<>();
        for (String lang : TestUtil.getRequiredLanguages(context)) {
            for (Snippet script : context.getScripts(null, lang)) {
                res.add(new TestRun(Pair.of(lang, script), Collections.emptyList()));
            }
        }
        return res;
    }

    @AfterClass
    public static void afterClass() throws IOException {
        context.close();
        context = null;
    }

    @Before
    public void setUp() {
        Engine.newBuilder().build();
    }

    public ScriptTest(final TestRun testRun) {
        Objects.requireNonNull(testRun);
        this.testRun = testRun;
    }

    @Test
    public void testScript() {
        Assume.assumeThat(testRun, TEST_RESULT_MATCHER);
        Value result = null;
        try {
            try {
                result = testRun.getSnippet().getExecutableValue().execute(testRun.getActualParameters().toArray());
                TestUtil.validateResult(testRun, result, null);
            } catch (PolyglotException pe) {
                TestUtil.validateResult(testRun, null, pe);
            }
        } finally {
            TEST_RESULT_MATCHER.accept(Pair.of(testRun, result != null));
        }
    }
}

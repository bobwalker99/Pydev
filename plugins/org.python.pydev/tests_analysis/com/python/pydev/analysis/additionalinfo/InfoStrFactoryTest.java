/**
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.python.pydev.analysis.additionalinfo;

import java.util.ArrayList;
import java.util.List;

import org.python.pydev.core.IInfo;

import junit.framework.TestCase;

/**
 * @author fabioz
 *
 */
public class InfoStrFactoryTest extends TestCase {

    public void testInfoStrFactory() throws Exception {
        List<IInfo> iInfo = new ArrayList<IInfo>();
        iInfo.add(new FuncInfo("Bar", "Foo", null, null, "c:\\temp\\a.py", 0, 0));
        iInfo.add(new ClassInfo("Class", "ClassMod", null, null, "c:\\temp\\a.py", 1, 1));
        iInfo.add(new FuncInfo("Bar", "Foo", null, null, "c:\\temp\\a.py", 2, 2));
        assertEquals(iInfo, InfoStrFactory.strToInfo(InfoStrFactory.infoToString(iInfo), null));
    }
}

package org.python.pydev.ast.codecompletion.revisited.modules;

import java.io.File;

import org.python.pydev.core.IPythonNature;
import org.python.pydev.core.ModulesKey;
import org.python.pydev.core.ModulesKeyForFolder;
import org.python.pydev.parser.jython.SimpleNode;

public class InitFromDirModule extends SourceModule {

    public InitFromDirModule(String name, File f, SimpleNode n, Throwable parseError, IPythonNature nature) {
        super(name, f, n, parseError, nature);
    }

    @Override
    public ModulesKey getModulesKey() {
        return new ModulesKeyForFolder(name, getFile());
    }
}

/**
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
/*
 * Created on 24/09/2005
 */
package org.python.pydev.ast.simpleassist;

import java.util.Collection;

import org.python.pydev.core.IPySyntaxHighlightingAndCodeCompletionEditor;
import org.python.pydev.core.docutils.PySelection;
import org.python.pydev.shared_core.code_completion.ICompletionProposalHandle;

public interface ISimpleAssistParticipant {

    /**
     * This method should be overridden to compute the completions
     *
     * @param activationToken this is the activation token
     * @param qualifier this is the qualifier
     * @param ps the selection in the editor
     * @param edit the edit
     * @param offset the offset
     *
     * @return a list of completions
     */
    Collection<ICompletionProposalHandle> computeCompletionProposals(String activationToken, String qualifier,
            PySelection ps, IPySyntaxHighlightingAndCodeCompletionEditor edit, int offset);

}

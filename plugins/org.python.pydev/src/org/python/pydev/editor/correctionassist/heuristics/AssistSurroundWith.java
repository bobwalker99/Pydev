/**
 * Copyright (c) 2005-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
/*
 * Created on Apr 12, 2005
 *
 * @author Fabio Zadrozny
 */
package org.python.pydev.editor.correctionassist.heuristics;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContext;
import org.python.pydev.ast.codecompletion.AbstractTemplateCodeCompletion;
import org.python.pydev.ast.codecompletion.CompletionRequest;
import org.python.pydev.core.IPyEdit;
import org.python.pydev.core.IPythonNature;
import org.python.pydev.core.TokensOrProposalsList;
import org.python.pydev.core.autoedit.DefaultIndentPrefs;
import org.python.pydev.core.docutils.PySelection;
import org.python.pydev.core.proposals.CompletionProposalFactory;
import org.python.pydev.editor.actions.PyAction;
import org.python.pydev.editor.codecompletion.PyTemplateProposal;
import org.python.pydev.editor.correctionassist.IAssistProps;
import org.python.pydev.shared_core.code_completion.ICompletionProposalHandle;
import org.python.pydev.shared_core.image.IImageCache;
import org.python.pydev.shared_core.image.UIConstants;
import org.python.pydev.shared_core.string.FastStringBuffer;
import org.python.pydev.shared_core.string.StringUtils;
import org.python.pydev.shared_ui.ImageCache;

/**
 * @author Fabio Zadrozny
 */
public class AssistSurroundWith extends AbstractTemplateCodeCompletion implements IAssistProps {

    /**
     * @throws BadLocationException
     * @see org.python.pydev.editor.correctionassist.IAssistProps#getProps(org.python.pydev.core.docutils.PySelection, org.python.pydev.shared_ui.ImageCache)
     */
    @Override
    public List<ICompletionProposalHandle> getProps(PySelection ps, IImageCache imageCache, File f,
            IPythonNature nature,
            IPyEdit edit, int offset) throws BadLocationException {

        ArrayList<ICompletionProposalHandle> l = new ArrayList<ICompletionProposalHandle>();
        String indentation = edit != null ? edit.getIndentPrefs().getIndentationString()
                : DefaultIndentPrefs.get(
                        nature).getIndentationString();

        ps.selectCompleteLine();
        String selectedText = ps.getSelectedText();
        // Templates substitute things as $var, so, make sure that we don't get this occurrence.
        selectedText = selectedText.replace("$", "$$");
        List<String> splitInLines = StringUtils.splitInLines(selectedText);
        int firstCharPosition = -1;
        int firstCommentCharPosition = -1;

        for (String string : splitInLines) {
            String trimmed = string.trim();
            if (trimmed.startsWith("#")) {
                int localFirst = PySelection.getFirstCharPosition(string);
                if (firstCommentCharPosition == -1) {
                    firstCommentCharPosition = localFirst;
                } else if (localFirst < firstCommentCharPosition) {
                    firstCommentCharPosition = localFirst;
                }
                continue;
            }
            if (trimmed.length() > 0) {
                int localFirst = PySelection.getFirstCharPosition(string);
                if (firstCharPosition == -1) {
                    firstCharPosition = localFirst;
                } else if (localFirst < firstCharPosition) {
                    firstCharPosition = localFirst;
                }
            }
        }
        if (firstCharPosition == -1) {
            if (firstCommentCharPosition != -1) {
                firstCharPosition = firstCommentCharPosition;
            } else {
                // Haven't found any non-empty line.
                return l;
            }
        }

        //delimiter to use
        String delimiter = PyAction.getDelimiter(ps.getDoc());

        //get the 1st char (determines indent)
        FastStringBuffer startIndentBuffer = new FastStringBuffer(firstCharPosition + 1);
        startIndentBuffer.appendN(' ', firstCharPosition);
        final String startIndent = startIndentBuffer.toString();

        //code to be surrounded
        String surroundedCode = selectedText;
        surroundedCode = indentation + surroundedCode.replaceAll(delimiter, delimiter + indentation);

        //region
        IRegion region = ps.getRegion();
        TemplateContext context = null;
        if (edit != null) {
            context = createContext(region, ps.getDoc());
        }

        //not static because we need the actual code.
        String[] replace0to3 = new String[] { startIndent, delimiter, surroundedCode, delimiter, startIndent,
                delimiter, startIndent, indentation, indentation };
        String[] replace4toEnd = new String[] { startIndent, delimiter, surroundedCode, delimiter, startIndent,
                indentation };

        //actually create the template
        for (int iComp = 0, iRep = 0; iComp < SURROUND_WITH_COMPLETIONS.length; iComp += 2, iRep++) {
            String comp = SURROUND_WITH_COMPLETIONS[iComp];
            if (iRep < 4) {
                comp = StringUtils.format(comp, (Object[]) replace0to3);
            } else {
                comp = StringUtils.format(comp, (Object[]) replace4toEnd);
            }

            l.add(createProposal(ps, imageCache, edit, startIndent, region, iComp, comp, context));
        }

        return l;
    }

    private ICompletionProposalHandle createProposal(PySelection ps, IImageCache imageCache, IPyEdit edit,
            final String startIndent, IRegion region, int iComp, String comp, TemplateContext context) {
        Template t = new Template("Surround with", SURROUND_WITH_COMPLETIONS[iComp + 1], "", comp, false);
        if (context != null) {
            PyTemplateProposal proposal = new PyTemplateProposal(t, context, region,
                    ImageCache.asImage(imageCache.get(UIConstants.COMPLETION_TEMPLATE)), 5) {
                @Override
                public String getAdditionalProposalInfo() {
                    return startIndent + super.getAdditionalProposalInfo();
                }
            };
            return proposal;
        } else {
            //In tests
            return CompletionProposalFactory.get().createPyCompletionProposal(comp, region.getOffset(),
                    region.getLength(), 0, 0);
        }
    }

    /**
     * Template completions available for surround with... They %s will be replaced later for the actual code/indentation.
     *
     * Could be refactored so that we don't have to put the actual indent here (creating a subclass of PyDocumentTemplateContext)
     * Also, if that refactoring was done, we could give an interface for the user to configure those templates better.
     *
     * Another nice thing may be analyzing the current context for local variables so that
     * for item in collection could have 'good' choices for the collection variable based on the local variables.
     */
    public static final String[] SURROUND_WITH_COMPLETIONS = new String[] {
            "%stry:%s%s%s%sexcept${cursor}:%s%s%sraise", "try..except",
            "%stry:%s%s%s%sexcept (${RuntimeError}, ), e:%s%s%s${raise}${cursor}", "try..except (RuntimeError, ), e",
            "%stry:%s%s%s%sfinally:%s%s%s${pass}", "try..finally", "%sif ${True}:%s%s%s%selse:%s%s%s${pass}",
            "if..else",

            "%swhile ${True}:%s%s%s%s%s", "while", "%sfor ${item} in ${collection}:%s%s%s%s%s${cursor}", "for",
            "%sif ${True}:%s%s%s%s%s${cursor}", "if", "%swith ${var}:%s%s%s%s%s${cursor}", "with", };

    /**
     * @see org.python.pydev.editor.correctionassist.IAssistProps#isValid(org.python.pydev.core.docutils.PySelection)
     */
    @Override
    public boolean isValid(PySelection ps, String sel, IPyEdit edit, int offset) {
        return ps.getTextSelection().getLength() > 0;
    }

    @Override
    public TokensOrProposalsList getCodeCompletionProposals(CompletionRequest request)
            throws CoreException,
            BadLocationException {
        throw new RuntimeException("Not implemented: completions should be gotten from the IAssistProps interface.");
    }

}

# Copyright (c) 2002-2004 LOGILAB S.A. (Paris, FRANCE).
# http://www.logilab.fr/ -- mailto:contact@logilab.fr
#
# This program is free software; you can redistribute it and/or modify it under
# the terms of the GNU General Public License as published by the Free Software
# Foundation; either version 2 of the License, or (at your option) any later
# version.
#
# This program is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along with
# this program; if not, write to the Free Software Foundation, Inc.,
# 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
"""utilities methods and classes for checkers
"""

__revision__ = "$Id: __init__.py,v 1.4 2005-02-16 16:45:47 fabioz Exp $"

import tokenize
from os import listdir
from os.path import dirname, join, isdir, splitext

from logilab.common.astng import ASTWalker
from logilab.common.configuration import OptionsProviderMixIn

from logilab.pylint.reporters import diff_string, EmptyReport

def table_lines_from_stats(stats, old_stats, columns):
    """get values listed in <columns> from <stats> and <old_stats>,
    and return a formated list of values, designed to be given to a
    ureport.Table object
    """
    lines = []
    for m_type in columns:
        new = stats[m_type]
        format = str
        if isinstance(new, float):
            format = lambda num: '%.3f' % num
        old = old_stats.get(m_type)
        if old is not None:
            diff_str = diff_string(old, new)
            old = format(old)
        else:
            old, diff_str = 'NC', 'NC'
        lines += (m_type.replace('_', ' '), format(new), old, diff_str)
    return lines

    
class BaseChecker(OptionsProviderMixIn, ASTWalker):
    """base class for checkers"""

    options = ()
    priority = -9
    may_be_disabled = True
    name = None
    
    def __init__(self, linter=None):
        """checker instances should have the linter as argument

        linter is an object implementing ILinter
        """
        ASTWalker.__init__(self, self)
        self.name = self.name.lower()
        if self.may_be_disabled:
            opt_name = 'enable-' + self.name
            self.options = (
                (opt_name,
                 {'type' : 'yn', 'default' : 1, 'metavar': '<y_or_n>',
                  'help' : "Enable / disable this checker"})
                ,) + self.options            
        OptionsProviderMixIn.__init__(self)
        self.linter = linter
        
    def add_message(self, msg_id, line=None, node=None, args=None):
        """add a message of a given type"""
        self.linter.add_message(msg_id, line, node, args)

    def is_enabled(self):
        """return true if the checker is enabled"""
        opt = 'enable_' + self.name
        return getattr(self.config, opt, 1)

    def enable(self, enable):
        """enable / disable this checker if true / false is given

        it false values has no effect if the checker can't be disabled
        """
        if self.may_be_disabled:
            setattr(self.config, 'enable_' + self.name, enable)
        
    def package_dir(self):
        """return the base directory for the analysed package"""
        return dirname(self.linter.base_file)


    # dummy methods implementing the IChecker interface
    
    def open(self):
        """called before visiting project (i.e set of modules)"""
        
    def close(self):
        """called after visiting project (i.e set of modules)"""


class BaseRawChecker(BaseChecker):
    """base class for raw checkers"""
    
    def process_module(self, stream):
        """process a module
        
        the module's content is accessible via the stream object
        
        stream must implements the readline method
        """
        self.process_tokens(tokenize.generate_tokens(stream.readline))
    
    def process_tokens(self, tokens):
        """should be overiden by syb classes"""
        raise NotImplementedError()

PYTHON_EXTENSIONS = ('.py', '.pyc', '.pyo', '.so')

def initialize(linter):
    """initialize linter with checkers in this package """
    package_load(linter, __path__[0])

def package_load(linter, directory):
    """load all module and package in the given directory, looking for a
    'register' function in each one, used to register pylint checkers
    """
    globs = globals()
    imported = {}
    for filename in listdir(directory):
        basename, extension = splitext(filename)
        if not imported.has_key(basename) and ((
            extension in PYTHON_EXTENSIONS and basename != '__init__') or (
            not extension and not basename == 'CVS' and
            isdir(join(directory, basename)))):
            try:
                module = __import__(basename, globs, globs, None)
            except ValueError:
                # empty module name (usually emacs auto-save files)
                continue
            except ImportError:
                import sys
                print >> sys.stderr, "Problem importing module: %s" % filename
            else:
                if hasattr(module, 'register'):
                    module.register(linter)
                    imported[basename] = 1
                
__all__ = ('CheckerHandler', 'BaseChecker', 'initialize', 'package_load')

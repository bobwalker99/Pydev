'''unit tests utilities for ureports
'''

__revision__ = "$Id: utils.py,v 1.2 2005-02-16 16:45:45 fabioz Exp $"

from cStringIO import StringIO
from logilab.common.ureports.nodes import *

class WriterTC:
    def _test_output(self, test_id, layout, msg=None):
        buffer = StringIO()
        self.writer.format(layout, buffer)
        got = buffer.getvalue()
        expected = getattr(self, test_id)
        self.assertLinesEquals(got, expected)

        
    def test_section(self):
        layout = Section('Section title',
                         'Section\'s description.\nBlabla bla')
        self._test_output('section_base', layout)
        layout.append(Section('Subsection', 'Sub section description'))
        self._test_output('section_nested', layout)
        
    def test_verbatim(self):
        layout = VerbatimText('blablabla')
        self._test_output('verbatim_base', layout)

        
    def test_list(self):
        layout = List(children=('item1', 'item2', 'item3', 'item4'))
        self._test_output('list_base', layout)
        
    def test_nested_list(self):
        layout = List(children=(Paragraph(("blabla", List(children=('1', "2", "3")))),
                                "an other point"))
        self._test_output('nested_list', layout)

        
    def test_table(self):
        layout = Table(cols=2, children=('head1', 'head2', 'cell1', 'cell2'))
        self._test_output('table_base', layout)
        
    def test_field_table(self):
        table = Table(cols=2, klass='field', id='mytable')
        for field, value in (('f1', 'v1'), ('f22', 'v22'), ('f333', 'v333')):
            table.append(Text(field))
            table.append(Text(value))
        self._test_output('field_table', table)

    def test_advanced_table(self):
        table = Table(cols=2, klass='whatever', id='mytable', cheaders=1, rheaders=1)
        for field, value in (('field', 'value') ,('f1', 'v1'), ('f22', 'v22'), ('f333', 'v333')):
            table.append(Text(field))
            table.append(Text(value))
        table.append(Link('http://www.perdu.com', 'toi perdu ?'))
        self._test_output('advanced_table', table)

        
##     def test_image(self):
##         layout = Verbatim('blablabla')
##         self._test_output('verbatim_base', layout)

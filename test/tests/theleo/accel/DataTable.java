/**
    Copyright (c) 2016, Juraj Papp
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:
        * Redistributions of source code must retain the above copyright
          notice, this list of conditions and the following disclaimer.
        * Redistributions in binary form must reproduce the above copyright
          notice, this list of conditions and the following disclaimer in the
          documentation and/or other materials provided with the distribution.
        * Neither the name of the copyright holder nor the
          names of its contributors may be used to endorse or promote products
          derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
    ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
    WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
    DISCLAIMED. IN NO EVENT SHALL COPYRIGHT HOLDER BE LIABLE FOR ANY
    DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
    (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
    LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
    ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package tests.theleo.accel;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

/**
 * Debug Table.
 * 
 * @author Juraj Papp
 */
public class DataTable {
    public interface Evaluable {
        public String name();
        public String eval();
    }
    public List<Evaluable> list = new ArrayList<Evaluable>();
    JFrame f;
    JTable table;
    public DataTable() {
        f = new JFrame();
        f.setFocusableWindowState(false);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setLayout(new BorderLayout());
        
        table = new JTable(new AbstractTableModel() {
            String[] colnames = {"name", "value"};

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }
            @Override
            public String getColumnName(int col) {
                return colnames[col];
            }
            public int getRowCount() {
                return list.size();
            }
            public int getColumnCount() {
                return 2;
            }
            public Object getValueAt(int row, int col) {
                Evaluable e = list.get(row);
                return col == 0?e.name():e.eval();
            }        
        });
        f.add(table, BorderLayout.CENTER);
        f.add(table.getTableHeader(), BorderLayout.NORTH);
        
        f.setVisible(true);
        f.setSize(300, 300);
        f.setLocation(0, Toolkit.getDefaultToolkit().getScreenSize().height-f.getHeight());
    }
    public void watch(Evaluable e) {
        list.add(e);
    }
    public void update() {
        table.repaint();
    }
    
    public static void main(String[] args) {
        DataTable dt = new DataTable();
        dt.watch(new Evaluable() {
            public String name() {
                return "var";
            }
            int i = 0;
            public String eval() {
                return ""+i;
            }
        });
    }
}

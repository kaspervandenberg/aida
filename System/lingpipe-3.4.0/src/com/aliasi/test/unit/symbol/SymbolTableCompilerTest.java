/*
 * LingPipe v. 2.0
 * Copyright (C) 2003-5 Alias-i
 *
 * This program is licensed under the Alias-i Royalty Free License
 * Version 1 WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the Alias-i
 * Royalty Free License Version 1 for more details.
 * 
 * You should have received a copy of the Alias-i Royalty Free License
 * Version 1 along with this program; if not, visit
 * http://www.alias-i.com/lingpipe/licenseV1.txt or contact
 * Alias-i, Inc. at 181 North 11th Street, Suite 401, Brooklyn, NY 11211,
 * +1 (718) 290-9170.
 */

package com.aliasi.test.unit.symbol;

import com.aliasi.symbol.SymbolTableCompiler;
import com.aliasi.symbol.SymbolTable;

import com.aliasi.util.AbstractExternalizable;

import java.io.*;

public class SymbolTableCompilerTest extends AbstractSymbolTable {
 
    public void testCompilation() 
    throws ClassNotFoundException, IOException {

    SymbolTableCompiler compiler = new SymbolTableCompiler();
    compiler.addSymbol("a");
    compiler.addSymbol("bb");

    SymbolTable compiledTable
        = (SymbolTable) AbstractExternalizable.compile(compiler);

    assertTwoElementTable(compiledTable);
    assertTwoElementTable(compiler);
    } 


}

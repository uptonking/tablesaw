/*
 * Copyright 2017 Andrew Rucker Jones.
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
package com.opencsv;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.SQLException;

public class NClobWrapper implements NClob {
   private final Clob wrapped;

   public NClobWrapper(Clob wrapped) {
       this.wrapped = wrapped;
   }

    @Override
    public long length() throws SQLException {
        return wrapped.length();
    }

    @Override
    public String getSubString(long pos, int length) throws SQLException {
        return wrapped.getSubString(pos, length);
    }

    @Override
    public Reader getCharacterStream() throws SQLException {
        return wrapped.getCharacterStream();
    }

    @Override
    public InputStream getAsciiStream() throws SQLException {
        return wrapped.getAsciiStream();
    }

    @Override
    public long position(String searchstr, long start) throws SQLException {
        return wrapped.position(searchstr, start);
    }

    @Override
    public long position(Clob searchstr, long start) throws SQLException {
        return wrapped.position(searchstr, start);
    }

    @Override
    public int setString(long pos, String str) throws SQLException {
        return wrapped.setString(pos, str);
    }

    @Override
    public int setString(long pos, String str, int offset, int len) throws SQLException {
        return wrapped.setString(pos, str, offset, len);
    }

    @Override
    public OutputStream setAsciiStream(long pos) throws SQLException {
        return wrapped.setAsciiStream(pos);
    }

    @Override
    public Writer setCharacterStream(long pos) throws SQLException {
        return wrapped.setCharacterStream(pos);
    }

    @Override
    public void truncate(long len) throws SQLException {
        wrapped.truncate(len);
    }

    @Override
    public void free() throws SQLException {
        wrapped.free();
    }

    @Override
    public Reader getCharacterStream(long pos, long length) throws SQLException {
        return wrapped.getCharacterStream(pos, length);
    }

}

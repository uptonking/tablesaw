package com.opencsv;
/*
 Copyright 2005 Bytecode Pty Ltd.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

import java.io.*;
import java.sql.Clob;
import java.sql.SQLException;

public class MockClob implements Clob {

   private final String clobValue;

   public MockClob(String value) {
      clobValue = value;
   }

   @Override
   public long length() {
      return 0;
   }

   @Override
   public String getSubString(long l, int i) {
      return null;
   }

   @Override
   public Reader getCharacterStream() {
      return new StringReader(clobValue);
   }

   @Override
   public InputStream getAsciiStream() {
      return null;
   }

   @Override
   public long position(String s, long l) {
      return 0;
   }

   @Override
   public long position(Clob clob, long l) {
      return 0;
   }

   @Override
   public int setString(long l, String s) {
      return 0;
   }

   @Override
   public int setString(long l, String s, int i, int i1) {
      return 0;
   }

   @Override
   public OutputStream setAsciiStream(long l) {
      return null;
   }

   @Override
   public Writer setCharacterStream(long l) {
      return null;
   }

   @Override
   public void truncate(long l) {

   }

   @Override
   public void free() {

   }

   @Override
   public Reader getCharacterStream(long l, long l1) {
      return null;
   }
}

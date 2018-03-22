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

import org.apache.commons.text.StrBuilder;

import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

/**
 * Helper class for processing JDBC ResultSet objects.
 */
public class ResultSetHelperService implements ResultSetHelper {
   protected static final int CLOBBUFFERSIZE = 2048;

   static final String DEFAULT_DATE_FORMAT = "dd-MMM-yyyy";
   static final String DEFAULT_TIMESTAMP_FORMAT = "dd-MMM-yyyy HH:mm:ss";
   private static final String DEFAULT_VALUE = StringUtils.EMPTY;

   private String dateFormat = DEFAULT_DATE_FORMAT;
   private String dateTimeFormat = DEFAULT_TIMESTAMP_FORMAT;

   /**
    * Default constructor.
    */
   public ResultSetHelperService() {
   }

   /**
    * Set a default date format pattern that will be used by the service.
    *
    * @param dateFormat Desired date format
    */
   public void setDateFormat(String dateFormat) {
      this.dateFormat = dateFormat;
   }

   /**
    * Set a default date time format pattern that will be used by the service.
    *
    * @param dateTimeFormat Desired date time format
    */
   public void setDateTimeFormat(String dateTimeFormat) {
      this.dateTimeFormat = dateTimeFormat;
   }

   @Override
   public String[] getColumnNames(ResultSet rs) throws SQLException {
      ResultSetMetaData metadata = rs.getMetaData();
      String[] nameArray = new String[metadata.getColumnCount()];
      for (int i = 0; i < metadata.getColumnCount(); i++) {
         nameArray[i] = metadata.getColumnLabel(i+1);
      }
      return nameArray;
   }

   @Override
   public String[] getColumnValues(ResultSet rs) throws SQLException, IOException {
      return this.getColumnValues(rs, false, dateFormat, dateTimeFormat);
   }

   @Override
   public String[] getColumnValues(ResultSet rs, boolean trim) throws SQLException, IOException {
      return this.getColumnValues(rs, trim, dateFormat, dateTimeFormat);
   }

   @Override
   public String[] getColumnValues(ResultSet rs, boolean trim, String dateFormatString, String timeFormatString) throws SQLException, IOException {
      ResultSetMetaData metadata = rs.getMetaData();
      String[] valueArray = new String[metadata.getColumnCount()];
      for (int i = 1; i <= metadata.getColumnCount(); i++) {
         valueArray[i-1] = getColumnValue(rs, metadata.getColumnType(i), i,
               trim, dateFormatString, timeFormatString);
      }
      return valueArray;
   }

   /**
    * The formatted timestamp.
    * @param timestamp Timestamp read from resultset
    * @param timestampFormatString Format string
    * @return Formatted time stamp.
    */
   protected String handleTimestamp(Timestamp timestamp, String timestampFormatString) {
      SimpleDateFormat timeFormat = new SimpleDateFormat(timestampFormatString);
      return timestamp == null ? null : timeFormat.format(timestamp);
   }

   private String getColumnValue(ResultSet rs, int colType, int colIndex, boolean trim, String dateFormatString, String timestampFormatString)
         throws SQLException, IOException {

      String value;

      switch (colType) {
         case Types.BOOLEAN:
            value = Objects.toString(rs.getBoolean(colIndex));
            break;
         case Types.NCLOB:
            value = handleNClob(rs, colIndex);
            break;
         case Types.CLOB:
            value = handleClob(rs, colIndex);
            break;
         case Types.BIGINT:
            value = Objects.toString(rs.getLong(colIndex));
            break;
         case Types.DECIMAL:
         case Types.REAL:
         case Types.NUMERIC:
            value = Objects.toString(rs.getBigDecimal(colIndex), DEFAULT_VALUE);
            break;
         case Types.DOUBLE:
            value = Objects.toString(rs.getDouble(colIndex));
            break;
         case Types.FLOAT:
            value = Objects.toString(rs.getFloat(colIndex));
            break;
         case Types.INTEGER:
         case Types.TINYINT:
         case Types.SMALLINT:
            value = Objects.toString(rs.getInt(colIndex));
            break;
         case Types.DATE:
            value = handleDate(rs, colIndex, dateFormatString);
            break;
         case Types.TIME:
            value = Objects.toString(rs.getTime(colIndex), DEFAULT_VALUE);
            break;
         case Types.TIMESTAMP:
            value = handleTimestamp(rs.getTimestamp(colIndex), timestampFormatString);
            break;
         case Types.NVARCHAR:
         case Types.NCHAR:
         case Types.LONGNVARCHAR:
            value = handleNVarChar(rs, colIndex, trim);
            break;
         case Types.LONGVARCHAR:
         case Types.VARCHAR:
         case Types.CHAR:
            value = handleVarChar(rs, colIndex, trim);
            break;
         default:
            // This takes care of Types.BIT, Types.JAVA_OBJECT, and anything
            // unknown.
            value = Objects.toString(rs.getObject(colIndex), DEFAULT_VALUE);
      }


      if (rs.wasNull() || value == null) {
         value = DEFAULT_VALUE;
      }

      return value;
   }

   private String handleVarChar(ResultSet rs, int colIndex, boolean trim) throws SQLException {
      String value;
      String columnValue = rs.getString(colIndex);
      if (trim && columnValue != null) {
         value = columnValue.trim();
      } else {
         value = columnValue;
      }
      return value;
   }

   private String handleNVarChar(ResultSet rs, int colIndex, boolean trim) throws SQLException {
      String value;
      String nColumnValue = rs.getNString(colIndex);
      if (trim && nColumnValue != null) {
         value = nColumnValue.trim();
      } else {
         value = nColumnValue;
      }
      return value;
   }

   private String handleDate(ResultSet rs, int colIndex, String dateFormatString) throws SQLException {
      String value = DEFAULT_VALUE;
      Date date = rs.getDate(colIndex);
      if (date != null) {
         SimpleDateFormat df = new SimpleDateFormat(dateFormatString);
         value = df.format(date);
      }
      return value;
   }

   private String handleClob(ResultSet rs, int colIndex) throws SQLException, IOException {
      String value = DEFAULT_VALUE;
      Clob c = rs.getClob(colIndex);
      if (c != null) {
         StrBuilder sb = new StrBuilder();
         sb.readFrom(c.getCharacterStream());
         value = sb.toString();
      }
      return value;
   }

   private String handleNClob(ResultSet rs, int colIndex) throws SQLException, IOException {
      String value = DEFAULT_VALUE;
      NClob nc = rs.getNClob(colIndex);
      if (nc != null) {
         StrBuilder sb = new StrBuilder();
         sb.readFrom(nc.getCharacterStream());
         value = sb.toString();
      }
      return value;
   }
}

package com.deathrayresearch.outlier.filter.dates;

import com.deathrayresearch.outlier.Row;
import com.deathrayresearch.outlier.filter.AbstractFilter;
import com.deathrayresearch.outlier.filter.ColumnReference;

import java.time.LocalDate;
import java.time.Month;

/**
 *
 */
public class IsInAugust extends AbstractFilter {

  public IsInAugust(ColumnReference columnReference) {
    super(columnReference);
  }

  @Override
  public boolean matches(Row row) {
    LocalDate date = (LocalDate) row.get(columnName());
    return date.getMonth() == Month.AUGUST;
  }

}

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tech.tablesaw.columns;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import tech.tablesaw.api.DateTimeColumn;
import tech.tablesaw.filtering.LongPredicate;

/**
 * DateTime 列 工具类
 */
public interface DateTimeColumnUtils extends Column {

    LongPredicate isMissing = i -> i == DateTimeColumn.MISSING_VALUE;
    LongPredicate isNotMissing = i -> i != DateTimeColumn.MISSING_VALUE;

    LongArrayList data();
}
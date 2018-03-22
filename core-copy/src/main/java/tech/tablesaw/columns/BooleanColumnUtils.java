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

import it.unimi.dsi.fastutil.ints.IntIterable;
import tech.tablesaw.api.BooleanColumn;
import tech.tablesaw.filtering.BooleanPredicate;

/**
 * Boolean列 工具类
 */
public interface BooleanColumnUtils extends Column, IntIterable {

    BooleanPredicate isMissing = i -> i == Byte.MIN_VALUE;

    BooleanPredicate isNotMissing = i -> i != BooleanColumn.MISSING_VALUE;
}

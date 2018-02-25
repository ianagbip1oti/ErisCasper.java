/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
 *     Copyright 2018-2018 "Princess" Lana Samson
 *
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
package net.dv8tion.jda.core.utils.tuple;

import java.io.Serializable;
import java.util.Objects;

/**
 * This class has been copied from <a href="https://commons.apache.org/proper/commons-lang/"
 * target="_blank">Lang 3</a>
 *
 * <p>A pair consisting of two elements.
 *
 * <p>This class is an abstract implementation defining the basic API. It refers to the elements as
 * 'left' and 'right'.
 *
 * <p>Subclass implementations may be mutable or immutable. However, there is no restriction on the
 * type of the stored objects that may be stored. If mutable objects are stored in the pair, then
 * the pair itself effectively becomes mutable.
 *
 * @param <L> the left element type
 * @param <R> the right element type
 * @since Lang 3.0
 */
public abstract class Pair<L, R>
    implements /*Map.Entry<L, R>, Comparable<Pair<L, R>>,*/ Serializable {

  /**
   * Obtains an immutable pair of from two objects inferring the generic types.
   *
   * <p>This factory allows the pair to be created using inference to obtain the generic types.
   *
   * @param <L> the left element type
   * @param <R> the right element type
   * @param left the left element, may be null
   * @param right the right element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static <L, R> Pair<L, R> of(final L left, final R right) {
    return new ImmutablePair<>(left, right);
  }

  // -----------------------------------------------------------------------
  /**
   * Gets the left element from this pair.
   *
   * <p>When treated as a key-value pair, this is the key.
   *
   * @return the left element, may be null
   */
  public abstract L getLeft();

  /**
   * Gets the right element from this pair.
   *
   * <p>When treated as a key-value pair, this is the value.
   *
   * @return the right element, may be null
   */
  public abstract R getRight();

  /**
   * Compares this pair to another based on the two elements.
   *
   * @param obj the object to compare to, null returns false
   * @return true if the elements of the pair are equal
   */
  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof Pair<?, ?>) {
      final Pair<?, ?> other = (Pair<?, ?>) obj;
      return Objects.equals(getLeft(), other.getLeft())
          && Objects.equals(getRight(), other.getRight());
    }
    return false;
  }

  /**
   * Returns a suitable hash code. The hash code follows the definition in {@code Map.Entry}.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    // see Map.Entry API specification
    return Objects.hashCode(getLeft()) ^ Objects.hashCode(getRight());
  }

  /**
   * Returns a String representation of this pair using the format {@code ($left,$right)}.
   *
   * @return a string describing this object, not null
   */
  @Override
  public String toString() {
    return "(" + getLeft() + ',' + getRight() + ')';
  }
}

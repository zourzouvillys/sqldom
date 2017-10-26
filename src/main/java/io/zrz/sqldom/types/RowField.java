package io.zrz.sqldom.types;

import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

@Value.Immutable
@Value.Style(visibility = ImplementationVisibility.PUBLIC, depluralize = true)
@Value.Modifiable
public interface RowField {

  /**
   * the name of the field.
   */

  String name();

  /**
   * the type.
   */

  TypeUse type();

}
